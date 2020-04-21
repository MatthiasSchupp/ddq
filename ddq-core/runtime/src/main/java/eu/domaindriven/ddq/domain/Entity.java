package eu.domaindriven.ddq.domain;

import eu.domaindriven.ddq.event.DomainEvent;
import eu.domaindriven.ddq.event.Events;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.json.bind.annotation.JsonbTransient;
import javax.persistence.MappedSuperclass;
import javax.persistence.PostLoad;
import javax.persistence.Version;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.time.Instant;
import java.util.function.Consumer;

@SuppressWarnings("unused")
@MappedSuperclass
public abstract class Entity extends IdentifiedDomainObject {

    private static final long serialVersionUID = 9003857489114509843L;

    @JsonbTransient
    @Version
    private Long version;

    @JsonbTransient
    @CreationTimestamp
    private Instant created;

    @JsonbTransient
    @UpdateTimestamp
    private Instant updated;

    private transient Consumer<DomainEvent> eventPublisher;

    @PostLoad
    void initEventPublisher() {
        this.eventPublisher = Events.publisher()::publish;
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        initEventPublisher();
    }

    protected void publishEvent(DomainEvent event) {
        eventPublisher.accept(event);
    }

    public Long version() {
        return version;
    }

    public Instant created() {
        return created;
    }

    public Instant updated() {
        return updated;
    }
}
