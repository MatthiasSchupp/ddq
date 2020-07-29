package eu.domaindriven.ddq.error;

import java.text.MessageFormat;

import static java.lang.System.Logger.Level.DEBUG;

public class ErrorPublisher {

    private static final System.Logger LOGGER = System.getLogger(ErrorPublisher.class.getName());

    private final ErrorStore errorStore;
    private final Class<?> source;

    public ErrorPublisher(ErrorStore errorStore, Class<?> source) {
        this.errorStore = errorStore;
        this.source = source;
    }

    public void technical(String message) {
        errorStore.appendTechnical(message, source);
        LOGGER.log(DEBUG, message);
    }

    public void technical(String message, Object... params) {
        errorStore.appendTechnical(format(message, params), source);
        LOGGER.log(DEBUG, () -> format(message, params));
    }

    public void technical(String message, Throwable throwable) {
        errorStore.appendTechnical(message, source, throwable);
        LOGGER.log(DEBUG, message, throwable);
    }

    public void technical(String message, Throwable throwable, Object... params) {
        errorStore.appendTechnical(format(message, params), source, throwable);
        LOGGER.log(DEBUG, () -> format(message, params), throwable);
    }

    public void business(String message) {
        errorStore.appendBusiness(message, source);
        LOGGER.log(DEBUG, message);
    }

    public void business(String message, Object... params) {
        errorStore.appendBusiness(format(message, params), source);
        LOGGER.log(DEBUG, () -> format(message, params));
    }

    public void business(String message, Throwable throwable) {
        errorStore.appendBusiness(message, source, throwable);
        LOGGER.log(DEBUG, message, throwable);
    }

    public void business(String message, Throwable throwable, Object... params) {
        errorStore.appendBusiness(format(message, params), source, throwable);
        LOGGER.log(DEBUG, () -> format(message, params), throwable);
    }

    private String format(String message, Object... params) {
        return MessageFormat.format(message, params);
    }
}
