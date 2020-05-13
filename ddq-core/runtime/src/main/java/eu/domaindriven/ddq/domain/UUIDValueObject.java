package eu.domaindriven.ddq.domain;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.MappedSuperclass;
import java.util.Objects;
import java.util.UUID;

@MappedSuperclass
@Access(AccessType.FIELD)
public abstract class UUIDValueObject extends ValueObject {
    private static final long serialVersionUID = -6101399519601429696L;

    private UUID id;

    protected UUIDValueObject() {
    }

    public UUIDValueObject(UUID id) {
        this.id = id;
    }

    public UUIDValueObject(String id) {
        this(UUID.fromString(id));
    }

    public UUID id() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UUIDValueObject that = (UUIDValueObject) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return id.toString();
    }
}
