package eu.domaindriven.ddq.event;

public class EventSourceAuthenticationException extends RuntimeException {
    private static final long serialVersionUID = 3255894971754360257L;

    public EventSourceAuthenticationException() {
    }

    public EventSourceAuthenticationException(String message) {
        super(message);
    }

    public EventSourceAuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }

    public EventSourceAuthenticationException(Throwable cause) {
        super(cause);
    }
}
