package eu.domaindriven.ddq.boundary;

import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import javax.json.Json;
import javax.json.JsonObject;
import java.net.URI;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.startsWith;
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

    @Test
    @Order(2)
    void testCreateGreetingAndSalute() {
        String personName = "Zoey";
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

        given()
                .contentType(ContentType.JSON)
                .when().post(saluteUri)
                .then()
                .statusCode(200);

        given()
                .when().get(locationHeader)
                .then()
                .statusCode(200)
                .body("salutes", is(1));

        given()
                .when().get(locationHeader + "/salutes")
                .then()
                .statusCode(200)
                .body("salutes", is(1));
    }

    @Test
    @Order(3)
    void testCreateExistingGreeting() {
        String personName = "Zoey";
        JsonObject person = Json.createObjectBuilder()
                .add("name", personName)
                .build();

        given().body(person.toString())
                .contentType(ContentType.JSON)
                .when().post("/resources/greetings")
                .then()
                .statusCode(409);
    }

    @Test
    @Order(4)
    void testCreateInvalidGreeting() {
        String personName = "Zoey";
        JsonObject person = Json.createObjectBuilder()
                .add("personName", personName)
                .build();

        given().body(person.toString())
                .contentType(ContentType.JSON)
                .when().post("/resources/greetings")
                .then()
                .statusCode(400);
    }

    @Test
    @Order(5)
    void testGetGreetingByPersonName() {
        given()
                .when().get("/resources/greetings?name=Zoey")
                .then()
                .statusCode(200);
        given()
                .when().get("/resources/greetings?name=Karl")
                .then()
                .statusCode(404);
    }

    @Test
    @Order(6)
    void testSaluteNonExistingGreeting() {
        String greetingId = UUID.randomUUID().toString();
        given()
                .when().post("/resources/greetings/salutes/" +greetingId + "/salute")
                .then()
                .statusCode(404);
    }

    @Test
    @Order(7)
    void testGetSalutes() {
        String eTag = given()
                .when().get("/resources/greetings/salutes")
                .then()
                .statusCode(200)
                .body("salutes", is(1))
                .extract()
                .header("ETag");

        given().header("If-None-Match", eTag)
                .when().get("/resources/greetings/salutes")
                .then()
                .statusCode(304)
                .header("ETag", eTag);
    }

    @Test
    @Order(8)
    void testEventNotifications() {
        String greetingId = given()
                .when().get("/resources/greetings")
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
                .body("notifications", hasSize(1))
                .body("notifications.name", hasItem("Greeted"))
                .body("notifications.id", hasItem(1))
                .body("notifications.detail.greetingId", hasItem(greetingId))
                .body("notifications.detail.person", hasItem("Zoey"))
                .body("status", is("ACTUAL"));
    }

}