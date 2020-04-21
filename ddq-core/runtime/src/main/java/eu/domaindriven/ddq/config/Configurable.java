package eu.domaindriven.ddq.config;

@FunctionalInterface
public interface Configurable {

    void configure(DdqConfig config);
}
