package eu.domaindriven.ddq.oidc;

import io.quarkus.arc.Arc;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.ext.Provider;

@Provider
public class OidcClientRequestFilter implements ClientRequestFilter {

    @Override
    public void filter(ClientRequestContext requestContext) {
        String host = requestContext.getUri().getHost();
        if (host != null) {
            tokenManager().token(host).ifPresent(token -> requestContext.getHeaders().add("Authorization", "Bearer " + token));
        } else {
            throw new IllegalArgumentException("The uri contains no host");
        }
    }

    private TokenManager tokenManager() {
        return Arc.container().instance(TokenManager.class).get();
    }
}
