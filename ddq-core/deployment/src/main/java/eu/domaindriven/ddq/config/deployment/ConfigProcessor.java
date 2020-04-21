package eu.domaindriven.ddq.config.deployment;

import eu.domaindriven.ddq.config.Configurable;
import eu.domaindriven.ddq.config.ConfigurationRecorder;
import eu.domaindriven.ddq.config.DdqConfig;
import io.quarkus.arc.deployment.BeanArchiveIndexBuildItem;
import io.quarkus.arc.deployment.BeanContainerBuildItem;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.Record;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;

import static io.quarkus.deployment.annotations.ExecutionTime.RUNTIME_INIT;

public class ConfigProcessor {

    @BuildStep
    @Record(RUNTIME_INIT)
    void configureBeans(ConfigurationRecorder recorder, BeanArchiveIndexBuildItem beanArchiveIndex, BeanContainerBuildItem beanContainer, DdqConfig config) {
        beanArchiveIndex.getIndex().getAllKnownImplementors(DotName.createSimple(Configurable.class.getName())).stream()
                .map(ClassInfo::name)
                .map(ConfigProcessor::createClass)
                .forEach(beanClass -> recorder.configureBeans(beanContainer.getValue(), beanClass, config));
    }

    @SuppressWarnings("unchecked")
    private static Class<? extends Configurable> createClass(DotName name) {
        try {
            return (Class<? extends Configurable>) Class.forName(name.toString());
        } catch (ClassNotFoundException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
