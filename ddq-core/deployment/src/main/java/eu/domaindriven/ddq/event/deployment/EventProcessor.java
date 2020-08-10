package eu.domaindriven.ddq.event.deployment;

import eu.domaindriven.ddq.event.*;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.arc.deployment.BeanArchiveIndexBuildItem;
import io.quarkus.arc.deployment.BeanContainerBuildItem;
import io.quarkus.arc.deployment.UnremovableBeanBuildItem;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.hibernate.orm.deployment.AdditionalJpaModelBuildItem;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Type;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static io.quarkus.deployment.annotations.ExecutionTime.RUNTIME_INIT;

class EventProcessor {

    private static final Set<DotName> UNREMOVABLE_BEANS = Collections.singleton(
            DotName.createSimple(EventPublisher.class.getName()));

    @BuildStep
    UnremovableBeanBuildItem ensureBeanLookupAvailable() {
        return new UnremovableBeanBuildItem(beanInfo -> beanInfo.getTypes().stream()
                .map(Type::name)
                .anyMatch(UNREMOVABLE_BEANS::contains));
    }

    @BuildStep
    List<AdditionalJpaModelBuildItem> produceModel() {
        return Arrays.asList(
                new AdditionalJpaModelBuildItem(StoredEvent.class),
                new AdditionalJpaModelBuildItem(EventSourceTracker.class)
        );
    }

    @BuildStep
    AdditionalBeanBuildItem registerBeans() {
        return AdditionalBeanBuildItem.builder()
                .addBeanClass(EventDispatcher.class)
                .addBeanClass(EventPublisher.class)
                .addBeanClass(EventSerializer.class)
                .addBeanClass(EventStore.class)
                .addBeanClass(StoredEventRepository.class)
                .addBeanClass(EventRegistry.class)
                .addBeanClass(EventReader.class)
                .addBeanClass(EventSourceCollector.class)
                .addBeanClass(EventSourceCollector.class)
                .addBeanClass(EventSourceClient.class)
                .addBeanClass(EventSourceTrackerRepository.class)
                .addBeanClass(ExecutorProducer.class)
                .build();
    }

    @BuildStep
    @Record(RUNTIME_INIT)
    void discoverDomainEvents(DomainEventDiscoveryRecorder recorder, BeanArchiveIndexBuildItem beanArchiveIndex, BeanContainerBuildItem beanContainer) {
        beanArchiveIndex.getIndex().getAllKnownSubclasses(DotName.createSimple(DomainEvent.class.getName())).stream()
                .map(ClassInfo::name)
                .map(DotName::toString)
                .forEach(eventType -> recorder.addEventType(beanContainer.getValue(), eventType));
    }
}
