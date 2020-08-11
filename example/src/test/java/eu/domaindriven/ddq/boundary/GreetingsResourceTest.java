package eu.domaindriven.ddq.boundary;

import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.MethodSource;

import javax.json.Json;
import javax.json.JsonObject;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GreetingsResourceTest {

    @TestHTTPResource("/example/resources")
    URI resourcesUri;

    @Test
    @Order(1)
    void testEmptyGreetings() {
        given()
                .when().get("/resources/greetings")
                .then()
                .statusCode(200)
                .body("_links", is(notNullValue()))
                .body("_links.self.href", is(resourcesUri + "/greetings"))
                .body("_links.salutes.href", is(resourcesUri + "/greetings/salutes"))
                .body("_embedded.greetings", hasSize(0));

        given()
                .when().get("/resources/greetings/salutes")
                .then()
                .body("salutes", is(0));
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/data.csv", numLinesToSkip = 1)
    @Order(2)
    void testCreateGreetingAndSalute(String personName, int salutes) {
        JsonObject person = Json.createObjectBuilder()
                .add("name", personName)
                .build();

        String locationHeader = given().body(person.toString())
                .contentType(ContentType.JSON)
                .when().post("/resources/greetings")
                .then()
                .statusCode(201)
                .header("Location", startsWith(resourcesUri + "/greetings/"))
                .extract()
                .header("Location");

        String saluteUri = given()
                .when().get(locationHeader)
                .then()
                .statusCode(200)
                .body("_links", is(notNullValue()))
                .body("_links.self.href", is(locationHeader))
                .body("_links.salute.href", is(locationHeader + "/salute"))
                .body("_links.salutes.href", is(locationHeader + "/salutes"))
                .body("person.name", is(personName))
                .body("salutes", is(0))
                .extract()
                .body()
                .path("_links.salute.href");

        for (int i = 0; i < salutes; i++) {
            given()
                    .contentType(ContentType.JSON)
                    .when().post(saluteUri)
                    .then()
                    .statusCode(200);
        }

        given()
                .when().get(locationHeader)
                .then()
                .statusCode(200)
                .body("salutes", is(salutes));

        given()
                .when().get(locationHeader + "/salutes")
                .then()
                .statusCode(200)
                .body("salutes", is(salutes));
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/data.csv", numLinesToSkip = 1)
    @Order(3)
    void testCreateExistingGreeting(String personName) {
        JsonObject person = Json.createObjectBuilder()
                .add("name", personName)
                .build();

        given().body(person.toString())
                .contentType(ContentType.JSON)
                .when().post("/resources/greetings")
                .then()
                .statusCode(409);
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/data.csv", numLinesToSkip = 1)
    @Order(4)
    void testCreateInvalidGreeting(String personName) {
        JsonObject person = Json.createObjectBuilder()
                .add("personName", personName)
                .build();

        given().body(person.toString())
                .contentType(ContentType.JSON)
                .when().post("/resources/greetings")
                .then()
                .statusCode(400);
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/data.csv", numLinesToSkip = 1)
    @Order(5)
    void testGetGreetingByPersonName(String personName) {
        given()
                .when().get("/resources/greetings?name=" + personName)
                .then()
                .statusCode(200);
        given()
                .when().get("/resources/greetings?name=Karl")
                .then()
                .statusCode(404);
    }

    @Test
    @Order(6)
    void testGetGreetingByUnknownPersonName() {
        String personName = UUID.randomUUID().toString();
        given()
                .when().get("/resources/greetings?name=" + personName)
                .then()
                .statusCode(404);
    }

    @Test
    @Order(7)
    void testSaluteNonExistingGreeting() {
        String greetingId = UUID.randomUUID().toString();
        given()
                .when().post("/resources/greetings/salutes/" +greetingId + "/salute")
                .then()
                .statusCode(404);
    }

    @ParameterizedTest
    @MethodSource("provideSumOfSalutes")
    @Order(8)
    void testGetSalutes(int salutes) {
        String eTag = given()
                .when().get("/resources/greetings/salutes")
                .then()
                .statusCode(200)
                .body("salutes", is(salutes))
                .extract()
                .header("ETag");

        given().header("If-None-Match", eTag)
                .when().get("/resources/greetings/salutes")
                .then()
                .statusCode(304)
                .header("ETag", eTag);
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/data.csv", numLinesToSkip = 1)
    @Order(9)
    void testEventNotifications(String personName) {
        String greetingId = given()
                .when().get("/resources/greetings?name=" + personName)
                .then()
                .statusCode(200)
                .body("_embedded.greetings", hasSize(1))
                .extract()
                .body()
                .path("_embedded.greetings.greetingId.id[0]");

        given()
                .when().get("/resources/notifications/events")
                .then()
                .statusCode(200)
                .body("_links", is(notNullValue()))
                .body("_links.self.href", is(resourcesUri + "/notifications/events/1,20"))
                .body("id", is("1,20"))
                .body("notifications", hasSize(greaterThan(1)))
                .body("notifications.name", everyItem(is("Greeted")))
                .body("notifications.id", hasItem(1))
                .body("notifications.detail.greetingId", hasItem(greetingId))
                .body("notifications.detail.findAll { it.greetingId=='" + greetingId + "' }.person[0]", is(personName))
                .body("status", is("ACTUAL"));
    }

    private static Stream<Arguments> provideSumOfSalutes() {
        try (InputStream is = GreetingsResourceTest.class.getClassLoader().getResourceAsStream("data.csv")) {
            String data = new String(Objects.requireNonNull(is).readAllBytes());
            int salutes = data.lines()
                    .skip(1)
                    .map(s -> s.split(",")[1])
                    .map(String::trim)
                    .mapToInt(Integer::parseInt)
                    .sum();

            return Stream.of(Arguments.of(salutes));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}