package eu.domaindriven.ddq.event;

import io.quarkus.test.QuarkusUnitTest;
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
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth8.assertThat;
import static org.awaitility.Awaitility.given;
import static org.hamcrest.Matchers.is;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class EventTest {

    @RegisterExtension
    static final QuarkusUnitTest config = new QuarkusUnitTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addClasses(BusinessService.class)
                    .addClass(TestEvent.class)
                    .addAsResource("application.properties")
            );

    @Inject
    BusinessService businessService;

    @Inject
    EventStore eventStore;

    @Inject
    ManagedExecutor managedExecutor;

    @Test
    @Order(1)
    @Transactional
    public void environment() {
        assertThat(businessService).isNotNull();
        assertThat(eventStore).isNotNull();
    }

    @Test
    @Order(2)
    public void testPublishEvents() throws InterruptedException {
        // Publish the events
        businessService.publishEvent1AndEvent2();
        businessService.publishEvent3();

        // Test, if the events were stored correctly in the event store
        List<StoredEvent> storedEvents = businessService.storedEvents();

        assertThat(storedEvents).hasSize(3);

        assertThat(storedEvents.stream().map(StoredEvent::id)).containsExactly(
                1L,
                2L,
                3L)
                .inOrder();

        assertThat(storedEvents.stream().map(StoredEvent::eventId)).containsExactly(
                BusinessService.TEST_EVENT_1.id(),
                BusinessService.TEST_EVENT_2.id(),
                BusinessService.TEST_EVENT_3.id())
                .inOrder();

        assertThat(storedEvents.stream().map(StoredEvent::name).distinct()).containsExactly("TestEvent");

        assertThat(storedEvents.stream().map(StoredEvent::processingStatus).distinct()).contains(EventProcessingStatus.NEW);

        assertThat(storedEvents.stream().map(StoredEvent::toDomainEvent)).containsExactly(
                BusinessService.TEST_EVENT_1,
                BusinessService.TEST_EVENT_2,
                BusinessService.TEST_EVENT_3)
                .inOrder();

        given().pollExecutorService(managedExecutor)
                .await().atMost(2, TimeUnit.SECONDS)
                .until(businessService::eventCount, is(3));

        // Test, if the events have been dispatched
        assertThat(businessService.pollEvent()).isEqualTo(BusinessService.TEST_EVENT_1);
        assertThat(businessService.pollEvent()).isEqualTo(BusinessService.TEST_EVENT_2);
        assertThat(businessService.pollEvent()).isEqualTo(BusinessService.TEST_EVENT_3);

        // Test, if the events have been marked as processed
        assertThat(businessService.storedEvents().stream().map(StoredEvent::processingStatus).distinct()).containsExactly(EventProcessingStatus.PROCESSED);
    }
}