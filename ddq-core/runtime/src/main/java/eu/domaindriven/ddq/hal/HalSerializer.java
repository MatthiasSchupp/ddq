package eu.domaindriven.ddq.hal;

import org.jboss.resteasy.core.ResteasyContext;

import javax.json.bind.annotation.JsonbTransient;
import javax.json.bind.serializer.JsonbSerializer;
import javax.json.bind.serializer.SerializationContext;
import javax.json.stream.JsonGenerator;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

public abstract class HalSerializer<T extends HalObject> implements JsonbSerializer<T> {

    @Override
    public void serialize(T obj, JsonGenerator generator, SerializationContext ctx) {
        generator.writeStartObject();
        Map<FieldType, List<Field>> fields = Arrays.stream(obj.getClass().getDeclaredFields())
                .filter(Field::trySetAccessible)
                .collect(Collectors.groupingBy(HalSerializer::classifyField));
        Map<String, Object> regularFields = extract(fields.getOrDefault(FieldType.REGULAR, List.of()), obj);
        regularFields.putAll(supplyRegularFields(obj));
        Map<String, Object> embeddedFields = extract(fields.getOrDefault(FieldType.EMBEDDED, List.of()), obj);
        embeddedFields.putAll(supplyEmbeddedFields(obj));
        Map<String, Object> transientFields = extract(fields.getOrDefault(FieldType.TRANSIENT, List.of()), obj);
        Map<String, Object> linkTemplateValues = new HashMap<>(regularFields);
        linkTemplateValues.putAll(transientFields);
        Map<String, String> linkFields = extractLinks(fields.getOrDefault(FieldType.LINK, List.of()), linkTemplateValues, obj);
        linkFields.putAll(supplyLinks(obj));

        serializeRegularFields(regularFields, generator, ctx);
        serializeLinks(linkFields, generator);
        serializeEmbeddedFields(embeddedFields, generator, ctx);
        generator.writeEnd();
    }

    protected Map<String, Object> supplyRegularFields(T obj) {
        return Map.of();
    }

    protected Map<String, Object> supplyEmbeddedFields(T obj) {
        return Map.of();
    }

    protected Map<String, String> supplyLinks(T obj) {
        return Map.of();
    }

    protected static UriInfo uriInfo() {
        return ResteasyContext.getContextData(UriInfo.class);
    }

    private static void serializeLinks(Map<String, String> links, JsonGenerator generator) {
        generator.writeStartObject("_links");
        if (!links.containsKey("self")) {
            serializeLink("self", uriInfo().getRequestUri().toString(), generator);
        }
        links.forEach((key, value) -> serializeLink(key, value, generator));
        generator.writeEnd();
    }

    private static Map<String, String> extractLinks(Collection<Field> fields, Map<String, Object> templateValues, Object obj) {
        return fields.stream().collect(Collectors.toMap(HalSerializer::createRel, field -> createHref(field, obj, templateValues), (href1, href2) -> href1, HashMap::new));
    }

    private static String createRel(Field field) {
        String rel = field.getName();
        if (field.isAnnotationPresent(BaseLink.class)) {
            String annotationRel = field.getAnnotation(BaseLink.class).rel();
            if (!annotationRel.isBlank()) {
                rel = annotationRel;
            }
        } else if (field.isAnnotationPresent(RequestLink.class)) {
            String annotationRel = field.getAnnotation(RequestLink.class).rel();
            if (!annotationRel.isBlank()) {
                rel = annotationRel;
            }
        }

        return rel;
    }

    private static String createHref(Field field, Object obj, Map<String, Object> templateValues) {
        try {
            String href;
            Link link = (Link) field.get(obj);
            if (link != null) {
                href = link.getUri().toString();
            } else if (isLinkAnnotationPresent(field)) {
                href = createUriBuilder(field).resolveTemplates(templateValues).build().toString();
            } else {
                href = "";
            }

            return href;
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    private static boolean isLinkAnnotationPresent(Field field) {
        return field.isAnnotationPresent(BaseLink.class) || field.isAnnotationPresent(RequestLink.class);
    }

    private static UriBuilder createUriBuilder(Field field) {
        if (field.isAnnotationPresent(BaseLink.class)) {
            return uriInfo().getBaseUriBuilder().path(field.getAnnotation(BaseLink.class).path());
        } else if (field.isAnnotationPresent(RequestLink.class)) {
            return uriInfo().getRequestUriBuilder().path(field.getAnnotation(RequestLink.class).path());
        } else {
            throw new IllegalArgumentException("No annotation present.");
        }
    }

    private static void serializeLink(String rel, String href, JsonGenerator generator) {
        generator.writeStartObject(rel);
        generator.write("href", href);
        generator.writeEnd();
    }

    private static void serializeEmbeddedFields(Map<String, Object> fields, JsonGenerator generator, SerializationContext ctx) {
        generator.writeStartObject("_embedded");
        serializeRegularFields(fields, generator, ctx);
        generator.writeEnd();
    }

    private static void serializeRegularFields(Map<String, Object> fields, JsonGenerator generator, SerializationContext ctx) {
        fields.forEach((key, value) -> ctx.serialize(key, value, generator));
    }

    private static Map<String, Object> extract(Collection<Field> fields, Object obj) {
        return fields.stream().collect(Collectors.toMap(Field::getName, field -> extractValue(field, obj), (value1, value2) -> value1, HashMap::new));
    }

    private static Object extractValue(Field field, Object obj) {
        try {
            return field.get(obj);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    private static FieldType classifyField(Field field) {
        if (field.isAnnotationPresent(JsonbTransient.class)) {
            return FieldType.TRANSIENT;
        } else if (field.getType().equals(Link.class)) {
            return FieldType.LINK;
        } else if (field.isAnnotationPresent(Embedded.class)) {
            return FieldType.EMBEDDED;
        } else {
            return FieldType.REGULAR;
        }
    }

    private enum FieldType {
        LINK,
        EMBEDDED,
        REGULAR,
        TRANSIENT
    }
}
