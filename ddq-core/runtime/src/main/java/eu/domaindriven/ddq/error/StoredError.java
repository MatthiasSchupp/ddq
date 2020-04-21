package eu.domaindriven.ddq.error;

import eu.domaindriven.ddq.notification.Notifiable;
import io.quarkus.arc.Arc;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;

@SuppressWarnings("unused")
@NamedQuery(name = StoredError.COUNT, query = "select count(error) from StoredError error")
@NamedQuery(name = StoredError.MAX_ID, query = "select max(error.id) from StoredError error")
@NamedQuery(name = StoredError.MIN_ID, query = "select min(error.id) from StoredError error")
@NamedQuery(name = StoredError.BETWEEN, query = "select error from StoredError error where error.id between :lowId and :highId order by error.id")
@NamedQuery(name = StoredError.ALL, query = "select error from StoredError error")
@NamedQuery(name = StoredError.BY_MESSAGE, query = "select error from StoredError error where error.message = :message and (error.exceptionMessage = :exceptionMessage or error.exceptionMessage is null and :exceptionMessage is null) and error.source = :source and error.type = :type", lockMode = LockModeType.PESSIMISTIC_WRITE)
@Entity
@Table(name = "error")
public class StoredError implements Serializable, Notifiable {

    private static final long serialVersionUID = 2162705985487616217L;

    public static final String BY_MESSAGE = "StoredError.BY_MESSAGE";
    public static final String COUNT = "StoredError.COUNT";
    public static final String MAX_ID = "StoredError.MAX_ID";
    public static final String MIN_ID = "StoredError.MIN_ID";
    public static final String ALL = "StoredError.ALL";
    public static final String BETWEEN = "StoredError.BETWEEN";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ErrorType type;

    @Column(nullable = false)
    private Class<?> source;

    @Column(nullable = false)
    private String message;

    @Column(name = "exception_message")
    private String exceptionMessage;

    @Column(name = "stack_trace", length = 2000)
    private String stackTrace;

    @Column(nullable = false)
    private Integer occurrences;

    @CreationTimestamp
    @Column(name = "first_occurrence", nullable = false)
    private Instant firstOccurrence;

    @UpdateTimestamp
    @Column(name = "last_occurrence", nullable = false)
    private Instant lastOccurrence;

    public StoredError() {
        occurrences = 1;
    }

    StoredError(ErrorType type, Class<?> source, String message, String exceptionMessage, String stackTrace) {
        this();
        this.type = type;
        this.source = source;
        this.message = message;
        this.exceptionMessage = exceptionMessage;
        this.stackTrace = stackTrace;
    }

    @Override
    public Long id() {
        return id;
    }

    @Override
    public Instant timestamp() {
        return lastOccurrence();
    }

    @Override
    public Object toProjection() {
        return errorFactory().create(this);
    }

    private ErrorFactory errorFactory() {
        return Arc.container().instance(ErrorFactory.class).get();
    }

    public ErrorType type() {
        return type;
    }

    public Class<?> source() {
        return source;
    }

    public String message() {
        return message;
    }

    public String exceptionMessage() {
        return exceptionMessage;
    }

    public String stackTrace() {
        return stackTrace;
    }

    public Integer occurrences() {
        return occurrences;
    }

    public StoredError incrementOccurrences() {
        occurrences++;
        return this;
    }

    public Instant firstOccurrence() {
        return firstOccurrence;
    }

    public Instant lastOccurrence() {
        return lastOccurrence;
    }
}
