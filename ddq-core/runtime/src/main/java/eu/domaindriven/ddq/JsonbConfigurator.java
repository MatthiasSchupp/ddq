package eu.domaindriven.ddq;

import io.quarkus.jsonb.JsonbConfigCustomizer;

import javax.inject.Singleton;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.bind.JsonbConfig;
import javax.json.bind.adapter.JsonbAdapter;
import javax.json.bind.config.PropertyVisibilityStrategy;
import javax.ws.rs.core.Link;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

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
        jsonbConfig.withAdapters(new LinkMapAdapter());
    }

    private static class LinkAdapter implements JsonbAdapter<Link, JsonObject> {
        @Override
        public JsonObject adaptToJson(Link link) {
            return Json.createObjectBuilder()
                    .add("rel", link.getRel())
                    .add("href", link.getUri().toString())
                    .build();
        }

        @Override
        public Link adaptFromJson(JsonObject link) {
            return Link.fromUri(link.getString("href"))
                    .rel(link.getString("rel"))
                    .build();
        }
    }

    private static class LinkMapAdapter implements JsonbAdapter<Map<String, Link>, JsonObject> {
        @Override
        public JsonObject adaptToJson(Map<String, Link> links) {
            JsonObjectBuilder json = Json.createObjectBuilder();
            links.forEach((rel, link) -> json.add(rel, convertLink(link)));

            return json.build();
        }

        @Override
        public Map<String, Link> adaptFromJson(JsonObject json) {
            Map<String, Link> links = new HashMap<>();
            json.forEach((rel, link) -> links.put(rel, convertLink(link.asJsonObject(), rel)));

            return links;
        }

        private JsonObjectBuilder convertLink(Link link) {
            return Json.createObjectBuilder()
                    .add("href", link.getUri().toString());
        }

        private Link convertLink(JsonObject link, String rel) {
            return Link.fromUri(link.getString("href"))
                    .rel(rel)
                    .build();
        }
    }
}