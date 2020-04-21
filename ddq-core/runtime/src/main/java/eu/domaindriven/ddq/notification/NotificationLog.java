package eu.domaindriven.ddq.notification;

import java.util.*;

public class NotificationLog {

    private final NotificationLogId id;
    private final NotificationLogId nextId;
    private final NotificationLogId previousId;
    private final List<Notification> notifications;
    private final NotificationLogStatus status;

    public NotificationLog(NotificationLogId id,
                           NotificationLogId nextId,
                           NotificationLogId previousId,
                           List<Notification> notifications,
                           NotificationLogStatus status) {
        this.id = id;
        this.nextId = nextId;
        this.previousId = previousId;
        this.notifications = new ArrayList<>(notifications);
        this.status = status;
    }

    public NotificationLogId id() {
        return id;
    }

    public Optional<NotificationLogId> nextId() {
        return Optional.ofNullable(nextId);
    }

    public Optional<NotificationLogId> previousId() {
        return Optional.ofNullable(previousId);
    }

    public List<Notification> notifications() {
        return Collections.unmodifiableList(notifications);
    }

    public NotificationLogStatus status() {
        return status;
    }

    public int size() {
        return notifications.size();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NotificationLog that = (NotificationLog) o;
        return notifications.equals(that.notifications);
    }

    @Override
    public int hashCode() {
        return Objects.hash(notifications);
    }
}
