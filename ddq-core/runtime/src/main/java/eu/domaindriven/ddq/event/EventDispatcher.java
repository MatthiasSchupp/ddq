package eu.domaindriven.ddq.event;

import eu.domaindriven.ddq.config.Configurable;
import eu.domaindriven.ddq.config.DdqConfig;
import org.eclipse.microprofile.context.ManagedExecutor;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static java.lang.System.Logger.Level.ERROR;

@ApplicationScoped
public class EventDispatcher extends SelfRunningProcessor implements Configurable {

    private static final System.Logger LOGGER = System.getLogger(EventDispatcher.class.getName());

    @Inject
    @EventExecutor
    ManagedExecutor managedExecutor;

    @Inject
    EventStore eventStore;

    @Inject
    Event<DomainEvent> events;

    private Duration processingThreshold;

    public EventDispatcher() {
        super("Event Dispatcher");
    }

    @Override
    protected ManagedExecutor managedExecutor() {
        return managedExecutor;
    }

    @Override
    public void configure(DdqConfig config) {
        processingThreshold = config.event.processingThreshold;
    }

    @SuppressWarnings("RedundantTypeArguments")
    @Override
    @Transactional
    boolean process() {
        Optional<StoredEvent> storedEvent = eventStore.nextUnprocessed(Instant.now().minus(processingThreshold));
        storedEvent.map(StoredEvent::<DomainEvent>toDomainEvent)
                .ifPresent(this::dispatchEvent);

        return storedEvent.isPresent();
    }

    private void dispatchEvent(DomainEvent event) {
        try {
            events.fire(event);
        } catch (RuntimeException e) {
            this.<EventDispatcher>selfReference().reportException(event, e);
            throw e;
        }
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    void reportException(DomainEvent event, Exception e) {
        LOGGER.log(ERROR, () -> "Exception while process event '" + event + "'", e);
        errorPublisher.technical("Exception while process event ''{0}''", e, event);
    }


}
