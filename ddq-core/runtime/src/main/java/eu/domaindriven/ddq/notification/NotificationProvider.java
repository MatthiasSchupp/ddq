package eu.domaindriven.ddq.notification;

import java.util.Optional;
import java.util.stream.Stream;

public interface NotificationProvider<T extends Notifiable> {
    Stream<T> between(long lowId, long highId);

    Optional<Long> maxId();

    Optional<Long> minId();
}
