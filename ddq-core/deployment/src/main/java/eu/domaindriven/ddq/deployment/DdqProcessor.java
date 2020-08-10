package eu.domaindriven.ddq.deployment;

import eu.domaindriven.ddq.JsonbConfigurator;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.GeneratedResourceBuildItem;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Objects;

public class DdqProcessor {

    private static final String FEATURE = "ddq-core";

    private static final String MIGRATION_SCRIPT = "db/migration/R__ddq_core.sql";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    AdditionalBeanBuildItem registerBeans() {
        return AdditionalBeanBuildItem.builder()
                .addBeanClass(JsonbConfigurator.class)
                .build();
    }

    @BuildStep
    GeneratedResourceBuildItem produceMigrationScript() {
        try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(MIGRATION_SCRIPT)) {
            return new GeneratedResourceBuildItem(MIGRATION_SCRIPT, Objects.requireNonNull(is).readAllBytes());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
