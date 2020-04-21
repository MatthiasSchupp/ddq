package eu.domaindriven.ddq;

import io.quarkus.jsonb.JsonbConfigCustomizer;

import javax.inject.Singleton;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.bind.JsonbConfig;
import javax.json.bind.adapter.JsonbAdapter;
import javax.json.bind.config.PropertyVisibilityStrategy;
import javax.ws.rs.core.Link;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

@Singleton
public class JsonbConfigurator implements JsonbConfigCustomizer {

    @Override
    public void customize(JsonbConfig jsonbConfig) {
        jsonbConfig.withPropertyVisibilityStrategy(new PropertyVisibilityStrategy() {
            @Override
            public boolean isVisible(Field field) {
                return true;
            }

            @Override
            public boolean isVisible(Method method) {
                return false;
            }
        });
        jsonbConfig.withAdapters(new LinkAdapter());
    }

    private static class LinkAdapter implements JsonbAdapter<Link, JsonObject> {
        @Override
        public JsonObject adaptToJson(Link link) {
            return Json.createObjectBuilder()
                    .add("rel", link.getRel())
                    .add("uri", link.getUri().toString())
                    .build();
        }

        @Override
        public Link adaptFromJson(JsonObject link) {
            return Link.fromUri(link.getString("uri"))
                    .rel(link.getString("rel"))
                    .build();
        }
    }
}