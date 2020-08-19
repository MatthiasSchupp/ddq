package eu.domaindriven.ddq.event;

import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Path("notifications-mock-2")
@Produces("application/hal+json")
public class EventSourceMock2 extends EventSourceMock {
}
