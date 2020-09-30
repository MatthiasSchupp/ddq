package eu.domaindriven.ddq.oidc;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import io.smallrye.jwt.algorithm.SignatureAlgorithm;
import io.smallrye.jwt.build.Jwt;

import javax.json.Json;
import javax.ws.rs.core.MediaType;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class MockAuthorizationServerTestResource implements QuarkusTestResourceLifecycleManager {

    private static final String CLIENT_ID = "oidc-test";
    private static final String CLIENT_SECRET = "e31d46c4-efd7-4d1e-ac24-722d6305b70a";
    private final WireMockServer wireMockServer;

    public MockAuthorizationServerTestResource() {
        wireMockServer = new WireMockServer(8086);
        configureFor(8086);
    }

    @Override
    public Map<String, String> start() {
        wireMockServer.start();
        String issuer = wireMockServer.url("auth");
        String token = createToken(issuer);

        stubFor(get("/auth/.well-known/openid-configuration")
                .willReturn(aResponse()
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON)
                        .withBody(createOpenIdEndpointConfiguration(issuer))));
        stubFor(get("/auth/protocol/openid-connect/certs")
                .willReturn(aResponse()
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON)
                        .withBody(createCertsResponse())));
        stubFor(post("/auth/protocol/openid-connect/token")
                .withBasicAuth(CLIENT_ID, CLIENT_SECRET)
                .withRequestBody(containing("scope=localhost&grant_type=client_credentials"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON)
                        .withBody(createTokenResponse(token))));

        // Stub for the test REST call
        stubFor(get("/hello")
                .withHeader("Authorization", equalTo("Bearer " + token))
                .willReturn(aResponse()
                        .withHeader("Content-Type", MediaType.TEXT_PLAIN)
                        .withBody("hello")));

        return Map.of(
                "quarkus.oidc.auth-server-url", issuer,
                "quarkus.oidc.client-id", CLIENT_ID,
                "quarkus.oidc.credentials.secret", CLIENT_SECRET
        );
    }

    @Override
    public void stop() {
        wireMockServer.stop();
    }

    private static String createTokenResponse(String token) {
        return Json.createObjectBuilder()
                .add("access_token", token)
                .build().toString();
    }

    private static String createToken(String issuer) {
        return Jwt.issuer(issuer)
                .upn("zoey@domain-driven.eu")
                .expiresAt(Instant.now().plus(Duration.ofMinutes(1)))
                .audience(CLIENT_ID)
                .jws().keyId("OE-A81zPzStw62ye2WyocvFU6rxjfGHSdVJn-z1NnTE").algorithm(SignatureAlgorithm.RS256).sign("privateKey.pem");
    }

    private static String createOpenIdEndpointConfiguration(String issuer) {
        return Json.createObjectBuilder()
                .add("issuer", issuer)
                .add("authorization_endpoint", issuer + "/protocol/openid-connect/auth")
                .add("token_endpoint", issuer + "/protocol/openid-connect/token")
                .add("token_introspection_endpoint", issuer + "/protocol/openid-connect/token/introspect")
                .add("jwks_uri", issuer + "/protocol/openid-connect/certs")
                .build().toString();
    }

    private static String createCertsResponse() {
        return Json.createObjectBuilder()
                .add("keys", Json.createArrayBuilder()
                        .add(Json.createObjectBuilder()
                                .add("kid", "OE-A81zPzStw62ye2WyocvFU6rxjfGHSdVJn-z1NnTE")
                                .add("kty", "RSA")
                                .add("alg", "RS256")
                                .add("use", "sig")
                                .add("e", "AQAB")
                                .add("n", "nbhf-DhRDFuNXCdLC5fK1PLTwiLITGxH2HP5oyMsq2SZ5u47GM6axL45mb_2rzlJLuh0-kcTyFytdAyEydPEi-v7R9cB4qhzky_G9LQ8-x8hhLsz4LEO3lW8337ThK-syT_X8g1JsDtWH9vWEQY3hMeTL-tPzsUSvFWVlDlS03YRI98kpB9a86gm2f9CW8OZyH8gzd1da8Yy1ymZiIDGWihvpzOEzParHERGTkOV7fi9CXwUQVxH2DJZ5GAjvu_fDxWTQNex5X_M-mk4-_2WQq2vnVVgCioPton4z97SMYqaTUiavceeGRn0RHc6mCp76icqaFd7n0ZqEPNeEgXCxw")))
                .build().toString();
    }
}
