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
To provide a better insight in the errors occurred during of the processing of the application than simply write it in an
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

        errorPublisher.business("Error Message");

        try {
            // Do some stuff here
        } catch (SomeException e) {
            errorPublisher.technical("Error Message", e);
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
        // or
        Errors.publisher(this.getClass()).business("Error Message");

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
@BasePath(GreetingsResource.PATH)
public class GreetingRepresentation implements Representation {

    private final GreetingId greetingId;
    private final Person person;
    private final Integer salutes;
    @JsonbTransient
    private final  String personName;

    @BaseLink(rel = "self", path = "{greetingId}")
    private Link selfLink;

    @BaseLink(rel = "person", queryParams = @QueryParam(name = "name", values = "{personName}"))
    private Link personLink;

    @BaseLink(rel = "salute", path = "{greetingId}/salute", condition = "maxSalutesNotReached")
    private Link saluteLink;

    @BaseLink(rel = "salutes", path = "{greetingId}/salutes")
    private Link salutesLink;

    public GreetingRepresentation(Greeting greeting) {
        this.greetingId = greeting.greetingId();
        this.person = greeting.person();
        this.salutes = greeting.salutes();
        this.personName = person.name();
    }

    public boolean maxSalutesNotReached() {
        return salutes < 100;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GreetingRepresentation that = (GreetingRepresentation) o;
        return greetingId.equals(that.greetingId) &&
                person.equals(that.person) &&
                salutes.equals(that.salutes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(greetingId, person, salutes);
    }
}
```
```java
public class GreetingLogRepresentation extends LogRepresentation {

    @BaseLink(path = "greetings/salutes")
    private Link salutes;

    public GreetingLogRepresentation(Collection<Greeting> greetings) {
        super(greetings, "greetings", GreetingRepresentation::new);
    }
}
```
```java
@Path(GreetingsResource.PATH)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Transactional
public class GreetingsResource {

    public static final String PATH = "greetings";

    @Inject
    GreetingService greetingService;

    @Inject
    EntityTagResponseFactory responseFactory;

    @GET
    public Response greetings(@QueryParam("name") String personName) {
        return personName != null
                ? greetingService.greeting(new Person(personName))
                        .map(greeting -> responseFactory.createResponse(Collections.singletonList(greeting), GreetingLogRepresentation::new))
                        .orElse(Response.status(NOT_FOUND).build())
                : responseFactory.createResponse(greetingService.greetings(), GreetingLogRepresentation::new);
    }

    @GET
    @Path("{id}")
    public Response greeting(@PathParam("id") String id) {
        return greetingService.greeting(new GreetingId(id))
                .map(greeting -> responseFactory.createResponse(greeting, GreetingRepresentation::new))
                .orElse(Response.status(NOT_FOUND).build());
    }
}
```
