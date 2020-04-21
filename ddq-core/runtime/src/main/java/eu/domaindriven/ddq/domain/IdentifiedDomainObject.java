package eu.domaindriven.ddq.domain;

import javax.json.bind.annotation.JsonbTransient;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import java.io.Serializable;
import java.util.Objects;
import java.util.StringJoiner;

@SuppressWarnings("unused")
@MappedSuperclass
public abstract class IdentifiedDomainObject implements Serializable {

    private static final long serialVersionUID = 2148995025198634406L;

    @JsonbTransient
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public Long id() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IdentifiedDomainObject that = (IdentifiedDomainObject) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", this.getClass().getSimpleName() + "[", "]")
                .add("id=" + id)
                .toString();
    }
}
