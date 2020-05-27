package eu.domaindriven.ddq;

import io.quarkus.arc.Arc;

public class ServiceDiscovery {

    public <T> T lookup(Class<T> service) {
        return Arc.container().instance(service).get();
    }
}
