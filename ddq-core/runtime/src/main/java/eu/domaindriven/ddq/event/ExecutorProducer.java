package eu.domaindriven.ddq.event;

import io.quarkus.arc.DefaultBean;
import org.eclipse.microprofile.context.ManagedExecutor;
import org.eclipse.microprofile.context.ThreadContext;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;

@Dependent
public class ExecutorProducer {

    @Produces
    @DefaultBean
    @EventExecutor
    @ApplicationScoped
    public ManagedExecutor createExecutor() {
        return ManagedExecutor.builder()
                .maxAsync(-1)
                .propagated(ThreadContext.ALL_REMAINING)
                .cleared()
                .build();
    }

    public void disposeExecutor(@Disposes @EventExecutor ManagedExecutor executor) {
        executor.shutdownNow();
    }
}
