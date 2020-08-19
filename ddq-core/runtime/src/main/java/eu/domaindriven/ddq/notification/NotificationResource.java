package eu.domaindriven.ddq.notification;

import eu.domaindriven.ddq.error.ErrorStore;
import eu.domaindriven.ddq.event.EventStore;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

@Path("notifications")
@Transactional
public class NotificationResource {

    @Inject
    EventStore eventStore;

    @Inject
    ErrorStore errorStore;

    @GET
    @Path("{type}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response currentNotificationLog(@PathParam("type") String type, @QueryParam("size") @DefaultValue("20") int size, @Context UriInfo uriInfo, @Context Request request) {
        NotificationLogFactory factory = new NotificationLogFactory(provider(type), size);
        NotificationLog notificationLog = factory.createCurrent();

        return createResponse(notificationLog, uriInfo, request, type);
    }

    @GET
    @Path("{type}")
    @Produces("application/hal+json")
    public Response currentNotificationLogHal(@PathParam("type") String type, @QueryParam("size") @DefaultValue("20") int size, @Context UriInfo uriInfo, @Context Request request) {
        NotificationLogFactory factory = new NotificationLogFactory(provider(type), size);
        NotificationLog notificationLog = factory.createCurrent();

        return createHalResponse(notificationLog, uriInfo, request, type);
    }

    @GET
    @Path("{type}/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response notificationLog(@PathParam("type") String type, @PathParam("id") String id, @Context UriInfo uriInfo, @Context Request request) {
        NotificationLogId notificationLogId = new NotificationLogId(id);
        NotificationLogFactory factory = new NotificationLogFactory(eventStore, notificationLogId.size());
        NotificationLog notificationLog = factory.create(notificationLogId);

        return createResponse(notificationLog, uriInfo, request, type);
    }

    @GET
    @Path("{type}/{id}")
    @Produces("application/hal+json")
    public Response notificationLogHal(@PathParam("type") String type, @PathParam("id") String id, @Context UriInfo uriInfo, @Context Request request) {
        NotificationLogId notificationLogId = new NotificationLogId(id);
        NotificationLogFactory factory = new NotificationLogFactory(eventStore, notificationLogId.size());
        NotificationLog notificationLog = factory.create(notificationLogId);

        return createHalResponse(notificationLog, uriInfo, request, type);
    }

    private NotificationProvider<?> provider(String type) {
        return switch (type) {
            case "events" -> eventStore;
            case "errors" -> errorStore;
            default -> throw new AssertionError("Unknown notification type.");
        };
    }

    private Response createResponse(NotificationLog notificationLog, UriInfo uriInfo, Request request, String type) {
        EntityTag eTag = new EntityTag(Integer.toHexString(notificationLog.hashCode()));
        Response.ResponseBuilder builder = request.evaluatePreconditions(eTag);

        if (builder == null) {
            builder = createResponseBuilderWithLinks(notificationLog, uriInfo, type, eTag);
        }

        return builder.build();
    }

    private Response.ResponseBuilder createResponseBuilderWithLinks(NotificationLog notificationLog, UriInfo uriInfo, String type, EntityTag eTag) {
        Response.ResponseBuilder builder = Response.ok(notificationLog)
                .tag(eTag);
        notificationLog.nextId()
                .map(NotificationLogId::encoded)
                .map(id -> uriInfo.getBaseUriBuilder().path("notifications").path(type).path(id).build())
                .ifPresent(uri -> builder.link(uri, "next"));
        notificationLog.previousId()
                .map(NotificationLogId::encoded)
                .map(id -> uriInfo.getBaseUriBuilder().path("notifications").path(type).path(id).build())
                .ifPresent(uri -> builder.link(uri, "previous"));

        return builder;
    }

    private Response createHalResponse(NotificationLog notificationLog, UriInfo uriInfo, Request request, String type) {
        EntityTag eTag = new EntityTag(Integer.toHexString(notificationLog.hashCode()));
        Response.ResponseBuilder builder = request.evaluatePreconditions(eTag);

        if (builder == null) {
            builder = Response.ok(new NotificationLogRepresentation(notificationLog, uriInfo, "notifications/" + type))
                    .tag(eTag);
        }

        return builder.build();
    }
}
