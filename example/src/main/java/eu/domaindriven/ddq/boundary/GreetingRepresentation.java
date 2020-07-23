package eu.domaindriven.ddq.boundary;

import eu.domaindriven.ddq.domain.Representation;
import eu.domaindriven.ddq.domain.model.Greeting;
import eu.domaindriven.ddq.domain.model.GreetingId;
import eu.domaindriven.ddq.domain.model.Person;

import javax.ws.rs.core.UriInfo;
import java.util.Objects;

public class GreetingRepresentation extends Representation {

    private final GreetingId greetingId;
    private final Person person;
    private final Integer salutes;

    public GreetingRepresentation(Greeting greeting, UriInfo uriInfo) {
        this.greetingId = greeting.greetingId();
        this.person = greeting.person();
        this.salutes = greeting.salutes();
        link("self", uriInfo.getBaseUriBuilder(), "greetings", greeting.greetingId().toString());
        link("salute", uriInfo.getBaseUriBuilder(), "greetings", greeting.greetingId().toString(), "/salute");
        link("salutes", uriInfo.getBaseUriBuilder(), "greetings", greeting.greetingId().toString(), "/salutes");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GreetingRepresentation that = (GreetingRepresentation) o;
        return greetingId.equals(that.greetingId) &&
                person.equals(that.person) &&
                salutes.equals(that.salutes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(greetingId, person, salutes);
    }
}
