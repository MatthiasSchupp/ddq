package eu.domaindriven.ddq.notification;

import java.time.Instant;

public interface Notifiable {

    Long id();

    Instant timestamp();

    Object toProjection();
}
