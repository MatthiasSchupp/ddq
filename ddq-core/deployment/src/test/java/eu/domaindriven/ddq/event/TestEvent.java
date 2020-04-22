package eu.domaindriven.ddq.event;

import javax.json.bind.annotation.JsonbCreator;
import javax.json.bind.annotation.JsonbProperty;
import java.time.Instant;
import java.util.UUID;

@SuppressWarnings({"unused", "squid:S2160"})
public class TestEvent extends DomainEvent {

    private final String name;
    private final int value;

    public TestEvent(String name, int value) {
        this.name = name;
        this.value = value;
    }

    @JsonbCreator
    public TestEvent(
            @JsonbProperty("id") UUID id,
            @JsonbProperty("timestamp") Instant timestamp,
            @JsonbProperty("source") String source,
            @JsonbProperty("name") String name,
            @JsonbProperty("value") int value) {
        super(id, timestamp, source);
        this.name = name;
        this.value = value;
    }

    public String name() {
        return name;
    }

    public int value() {
        return value;
    }
}
