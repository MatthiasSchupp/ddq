package eu.domaindriven.ddq.event;

import eu.domaindriven.ddq.notification.*;
import eu.domaindriven.ddq.notification.*;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Path("notifications-mock")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EventSourceMock {

    private final NotificationProvider<NotificationEvent> notificationProvider;

    public EventSourceMock() {
        notificationProvider = new NotificationProviderMock();
    }

    @GET
    public Response currentNotificationLog(@QueryParam("size") @DefaultValue("20") int size, @Context UriInfo uriInfo, @Context Request request) {
        NotificationLogFactory factory = new NotificationLogFactory(notificationProvider, size);
        NotificationLog notificationLog = factory.createCurrent();

        return createResponse(notificationLog, uriInfo, request);
    }

    @GET
    @Path("{id}")
    public Response notificationLog(@PathParam("id") String id, @Context UriInfo uriInfo, @Context Request request) {
        NotificationLogId notificationLogId = new NotificationLogId(id);
        NotificationLogFactory factory = new NotificationLogFactory(notificationProvider, notificationLogId.size());
        NotificationLog notificationLog = factory.create(notificationLogId);

        return createResponse(notificationLog, uriInfo, request);
    }

    private Response createResponse(NotificationLog notificationLog, UriInfo uriInfo, Request request) {
        EntityTag eTag = new EntityTag(Integer.toHexString(notificationLog.hashCode()));
        Response.ResponseBuilder builder = request.evaluatePreconditions(eTag);

        if (builder == null) {
            builder = Response.ok(new NotificationLogRepresentation(notificationLog, uriInfo, "notifications-mock")).tag(eTag);
        }

        return builder.build();
    }

    private static final class NotificationProviderMock implements NotificationProvider<NotificationEvent> {

        private final List<NotificationEvent> events = new ArrayList<>();

        NotificationProviderMock() {
            for (int i = 1; i <= 50; i++) {
                events.add(new NotificationEvent(i, new TestEvent("test " + i, i)));
            }
        }

        @Override
        public Stream<NotificationEvent> between(long lowId, long highId) {
            int upperId = (int) Math.min(highId, events.size());
            return events.subList((int) lowId - 1, upperId).stream();
        }

        @Override
        public Optional<Long> maxId() {
            return Optional.of((long) events.size());
        }

        @Override
        public Optional<Long> minId() {
            return Optional.of(1L);
        }
    }

    private static final class NotificationEvent implements Notifiable {

        private final long id;
        private final Instant timestamp;
        private final DomainEvent event;

        private NotificationEvent(long id, DomainEvent event) {
            this.id = id;
            this.timestamp = Instant.now();
            this.event = event;
        }

        @Override
        public Long id() {
            return id;
        }

        @Override
        public Instant timestamp() {
            return timestamp;
        }

        @Override
        public Object toProjection() {
            return event;
        }
    }

    private static final class NotificationLogFactory {

        private final NotificationProvider<?> notificationProvider;
        private final int logSize;

        private NotificationLogFactory(NotificationProvider<?> notificationProvider, int logSize) {
            this.notificationProvider = notificationProvider;
            this.logSize = logSize;
        }

        public NotificationLog createCurrent() {
            return createNotificationLog(createCurrentNotificationLogId());
        }

        public NotificationLog create(NotificationLogId id) {
            NotificationLogInfo info = new NotificationLogInfo(id, notificationProvider.maxId().orElse(0L), notificationProvider.minId().orElse(0L));

            return this.createNotificationLog(info);
        }

        private NotificationLog createNotificationLog(NotificationLogInfo info) {
            List<Notification> notifications = notificationProvider.between(info.id().low(), info.id().high())
                    .map(this::createNotification)
                    .collect(Collectors.toList());

            NotificationLogStatus status = calculateStatus(info);

            NotificationLogId next = status == NotificationLogStatus.ARCHIVED
                    ? info.id().next(logSize).orElse(null)
                    : null;

            NotificationLogId previous = info.minId() < info.id().low()
                    ? info.id().previous(logSize).orElse(null)
                    : null;

            return new NotificationLog(info.id(), next, previous, notifications, status);
        }

        private NotificationLogStatus calculateStatus(NotificationLogInfo info) {
            return info.id().high() < info.maxId()
                    ? NotificationLogStatus.ARCHIVED
                    : NotificationLogStatus.ACTUAL;
        }

        private Notification createNotification(Notifiable notifiable) {
            return new Notification(notifiable.id(), notifiable.timestamp(), notifiable.toProjection());
        }

        private NotificationLogInfo createCurrentNotificationLogId() {
            long maxId = notificationProvider.maxId().orElse(0L);
            long minId = notificationProvider.minId().orElse(0L);

            long remainder = maxId % logSize;

            if (remainder == 0 && maxId > 0) {
                remainder = logSize;
            }

            long low = maxId - remainder + 1;
            long high = low + logSize - 1;

            return new NotificationLogInfo(new NotificationLogId(low, high), maxId, minId);
        }

        private static final class NotificationLogInfo {
            private final NotificationLogId id;
            private final long maxId;
            private final long minId;

            private NotificationLogInfo(NotificationLogId id, long maxId, long minId) {
                this.id = id;
                this.maxId = maxId;
                this.minId = minId;
            }

            private NotificationLogId id() {
                return id;
            }

            private long maxId() {
                return maxId;
            }

            public long minId() {
                return minId;
            }
        }

    }
}
