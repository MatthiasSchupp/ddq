package eu.domaindriven.ddq.error;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;

public class Error {

    private final String source;

    private final String message;

    private final String exceptionMessage;

    private final String stackTrace;

    private final Integer occurrences;

    private final Instant firstOccurrence;

    private final Instant lastOccurrence;

    public Error(String source, String message, String exceptionMessage, String stackTrace, Integer occurrences, Instant firstOccurrence, Instant lastOccurrence) {
        this.source = source;
        this.message = message;
        this.exceptionMessage = exceptionMessage;
        this.stackTrace = stackTrace;
        this.occurrences = occurrences;
        this.firstOccurrence = firstOccurrence;
        this.lastOccurrence = lastOccurrence;
    }

    public String source() {
        return source;
    }

    public String message() {
        return message;
    }

    public Optional<String> exceptionMessage() {
        return Optional.ofNullable(exceptionMessage);
    }

    public Optional<String> stackTrace() {
        return Optional.ofNullable(stackTrace);
    }

    public Integer occurrences() {
        return occurrences;
    }

    public Instant firstOccurrence() {
        return firstOccurrence;
    }

    public Instant lastOccurrence() {
        return lastOccurrence;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Error error = (Error) o;
        return source.equals(error.source) &&
                message.equals(error.message) &&
                Objects.equals(exceptionMessage, error.exceptionMessage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, message, exceptionMessage);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", this.getClass().getSimpleName() + "[", "]")
                .add("source='" + source + "'")
                .add("message='" + message + "'")
                .add("exceptionMessage='" + exceptionMessage + "'")
                .add("stackTrace='" + stackTrace + "'")
                .add("occurrences=" + occurrences)
                .add("firstOccurrence=" + firstOccurrence)
                .add("lastOccurrence=" + lastOccurrence)
                .toString();
    }
}
