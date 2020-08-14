package eu.domaindriven.ddq.domain;

import javax.enterprise.context.Dependent;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;

@Dependent
public class EntityTagResponseFactory {

    public Response createResponse(Object entity, Request request) {
        EntityTag eTag = createEntityTag(entity);
        Response.ResponseBuilder builder = request.evaluatePreconditions(eTag);

        if (builder == null) {
            builder = Response.ok(entity).tag(eTag);
        }

        return builder.build();
    }

    protected EntityTag createEntityTag(Object entity) {
        return new EntityTag(Integer.toHexString(entity.hashCode()));
    }
}
