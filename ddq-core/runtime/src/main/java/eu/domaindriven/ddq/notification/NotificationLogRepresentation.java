package eu.domaindriven.ddq.notification;

import javax.json.bind.annotation.JsonbProperty;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.*;

@SuppressWarnings("unused")
public class NotificationLogRepresentation {

    private final NotificationLogId id;
    private final NotificationLogStatus status;
    private final long size;
    private final Collection<Notification> notifications;
    @JsonbProperty("_links")
    private final Map<String, Link> links;

    public NotificationLogRepresentation(NotificationLog notificationLog, UriInfo uriInfo, String path) {
        this.id = notificationLog.id();
        this.status = notificationLog.status();
        this.size = notificationLog.size();
        this.notifications = new ArrayList<>(notificationLog.notifications());
        this.links = new HashMap<>();
        link(selfLink(notificationLog, uriInfo.getBaseUriBuilder(), path));
        nextLink(notificationLog, uriInfo.getBaseUriBuilder(), path).ifPresent(this::link);
        previousLink(notificationLog, uriInfo.getBaseUriBuilder(), path).ifPresent(this::link);
    }

    private void link(Link link) {
        links.put(link.getRel(), link);
    }

    public NotificationLogId id() {
        return id;
    }

    public NotificationLogStatus status() {
        return status;
    }

    public long size() {
        return size;
    }

    public Collection<Notification> notifications() {
        return Collections.unmodifiableCollection(notifications);
    }

    public Map<String, Link> links() {
        return Collections.unmodifiableMap(links);
    }

    private static Link link(String relationship, String id, UriBuilder builder, String path) {
        URI uri = builder.path(path).path(id).build();

        return Link.fromUri(uri)
                .rel(relationship)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

    private static Link selfLink(NotificationLog notificationLog, UriBuilder builder, String path) {
        return link("self", notificationLog.id().encoded(), builder, path);
    }

    private static Optional<Link> nextLink(NotificationLog notificationLog, UriBuilder builder, String path) {
        return notificationLog.nextId()
                .map(NotificationLogId::encoded)
                .map(id -> link("next", id, builder, path));
    }

    private static Optional<Link> previousLink(NotificationLog notificationLog, UriBuilder builder, String path) {
        return notificationLog.previousId()
                .map(NotificationLogId::encoded)
                .map(id -> link("previous", id, builder, path));
    }
}
