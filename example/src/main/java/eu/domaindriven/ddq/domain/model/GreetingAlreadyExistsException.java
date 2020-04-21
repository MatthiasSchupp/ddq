package eu.domaindriven.ddq.domain.model;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

public class GreetingAlreadyExistsException extends WebApplicationException {

    private static final long serialVersionUID = -5535684941212174642L;

    public GreetingAlreadyExistsException() {
        super("Greeting already exists.", Response.Status.CONFLICT);
    }
}
