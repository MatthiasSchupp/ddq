package eu.domaindriven.ddq.domain.model;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

public class GreetingNotFoundException extends WebApplicationException {

    private static final long serialVersionUID = 8822656769422653227L;

    public GreetingNotFoundException() {
        super("No Greeting found.", Response.Status.NOT_FOUND);
    }
}
