package eu.domaindriven.ddq.config;

import io.quarkus.arc.runtime.BeanContainer;
import io.quarkus.runtime.annotations.Recorder;

@Recorder
public class ConfigurationRecorder {

    public void configureBeans(BeanContainer beanContainer, Class<? extends Configurable> beanClass, DdqConfig ddqConfig) {
        Configurable instance = beanContainer.instance(beanClass);
        instance.configure(ddqConfig);
    }
}
