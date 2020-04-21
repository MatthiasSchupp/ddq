package eu.domaindriven.ddq.event;

import javax.persistence.*;
import java.net.URI;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;

@NamedQuery(name = EventSourceTracker.ALL, query = "select tracker from EventSourceTracker tracker")
@NamedQuery(name = EventSourceTracker.COUNT, query = "select count(tracker) from EventSourceTracker tracker")
@Entity
@Table(name = "event_source")
public class EventSourceTracker {

    public static final String ALL = "EventSourceTracker.ALL";
    public static final String COUNT = "EventSourceTracker.COUNT";

    @Id
    private String name;

    @Column(nullable = false)
    private URI uri;

    @Column(name = "last_id")
    private Long lastId;

    @Version
    private Long version;

    public EventSourceTracker() {
    }

    EventSourceTracker(EventSource eventSource) {
        this.name = eventSource.name();
        this.uri = eventSource.uri();
        this.lastId = eventSource.startId().orElse(null);
    }

    public String name() {
        return name;
    }

    public URI uri() {
        return uri;
    }

    public EventSourceTracker uri(URI uri) {
        this.uri = uri;
        return this;
    }

    public Optional<Long> lastId() {
        return Optional.ofNullable(lastId);
    }

    public EventSourceTracker lastId(Long lastId) {
        this.lastId = lastId;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EventSourceTracker that = (EventSourceTracker) o;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", EventSourceTracker.class.getSimpleName() + "[", "]")
                .add("name='" + name + "'")
                .add("uri=" + uri)
                .add("lastId=" + lastId)
                .add("version=" + version)
                .toString();
    }
}
