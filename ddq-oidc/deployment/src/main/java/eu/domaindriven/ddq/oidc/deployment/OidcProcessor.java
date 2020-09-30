package eu.domaindriven.ddq.oidc.deployment;

import eu.domaindriven.ddq.oidc.OidcClientRequestFilter;
import eu.domaindriven.ddq.oidc.TokenManager;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.arc.deployment.UnremovableBeanBuildItem;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.oidc.runtime.AuthProvider;
import io.quarkus.oidc.runtime.OidcBuildTimeConfig;
import io.quarkus.resteasy.common.spi.ResteasyJaxrsProviderBuildItem;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Type;

import java.util.Set;
import java.util.function.BooleanSupplier;

public class OidcProcessor {

    private static final String FEATURE = "ddq-oidc";

    private static final Set<DotName> UNREMOVABLE_BEANS = Set.of(
            DotName.createSimple(TokenManager.class.getName()));

    @BuildStep(onlyIf = IsEnabled.class)
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    UnremovableBeanBuildItem ensureBeanLookupAvailable() {
        return new UnremovableBeanBuildItem(beanInfo -> beanInfo.getTypes().stream()
                .map(Type::name)
                .anyMatch(UNREMOVABLE_BEANS::contains));
    }

    @BuildStep(onlyIf = IsEnabled.class)
    AdditionalBeanBuildItem registerBeans() {
        return AdditionalBeanBuildItem.builder()
                .addBeanClass(TokenManager.class)
                .addBeanClass(AuthProvider.class)
                .build();
    }

    @BuildStep(onlyIf = IsEnabled.class)
    ResteasyJaxrsProviderBuildItem registerClientRequestFilter() {
        return new ResteasyJaxrsProviderBuildItem(OidcClientRequestFilter.class.getName());
    }

    static class IsEnabled implements BooleanSupplier {
        OidcBuildTimeConfig config;

        public boolean getAsBoolean() {
            return config.enabled;
        }
    }
}
