package ${package}.domain.service;

import ${package}.domain.model.Greeting;

import javax.enterprise.context.Dependent;
import java.util.Collection;

@Dependent
public class SaluteCalculator {

    public int sumOfSalutes(Collection<? extends Greeting> greetings) {
        return greetings.stream()
                .mapToInt(Greeting::salutes)
                .sum();
    }
}
