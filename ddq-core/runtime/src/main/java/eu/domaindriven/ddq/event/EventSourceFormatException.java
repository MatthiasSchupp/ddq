package eu.domaindriven.ddq.event;

public class EventSourceFormatException extends RuntimeException {
    private static final long serialVersionUID = 3255894971754360257L;

    public EventSourceFormatException() {
    }

    public EventSourceFormatException(String message) {
        super(message);
    }

    public EventSourceFormatException(String message, Throwable cause) {
        super(message, cause);
    }

    public EventSourceFormatException(Throwable cause) {
        super(cause);
    }
}
