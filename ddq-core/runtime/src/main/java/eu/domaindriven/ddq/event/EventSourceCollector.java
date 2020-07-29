package eu.domaindriven.ddq.event;

import eu.domaindriven.ddq.config.Configurable;
import eu.domaindriven.ddq.config.DdqConfig;
import eu.domaindriven.ddq.error.ErrorPublisher;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import org.eclipse.microprofile.context.ManagedExecutor;
import org.eclipse.microprofile.context.ThreadContext;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

@ApplicationScoped
public class EventSourceCollector implements Configurable {

    @Inject
    EventSourceCollector self;

    @Inject
    EventSourceClient client;

    @Inject
    EventSourceTrackerRepository repository;

    @Inject
    EventStore eventStore;

    @Inject
    ErrorPublisher errorPublisher;

    private ManagedExecutor managedExecutor;

    private final List<EventSource> eventSources;
    private final List<ContinuousExecutor> executors;

    public EventSourceCollector() {
        eventSources = new ArrayList<>();
        executors = new ArrayList<>();
    }

    @PostConstruct
    public void init() {
        managedExecutor = ManagedExecutor.builder()
                .maxAsync(5)
                .propagated(ThreadContext.ALL_REMAINING)
                .cleared()
                .build();
    }

    @Override
    public void configure(DdqConfig config) {
        config.event.eventSources.entrySet().stream()
                .map(entry -> createEventSource(
                        entry.getKey(),
                        entry.getValue(),
                        config.event.eventSourceProtocol,
                        config.event.eventSourceResource))
                .forEach(eventSources::add);
    }

    private static URI createUri(String eventSource, String protocol, String resource) {
        // Starts the event source with a protocol?
        if (eventSource.contains("://")) {
            // Defines the event source the full path? Indicated by containing more than the protocol (://) and top-level slashes
            return eventSource.chars().filter(ch -> ch == '/').count() > 3
                    ? URI.create(eventSource)
                    : URI.create(eventSource + resource);
        } else {
            // Defines the event source the sub path?
            return eventSource.chars().filter(ch -> ch == '/').count() > 1
                    ? URI.create(protocol + "://" + eventSource)
                    : URI.create(protocol + "://" + eventSource + resource);
        }
    }

    private static EventSource createEventSource(
            String name,
            eu.domaindriven.ddq.config.EventSource eventSource,
            String protocol,
            String resource) {
        Long startId = eventSource.startId.isPresent()
                ? eventSource.startId.getAsLong()
                : null;

        return new EventSource(
                name,
                createUri(eventSource.uri, protocol, resource),
                startId);
    }

    void onStart(@Observes StartupEvent event) {
        eventSources.stream()
                .map(this::createExecutor)
                .map(this::addExecutor)
                .forEach(ContinuousExecutor::startDelayed);
    }

    void onStop(@Observes ShutdownEvent event) {
        executors.forEach(ContinuousExecutor::stop);
    }

    private ContinuousExecutor addExecutor(ContinuousExecutor executor) {
        executors.add(executor);
        return executor;
    }

    private ContinuousExecutor createExecutor(EventSource eventSource) {
        return new ContinuousExecutor(
                eventSource.name(),
                managedExecutor,
                Executors.newSingleThreadScheduledExecutor(),
                () -> self.collect(eventSource),
                self::handleError);
    }

    @Transactional
    boolean collect(EventSource eventSource) {
        EventSourceTracker tracker = repository.tracker(eventSource)
                .uri(eventSource.uri());

        EventSourceLog log = tracker.lastId().isPresent()
                ? client.since(tracker.uri(), tracker.lastId().orElseThrow())
                : client.current(tracker.uri());

        tracker.lastId(log.lastId());
        log.domainEvents().stream()
                .map(domainEvent -> defineSource(domainEvent, eventSource.name()))
                .forEach(eventStore::append);

        return !log.empty();
    }

    private DomainEvent defineSource(DomainEvent domainEvent, String source) {
        return domainEvent.source().equals("self")
                ? domainEvent.source(source)
                : domainEvent;
    }

    @Transactional
    void handleError(String identifier, Throwable throwable) {
        errorPublisher.technical("Exception while collecting events from source ''{0}''.", throwable, identifier);
    }

}
