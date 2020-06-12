package eu.domaindriven.ddq.event;

import javax.enterprise.context.ApplicationScoped;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@ApplicationScoped
public class EventRegistry {

    private final ConcurrentMap<String, Class<? extends DomainEvent>> eventTypes;
    private final Set<String> collectableEvents;

    public EventRegistry() {
        eventTypes = new ConcurrentHashMap<>();
        collectableEvents = new HashSet<>();
    }

    void add(Class<? extends DomainEvent> eventType) {
        String name = eventType.getSimpleName();
        eventTypes.put(name, eventType);
        if (eventType.isAnnotationPresent(CollectableEvent.class)) {
            collectableEvents.add(name);
        }
    }

    @SuppressWarnings("unchecked")
    <T extends DomainEvent> Optional<Class<T>> eventType(String name) {
        return Optional.ofNullable((Class<T>) eventTypes.get(name));
    }

    boolean contains(String name) {
        return eventTypes.containsKey(name);
    }

    boolean collectable(String name) {
        return collectableEvents.contains(name);
    }
}
