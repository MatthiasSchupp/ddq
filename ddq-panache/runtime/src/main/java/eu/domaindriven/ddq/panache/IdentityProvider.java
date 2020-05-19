package eu.domaindriven.ddq.panache;

import eu.domaindriven.ddq.domain.ValueObject;

@FunctionalInterface
public interface IdentityProvider<Id extends ValueObject> {
    Id next();
}
