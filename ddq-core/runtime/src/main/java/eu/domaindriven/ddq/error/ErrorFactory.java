package eu.domaindriven.ddq.error;

import javax.enterprise.context.Dependent;
import java.time.Instant;

@Dependent
public class ErrorFactory {

    public Error create(StoredError storedError) {
        String source = storedError.source().getName();
        String message = storedError.message();
        String exceptionMessage = storedError.exceptionMessage();
        String stackTrace = storedError.stackTrace();
        Integer occurrences = storedError.occurrences();
        Instant firstOccurrence = storedError.firstOccurrence();
        Instant lastOccurrence = storedError.lastOccurrence();

        return storedError.type() == ErrorType.TECHNICAL
                ? new TechnicalError(source, message, exceptionMessage, stackTrace, occurrences, firstOccurrence, lastOccurrence)
                : new BusinessError(source, message, exceptionMessage, stackTrace, occurrences, firstOccurrence, lastOccurrence);
    }
}
