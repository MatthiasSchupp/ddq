package ${package}.domain.model;

import eu.domaindriven.ddq.domain.UUIDValueObject;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.util.UUID;

@Embeddable
@AttributeOverride(name = "id", column = @Column(name = "greeting_id", nullable = false))
public class GreetingId extends UUIDValueObject {

    private static final long serialVersionUID = -7869034642975270L;

    public GreetingId() {
    }

    public GreetingId(UUID id) {
        super(id);
    }

    public GreetingId(String id) {
        super(id);
    }
}
