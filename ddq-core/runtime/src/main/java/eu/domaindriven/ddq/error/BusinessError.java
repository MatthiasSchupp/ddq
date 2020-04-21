package eu.domaindriven.ddq.error;

import java.time.Instant;

public class BusinessError extends Error {
    public BusinessError(String source, String message, String exceptionMessage, String stackTrace, Integer occurrences, Instant firstOccurrence, Instant lastOccurrence) {
        super(source, message, exceptionMessage, stackTrace, occurrences, firstOccurrence, lastOccurrence);
    }
}
