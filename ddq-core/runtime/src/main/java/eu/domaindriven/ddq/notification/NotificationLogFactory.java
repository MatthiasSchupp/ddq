package eu.domaindriven.ddq.notification;

import java.util.List;
import java.util.stream.Collectors;

public class NotificationLogFactory {

    private final NotificationProvider<?> notificationProvider;
    private final int logSize;

    public NotificationLogFactory(NotificationProvider<?> notificationProvider, int logSize) {
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
