package eu.domaindriven.ddq.error;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.InjectionPoint;

public class ErrorPublisherProducer {

    @Produces
    ErrorPublisher produce(ErrorStore errorStore, InjectionPoint ip) {
        return new ErrorPublisher(errorStore, extractErrorSource(ip));
    }

    private static Class<?> extractErrorSource(InjectionPoint ip) {
        Bean<?> bean = ip.getBean();
        return bean != null ? bean.getBeanClass() : Void.class;
    }
}
