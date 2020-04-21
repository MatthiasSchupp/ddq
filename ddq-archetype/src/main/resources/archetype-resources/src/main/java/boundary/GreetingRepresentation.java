package ${package}.boundary;

import ${package}.domain.model.Greeting;
import ${package}.domain.model.Person;
import eu.domaindriven.ddq.domain.Representation;

import javax.ws.rs.core.UriInfo;
import java.util.UUID;

public class GreetingRepresentation extends Representation {

    private final UUID greetingId;
    private final Person person;
    private final Integer salutes;

    public GreetingRepresentation(Greeting greeting, UriInfo uriInfo, String path) {
        this.greetingId = greeting.greetingId().id();
        this.person = greeting.person();
        this.salutes = greeting.salutes();
        link("self", uriInfo.getBaseUriBuilder(), path, greeting.greetingId().id().toString());
        link("salute", uriInfo.getBaseUriBuilder(), path, greeting.greetingId().id().toString(), "/salute");
        link("salutes", uriInfo.getBaseUriBuilder(), path, greeting.greetingId().id().toString(), "/salutes");
    }

    public UUID greetingId() {
        return greetingId;
    }

    public Person person() {
        return person;
    }

    public Integer salutes() {
        return salutes;
    }
}
