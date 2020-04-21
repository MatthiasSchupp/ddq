package eu.domaindriven.ddq.notification;

import java.time.Instant;
import java.util.Objects;
import java.util.StringJoiner;

@SuppressWarnings("unused")
public class Notification {

    private final long id;
    private final Instant timestamp;
    private final String name;
    private final Object detail;

    public Notification(long id, Instant timestamp, Object detail) {
        this.id = id;
        this.timestamp = timestamp;
        this.name = detail.getClass().getSimpleName();
        this.detail = detail;
    }

    public long id() {
        return id;
    }

    public Instant timestamp() {
        return timestamp;
    }

    public String name() {
        return name;
    }

    public Object detail() {
        return detail;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Notification that = (Notification) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Notification.class.getSimpleName() + "[", "]")
                .add("id=" + id)
                .add("timestamp=" + timestamp)
                .add("name='" + name + "'")
                .add("detail=" + detail)
                .toString();
    }
}
