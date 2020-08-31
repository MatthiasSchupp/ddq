package eu.domaindriven.ddq.event;

public interface AuthorizationProvider {

    String header(String eventSource);
}
