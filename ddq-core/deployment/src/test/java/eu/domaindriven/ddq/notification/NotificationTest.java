package eu.domaindriven.ddq.notification;

import eu.domaindriven.ddq.event.EventPublisher;
import eu.domaindriven.ddq.event.EventStore;
import io.quarkus.test.QuarkusUnitTest;
import io.quarkus.test.common.http.TestHTTPResource;
import io.restassured.RestAssured;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.RegisterExtension;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.net.URI;
import java.time.Instant;

import static com.google.common.truth.Truth.assertThat;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;

@SuppressWarnings("squid:S1192")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class NotificationTest {

    @RegisterExtension
    static final QuarkusUnitTest config = new QuarkusUnitTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addClass(NotificationTestEvent.class)
                    .addAsResource("application.properties")
            );

    @Inject
    EventPublisher eventPublisher;

    @Inject
    EventStore eventStore;

    @SuppressWarnings("unused")
    @TestHTTPResource("domain-test/resources")
    URI resourcesUri;

    @Test
    @Order(1)
    @Transactional
    public void environment() {
        assertThat(eventPublisher).isNotNull();
    }

    @Test
    @Order(2)
    public void testEmptyNotificationEndpoint() {
        RestAssured.given()
                .when().get("/resources/notifications/events")
                .then()
                .statusCode(200)
                .body("_links", hasSize(1))
                .body("_links[0].rel", is("self"))
                .body("_links[0].uri", is(resourcesUri + "/notifications/events/1,20"))
                .body("id", is("1,20"))
                .body("notifications", hasSize(0))
                .body("status", is("ACTUAL"));

        RestAssured.given()
                .when().get("/resources/notifications/events/1,20")
                .then()
                .statusCode(200)
                .body("_links", hasSize(1))
                .body("_links[0].rel", is("self"))
                .body("_links[0].uri", is(resourcesUri + "/notifications/events/1,20"))
                .body("id", is("1,20"))
                .body("notifications", hasSize(0))
                .body("status", is("ACTUAL"));

        RestAssured.given()
                .when().get("/resources/notifications/events/21,40")
                .then()
                .statusCode(200)
                .body("_links", hasSize(2))
                .body("_links[0].rel", is("self"))
                .body("_links[0].uri", is(resourcesUri + "/notifications/events/21,40"))
                .body("_links[1].rel", is("previous"))
                .body("_links[1].uri", is(resourcesUri + "/notifications/events/1,20"))
                .body("id", is("21,40"))
                .body("notifications", hasSize(0))
                .body("status", is("ACTUAL"));
    }

    @Test
    @Order(3)
    @Transactional
    public void produceFirst10Events() {
        for (int i = 0; i < 10; i++) {
            eventPublisher.publish(new NotificationTestEvent("test", i, Instant.EPOCH));
        }
        assertThat(eventStore.size()).isEqualTo(10);
    }

    @Test
    @Order(4)
    public void test10EventsNotificationEndpoint() {
        RestAssured.given()
                .when().get("/resources/notifications/events")
                .then()
                .statusCode(200)
                .body("_links", hasSize(1))
                .body("_links[0].rel", is("self"))
                .body("_links[0].uri", is(resourcesUri + "/notifications/events/1,20"))
                .body("id", is("1,20"))
                .body("notifications", hasSize(10))
                .body("notifications.name", hasItem("NotificationTestEvent"))
                .body("notifications.id", contains(1, 2, 3, 4, 5, 6, 7, 8, 9, 10))
                .body("notifications.detail.name", hasItem("test"))
                .body("notifications.detail.value", contains(0, 1, 2, 3, 4, 5, 6, 7, 8, 9))
                .body("notifications.detail.exampleTimestamp", hasItem(Instant.EPOCH.toString()))
                .body("status", is("ACTUAL"));
    }

    @Test
    @Order(5)
    @Transactional
    public void produceNext20Events() {
        for (int i = 10; i < 30; i++) {
            eventPublisher.publish(new NotificationTestEvent("test", i, Instant.EPOCH));
        }
        assertThat(eventStore.size()).isEqualTo(30);
    }

    @Test
    @Order(6)
    public void test30EventsNotificationEndpoint() {
        RestAssured.given()
                .when().get("/resources/notifications/events")
                .then()
                .statusCode(200)
                .body("_links", hasSize(2))
                .body("_links[0].rel", is("self"))
                .body("_links[0].uri", is(resourcesUri + "/notifications/events/21,40"))
                .body("_links[1].rel", is("previous"))
                .body("_links[1].uri", is(resourcesUri + "/notifications/events/1,20"))
                .body("id", is("21,40"))
                .body("notifications", hasSize(10))
                .body("status", is("ACTUAL"));

        RestAssured.given()
                .when().get("/resources/notifications/events/1,20")
                .then()
                .statusCode(200)
                .body("_links", hasSize(2))
                .body("_links[0].rel", is("self"))
                .body("_links[0].uri", is(resourcesUri + "/notifications/events/1,20"))
                .body("_links[1].rel", is("next"))
                .body("_links[1].uri", is(resourcesUri + "/notifications/events/21,40"))
                .body("id", is("1,20"))
                .body("notifications", hasSize(20))
                .body("status", is("ARCHIVED"));
    }
}
