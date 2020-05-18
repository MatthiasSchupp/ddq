package eu.domaindriven.ddq.domain.model;

import eu.domaindriven.ddq.event.DomainEvent;

import javax.json.bind.annotation.JsonbCreator;
import java.time.Instant;
import java.util.UUID;

@SuppressWarnings("squid:S2160")
public class Greeted extends DomainEvent {

    private final String greetingId;
    private final String person;

    public Greeted(String greetingId, String person) {
        this.greetingId = greetingId;
        this.person = person;
    }

    @JsonbCreator
    public Greeted(UUID id, Instant timestamp, String source, String greetingId, String person) {
        super(id, timestamp, source);
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
