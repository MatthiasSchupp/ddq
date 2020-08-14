package eu.domaindriven.ddq.hal;

import javax.json.bind.serializer.JsonbSerializer;
import java.util.Map;

public class LogRepresentationSerializer extends HalSerializer<LogRepresentation> implements JsonbSerializer<LogRepresentation> {

    @Override
    protected Map<String, Object> supplyRegularFields(LogRepresentation representation) {
        return Map.of("count", representation.count());
    }

    @Override
    protected Map<String, Object> supplyEmbeddedFields(LogRepresentation representation) {
        return Map.of(representation.embeddedKey(), representation.elements());
    }
}
