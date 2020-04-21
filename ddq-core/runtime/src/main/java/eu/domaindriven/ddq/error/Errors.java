package eu.domaindriven.ddq.error;

import io.quarkus.arc.Arc;

@SuppressWarnings("UtilityClass,unused")
public final class Errors {

    private Errors() {
    }

    public static void technical(String message, Class<?> source) {
        publisher().technical(message, source);
    }

    public static void technical(String message, Class<?> source, Object... params) {
        publisher().technical(message, source, params);
    }

    public static void technical(String message, Class<?> source, Throwable throwable) {
        publisher().technical(message, source, throwable);
    }

    public static void technical(String message, Class<?> source, Throwable throwable, Object... params) {
        publisher().technical(message, source, throwable, params);
    }

    public static void business(String message, Class<?> source) {
        publisher().business(message, source);
    }

    public static void business(String message, Class<?> source, Object... params) {
        publisher().business(message, source, params);
    }

    public static void business(String message, Class<?> source, Throwable throwable) {
        publisher().business(message, source, throwable);
    }

    public static void business(String message, Class<?> source, Throwable throwable, Object... params) {
        publisher().business(message, source, throwable, params);
    }

    public static ErrorPublisher publisher() {
        return Arc.container().instance(ErrorPublisher.class).get();
    }
}
