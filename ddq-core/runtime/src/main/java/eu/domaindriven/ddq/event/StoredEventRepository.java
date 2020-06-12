package eu.domaindriven.ddq.event;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

@Dependent
@Transactional(Transactional.TxType.MANDATORY)
public class StoredEventRepository {

    @Inject
    EntityManager em;

    public void persist(StoredEvent storedEvent) {
        em.persist(storedEvent);
    }

    public boolean exists(UUID eventId) {
        return em.createNamedQuery(StoredEvent.EXIST, Boolean.class)
                .setParameter("eventId", eventId)
                .getSingleResult();
    }

    public StoredEvent updateProcessingState(StoredEvent storedEvent) {
        int updates = em.createNamedQuery(StoredEvent.UPDATE_PROCESSING_STATE)
                .setParameter("id", storedEvent.id())
                .setParameter("processingInstance", storedEvent.processingInstance().orElseThrow())
                .setParameter("processingStatus", storedEvent.processingStatus())
                .setParameter("processingTimestamp", storedEvent.processingTimestamp().orElseThrow())
                .setParameter("version", storedEvent.version())
                .executeUpdate();

        if (updates != 1) {
            throw new IllegalStateException("StoredEvent was modified by a different process.");
        }
        storedEvent.incrementVersion();

        return storedEvent;
    }

    public long size() {
        return em.createNamedQuery(StoredEvent.COUNT, Long.class)
                .getSingleResult();
    }

    public Optional<Long> maxId() {
        return Optional.ofNullable(em.createNamedQuery(StoredEvent.MAX_ID, Long.class).getSingleResult());
    }

    public Optional<Long> minId() {
        return Optional.ofNullable(em.createNamedQuery(StoredEvent.MIN_ID, Long.class).getSingleResult());
    }

    public Stream<StoredEvent> between(long lowId, long highId) {
        return em.createNamedQuery(StoredEvent.BETWEEN, StoredEvent.class)
                .setParameter("lowId", lowId)
                .setParameter("highId", highId)
                .getResultList().stream();
    }

    public Stream<StoredEvent> since(long id) {
        return em.createNamedQuery(StoredEvent.SINCE, StoredEvent.class)
                .setParameter("id", id)
                .getResultList().stream();
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public Optional<StoredEvent> nextUnprocessed(UUID processingInstance, Instant processingThreshold) {
        try (Stream<StoredEvent> unprocessed = em.createNamedQuery(StoredEvent.NEXT_UNPROCESSED, StoredEvent.class)
                .setParameter("processingInstance", processingInstance)
                .setParameter("processingThreshold", processingThreshold)
                .setMaxResults(1)
                .getResultStream()) {

            return unprocessed.findFirst()
                    .map(storedEvent -> storedEvent.inProcess(processingInstance));
        }
    }

}
