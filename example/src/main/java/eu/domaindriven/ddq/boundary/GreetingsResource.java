package eu.domaindriven.ddq.boundary;

import eu.domaindriven.ddq.application.GreetingService;
import eu.domaindriven.ddq.domain.model.Greeting;
import eu.domaindriven.ddq.domain.model.GreetingId;
import eu.domaindriven.ddq.domain.model.Person;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;

@Path("greetings")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Transactional
public class GreetingsResource {

    @Inject
    GreetingService greetingService;

    @GET
    public Response greetings(@QueryParam("name") String personName, @Context UriInfo uriInfo, @Context Request request) {
        return personName != null
                ? greetingService.greeting(new Person(personName))
                .map(greeting -> createResponse(Collections.singletonList(greeting), uriInfo, request))
                .orElse(Response.status(NOT_FOUND).build())
                : createResponse(greetingService.greetings(), uriInfo, request);
    }

    @GET
    @Path("{id}")
    public Response greeting(@PathParam("id") String id, @Context UriInfo uriInfo, @Context Request request) {
        return greetingService.greeting(new GreetingId(id))
                .map(greeting -> createResponse(greeting, uriInfo, request))
                .orElse(Response.status(NOT_FOUND).build());
    }

    @POST
    public Response create(JsonObject content, @Context UriInfo uriInfo) {
        if (content.containsKey("name")) {
            String personName = content.getString("name");
            Greeting greeting = greetingService.create(personName);
            URI location = uriInfo.getRequestUriBuilder().path(greeting.greetingId().id().toString()).build();

            return Response.created(location).build();
        } else {
            return Response.status(BAD_REQUEST).build();
        }
    }

    @POST
    @Path("{id}/salute")
    public Response salute(@PathParam("id") String id, @Context Request request) {
        greetingService.salute(new GreetingId(id));

        return Response.ok().build();
    }
    @GET
    @Path("{id}/salutes")
    public Response salutes(@PathParam("id") String id, @Context Request request) {
        int salutes = greetingService.salutes(new GreetingId(id));

        return createResponse(salutes, request);
    }

    @GET
    @Path("salutes")
    public Response salutes(@Context Request request) {
        int salutes = greetingService.salutes();

        return createResponse(salutes, request);
    }

    private Response createResponse(int salutes, Request request) {
        EntityTag eTag = new EntityTag(Integer.toHexString(salutes));
        Response.ResponseBuilder builder = request.evaluatePreconditions(eTag);

        if (builder == null) {
            builder = Response.ok(Json.createObjectBuilder()
                    .add("salutes", salutes)
                    .build()
            ).tag(eTag);
        }

        return builder.build();
    }

    private Response createResponse(Greeting greeting, UriInfo uriInfo, Request request) {
        EntityTag eTag = new EntityTag(Integer.toHexString(greeting.hashCode()));
        Response.ResponseBuilder builder = request.evaluatePreconditions(eTag);

        if (builder == null) {
            builder = Response.ok(new GreetingRepresentation(greeting, uriInfo, "greetings"))
                    .tag(eTag);
        }

        return builder.build();
    }

    private Response createResponse(Collection<Greeting> greetings, UriInfo uriInfo, Request request) {
        EntityTag eTag = new EntityTag(Integer.toHexString(greetings.hashCode()));
        Response.ResponseBuilder builder = request.evaluatePreconditions(eTag);

        if (builder == null) {
            builder = Response.ok(new GreetingLogRepresentation(greetings, uriInfo, "greetings")).tag(eTag);
        }

        return builder.build();
    }
}
