package eu.domaindriven.ddq.boundary;

import eu.domaindriven.ddq.domain.model.Greeting;
import eu.domaindriven.ddq.hal.BaseLink;
import eu.domaindriven.ddq.hal.LogRepresentation;

import javax.ws.rs.core.Link;
import java.util.Collection;

public class GreetingLogRepresentation extends LogRepresentation {

    @BaseLink(path = "greetings/salutes")
    private Link salutes;

    public GreetingLogRepresentation(Collection<Greeting> greetings) {
        super(greetings, "greetings", GreetingRepresentation::new);
    }
}
