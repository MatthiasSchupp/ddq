package eu.domaindriven.ddq.error.deployment;

import eu.domaindriven.ddq.error.*;
import eu.domaindriven.ddq.error.*;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.arc.deployment.UnremovableBeanBuildItem;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.hibernate.orm.deployment.AdditionalJpaModelBuildItem;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Type;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class ErrorProcessor {

    private static final Set<DotName> UNREMOVABLE_BEANS = Set.of(
            DotName.createSimple(ErrorPublisher.class.getName()),
            DotName.createSimple(ErrorFactory.class.getName()));

    @BuildStep
    UnremovableBeanBuildItem ensureBeanLookupAvailable() {
        return new UnremovableBeanBuildItem(beanInfo -> beanInfo.getTypes().stream()
                .map(Type::name)
                .anyMatch(UNREMOVABLE_BEANS::contains));
    }

    @BuildStep
    List<AdditionalJpaModelBuildItem> produceModel() {
        return Collections.singletonList(
                new AdditionalJpaModelBuildItem(StoredError.class)
        );
    }

    @BuildStep
    AdditionalBeanBuildItem registerBeans() {
        return AdditionalBeanBuildItem.builder()
                .addBeanClass(ErrorPublisher.class)
                .addBeanClass(StoredErrorRepository.class)
                .addBeanClass(ErrorStore.class)
                .addBeanClass(ErrorFactory.class)
                .build();
    }
}
