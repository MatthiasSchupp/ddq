package eu.domaindriven.ddq.hal;

import org.jboss.resteasy.core.ResteasyContext;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import java.util.function.Function;

@Dependent
public class EntityTagResponseFactory {

    @Inject
    eu.domaindriven.ddq.domain.EntityTagResponseFactory responseFactory;

    public <T, R extends HalObject> Response createResponse(T entity, Function<T, R> representationFactory) {
        R representation = representationFactory.apply(entity);
        return responseFactory.createResponse(representation, request());
    }

    private Request request() {
        return ResteasyContext.getContextData(Request.class);
    }
}
