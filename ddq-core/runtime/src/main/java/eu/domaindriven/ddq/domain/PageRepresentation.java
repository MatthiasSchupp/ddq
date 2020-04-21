package eu.domaindriven.ddq.domain;

import javax.json.bind.annotation.JsonbTransient;
import javax.ws.rs.core.UriInfo;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class PageRepresentation<T extends Entity, E extends Representation> extends Representation {

    private static final String QUERY_PARAM_PAGE_INDEX = "pageIndex";
    private static final String QUERY_PARAM_PAGE_SIZE = "pageSize";

    @JsonbTransient
    private final List<E> elements;
    private final int count;
    private final int pageCount;
    private final int pageSize;

    public PageRepresentation(Page<T> page, UriInfo uriInfo, String path, String embeddedKey,  BiFunction<T, ? super UriInfo, E> representationFactory) {
        this.elements = page.entries().stream()
                .map(element -> representationFactory.apply(element, uriInfo))
                .collect(Collectors.toList());
        this.count = page.count();
        this.pageCount = page.pageCount();
        this.pageSize = page.pageSize();
        this.embedded(embeddedKey, elements);
        link("self", uriInfo.getBaseUriBuilder().path(path).queryParam(QUERY_PARAM_PAGE_INDEX, page.pageIndex()).queryParam(QUERY_PARAM_PAGE_SIZE, page.pageSize()));
        if (page.hasPrevious()) {
            link("previous", uriInfo.getBaseUriBuilder().path(path).queryParam(QUERY_PARAM_PAGE_INDEX, page.previousPageIndex()).queryParam(QUERY_PARAM_PAGE_SIZE, page.pageSize()));
        }
        if (page.hasNext()) {
            link("next", uriInfo.getBaseUriBuilder().path(path).queryParam(QUERY_PARAM_PAGE_INDEX, page.nextPageIndex()).queryParam(QUERY_PARAM_PAGE_SIZE, page.pageSize()));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PageRepresentation<?, ?> that = (PageRepresentation<?, ?>) o;
        return count == that.count &&
                pageCount == that.pageCount &&
                pageSize == that.pageSize &&
                elements.equals(that.elements);
    }

    @Override
    public int hashCode() {
        return Objects.hash(elements, count, pageCount, pageSize);
    }
}
