package ${package}.domain.model;

import eu.domaindriven.ddq.domain.ValueObject;

import javax.persistence.Column;
import javax.persistence.Embeddable;
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
}
