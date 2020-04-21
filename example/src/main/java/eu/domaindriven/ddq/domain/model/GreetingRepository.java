package eu.domaindriven.ddq.domain.model;

import java.util.List;
import java.util.Optional;

public interface GreetingRepository {
    GreetingId nextId();

    void add(Greeting greeting);

    Optional<Greeting> byId(GreetingId greetingId);

    Optional<Greeting> byPerson(Person person);

    List<Greeting> list();
}
