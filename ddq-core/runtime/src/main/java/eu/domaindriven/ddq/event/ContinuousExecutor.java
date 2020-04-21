package eu.domaindriven.ddq.event;

import org.eclipse.microprofile.context.ManagedExecutor;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;

class ContinuousExecutor {

    private final String identifier;
    private final ManagedExecutor managedExecutor;
    private final ScheduledExecutorService scheduledExecutorService;
    private final BooleanSupplier handler;
    private final BiConsumer<? super String, ? super Throwable> errorConsumer;
    private final AtomicBoolean active;
    private final ProcessingWatch processingWatch;

    ContinuousExecutor(String identifier,
                       ManagedExecutor managedExecutor,
                       ScheduledExecutorService scheduledExecutorService,
                       BooleanSupplier handler,
                       BiConsumer<? super String, ? super Throwable> errorConsumer) {
        this.identifier = identifier;
        this.managedExecutor = managedExecutor;
        this.scheduledExecutorService = scheduledExecutorService;
        this.handler = handler;
        this.errorConsumer = errorConsumer;
        this.active = new AtomicBoolean(true);
        this.processingWatch = new ProcessingWatch();
    }

    void startDelayed() {
        scheduledExecutorService.schedule(this::start, 10, TimeUnit.MILLISECONDS);
    }

    void start() {
        activate();
        run();
    }

    void stop() {
        deactivate();
        scheduledExecutorService.shutdown();
    }

    private void activate() {
        active.set(true);
    }

    private void deactivate() {
        active.set(false);
    }

    private boolean active() {
        return active.get();
    }

    private boolean handle() {
        return handler.getAsBoolean();
    }

    private void run() {
        if (active()) {
            managedExecutor.supplyAsync(this::handle)
                    .exceptionally(this::handleError)
                    .thenAccept(this::nextRun);
        }
    }

    @SuppressWarnings("SameReturnValue")
    private boolean handleError(Throwable throwable) {
        errorConsumer.accept(identifier, throwable);

        return false;
    }

    private void nextRun(boolean processed) {
        if (processed) {
            processingWatch.reset();
            run();
        } else {
            processingWatch.increment();
            scheduledExecutorService.schedule(this::run, processingWatch.delay(), TimeUnit.MILLISECONDS);
        }
    }

    private static final class ProcessingWatch {
        private final LongAdder count;

        private ProcessingWatch() {
            count = new LongAdder();
        }

        private void increment() {
            count.increment();
        }

        private void reset() {
            count.reset();
        }

        private int delay() {
            return (int) Math.floor(Math.log(count.doubleValue()) * 1000);
        }
    }
}
