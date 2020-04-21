package eu.domaindriven.ddq.infrastructure;

import eu.domaindriven.ddq.domain.model.Greeting;
import eu.domaindriven.ddq.domain.model.GreetingId;
import eu.domaindriven.ddq.domain.model.GreetingRepository;
import eu.domaindriven.ddq.domain.model.Person;
import io.quarkus.hibernate.orm.panache.PanacheRepository;

import javax.enterprise.context.Dependent;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Dependent
public class PanacheGreetingRepository implements GreetingRepository, PanacheRepository<Greeting> {


    @Override
    public GreetingId nextId() {
        return new GreetingId(UUID.randomUUID());
    }

    @Override
    public void add(Greeting greeting) {
        persist(greeting);
    }

    @Override
    public Optional<Greeting> byId(GreetingId greetingId) {
        return Optional.ofNullable(find("greetingId", greetingId).firstResult());
    }

    @Override
    public Optional<Greeting> byPerson(Person person) {
        return Optional.ofNullable(find("person", person).firstResult());
    }

    @Override
    public List<Greeting> list() {
        return listAll();
    }
}
