package eu.domaindriven.ddq.error;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.text.MessageFormat;

import static java.lang.System.Logger.Level.DEBUG;

@Dependent
public class ErrorPublisher {

    private static final System.Logger LOGGER = System.getLogger(ErrorPublisher.class.getName());

    @Inject
    ErrorStore errorStore;

    public void technical(String message, Class<?> source) {
        errorStore.appendTechnical(message, source);
        LOGGER.log(DEBUG, message);
    }

    public void technical(String message, Class<?> source, Object... params) {
        errorStore.appendTechnical(format(message, params), source);
        LOGGER.log(DEBUG, () -> format(message, params));
    }

    public void technical(String message, Class<?> source, Throwable throwable) {
        errorStore.appendTechnical(message, source, throwable);
        LOGGER.log(DEBUG, message, throwable);
    }

    public void technical(String message, Class<?> source, Throwable throwable, Object... params) {
        errorStore.appendTechnical(format(message, params), source, throwable);
        LOGGER.log(DEBUG, () -> format(message, params), throwable);
    }

    public void business(String message, Class<?> source) {
        errorStore.appendBusiness(message, source);
        LOGGER.log(DEBUG, message);
    }

    public void business(String message, Class<?> source, Object... params) {
        errorStore.appendBusiness(format(message, params), source);
        LOGGER.log(DEBUG, () -> format(message, params));
    }

    public void business(String message, Class<?> source, Throwable throwable) {
        errorStore.appendBusiness(message, source, throwable);
        LOGGER.log(DEBUG, message, throwable);
    }

    public void business(String message, Class<?> source, Throwable throwable, Object... params) {
        errorStore.appendBusiness(format(message, params), source, throwable);
        LOGGER.log(DEBUG, () -> format(message, params), throwable);
    }

    private String format(String message, Object... params) {
        return MessageFormat.format(message, params);
    }
}
