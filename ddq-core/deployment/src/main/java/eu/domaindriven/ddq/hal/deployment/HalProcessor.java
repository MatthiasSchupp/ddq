package eu.domaindriven.ddq.hal.deployment;

import eu.domaindriven.ddq.hal.EntityTagResponseFactory;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.deployment.annotations.BuildStep;

public class HalProcessor {

    @BuildStep
    AdditionalBeanBuildItem registerBeans() {
        return AdditionalBeanBuildItem.builder()
                .addBeanClass(EntityTagResponseFactory.class)
                .build();
    }
}
