package eu.domaindriven.ddq.error;

import eu.domaindriven.ddq.notification.NotificationProvider;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.util.Optional;
import java.util.stream.Stream;

@Dependent
@Transactional(Transactional.TxType.MANDATORY)
public class ErrorStore implements NotificationProvider<StoredError> {

    @Inject
    StoredErrorRepository repository;

    public void appendTechnical(String message, Class<?> source) {
        appendTechnical(message, source, null);
    }

    public void appendTechnical(String message, Class<?> source, Throwable throwable) {
        append(message, source, throwable, ErrorType.TECHNICAL);
    }

    public void appendBusiness(String message, Class<?> source) {
        appendBusiness(message, source, null);
    }

    public void appendBusiness(String message, Class<?> source, Throwable throwable) {
        append(message, source, throwable, ErrorType.BUSINESS);
    }

    private void append(String message, Class<?> source, Throwable throwable, ErrorType type) {
        String exceptionMessage = throwable != null
                ? throwable.getMessage()
                : null;
        Optional<StoredError> storedError = repository.byHash(message, exceptionMessage, source, type)
                .map(StoredError::incrementOccurrences);

        if (storedError.isEmpty()) {
            repository.persist(new StoredError(type, source, message, exceptionMessage, printStackTrace(throwable).orElse(null)));
        }
    }

    private static Optional<String> printStackTrace(Throwable throwable) {
        if (throwable != null) {
            try (StringWriter sw = new StringWriter(); PrintWriter pw = new PrintWriter(sw)) {
                throwable.printStackTrace(pw);
                String stackTrace = sw.toString();

                stackTrace = stackTrace.length() > 2000
                        ? stackTrace.substring(0, 2000)
                        : stackTrace;

                return Optional.of(stackTrace);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Stream<StoredError> between(long lowId, long highId) {
        return repository.between(lowId, highId);
    }

    @Override
    public Optional<Long> maxId() {
        return repository.maxId();
    }

    @Override
    public Optional<Long> minId() {
        return repository.minId();
    }
}
