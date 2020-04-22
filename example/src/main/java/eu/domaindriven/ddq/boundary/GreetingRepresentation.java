package eu.domaindriven.ddq.boundary;

import eu.domaindriven.ddq.domain.Representation;
import eu.domaindriven.ddq.domain.model.Greeting;
import eu.domaindriven.ddq.domain.model.GreetingId;
import eu.domaindriven.ddq.domain.model.Person;

import javax.ws.rs.core.UriInfo;

public class GreetingRepresentation extends Representation {

    private final GreetingId greetingId;
    private final Person person;
    private final Integer salutes;

    public GreetingRepresentation(Greeting greeting, UriInfo uriInfo) {
        this.greetingId = greeting.greetingId();
        this.person = greeting.person();
        this.salutes = greeting.salutes();
        link("self", uriInfo.getBaseUriBuilder(), "greetings", greeting.greetingId().id().toString());
        link("salute", uriInfo.getBaseUriBuilder(), "greetings", greeting.greetingId().id().toString(), "/salute");
        link("salutes", uriInfo.getBaseUriBuilder(), "greetings", greeting.greetingId().id().toString(), "/salutes");
    }

    public GreetingId greetingId() {
        return greetingId;
    }

    public Person person() {
        return person;
    }

    public Integer salutes() {
        return salutes;
    }
}
