package eu.domaindriven.ddq.notitifaction.deployment;

import eu.domaindriven.ddq.notification.Notifiable;
import eu.domaindriven.ddq.notification.NotificationResource;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.hibernate.orm.deployment.AdditionalJpaModelBuildItem;

import java.util.Collections;
import java.util.List;

public class NotificationProcessor {

    @BuildStep
    List<AdditionalJpaModelBuildItem> produceModel() {
        return Collections.singletonList(
                new AdditionalJpaModelBuildItem(Notifiable.class)
        );
    }

    @BuildStep
    AdditionalBeanBuildItem registerBeans() {
        return AdditionalBeanBuildItem.builder()
                .addBeanClass(NotificationResource.class)
                .build();
    }
}
