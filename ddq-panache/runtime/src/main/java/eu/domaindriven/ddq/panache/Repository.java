package eu.domaindriven.ddq.panache;

import eu.domaindriven.ddq.domain.Page;
import eu.domaindriven.ddq.domain.ValueObject;

import java.util.List;
import java.util.Optional;

public interface Repository<Entity extends eu.domaindriven.ddq.domain.Entity, Id extends ValueObject, Provider extends IdentityProvider<Id>> {
    Provider identity();

    void add(Entity entity);

    Optional<Entity> byId(Id id);

    List<Entity> list();

    Page<Entity> page(int pageSize);

    Page<Entity> page(int pageIndex, int pageSize);

}
