package eu.domaindriven.ddq.hal;

import eu.domaindriven.ddq.domain.Page;

import javax.json.bind.serializer.JsonbSerializer;
import java.util.HashMap;
import java.util.Map;

public class PageRepresentationSerializer extends HalSerializer<PageRepresentation> implements JsonbSerializer<PageRepresentation> {

    @Override
    protected Map<String, Object> supplyRegularFields(PageRepresentation representation) {
        Page<?> page = representation.page();
        return Map.of(
                "count", page.count(),
                "pageCount", page.pageCount(),
                "pageIndex", page.pageIndex(),
                "pageSize", page.pageSize()
        );
    }

    @Override
    protected Map<String, Object> supplyEmbeddedFields(PageRepresentation representation) {
        return Map.of(representation.embeddedKey(), representation.elements());
    }

    @Override
    protected Map<String, String> supplyLinks(PageRepresentation representation) {
        Map<String, String> links = new HashMap<>();
        Page<?> page = representation.page();
        String queryParamIndex = representation.queryParamIndex();
        String queryParamSize = representation.queryParamSize();

        String path = uriInfo().getPath();

        links.put("self", uriInfo().getBaseUriBuilder().path(path).queryParam(queryParamIndex, page.pageIndex()).queryParam(queryParamSize, page.pageSize()).build().toString());
        if (page.hasPrevious()) {
            links.put("previous", uriInfo().getBaseUriBuilder().path(path).queryParam(queryParamIndex, page.previousPageIndex()).queryParam(queryParamSize, page.pageSize()).build().toString());
        }
        if (page.hasNext()) {
            links.put("next", uriInfo().getBaseUriBuilder().path(path).queryParam(queryParamIndex, page.nextPageIndex()).queryParam(queryParamSize, page.pageSize()).build().toString());
        }
        return links;
    }
}
