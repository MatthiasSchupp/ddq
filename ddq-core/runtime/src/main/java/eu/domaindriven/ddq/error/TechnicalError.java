package eu.domaindriven.ddq.error;

import java.time.Instant;

public class TechnicalError extends Error {
    public TechnicalError(String source, String message, String exceptionMessage, String stackTrace, Integer occurrences, Instant firstOccurrence, Instant lastOccurrence) {
        super(source, message, exceptionMessage, stackTrace, occurrences, firstOccurrence, lastOccurrence);
    }
}
