package eu.domaindriven.ddq.error;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.Optional;
import java.util.stream.Stream;

@Dependent
@Transactional(Transactional.TxType.MANDATORY)
public class StoredErrorRepository {

    @Inject
    EntityManager em;

    public void persist(StoredError storedError) {
        em.persist(storedError);
    }

    public Optional<StoredError> byMessage(String message, String exceptionMessage, Class<?> source, ErrorType type) {
        try (Stream<StoredError> errors = em.createNamedQuery(StoredError.BY_MESSAGE, StoredError.class)
                .setParameter("message", message)
                .setParameter("exceptionMessage", exceptionMessage)
                .setParameter("source", source)
                .setParameter("type", type)
                .setMaxResults(1)
                .getResultStream()) {

            return errors.findFirst();
        }
    }

    public Stream<StoredError> stream() {
        return em.createNamedQuery(StoredError.ALL, StoredError.class)
                .getResultList().stream();
    }

    @SuppressWarnings("unused")
    public long size() {
        return em.createNamedQuery(StoredError.COUNT, Long.class)
                .getSingleResult();
    }

    public Optional<Long> maxId() {
        return Optional.ofNullable(em.createNamedQuery(StoredError.MAX_ID, Long.class).getSingleResult());
    }

    public Optional<Long> minId() {
        return Optional.ofNullable(em.createNamedQuery(StoredError.MIN_ID, Long.class).getSingleResult());
    }

    public Stream<StoredError> between(long lowId, long highId) {
        return em.createNamedQuery(StoredError.BETWEEN, StoredError.class)
                .setParameter("lowId", lowId)
                .setParameter("highId", highId)
                .getResultList().stream();
    }
}
