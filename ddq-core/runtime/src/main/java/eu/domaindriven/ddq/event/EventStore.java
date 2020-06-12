package eu.domaindriven.ddq.event;

import eu.domaindriven.ddq.Instance;
import eu.domaindriven.ddq.notification.NotificationProvider;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

@Dependent
@Transactional(Transactional.TxType.MANDATORY)
public class EventStore implements NotificationProvider<StoredEvent> {

    private static final UUID INSTANCE = Instance.id();

    @Inject
    StoredEventRepository repository;

    @Inject
    EventSerializer serializer;

    public void append(DomainEvent event) {
        if (!repository.exists(event.id())) {
            StoredEvent storedEvent = new StoredEvent(event, serializer);
            repository.persist(storedEvent);
        }
    }

    @Override
    public Stream<StoredEvent> between(long lowId, long highId) {
        return repository.between(lowId, highId)
                .map(storedEvent -> storedEvent.serializer(serializer));
    }

    public Stream<StoredEvent> since(long id) {
        return repository.since(id)
                .map(storedEvent -> storedEvent.serializer(serializer));
    }

    public Optional<StoredEvent> nextUnprocessed(Instant processingThreshold) {
        return repository.nextUnprocessed(INSTANCE, processingThreshold)
                .map(StoredEvent::processed)
                .map(repository::updateProcessingState)
                .map(storedEvent -> storedEvent.serializer(serializer));
    }

    public long size() {
        return repository.size();
    }

    @Override
    public Optional<Long> maxId() {
        return repository.maxId();
    }

    @Override
    public Optional<Long> minId() {
        return repository.minId();
    }
}
