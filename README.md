# Domain-driven Quarkus (DDQ)
Quarkus extension for domain-driven Microservices.

* **Following Domain-driven design principles**: 
The extension offers different features to support the development of Microservices according to domain-driven design
following the book _Implementing Domain-Driven Design_ from _Vaughn Vernon_.
* **All optional**:
It is an extension, not a framework. With it you can use the different features you like, but you are not forced to.

> To benefit from this extension it is necessary to understand the principles and tactical patterns of domain-driven design.

## Usage
To create a Quarkus application using this extension and get the right configuration and plugins, the archetype can be
used. There is also an example application in the repository to provide some code.

    mvn archetype:generate -DarchetypeGroupId=eu.domain-driven -DarchetypeArtifactId=ddq-archetype -DarchetypeVersion=<version>

## Features

### Domain events
The extension ensures the publishing, collection and correct processing of domain events, also with multiple instances
of the same service running at the same time.
To make this possible, each event will first written to the database and then processed from there. This ensures the
decoupling and transactional security of event processing.

#### Publish events
In CDI beans:
```java
@Dependend
public class ExampleService {

    @Inject
    EventPublisher eventPublisher;

    public void doBusinessStuff(StuffId stuffId) {
        // Do some stuff here

        eventPublisher.publish(new StuffDone(stuffId));
    }
}
```
In Pojo classes:
```java
public class Example {

    public void doBusinessStuff(StuffId stuffId) {
        // Do some stuff here

        Events.publish(new StuffDone(stuffId));
    }
}
```
In the domain model:
```java
@javax.persistence.Entity
public class Example extends Entity {

    @Embedded
    private StuffId stuffId;

    public void doBusinessStuff() {
        // Do some stuff here

        publishEvent(new StuffDone(stuffId));
    }
}
```
#### Consume events
In CDI beans:
```java
@Dependend
public class ExampleService {

    void onStuffDone(@Observes StuffDone stuffDone) {
        // Do some other stuff here
    }
}
```

### Publish events to other applications
All events published in the application and collected from other applications are available via REST as notifications

    http://hostname[:port]/example/resources/notifications/events


#### Collect events from other application
To collect events from other applications using this extension, an event source must be defined in the
application.properties file.
Valid url formats are:
* hostname[:port]/example
* hostname[:port]/example/resources/notifications/events
* http://hostname[:port]/example
* http://hostname[:port]/example/resources/notifications/events

For an event source, a start id can be defined, which sets the oldest event to collect. Without a start id, the
collection begins with the actual newest event.

```properties
quarkus.ddq.event.event-source.<name>.uri=hostname/example
quarkus.ddq.event.event-source.<name>.start-id=0 #optional
```

### Errors
To provide a better inside in the errors occurred during of the processing of the application than simply write it in an
unstructured logfile,the extension provides the errors similar to the events.

#### Publish errors
In CDI beans:
```java
@Dependend
public class ExampleService {

    @Inject
    ErrorPublisher errorPublisher;

    public void doBusinessStuff(StuffId stuffId) {
        // Do some stuff here

        errorPublisher.business("Error Message", this.getClass());

        try {
            // Do some stuff here
        } catch (SomeException e) {
            errorPublisher.technical("Error Message", this.getClass(), e);
        }
    }
}
```
In Pojo classes:
```java
public class Example {

    public void doBusinessStuff(StuffId stuffId) {
        // Do some stuff here

        Errors.business("Error Message", this.getClass());

        try {
            // Do some stuff here
        } catch (SomeException e) {
            Errors.technical("Error Message", this.getClass(), e);
        }
    }
}
```

### Domain model
There are some base classes available to easily implement the domain model:
* **ValueObject**
* **UUIDValueObject**
* **IdentifiedValueObject**
* **IdentifiedDomainObject**
* **Entity**

### HATEOAS REST resources
HATEOAS REST endpoint with ETag support can be implemented as follows:
```java
public class GreetingRepresentation extends Representation {

    private final GreetingId greetingId;
    private final Person person;
    private final Integer salutes;

    public GreetingRepresentation(Greeting greeting, UriInfo uriInfo) {
        this.greetingId = greeting.greetingId();
        this.person = greeting.person();
        this.salutes = greeting.salutes();
        link("self", uriInfo.getBaseUriBuilder(), "greetings", greeting.greetingId().id().toString());
    }

    public GreetingId greetingId() {
        return greetingId;
    }

    public Person person() {
        return person;
    }

    public Integer salutes() {
        return salutes;
    }
}
```
```java
public class GreetingLogRepresentation extends LogRepresentation<Greeting, GreetingRepresentation> {

    public GreetingLogRepresentation(Collection<Greeting> greetings, UriInfo uriInfo) {
        super(greetings, uriInfo, "greetings", GreetingRepresentation::new);
        link("salutes", uriInfo.getBaseUriBuilder(), "greetings", "/salutes");
    }
}
```
```java
@Path("greetings")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Transactional
public class GreetingsResource {

    @Inject
    GreetingService greetingService;

    @Inject
    EntityTagResponseFactory responseFactory;

    @GET
    public Response greetings(@QueryParam("name") String personName, @Context UriInfo uriInfo, @Context Request request) {
        return personName != null
                ? greetingService.greeting(new Person(personName))
                .map(greeting -> responseFactory.createResponse(Collections.singletonList(greeting), uriInfo, request, GreetingLogRepresentation::new))
                .orElse(Response.status(NOT_FOUND).build())
                : responseFactory.createResponse(greetingService.greetings(), uriInfo, request, GreetingLogRepresentation::new);
    }

    @GET
    @Path("{id}")
    public Response greeting(@PathParam("id") String id, @Context UriInfo uriInfo, @Context Request request) {
        return greetingService.greeting(new GreetingId(id))
                .map(greeting -> responseFactory.createResponse(greeting, uriInfo, request, GreetingRepresentation::new))
                .orElse(Response.status(NOT_FOUND).build());
    }
}
```
