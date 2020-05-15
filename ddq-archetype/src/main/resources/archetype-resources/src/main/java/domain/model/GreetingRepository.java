package ${package}.domain.model;

import eu.domaindriven.ddq.panache.Repository;

import java.util.Optional;

public interface GreetingRepository extends Repository<Greeting, GreetingId> {

    Optional<Greeting> byPerson(Person person);
}
