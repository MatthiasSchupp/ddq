package eu.domaindriven.ddq.error;

import io.quarkus.arc.Arc;

@SuppressWarnings("UtilityClass,unused")
public final class Errors {

    private Errors() {
    }

    public static void technical(String message, Class<?> source) {
        publisher(source).technical(message, source);
    }

    public static void technical(String message, Class<?> source, Object... params) {
        publisher(source).technical(message, source, params);
    }

    public static void technical(String message, Class<?> source, Throwable throwable) {
        publisher(source).technical(message, source, throwable);
    }

    public static void technical(String message, Class<?> source, Throwable throwable, Object... params) {
        publisher(source).technical(message, source, throwable, params);
    }

    public static void business(String message, Class<?> source) {
        publisher(source).business(message, source);
    }

    public static void business(String message, Class<?> source, Object... params) {
        publisher(source).business(message, source, params);
    }

    public static void business(String message, Class<?> source, Throwable throwable) {
        publisher(source).business(message, source, throwable);
    }

    public static void business(String message, Class<?> source, Throwable throwable, Object... params) {
        publisher(source).business(message, source, throwable, params);
    }

    public static ErrorPublisher publisher(Class<?> source) {
        ErrorStore errorStore = Arc.container().instance(ErrorStore.class).get();
        return new ErrorPublisher(errorStore, source);
    }
}
