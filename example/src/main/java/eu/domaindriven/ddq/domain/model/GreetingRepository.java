package eu.domaindriven.ddq.domain.model;

import eu.domaindriven.ddq.panache.IdentityProvider;
import eu.domaindriven.ddq.panache.Repository;

import java.util.Optional;

public interface GreetingRepository extends Repository<Greeting, GreetingId, IdentityProvider<GreetingId>> {

    Optional<Greeting> byPerson(Person person);
}
