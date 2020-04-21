package eu.domaindriven.ddq.event;

import java.net.URI;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;

final class EventSource {
    private final String name;
    private final URI uri;
    private final Long startId;

    EventSource(String name, URI uri, Long startId) {
        this.name = name;
        this.uri = uri;
        this.startId = startId;
    }

    String name() {
        return name;
    }

    URI uri() {
        return uri;
    }

    Optional<Long> startId() {
        return Optional.ofNullable(startId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EventSource that = (EventSource) o;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", EventSource.class.getSimpleName() + "[", "]")
                .add("name='" + name + "'")
                .add("uri=" + uri)
                .add("startId=" + startId)
                .toString();
    }
}
