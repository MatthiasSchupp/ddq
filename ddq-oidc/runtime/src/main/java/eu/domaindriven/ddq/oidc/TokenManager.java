package eu.domaindriven.ddq.oidc;

import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.subscription.UniEmitter;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.oauth2.AccessToken;
import io.vertx.ext.auth.oauth2.OAuth2Auth;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@ApplicationScoped
public class TokenManager {

    private static final int THRESHOLD = 10;

    @Inject
    OAuth2Auth auth;

    private final ConcurrentMap<String, AccessToken> tokens;
    private final ScheduledExecutorService executorService;

    TokenManager() {
        tokens = new ConcurrentHashMap<>();
        executorService = Executors.newSingleThreadScheduledExecutor();
    }

    void init() {
        executorService.scheduleAtFixedRate(this::refreshTokens, 60, 1, TimeUnit.SECONDS);
    }

    Optional<String> token(String scope) {
        AccessToken token;
        if (tokens.containsKey(scope)) {
            token = tokens.get(scope);
        } else {
            token = requestToken(scope);
            if (token.accessToken() != null) {
                tokens.put(scope, token);
            } else {
                token = null;
            }
        }
        return token != null
                ? Optional.ofNullable(token.opaqueAccessToken())
                : Optional.empty();
    }

    private AccessToken requestToken(String scope) {
        return Uni.createFrom().emitter((Consumer<UniEmitter<? super AccessToken>>) uniEmitter -> {
            JsonObject tokenConfig = new JsonObject()
                    .put("scope", scope);
            auth.authenticate(tokenConfig, result -> {
                if (result.failed()) {
                    uniEmitter.fail(result.cause());
                } else {
                    uniEmitter.complete((AccessToken) result.result());
                }
            });
        }).await().indefinitely();
    }

    private void refreshTokens() {
        List<Map.Entry<String, AccessToken>> expiredTokens = tokens.entrySet().stream()
                .filter(entry -> entry.getValue().expired() || expires(entry.getValue()))
                .collect(Collectors.toList());

        expiredTokens.forEach(entry -> entry.getValue().refresh(result -> {
            if (result.failed()) {
                tokens.remove(entry.getKey());
            }
        }));
    }

    private boolean expires(AccessToken token) {
        JsonObject jwt = token.accessToken();
        final long now = (System.currentTimeMillis() / 1000);

        return jwt != null && jwt.containsKey("exp") && now + THRESHOLD >= jwt.getLong("exp");
    }
}
