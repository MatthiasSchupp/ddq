package eu.domaindriven.ddq.event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EventSourceLog {
    private final List<? extends DomainEvent> domainEvents;
    private final long lastId;

    EventSourceLog(List<? extends DomainEvent> domainEvents, long lastId) {
        this.domainEvents = new ArrayList<>(domainEvents);
        this.lastId = lastId;
    }

    public List<DomainEvent> domainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    public long lastId() {
        return lastId;
    }

    public boolean empty() {
        return domainEvents.isEmpty();
    }
}
