package eu.domaindriven.ddq.boundary;

import eu.domaindriven.ddq.domain.LogRepresentation;
import eu.domaindriven.ddq.domain.model.Greeting;

import javax.ws.rs.core.UriInfo;
import java.util.Collection;

public class GreetingLogRepresentation extends LogRepresentation<Greeting, GreetingRepresentation> {

    public GreetingLogRepresentation(Collection<Greeting> greetings, UriInfo uriInfo) {
        super(greetings, uriInfo, "greetings", GreetingRepresentation::new);
        link("salutes", uriInfo.getBaseUriBuilder(), "greetings", "/salutes");
    }
}
