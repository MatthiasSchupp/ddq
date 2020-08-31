package eu.domaindriven.ddq.event;

import com.github.tomakehurst.wiremock.WireMockServer;
import eu.domaindriven.ddq.JsonbConfigurator;
import eu.domaindriven.ddq.notification.*;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.jboss.resteasy.specimpl.ResteasyUriInfo;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;
import javax.ws.rs.core.UriInfo;
import java.time.Instant;
import java.util.*;
import java.util.stream.Stream;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class MockEventSourceTestResource implements QuarkusTestResourceLifecycleManager {

    private static final String CONTENT_TYPE = "application/hal+json";
    private static final String USERNAME = "duke";
    private static final String PASSWORD = "dukePassword";

    private final Map<Provider, NotificationProvider<NotificationEvent>> notificationProviders;
    private final Map<Provider, UriInfo> uriInfo;

    private final WireMockServer wireMockServer;

    private final Jsonb jsonb;

    public MockEventSourceTestResource() {
        wireMockServer = new WireMockServer(8086);
        configureFor(8086);
        notificationProviders = new EnumMap<>(Provider.class);
        notificationProviders.put(Provider.EVENT_PROVIDER_1, new NotificationProviderMock());
        notificationProviders.put(Provider.EVENT_PROVIDER_2, new NotificationProviderMock());
        uriInfo = new EnumMap<>(Provider.class);

        JsonbConfig jsonbConfig = new JsonbConfig();
        JsonbConfigurator configurator = new JsonbConfigurator();
        configurator.customize(jsonbConfig);

        jsonb = JsonbBuilder.create(jsonbConfig);
    }

    @Override
    public Map<String, String> start() {
        wireMockServer.start();
        uriInfo.put(Provider.EVENT_PROVIDER_1, new ResteasyUriInfo(wireMockServer.baseUrl() + "/event-source-mock-1/resources/notifications/events", "/event-source-mock-1/resources"));
        uriInfo.put(Provider.EVENT_PROVIDER_2, new ResteasyUriInfo(wireMockServer.baseUrl() + "/event-source-mock-2/resources/notifications/events", "/event-source-mock-2/resources"));

        stubFor(get("/event-source-mock-1/resources/notifications/events/1,20")
                .withBasicAuth(USERNAME, PASSWORD)
                .willReturn(aResponse()
                        .withHeader("Content-Type", CONTENT_TYPE)
                        .withBody(response(Provider.EVENT_PROVIDER_1, 1, 20))));
        stubFor(get("/event-source-mock-1/resources/notifications/events/21,40")
                .withBasicAuth(USERNAME, PASSWORD)
                .willReturn(aResponse()
                        .withHeader("Content-Type", CONTENT_TYPE)
                        .withBody(response(Provider.EVENT_PROVIDER_1, 21, 40))));
        stubFor(get("/event-source-mock-1/resources/notifications/events/41,60")
                .withBasicAuth(USERNAME, PASSWORD)
                .willReturn(aResponse()
                        .withHeader("Content-Type", CONTENT_TYPE)
                        .withBody(response(Provider.EVENT_PROVIDER_1, 41, 60))));
        stubFor(get("/event-source-mock-1/resources/notifications/events")
                .withBasicAuth(USERNAME, PASSWORD)
                .willReturn(aResponse()
                        .withHeader("Content-Type", CONTENT_TYPE)
                        .withBody(currentResponse(Provider.EVENT_PROVIDER_1, 20))));

        stubFor(get("/event-source-mock-2/resources/notifications/events/1,20")
                .withBasicAuth(USERNAME, PASSWORD)
                .willReturn(aResponse()
                        .withHeader("Content-Type", CONTENT_TYPE)
                        .withBody(response(Provider.EVENT_PROVIDER_2, 1, 20))));
        stubFor(get("/event-source-mock-2/resources/notifications/events/21,40")
                .withBasicAuth(USERNAME, PASSWORD)
                .willReturn(aResponse()
                        .withHeader("Content-Type", CONTENT_TYPE)
                        .withBody(response(Provider.EVENT_PROVIDER_2, 21, 40))));
        stubFor(get("/event-source-mock-2/resources/notifications/events/41,60")
                .withBasicAuth(USERNAME, PASSWORD)
                .willReturn(aResponse()
                        .withHeader("Content-Type", CONTENT_TYPE)
                        .withBody(response(Provider.EVENT_PROVIDER_2, 41, 60))));
        stubFor(get("/event-source-mock-2/resources/notifications/events")
                .withBasicAuth(USERNAME, PASSWORD)
                .willReturn(aResponse()
                        .withHeader("Content-Type", CONTENT_TYPE)
                        .withBody(currentResponse(Provider.EVENT_PROVIDER_2, 20))));

        return Map.of();
    }

    @Override
    public void stop() {
        wireMockServer.stop();
    }

    private String response(Provider provider, int low, int high) {
        NotificationLogId notificationLogId = new NotificationLogId(low, high);
        NotificationLogFactory factory = new NotificationLogFactory(notificationProviders.get(provider), notificationLogId.size());
        NotificationLog notificationLog = factory.create(notificationLogId);

        NotificationLogRepresentation representation = new NotificationLogRepresentation(notificationLog, uriInfo.get(provider), "/notifications/events");

        return jsonb.toJson(representation);
    }

    private String currentResponse(Provider provider, int size) {
        NotificationLogFactory factory = new NotificationLogFactory(notificationProviders.get(provider), size);
        NotificationLog notificationLog = factory.createCurrent();

        NotificationLogRepresentation representation = new NotificationLogRepresentation(notificationLog, uriInfo.get(provider), "/notifications/events");

        return jsonb.toJson(representation);
    }

    private enum Provider {
        EVENT_PROVIDER_1,
        EVENT_PROVIDER_2
    }

    private static final class NotificationProviderMock implements NotificationProvider<NotificationEvent> {

        private final List<NotificationEvent> events = new ArrayList<>();

        NotificationProviderMock() {
            for (int i = 1; i <= 50; i++) {
                events.add(new NotificationEvent(i, new TestEvent("test " + i, i)));
            }
        }

        @Override
        public Stream<NotificationEvent> between(long lowId, long highId) {
            int upperId = (int) Math.min(highId, events.size());
            return events.subList((int) lowId - 1, upperId).stream();
        }

        @Override
        public Optional<Long> maxId() {
            return Optional.of((long) events.size());
        }

        @Override
        public Optional<Long> minId() {
            return Optional.of(1L);
        }
    }

    private static final class NotificationEvent implements Notifiable {

        private final long id;
        private final Instant timestamp;
        private final DomainEvent event;

        private NotificationEvent(long id, DomainEvent event) {
            this.id = id;
            this.timestamp = Instant.now();
            this.event = event;
        }

        @Override
        public Long id() {
            return id;
        }

        @Override
        public Instant timestamp() {
            return timestamp;
        }

        @Override
        public Object toProjection() {
            return event;
        }
    }
}
