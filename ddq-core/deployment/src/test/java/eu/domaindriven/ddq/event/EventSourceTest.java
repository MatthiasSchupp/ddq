package eu.domaindriven.ddq.event;

import io.quarkus.test.QuarkusUnitTest;
import io.quarkus.test.common.QuarkusTestResource;
import org.eclipse.microprofile.context.ManagedExecutor;
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
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth8.assertThat;
import static org.awaitility.Awaitility.given;
import static org.hamcrest.Matchers.is;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@QuarkusTestResource(MockEventSourceTestResource.class)
class EventSourceTest {

    @RegisterExtension
    static final QuarkusUnitTest config = new QuarkusUnitTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addClass(TestEvent.class)
                    .addClass(BasicAuthAuthorizationProvider.class)
                    .addAsResource("event-source-application.properties", "application.properties")
            );

    @Inject
    EventStore eventStore;

    @Inject
    EventSourceTrackerRepository repository;

    @Inject
    ManagedExecutor managedExecutor;

    @Test
    @Order(1)
    void environment() {
        assertThat(eventStore).isNotNull();
        assertThat(repository).isNotNull();
        assertThat(managedExecutor).isNotNull();
    }

    @Test
    @Order(2)
    @Transactional
    void testEventSourceCollection() {
        given().pollExecutorService(managedExecutor)
                .await().atMost(4, TimeUnit.SECONDS)
                .until(repository::size, is(2L));

        Map<String, EventSourceTracker> trackers = repository.stream()
                .collect(Collectors.groupingBy(EventSourceTracker::name, Collectors.reducing(null, (t, t2) -> t2)));

        EventSourceTracker tracker = trackers.get("test");
        assertThat(tracker).isNotNull();
        assertThat(tracker.name()).isEqualTo("test");
        assertThat(tracker.uri()).isEqualTo(URI.create("http://localhost:8086/event-source-mock-1/resources/notifications/events"));
        assertThat(tracker.lastId()).isPresent();
        assertThat(tracker.lastId()).hasValue(50);

        tracker = trackers.get("test-current");
        assertThat(tracker).isNotNull();
        assertThat(tracker.name()).isEqualTo("test-current");
        assertThat(tracker.uri()).isEqualTo(URI.create("http://localhost:8086/event-source-mock-2/resources/notifications/events"));
        assertThat(tracker.lastId()).isPresent();
        assertThat(tracker.lastId()).hasValue(50);

        assertThat(eventStore.since(0)).hasSize(60);
    }
}
