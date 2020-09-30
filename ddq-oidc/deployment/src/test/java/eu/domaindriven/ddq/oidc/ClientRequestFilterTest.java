package eu.domaindriven.ddq.oidc;

import io.quarkus.test.QuarkusUnitTest;
import io.quarkus.test.common.QuarkusTestResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static com.google.common.truth.Truth.assertThat;

@QuarkusTestResource(MockAuthorizationServerTestResource.class)
class ClientRequestFilterTest {

    @RegisterExtension
    static final QuarkusUnitTest config = new QuarkusUnitTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addAsResource("application.properties")
                    .addAsResource("privateKey.pem")
            );

    @Test
    void testSendRestRequest() {
        Client client = ClientBuilder.newClient();
        Response response = client.target("http://localhost:8086/hello").request(MediaType.TEXT_PLAIN).get();
        String body = response.readEntity(String.class);
        assertThat(body).isEqualTo("hello");
    }
}
