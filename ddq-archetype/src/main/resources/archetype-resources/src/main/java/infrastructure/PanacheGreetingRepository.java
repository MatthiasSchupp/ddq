package ${package}.infrastructure;

import ${package}.domain.model.Greeting;
import ${package}.domain.model.GreetingId;
import ${package}.domain.model.GreetingRepository;
import ${package}.domain.model.Person;
import eu.domaindriven.ddq.panache.PanacheRepository;

import javax.enterprise.context.Dependent;
import java.util.Optional;
import java.util.UUID;

@Dependent
public class PanacheGreetingRepository extends PanacheRepository<Greeting, GreetingId> implements GreetingRepository {

    public PanacheGreetingRepository() {
        super("greetingId", () -> new GreetingId(UUID.randomUUID()));
    }

    @Override
    public Optional<Greeting> byPerson(Person person) {
        return Optional.ofNullable(find("person", person).firstResult());
    }
}
