package eu.domaindriven.ddq.hal;

import eu.domaindriven.ddq.domain.Entity;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public class LogRepresentation implements HalObject {

    private final List<?> elements;
    private final int count;
    private final String embeddedKey;

    public <T extends Entity, E extends Representation> LogRepresentation(Collection<T> elements, String embeddedKey, Function<T, E> representationFactory) {
        this.embeddedKey = embeddedKey;
        this.elements = elements.stream()
                .map(representationFactory)
                .collect(Collectors.toList());
        this.count = this.elements.size();
    }

    List<?> elements() {
        return elements;
    }

    int count() {
        return count;
    }

    String embeddedKey() {
        return embeddedKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LogRepresentation that = (LogRepresentation) o;
        return count == that.count &&
                elements.equals(that.elements) &&
                embeddedKey.equals(that.embeddedKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(elements, count, embeddedKey);
    }
}
