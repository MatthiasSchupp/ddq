package eu.domaindriven.ddq.event;

import javax.json.bind.annotation.JsonbCreator;
import javax.json.bind.annotation.JsonbProperty;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.UUID;

public abstract class DomainEvent {

    private static final String DEFAULT_GROUP = "default";

    private final UUID id;
    private final Instant timestamp;
    private String source;

    public DomainEvent() {
        this(UUID.randomUUID(), Instant.now(), "self");
    }

    @JsonbCreator
    public DomainEvent(
            @JsonbProperty("id") UUID id,
            @JsonbProperty("timestamp") Instant timestamp,
            @JsonbProperty("source") String source) {
        this.id = id;
        this.timestamp = timestamp;
        this.source = source;
    }

    public UUID id() {
        return id;
    }

    public Instant timestamp() {
        return timestamp;
    }

    public String source() {
        return source;
    }

    DomainEvent source(String source) {
        this.source = source;
        return this;
    }

    public Optional<String> group() {
        return Optional.of(DEFAULT_GROUP);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DomainEvent that = (DomainEvent) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", this.getClass().getSimpleName() + "[", "]")
                .add("id=" + id)
                .add("timestamp=" + timestamp)
                .toString();
    }
}
