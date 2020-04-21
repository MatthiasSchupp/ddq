package eu.domaindriven.ddq.domain;

import javax.enterprise.context.Dependent;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.function.BiFunction;

@Dependent
public class EntityTagResponseFactory {

    public <T, R extends Representation> Response createResponse(T entity, UriInfo uriInfo, Request request, BiFunction<T, ? super UriInfo, R> representationFactory) {
        EntityTag eTag = createEntityTag(entity);
        Response.ResponseBuilder builder = request.evaluatePreconditions(eTag);

        if (builder == null) {
            builder = Response.ok(representationFactory.apply(entity, uriInfo)).tag(eTag);
        }

        return builder.build();
    }

    protected EntityTag createEntityTag(Object entity) {
        return new EntityTag(Integer.toHexString(entity.hashCode()));
    }
}
