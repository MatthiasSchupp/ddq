package eu.domaindriven.ddq.event;

import eu.domaindriven.ddq.notification.Notifiable;

import javax.persistence.*;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.UUID;

@NamedQuery(name = StoredEvent.COUNT, query = "select count(event) from StoredEvent event")
@NamedQuery(name = StoredEvent.MAX_ID, query = "select max(event.id) from StoredEvent event")
@NamedQuery(name = StoredEvent.MIN_ID, query = "select min(event.id) from StoredEvent event")
@NamedQuery(name = StoredEvent.BETWEEN, query = "select event from StoredEvent event where event.id between :lowId and :highId order by event.id")
@NamedQuery(name = StoredEvent.SINCE, query = "select event from StoredEvent event where event.id > :id order by event.id")
@NamedQuery(name = StoredEvent.NEXT_UNPROCESSED, lockMode = LockModeType.PESSIMISTIC_WRITE,
        query = "select event from StoredEvent event " +
                "where event.processingStatus IN (eu.domaindriven.ddq.event.EventProcessingStatus.NEW, eu.domaindriven.ddq.event.EventProcessingStatus.UNPROCESSED, eu.domaindriven.ddq.event.EventProcessingStatus.IN_PROCESS) " +
                "and not exists (" +
                "select event2 from StoredEvent event2 " +
                "where event2.processingStatus = eu.domaindriven.ddq.event.EventProcessingStatus.IN_PROCESS " +
                "and event2.group is not null " +
                "and event2.group = event.group " +
                "and event2.processingInstance <> :processingInstance " +
                "and event2.processingTimestamp > :processingThreshold" +
                ") order by event.id")
@NamedQuery(name = StoredEvent.UPDATE_PROCESSING_STATE,
        query = "update StoredEvent event set " +
                "event.processingInstance = :processingInstance, " +
                "event.processingStatus = :processingStatus, " +
                "event.processingTimestamp = :processingTimestamp, " +
                "event.version = :version + 1 " +
                "where event.id = :id " +
                "and event.version = :version")
@Entity
@Table(name = "event_store")
public class StoredEvent implements Notifiable {

    public static final String COUNT = "StoredEvent.COUNT";
    public static final String MAX_ID = "StoredEvent.MAX_ID";
    public static final String MIN_ID = "StoredEvent.MIN_ID";
    public static final String BETWEEN = "StoredEvent.BETWEEN";
    public static final String SINCE = "StoredEvent.SINCE";
    public static final String NEXT_UNPROCESSED = "StoredEvent.NEXT_UNPROCESSED";
    public static final String UPDATE_PROCESSING_STATE = "StoredEvent.UPDATE_PROCESSING_STATE";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "event_id", nullable = false, unique = true, updatable = false)
    private UUID eventId;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private Class<? extends DomainEvent> type;
    @Column(nullable = false)
    private String source;
    @Column(nullable = false)
    private Instant timestamp;
    @Column(name = "event_group")
    private String group;
    @Column(nullable = false, length = 4000)
    private String event;
    @Column(name = "processing_instance")
    private UUID processingInstance;
    @Enumerated(EnumType.STRING)
    @Column(name="processing_status", nullable = false)
    private EventProcessingStatus processingStatus;
    @Column(name = "processing_timestamp")
    private Instant processingTimestamp;
    @Version
    private Integer version;
    @Transient
    private EventSerializer serializer;

    public StoredEvent() {
    }

    StoredEvent(DomainEvent event, EventSerializer serializer) {
        Objects.requireNonNull(event, "DomainEvent must not be null");
        serializer(serializer);
        this.eventId = event.id();
        this.name = event.getClass().getSimpleName();
        this.type = event.getClass();
        this.source = event.source();
        this.timestamp = event.timestamp();
        this.group = event.group().orElse(null);
        this.event = serializer.serialize(event);
        this.processingStatus = EventProcessingStatus.NEW;
    }

    StoredEvent serializer(EventSerializer serializer) {
        this.serializer = Objects.requireNonNull(serializer, "EventSerializer mut not be null");
        return this;
    }

    @Override
    public Long id() {
        return id;
    }

    public UUID eventId() {
        return eventId;
    }

    public String name() {
        return name;
    }

    public Class<? extends DomainEvent> type() {
        return type;
    }

    public String source() {
        return source;
    }

    @Override
    public Instant timestamp() {
        return timestamp;
    }

    public Optional<String> group() {
        return Optional.ofNullable(group);
    }

    public String event() {
        return event;
    }

    public Optional<UUID> processingInstance() {
        return Optional.ofNullable(processingInstance);
    }

    public Integer version() {
        return version;
    }

    void incrementVersion() {
        version++;
    }

    public EventProcessingStatus processingStatus() {
        return processingStatus;
    }

    public Optional<Instant> processingTimestamp() {
        return Optional.ofNullable(processingTimestamp);
    }

    public StoredEvent inProcess(UUID processingInstance) {
        this.processingStatus = EventProcessingStatus.IN_PROCESS;
        this.processingInstance = processingInstance;
        this.processingTimestamp = Instant.now();

        return this;
    }

    public StoredEvent processed() {
        this.processingStatus = EventProcessingStatus.PROCESSED;

        return this;
    }

    @SuppressWarnings("unused")
    public StoredEvent release() {
        this.processingStatus = EventProcessingStatus.UNPROCESSED;
        this.processingInstance = null;
        this.processingTimestamp = null;

        return this;
    }

    @Override
    public Object toProjection() {
        return toDomainEvent();
    }

    @SuppressWarnings("unchecked")
    public <T extends DomainEvent> T toDomainEvent() {
        return serializer.deserialize(event(), (Class<T>) type());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StoredEvent that = (StoredEvent) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", StoredEvent.class.getSimpleName() + "[", "]")
                .add("id=" + id)
                .add("eventId=" + eventId)
                .add("name='" + name + "'")
                .add("type=" + type)
                .add("timestamp=" + timestamp)
                .add("group='" + group + "'")
                .add("processingInstance=" + processingInstance)
                .add("processingStatus=" + processingStatus)
                .add("processingTimestamp=" + processingTimestamp)
                .add("version=" + version)
                .add("event=" + event)
                .toString();
    }
}
