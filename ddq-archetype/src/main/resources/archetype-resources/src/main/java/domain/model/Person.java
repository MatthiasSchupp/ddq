package ${package}.domain.model;

import eu.domaindriven.ddq.domain.ValueObject;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class Person extends ValueObject {

    private static final long serialVersionUID = 826689370566319192L;

    @Column(nullable = false, unique = true)
    private String name;

    public Person() {
    }

    public Person(String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }
}
