package eu.domaindriven.ddq.event;

import io.quarkus.arc.runtime.BeanContainer;
import io.quarkus.runtime.annotations.Recorder;

@Recorder
public class DomainEventDiscoveryRecorder {

    public void addEventType(BeanContainer beanContainer, String eventType) {
        EventRegistry registry = beanContainer.instance(EventRegistry.class);
        registry.add(createClass(eventType));
    }

    @SuppressWarnings("unchecked")
    private static Class<? extends DomainEvent> createClass(String name) {
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            return (Class<? extends DomainEvent>) classLoader.loadClass(name);
        } catch (ClassNotFoundException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
