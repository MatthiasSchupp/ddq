package ${package}.domain.model;

import eu.domaindriven.ddq.event.DomainEvent;

import javax.json.bind.annotation.JsonbCreator;
import javax.json.bind.annotation.JsonbProperty;
import java.time.Instant;
import java.util.UUID;

public class Greeted extends DomainEvent {

    private final String greetingId;
    private final String person;

    public Greeted(String greetingId, String person) {
        this.greetingId = greetingId;
        this.person = person;
    }

    @JsonbCreator
    public Greeted(@JsonbProperty("id") UUID id, @JsonbProperty("timestamp") Instant timestamp, @JsonbProperty("greetingId") String greetingId, @JsonbProperty("person") String person) {
        super(id, timestamp);
        this.greetingId = greetingId;
        this.person = person;
    }

    public String greetingId() {
        return greetingId;
    }

    public String person() {
        return person;
    }
}
