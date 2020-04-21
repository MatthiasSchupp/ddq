package eu.domaindriven.ddq.boundary;

import eu.domaindriven.ddq.domain.Representation;
import eu.domaindriven.ddq.domain.model.Greeting;

import javax.ws.rs.core.UriInfo;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class GreetingLogRepresentation extends Representation {

    private final List<GreetingRepresentation> greetings;

    public GreetingLogRepresentation(Collection<Greeting> greetings, UriInfo uriInfo, String path) {
        this.greetings = greetings.stream()
                .map(greeting -> new GreetingRepresentation(greeting, uriInfo, path))
                .collect(Collectors.toList());
        link("salutes", uriInfo.getBaseUriBuilder(), path, "/salutes");
    }

    public List<GreetingRepresentation> greetings() {
        return Collections.unmodifiableList(greetings);
    }
}
