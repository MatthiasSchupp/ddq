package eu.domaindriven.ddq.notification;

import eu.domaindriven.ddq.event.DomainEvent;

import javax.json.bind.annotation.JsonbCreator;
import javax.json.bind.annotation.JsonbProperty;
import java.time.Instant;
import java.util.UUID;

@SuppressWarnings({"unused", "squid:S2160"})
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
    public NotificationTestEvent(@JsonbProperty("id") UUID id, @JsonbProperty("timestamp") Instant timestamp, @JsonbProperty("name") String name, @JsonbProperty("value") int value, @JsonbProperty("exampleTimestamp") Instant exampleTimestamp) {
        super(id, timestamp);
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
