package eu.domaindriven.ddq.oidc;

import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;

public class OidcProcessor {

    private static final String FEATURE = "ddq-oidc";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }
}
