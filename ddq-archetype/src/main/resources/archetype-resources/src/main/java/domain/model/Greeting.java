package ${package}.domain.model;


import eu.domaindriven.ddq.domain.Entity;

import javax.persistence.Column;
import javax.persistence.Embedded;

@javax.persistence.Entity
public class Greeting extends Entity {

    private static final long serialVersionUID = -4885017542452489441L;

    @Embedded
    private GreetingId greetingId;
    @Embedded
    private Person person;
    @Column(nullable = false)
    private Integer salutes;

    Greeting() {
        this.salutes = 0;
    }

    public Greeting(GreetingId greetingId, Person person) {
        this();
        this.greetingId = greetingId;
        this.person = person;
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

    public void salute() {
        salutes++;
        publishEvent(new Greeted(greetingId.id().toString(), person().name()));
    }
}
