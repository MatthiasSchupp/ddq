package eu.domaindriven.ddq.panache;

import eu.domaindriven.ddq.domain.Page;
import eu.domaindriven.ddq.domain.ValueObject;
import io.quarkus.hibernate.orm.panache.PanacheQuery;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static io.quarkus.panache.common.Page.of;
import static io.quarkus.panache.common.Page.ofSize;

public abstract class PanacheRepository<Entity extends eu.domaindriven.ddq.domain.Entity, Id extends ValueObject> implements Repository<Entity, Id>, io.quarkus.hibernate.orm.panache.PanacheRepository<Entity> {

    private final String idField;
    private final Supplier<Id> idSupplier;

    @SuppressWarnings("BoundedWildcard")
    public PanacheRepository(String idField, Supplier<Id> idSupplier) {
        this.idField = idField;
        this.idSupplier = idSupplier;
    }

    @Override
    public Id nextIdentity() {
        return idSupplier.get();
    }

    @Override
    public void add(Entity entity) {
        persist(entity);
    }

    @Override
    public Optional<Entity> byId(Id id) {
        return Optional.ofNullable(find(idField, id).firstResult());
    }

    @Override
    public List<Entity> list() {
        return listAll();
    }

    @Override
    public Page<Entity> page(int pageSize) {
        return createPage(findAll().page(ofSize(pageSize)).lastPage());
    }

    @Override
    public Page<Entity> page(int pageIndex, int pageSize) {
        return createPage(findAll().page(of(pageIndex, pageSize)).lastPage());
    }

    protected <T extends eu.domaindriven.ddq.domain.Entity> Page<T> createPage(PanacheQuery<T> query) {
        return new Page<>(query.list(), query.page().index, query.pageCount(), query.page().size);
    }
}
