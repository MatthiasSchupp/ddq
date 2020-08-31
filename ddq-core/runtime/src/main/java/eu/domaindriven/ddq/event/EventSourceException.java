package eu.domaindriven.ddq.event;

public class EventSourceException extends RuntimeException {
    private static final long serialVersionUID = 3255894971754360257L;

    public EventSourceException() {
    }

    public EventSourceException(String message) {
        super(message);
    }

    public EventSourceException(String message, Throwable cause) {
        super(message, cause);
    }

    public EventSourceException(Throwable cause) {
        super(cause);
    }
}
