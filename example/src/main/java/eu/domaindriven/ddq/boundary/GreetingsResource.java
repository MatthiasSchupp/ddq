package eu.domaindriven.ddq.boundary;

import eu.domaindriven.ddq.application.GreetingService;
import eu.domaindriven.ddq.domain.EntityTagResponseFactory;
import eu.domaindriven.ddq.domain.model.Greeting;
import eu.domaindriven.ddq.domain.model.GreetingId;
import eu.domaindriven.ddq.domain.model.Person;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.links.Link;
import org.eclipse.microprofile.openapi.annotations.links.LinkParameter;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.ExampleObject;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.net.URI;
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

    @Inject
    EntityTagResponseFactory responseFactory;

    @GET
    @Operation(operationId = "greetings", summary = "Get all greetings or one greeting by person name")
    @APIResponse(responseCode = "200",
                 description = "The greetings",
                 content = @Content(mediaType = MediaType.APPLICATION_JSON,
                                    schema = @Schema(ref = "#/components/schemas/GreetingLog"),
                                    examples = {@ExampleObject(ref = "#/components/examples/GreetingLog"), @ExampleObject(ref = "#/components/examples/GreetingLogWithNameQuery")}),
                 links = {
                         @Link(name = "self", operationId = "greetings", parameters = @LinkParameter(name = "query.name", expression = "$request.query.name")),
                         @Link(name = "salutes", operationId = "salutes")
                 })
    @APIResponse(responseCode = "404",
                 description = "Greeting not found")
    public Response greetings(@QueryParam("name") String personName, @Context UriInfo uriInfo, @Context Request request) {
        return personName != null
                ? greetingService.greeting(new Person(personName))
                .map(greeting -> responseFactory.createResponse(Collections.singletonList(greeting), uriInfo, request, GreetingLogRepresentation::new))
                .orElse(Response.status(NOT_FOUND).build())
                : responseFactory.createResponse(greetingService.greetings(), uriInfo, request, GreetingLogRepresentation::new);
    }

    @GET
    @Path("{id}")
    @Operation(operationId = "greeting", summary = "Get greeting by id")
    @APIResponse(responseCode = "200",
                 description = "The greeting",
                 content = @Content(mediaType = MediaType.APPLICATION_JSON,
                                    schema = @Schema(ref = "#/components/schemas/Greeting"),
                                    examples = @ExampleObject(ref = "#/components/examples/Greeting")),
                 links = {
                         @Link(name = "self", operationId = "greeting", parameters = @LinkParameter(name = "path.id", expression = "$request.path.id")),
                         @Link(name = "salutes", operationId = "salutes", parameters = @LinkParameter(name = "path.id", expression = "$request.path.id"))
                 })
    @APIResponse(responseCode = "404",
                 description = "Greeting not found")
    public Response greeting(@PathParam("id") String id, @Context UriInfo uriInfo, @Context Request request) {
        return greetingService.greeting(new GreetingId(id))
                .map(greeting -> responseFactory.createResponse(greeting, uriInfo, request, GreetingRepresentation::new))
                .orElse(Response.status(NOT_FOUND).build());
    }

    @POST
    @Operation(operationId = "createGreeting", summary = "Creates a greeting for the given person name")
    @RequestBody(content = @Content(mediaType = MediaType.APPLICATION_JSON,
                                    schema = @Schema(ref = "#/components/schemas/GreetingCreation")),
                 required = true)
    @APIResponse(responseCode = "201",
                 description = "Greeting created")
    @APIResponse(responseCode = "400",
                 description = "Invalid request")
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
    @Operation(operationId = "saluteToGreeting", summary = "Salute to the greeting")
    @APIResponse(responseCode = "404",
                 description = "Greeting not found")
    public Response salute(@PathParam("id") String id, @Context Request request) {
        greetingService.salute(new GreetingId(id));

        return Response.ok().build();
    }

    @GET
    @Path("{id}/salutes")
    @Operation(operationId = "salutesByGreeting", summary = "Get the salutes for the greeting")
    @APIResponse(responseCode = "200",
                 description = "The salutes for the greeting",
                 content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(ref = "#/components/schemas/Salutes")))
    @APIResponse(responseCode = "404",
                 description = "Greeting not found")
    public Response salutes(@PathParam("id") String id, @Context Request request) {
        int salutes = greetingService.salutes(new GreetingId(id));

        return createResponse(salutes, request);
    }

    @GET
    @Path("salutes")
    @Operation(operationId = "salutes", summary = "Get the sum of all salutes")
    @APIResponse(responseCode = "200",
                 description = "All salutes",
                 content = @Content(mediaType = MediaType.APPLICATION_JSON,
                                    schema = @Schema(ref = "#/components/schemas/Salutes")))
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
}
