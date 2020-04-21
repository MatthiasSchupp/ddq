package eu.domaindriven.ddq.domain;

import javax.json.bind.annotation.JsonbTransient;
import javax.ws.rs.core.UriInfo;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class LogRepresentation<T extends Entity, E extends Representation> extends Representation {

    @JsonbTransient
    private final List<E> elements;
    private final int count;

    public LogRepresentation(Collection<T> page, UriInfo uriInfo, String embeddedKey, BiFunction<T, ? super UriInfo, E> representationFactory) {
        this.elements = page.stream()
                .map(element -> representationFactory.apply(element, uriInfo))
                .collect(Collectors.toList());
        this.count = elements.size();
        this.embedded(embeddedKey, elements);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LogRepresentation<?, ?> that = (LogRepresentation<?, ?>) o;
        return count == that.count &&
                elements.equals(that.elements);
    }

    @Override
    public int hashCode() {
        return Objects.hash(elements, count);
    }
}
