package eu.domaindriven.ddq.event;

import javax.enterprise.context.ApplicationScoped;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@ApplicationScoped
public class EventRegistry {

    private final ConcurrentMap<String, Class<? extends DomainEvent>> eventTypes;

    public EventRegistry() {
        eventTypes = new ConcurrentHashMap<>();
    }

    void add(Class<? extends DomainEvent> eventType) {
        eventTypes.put(eventType.getSimpleName(), eventType);
    }

    @SuppressWarnings("unchecked")
    <T extends DomainEvent> Optional<Class<T>> eventType(String name) {
        return Optional.ofNullable((Class<T>) eventTypes.get(name));
    }

    boolean contains(String name) {
        return eventTypes.containsKey(name);
    }
}
