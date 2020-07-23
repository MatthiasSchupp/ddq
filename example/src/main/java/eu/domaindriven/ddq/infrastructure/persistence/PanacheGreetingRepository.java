package eu.domaindriven.ddq.infrastructure.persistence;

import eu.domaindriven.ddq.domain.model.Greeting;
import eu.domaindriven.ddq.domain.model.GreetingId;
import eu.domaindriven.ddq.domain.model.GreetingRepository;
import eu.domaindriven.ddq.domain.model.Person;
import eu.domaindriven.ddq.panache.IdentityProvider;
import eu.domaindriven.ddq.panache.PanacheRepository;

import javax.enterprise.context.Dependent;
import java.util.Optional;
import java.util.UUID;

@Dependent
public class PanacheGreetingRepository extends PanacheRepository<Greeting, GreetingId, IdentityProvider<GreetingId>> implements GreetingRepository {

    public PanacheGreetingRepository() {
        super("greetingId", () -> new GreetingId(UUID.randomUUID()));
    }

    @Override
    public Optional<Greeting> byPerson(Person person) {
        return Optional.ofNullable(find("person", person).firstResult());
    }
}
