package eu.domaindriven.ddq;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.net.URI;

@SuppressWarnings("ReturnOfNull")
@Converter
public class UriConverter implements AttributeConverter<URI, String> {

    @Override
    public String convertToDatabaseColumn(URI attribute) {
        return attribute == null
                ? null
                : attribute.toString();
    }

    @Override
    public URI convertToEntityAttribute(String dbData) {
        return dbData == null || dbData.isEmpty()
                ? null
                : URI.create(dbData.trim());
    }
}
