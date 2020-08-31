package eu.domaindriven.ddq.event;

import javax.enterprise.context.ApplicationScoped;
import java.util.Base64;

@ApplicationScoped
public class BasicAuthAuthorizationProvider implements AuthorizationProvider {

    private static final String  HEADER = "Basic " + Base64.getEncoder().encodeToString("duke:dukePassword".getBytes());

    @Override
    public String header(String eventSource) {
        return HEADER;
    }
}
