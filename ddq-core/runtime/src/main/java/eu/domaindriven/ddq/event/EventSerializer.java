package eu.domaindriven.ddq.event;

import javax.enterprise.context.Dependent;
import javax.json.JsonObject;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;
import javax.json.bind.config.PropertyVisibilityStrategy;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

@Dependent
public class EventSerializer {

    private final Jsonb jsonb;

    public EventSerializer() {
        JsonbConfig config = new JsonbConfig();
        config.withPropertyVisibilityStrategy(new PropertyVisibilityStrategy() {
            @Override
            public boolean isVisible(Field field) {
                return true;
            }

            @Override
            public boolean isVisible(Method method) {
                return false;
            }
        });
        config.withNullValues(true);

        jsonb = JsonbBuilder.create(config);
    }

    public String serialize(DomainEvent event) {
        return jsonb.toJson(event);
    }

    public <T extends DomainEvent> T deserialize(String event, final Class<T> type) {
        return jsonb.fromJson(event, type);
    }

    public <T extends DomainEvent> T deserialize(JsonObject event, Class<T> type) {
        return deserialize(event.toString(), type);
    }
}
