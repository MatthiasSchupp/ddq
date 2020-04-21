package ${package}.infrastructure;

import ${package}.domain.model.Greeting;
import ${package}.domain.model.GreetingId;
import ${package}.domain.model.GreetingRepository;
import ${package}.domain.model.Person;
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
