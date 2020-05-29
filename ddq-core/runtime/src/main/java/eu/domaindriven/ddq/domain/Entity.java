package eu.domaindriven.ddq.domain;

import eu.domaindriven.ddq.ServiceDiscovery;
import eu.domaindriven.ddq.event.DomainEvent;
import eu.domaindriven.ddq.event.Events;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.json.bind.annotation.JsonbTransient;
import javax.persistence.MappedSuperclass;
import javax.persistence.PostLoad;
import javax.persistence.PostPersist;
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

    private transient ServiceDiscovery serviceDiscovery;

    @PostLoad
    @PostPersist
    void initEnvironment() {
        if (this.eventPublisher == null) {
            this.eventPublisher = Events.publisher()::publish;
        }
        if (this.serviceDiscovery == null) {
            this.serviceDiscovery = new ServiceDiscovery();
        }
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        initEnvironment();
    }

    protected void publishEvent(DomainEvent event) {
        eventPublisher.accept(event);
    }

    protected <T> T lookup(Class<T> service) {
        return serviceDiscovery.lookup(service);
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
