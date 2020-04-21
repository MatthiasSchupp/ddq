package eu.domaindriven.ddq.event;

import eu.domaindriven.ddq.error.ErrorPublisher;
import io.quarkus.arc.Arc;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import org.eclipse.microprofile.context.ManagedExecutor;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.concurrent.Executors;

abstract class SelfRunningProcessor {

    @Inject
    ErrorPublisher errorPublisher;

    private final String identifier;

    private ContinuousExecutor executor;

    @SuppressWarnings("SameParameterValue")
    protected SelfRunningProcessor(String identifier) {
        this.identifier = identifier;
    }

    @PostConstruct
    void init() {
        executor = new ContinuousExecutor(
                identifier,
                managedExecutor(),
                Executors.newSingleThreadScheduledExecutor(),
                selfReference()::process,
                selfReference()::handleError);
    }

    protected abstract ManagedExecutor managedExecutor();

    @Transactional
    abstract boolean process();

    void handleError(String identifier, Throwable throwable) {
        errorPublisher.technical("Exception while running ''{0}''.", this.getClass(), throwable, identifier);
    }

    void onStart(@Observes StartupEvent event) {
        executor.startDelayed();
    }

    void onStop(@Observes ShutdownEvent event) {
        executor.stop();
    }

    protected <T extends SelfRunningProcessor> T selfReference() {
        return Arc.container().<T>instance(encapsulateClass(this.getClass())).get();
    }

    @SuppressWarnings("unchecked")
    private static <T extends SelfRunningProcessor> Class<T> encapsulateClass(Class<T> clazz) {
        return clazz.getSimpleName().endsWith("_Subclass")
                ? (Class<T>) clazz.getSuperclass()
                : clazz;
    }
}
