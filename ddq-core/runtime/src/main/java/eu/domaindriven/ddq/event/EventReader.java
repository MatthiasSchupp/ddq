package eu.domaindriven.ddq.event;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.json.JsonObject;
import java.util.Optional;

@SuppressWarnings("unused")
@Dependent
public class EventReader {

    @Inject
    EventRegistry registry;

    @Inject
    EventSerializer serializer;

    public <T extends DomainEvent> Optional<T> read(String event, String name) {
        return registry.<T>eventType(name)
                .map(type -> serializer.deserialize(event, type));
    }

    public <T extends DomainEvent> Optional<T> read(JsonObject event, String name) {
        return registry.<T>eventType(name)
                .map(type -> serializer.deserialize(event, type));
    }

    public boolean isReadable(String name) {
        return registry.contains(name);
    }
}
