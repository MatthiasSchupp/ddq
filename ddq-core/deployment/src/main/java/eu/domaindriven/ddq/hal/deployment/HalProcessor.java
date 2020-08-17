package eu.domaindriven.ddq.hal.deployment;

import eu.domaindriven.ddq.hal.EntityTagResponseFactory;
import eu.domaindriven.ddq.hal.LogRepresentationSerializer;
import eu.domaindriven.ddq.hal.PageRepresentationSerializer;
import eu.domaindriven.ddq.hal.RepresentationSerializer;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.jsonb.spi.JsonbSerializerBuildItem;

import java.util.Arrays;

public class HalProcessor {

    @BuildStep
    AdditionalBeanBuildItem registerBeans() {
        return AdditionalBeanBuildItem.builder()
                .addBeanClass(EntityTagResponseFactory.class)
                .build();
    }

    @BuildStep
    JsonbSerializerBuildItem registerJsonbSerializers() {
        return new JsonbSerializerBuildItem(Arrays.asList(
                RepresentationSerializer.class.getName(),
                LogRepresentationSerializer.class.getName(),
                PageRepresentationSerializer.class.getName()));
    }
}
