package ${package}.application;

import ${package}.domain.model.*;
import ${package}.domain.service.SaluteCalculator;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

@Dependent
public class GreetingService {

    @Inject
    GreetingRepository greetingRepository;

    @Inject
    SaluteCalculator saluteCalculator;

    public Greeting create(String personName) {
        Person person = new Person(personName);
        if (greetingRepository.byPerson(person).isEmpty()) {
            Greeting greeting = new Greeting(greetingRepository.identity().next(), person);
            greetingRepository.add(greeting);
            return greeting;
        } else {
            throw new GreetingAlreadyExistsException();
        }
    }

    public List<Greeting> greetings() {
        return greetingRepository.list();
    }

    public Optional<Greeting> greeting(GreetingId greetingId) {
        return greetingRepository.byId(greetingId);
    }

    public Optional<Greeting> greeting(Person person) {
        return greetingRepository.byPerson(person);
    }

    public void salute(GreetingId greetingId) {
        Greeting greeting = greeting(greetingId).orElseThrow(GreetingNotFoundException::new);
        greeting.salute();
    }

    public int salutes(GreetingId greetingId) {
        return greeting(greetingId).orElseThrow(GreetingNotFoundException::new).salutes();
    }

    public int salutes() {
        return saluteCalculator.sumOfSalutes(greetingRepository.list());
    }
}
