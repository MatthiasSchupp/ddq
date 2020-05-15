package eu.domaindriven.ddq.domain.deployment;

import eu.domaindriven.ddq.domain.*;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.hibernate.orm.deployment.AdditionalJpaModelBuildItem;

import java.util.Arrays;
import java.util.List;

public class DomainProcessor {

    @BuildStep
    List<AdditionalJpaModelBuildItem> produceModel() {
        return Arrays.asList(
                new AdditionalJpaModelBuildItem(Entity.class),
                new AdditionalJpaModelBuildItem(IdentifiedDomainObject.class),
                new AdditionalJpaModelBuildItem(IdentifiedValueObject.class),
                new AdditionalJpaModelBuildItem(ValueObject.class),
                new AdditionalJpaModelBuildItem(UUIDValueObject.class)
        );
    }

    @BuildStep
    AdditionalBeanBuildItem registerBeans() {
        return AdditionalBeanBuildItem.builder()
                .addBeanClass(EntityTagResponseFactory.class)
                .build();
    }
}
