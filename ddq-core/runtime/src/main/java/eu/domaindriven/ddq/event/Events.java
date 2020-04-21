package eu.domaindriven.ddq.event;

import io.quarkus.arc.Arc;

@SuppressWarnings("UtilityClass")
public final class Events {

    private Events() {
    }

    public static void publish(DomainEvent event) {
        publisher().publish(event);
    }

    public static EventPublisher publisher() {
        return Arc.container().instance(EventPublisher.class).get();
    }
}
