package eu.domaindriven.ddq.notification;

import eu.domaindriven.ddq.event.DomainEvent;

import javax.json.bind.annotation.JsonbCreator;
import java.time.Instant;
import java.util.UUID;

@SuppressWarnings("unused")
public class NotificationTestEvent extends DomainEvent {

    private final String name;
    private final int value;
    private final Instant exampleTimestamp;

    public NotificationTestEvent(String name, int value, Instant exampleTimestamp) {
        this.name = name;
        this.value = value;
        this.exampleTimestamp = exampleTimestamp;
    }

    @JsonbCreator
    public NotificationTestEvent(UUID id, Instant timestamp, String source, String name, int value, Instant exampleTimestamp) {
        super(id, timestamp, source);
        this.name = name;
        this.value = value;
        this.exampleTimestamp = exampleTimestamp;
    }

    public String name() {
        return name;
    }

    public int value() {
        return value;
    }

    public Instant exampleTimestamp() {
        return exampleTimestamp;
    }
}
