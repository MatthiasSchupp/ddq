package eu.domaindriven.ddq.deployment;

import eu.domaindriven.ddq.JsonbConfigurator;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;

public class DdqProcessor {

    private static final String FEATURE = "ddq-core";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    AdditionalBeanBuildItem registerBeans() {
        return AdditionalBeanBuildItem.builder()
                .addBeanClass(JsonbConfigurator.class)
                .build();
    }
}
