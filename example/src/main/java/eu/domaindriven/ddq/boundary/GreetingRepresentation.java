package eu.domaindriven.ddq.boundary;

import eu.domaindriven.ddq.domain.model.Greeting;
import eu.domaindriven.ddq.domain.model.GreetingId;
import eu.domaindriven.ddq.domain.model.Person;
import eu.domaindriven.ddq.hal.BaseLink;
import eu.domaindriven.ddq.hal.Representation;

import javax.ws.rs.core.Link;
import java.util.Objects;

public class GreetingRepresentation implements Representation {

    private final GreetingId greetingId;
    private final Person person;
    private final Integer salutes;

    @BaseLink(rel = "self", path = "greetings/{greetingId}")
    private Link selfLink;

    @BaseLink(rel = "salute", path = "greetings/{greetingId}/salute")
    private Link saluteLink;

    @BaseLink(rel = "salutes", path = "greetings/{greetingId}/salutes")
    private Link salutesLink;

    public GreetingRepresentation(Greeting greeting) {
        this.greetingId = greeting.greetingId();
        this.person = greeting.person();
        this.salutes = greeting.salutes();
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
