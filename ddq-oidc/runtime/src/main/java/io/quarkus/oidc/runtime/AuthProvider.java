package io.quarkus.oidc.runtime;

import io.quarkus.arc.Arc;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.subscription.UniEmitter;
import io.vertx.core.Vertx;
import io.vertx.ext.auth.oauth2.OAuth2Auth;
import io.vertx.ext.auth.oauth2.OAuth2ClientOptions;
import io.vertx.ext.auth.oauth2.OAuth2FlowType;
import io.vertx.ext.auth.oauth2.impl.OAuth2AuthProviderImpl;
import io.vertx.ext.auth.oauth2.providers.KeycloakAuth;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import java.util.function.Consumer;

public class AuthProvider {

    @Produces
    @ApplicationScoped
    OAuth2Auth produce(TenantConfigBean tenantConfigBean) {
        TenantConfigContext tenant = tenantConfigBean.getDefaultTenant();
        OAuth2AuthProviderImpl auth = (OAuth2AuthProviderImpl) tenant.auth;
        OAuth2ClientOptions clientOptions = new OAuth2ClientOptions(auth.getConfig());
        clientOptions.setFlow(OAuth2FlowType.CLIENT);

        return discoverOidcEndpoints(vertx(), clientOptions);
    }

    private static OAuth2Auth discoverOidcEndpoints(Vertx vertx, OAuth2ClientOptions options) {
        return Uni.createFrom().emitter((Consumer<UniEmitter<? super OAuth2Auth>>) uniEmitter ->
                KeycloakAuth.discover(vertx, options, event -> {
                    if (event.failed()) {
                        uniEmitter.fail(event.cause());
                    } else {
                        uniEmitter.complete(event.result());
                    }
                })).await().indefinitely();
    }

    private static Vertx vertx() {
        return Arc.container().instance(Vertx.class).get();
    }
}
