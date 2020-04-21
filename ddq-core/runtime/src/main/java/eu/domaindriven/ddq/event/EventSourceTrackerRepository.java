package eu.domaindriven.ddq.event;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.transaction.Transactional;
import java.util.stream.Stream;

@Dependent
@Transactional(Transactional.TxType.MANDATORY)
public class EventSourceTrackerRepository {

    @Inject
    EntityManager em;

    public EventSourceTracker tracker(EventSource eventSource) {
        EventSourceTracker tracker = em.find(EventSourceTracker.class, eventSource.name(), LockModeType.PESSIMISTIC_WRITE);
        if (tracker == null) {
            tracker = new EventSourceTracker(eventSource);
            em.persist(tracker);
        }

        return tracker;
    }

    public Stream<EventSourceTracker> stream() {
        return em.createNamedQuery(EventSourceTracker.ALL, EventSourceTracker.class)
                .getResultList().stream();
    }

    public long size() {
        return em.createNamedQuery(EventSourceTracker.COUNT, Long.class).getSingleResult();
    }
}
