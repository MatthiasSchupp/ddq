package eu.domaindriven.ddq.event;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.net.http.HttpResponse.BodyHandlers.ofInputStream;
import static javax.ws.rs.core.HttpHeaders.ACCEPT;
import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;

@Dependent
public class EventSourceClient {

    private static final String NOTIFICATIONS_FIELD = "notifications";
    private static final String ID_FIELD = "id";
    private static final String NAME_FIELD = "name";
    private static final String DETAIL_FIELD = "detail";
    private static final String ACCEPT_HEADER = "application/hal+json";

    @Inject
    EventReader eventReader;
    @Inject
    Instance<AuthorizationProvider> authorizationProviders;

    private final HttpClient client;

    public EventSourceClient() {
        client = HttpClient.newHttpClient();
    }

    public EventSourceLog since(String name, URI uri, long id) {
        JsonObject notificationLog = getNotificationLog(name, uri);
        long lastId = extractHighId(notificationLog);
        List<DomainEvent> notifications = new ArrayList<>(extractNotifications(notificationLog, id));
        Optional<URI> previousUri = extractPreviousUri(notificationLog);

        while (previousUri.isPresent() && extractLowId(notificationLog) > id) {
            notificationLog = getNotificationLog(name, previousUri.get());
            notifications.addAll(extractNotifications(notificationLog, id));
            previousUri = extractPreviousUri(notificationLog);
        }

        return new EventSourceLog(notifications, lastId);
    }

    public EventSourceLog current(String name, URI uri) {
        JsonObject notificationLog = getNotificationLog(name, uri);
        long lastId = extractHighId(notificationLog);
        List<DomainEvent> notifications = extractNotifications(notificationLog, 0);

        return new EventSourceLog(notifications, lastId);
    }

    private Optional<URI> extractPreviousUri(JsonObject notificationLog) {
        JsonObject links = notificationLog.getJsonObject("_links");
        return links.containsKey("previous")
                ? Optional.of(links.getJsonObject("previous").getString("href")).map(URI::create)
                : Optional.empty();
    }

    private List<DomainEvent> extractNotifications(JsonObject notificationLog, long minId) {
        return notificationLog.getJsonArray(NOTIFICATIONS_FIELD).stream()
                .map(JsonValue::asJsonObject)
                .filter(notification -> notification.getJsonNumber(ID_FIELD).longValue() > minId)
                .filter(this::considerEvent)
                .map(this::read)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private long extractHighId(JsonObject notificationLog) {
        return notificationLog.getJsonArray(NOTIFICATIONS_FIELD).stream()
                .map(JsonValue::asJsonObject)
                .mapToLong(notification -> notification.getJsonNumber(ID_FIELD).longValue())
                .max()
                .orElse(0);
    }

    private long extractLowId(JsonObject notificationLog) {
        return notificationLog.getJsonArray(NOTIFICATIONS_FIELD).stream()
                .map(JsonValue::asJsonObject)
                .mapToLong(notification -> notification.getJsonNumber(ID_FIELD).longValue())
                .min()
                .orElse(0);
    }

    private JsonObject getNotificationLog(String name, URI uri) {
        HttpRequest.Builder builder = HttpRequest.newBuilder(uri)
                .header(ACCEPT, ACCEPT_HEADER);
        if (authorizationProviders.isResolvable()) {
            builder.header(AUTHORIZATION, authorizationProviders.get().header(name));
        }
        HttpRequest request = builder.build();

        try {
            HttpResponse<InputStream> response = client.send(request, ofInputStream());
            if (response.statusCode() == 200) {
                return readJson(response.body());
            } else if (response.statusCode() == 401 || response.statusCode() == 403) {
                throw new EventSourceAuthenticationException("Authentication failed for event source '" + name + "' with status code '" + response.statusCode() + "'");
            } else {
                throw new EventSourceException("Response status code was '" + response.statusCode() + "'");
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(ex);
        }
    }

    private JsonObject readJson(InputStream inputStream) {
        try (JsonReader reader = Json.createReader(inputStream)) {
            return reader.readObject();
        }
    }

    private boolean considerEvent(JsonObject notification) {
        return eventReader.collectable(name(notification));
    }

    private Optional<DomainEvent> read(JsonObject notification) {
        String name = name(notification);
        JsonObject event = detail(notification);

        return eventReader.read(event, name);
    }

    private static String name(JsonObject notification) {
        if (notification.containsKey(NAME_FIELD)) {
            return notification.getString(NAME_FIELD);
        } else {
            throw new EventSourceFormatException("The notification has no field with the name 'name'");
        }
    }

    private JsonObject detail(JsonObject notification) {
        if (notification.containsKey(DETAIL_FIELD)) {
            return notification.getJsonObject(DETAIL_FIELD);
        } else {
            throw new EventSourceFormatException("The notification has no field with the name 'detail'");
        }
    }

}
