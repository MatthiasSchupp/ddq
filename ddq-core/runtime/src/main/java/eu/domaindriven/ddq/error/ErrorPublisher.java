package eu.domaindriven.ddq.error;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.text.MessageFormat;

@Dependent
public class ErrorPublisher {

    @Inject
    ErrorStore errorStore;

    public void technical(String message, Class<?> source) {
        errorStore.appendTechnical(message, source);
    }

    public void technical(String message, Class<?> source, Object... params) {
        errorStore.appendTechnical(format(message, params), source);
    }

    public void technical(String message, Class<?> source, Throwable throwable) {
        errorStore.appendTechnical(message, source, throwable);
    }

    public void technical(String message, Class<?> source, Throwable throwable, Object... params) {
        errorStore.appendTechnical(format(message, params), source, throwable);
    }

    public void business(String message, Class<?> source) {
        errorStore.appendBusiness(message, source);
    }

    public void business(String message, Class<?> source, Object... params) {
        errorStore.appendBusiness(format(message, params), source);
    }

    public void business(String message, Class<?> source, Throwable throwable) {
        errorStore.appendBusiness(message, source, throwable);
    }

    public void business(String message, Class<?> source, Throwable throwable, Object... params) {
        errorStore.appendBusiness(format(message, params), source, throwable);
    }

    private String format(String message, Object... params) {
        return MessageFormat.format(message, params);
    }
}
