package eu.domaindriven.ddq.hal;

import eu.domaindriven.ddq.domain.Entity;
import eu.domaindriven.ddq.domain.Page;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Paged
public class PageRepresentation implements HalObject {

    private final List<?> elements;
    private final Page<?> page;
    private final String embeddedKey;

    public <T extends Entity, E extends Representation> PageRepresentation(Page<T> page, String embeddedKey, Function<T, E> representationFactory) {
        this.page = page;
        this.embeddedKey = embeddedKey;
        this.elements = page.entries().stream()
                .map(representationFactory)
                .collect(Collectors.toList());
    }

    List<?> elements() {
        return elements;
    }

    Page<?> page() {
        return page;
    }

    String embeddedKey() {
        return embeddedKey;
    }

    String queryParamIndex() {
        return this.getClass().getAnnotation(Paged.class).queryParamIndex();
    }

    String queryParamSize() {
        return this.getClass().getAnnotation(Paged.class).queryParamSize();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PageRepresentation that = (PageRepresentation) o;
        return elements.equals(that.elements) &&
                page.equals(that.page) &&
                embeddedKey.equals(that.embeddedKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(elements, page, embeddedKey);
    }
}
