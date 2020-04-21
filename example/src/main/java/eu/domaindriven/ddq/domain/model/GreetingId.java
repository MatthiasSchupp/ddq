package eu.domaindriven.ddq.domain.model;

import eu.domaindriven.ddq.domain.ValueObject;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.UUID;

@Embeddable
public class GreetingId extends ValueObject {

    private static final long serialVersionUID = -7869034642975270L;

    @Column(name = "greeting_id", nullable = false)
    private UUID id;

    public GreetingId() {
    }

    public GreetingId(UUID id) {
        this.id = id;
    }

    public GreetingId(String id) {
        this(UUID.fromString(id));
    }

    public UUID id() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GreetingId that = (GreetingId) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", GreetingId.class.getSimpleName() + "[", "]")
                .add("id=" + id)
                .toString();
    }
}
