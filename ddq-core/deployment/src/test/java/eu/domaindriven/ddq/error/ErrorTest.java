package eu.domaindriven.ddq.error;

import io.quarkus.test.QuarkusUnitTest;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.RegisterExtension;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth8.assertThat;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ErrorTest {

    public static final String BUSINESS_ERROR_MESSAGE = "A business error.";
    public static final String TECHNICAL_ERROR_MESSAGE = "A technical error.";
    public static final String STATIC_ERROR_MESSAGE = "An error published static.";

    @RegisterExtension
    static final QuarkusUnitTest config = new QuarkusUnitTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addAsResource("application.properties")
            );

    @Inject
    ErrorPublisher errorPublisher;

    @Inject
    StoredErrorRepository repository;

    @Test
    @Order(1)
    void environment() {
        assertThat(errorPublisher).isNotNull();
        assertThat(repository).isNotNull();
    }

    @Test
    @Order(2)
    @Transactional
    void testPublishErrors() {
        errorPublisher.business(BUSINESS_ERROR_MESSAGE);

        List<? extends Error> errors = errors();

        assertThat(errors).hasSize(1);
        assertThat(errors).containsExactly(
                new BusinessError(ErrorTest.class.getName(), BUSINESS_ERROR_MESSAGE, null, null, 1, null, null));

        Error error = errors.get(0);

        assertThat(error.source()).isEqualTo(ErrorTest.class.getName());
        assertThat(error.message()).isEqualTo(BUSINESS_ERROR_MESSAGE);
        assertThat(error.exceptionMessage()).isEmpty();
        assertThat(error.stackTrace()).isEmpty();
        assertThat(error.occurrences()).isEqualTo(1);
        assertThat(error.firstOccurrence()).isAtMost(Instant.now());
        assertThat(error.lastOccurrence()).isAtMost(Instant.now());

        errorPublisher.business(BUSINESS_ERROR_MESSAGE);
        errorPublisher.business(BUSINESS_ERROR_MESSAGE);

        errors = errors();
        assertThat(errors).hasSize(1);

        error = errors.get(0);
        assertThat(error.occurrences()).isEqualTo(3);

        errorPublisher.business("A business error 2.", new Exception());
        errorPublisher.business("A business error 3.", new Exception());
        errorPublisher.business("A business error 4.", new Exception());

        errors = errors();
        assertThat(errors).hasSize(4);

        errorPublisher.technical(TECHNICAL_ERROR_MESSAGE, new Exception());
        errorPublisher.technical(TECHNICAL_ERROR_MESSAGE, new Exception());
        errorPublisher.technical(TECHNICAL_ERROR_MESSAGE, new Exception());
        errorPublisher.technical(TECHNICAL_ERROR_MESSAGE, new Exception());
        errorPublisher.technical(TECHNICAL_ERROR_MESSAGE, new Exception());

        errors = errors();
        assertThat(errors).hasSize(5);

        errorPublisher.technical(TECHNICAL_ERROR_MESSAGE, new Exception("Message"));

        errors = errors();
        assertThat(errors).hasSize(6);

        error = errors.get(5);
        assertThat(error.stackTrace()).isPresent();
        assertThat(error.exceptionMessage()).isPresent();

        errorPublisher.business("A business error with params ''{0}''.", "param");

        errors = errors();
        assertThat(errors).hasSize(7);

        error = errors.get(6);
        assertThat(error.message()).isEqualTo("A business error with params 'param'.");
    }

    @Test
    @Order(3)
    @Transactional
    void testStaticPublishErrors() {
        Errors.publisher(ErrorTest.class).technical(STATIC_ERROR_MESSAGE);
        assertThat(errors()).contains(
                new TechnicalError(ErrorTest.class.getName(), STATIC_ERROR_MESSAGE, null, null, 1, null, null));
    }

    private List<Error> errors() {
        return repository.stream()
                .map(StoredError::toProjection)
                .map(Error.class::cast)
                .collect(Collectors.toList());
    }
}
