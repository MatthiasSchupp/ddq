package eu.domaindriven.ddq.event;

import javax.json.bind.annotation.JsonbCreator;
import java.time.Instant;
import java.util.UUID;

@SuppressWarnings("unused")
@CollectableEvent
public class TestEvent extends DomainEvent {

    private final String name;
    private final int value;

    public TestEvent(String name, int value) {
        this.name = name;
        this.value = value;
    }

    @JsonbCreator
    public TestEvent(UUID id, Instant timestamp, String source, String name, int value) {
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
