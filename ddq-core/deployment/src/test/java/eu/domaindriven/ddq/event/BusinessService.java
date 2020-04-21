package eu.domaindriven.ddq.event;

import com.google.common.truth.Truth;
import io.agroal.api.AgroalDataSource;
import io.quarkus.arc.Arc;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.transaction.Transactional;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Singleton
public class BusinessService {

    public static final TestEvent TEST_EVENT_1 = new TestEvent("test1", 1001);
    public static final TestEvent TEST_EVENT_2 = new TestEvent("test2", 1002);
    public static final TestEvent TEST_EVENT_3 = new TestEvent("test3", 1003);

    @Inject
    EventPublisher eventPublisher;

    @Inject
    EventStore eventStore;

    @Inject
    AgroalDataSource defaultDataSource;

    private final BlockingQueue<TestEvent> testEvents;
    private final AtomicInteger eventCount;

    public BusinessService() {
        testEvents = new ArrayBlockingQueue<>(10);
        eventCount = new AtomicInteger();
    }

    @Transactional
    public void publishEvent1AndEvent2() {
        eventPublisher.publish(TEST_EVENT_1);
        eventPublisher.publish(TEST_EVENT_2);
    }

    @Transactional
    public void publishEvent3() {
        Events.publish(TEST_EVENT_3);
    }

    @Transactional
    public List<StoredEvent> storedEvents() {
        return eventStore.since(0).collect(Collectors.toList());
    }

    @Transactional(Transactional.TxType.MANDATORY)
    public void onTestEvent(@Observes TestEvent testEvent) {
        Truth.assertThat(selfReference().processingStatus(eventCount.incrementAndGet())).isEqualTo(EventProcessingStatus.IN_PROCESS);
        testEvents.add(testEvent);
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public EventProcessingStatus processingStatus(int id) {
        try (Connection connection = defaultDataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("select processing_status from event_store where id = ?")) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return EventProcessingStatus.valueOf(resultSet.getString(1));
                }
            }

            throw new IllegalStateException("No event found.");
        } catch (SQLException ex) {
            throw new IllegalStateException(ex);
        }
    }

    public TestEvent pollEvent(int timeout) throws InterruptedException {
        return testEvents.poll(timeout, TimeUnit.MILLISECONDS);
    }

    public TestEvent pollEvent() {
        return testEvents.poll();
    }

    public int eventCount() {
        return testEvents.size();
    }

    private BusinessService selfReference() {
        return Arc.container().instance(BusinessService.class).get();
    }
}
