package eu.domaindriven.ddq.event;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

@Dependent
public class EventPublisher {

    @Inject
    EventStore eventStore;

    public void publish(DomainEvent event) {
        eventStore.append(event);
    }
}
