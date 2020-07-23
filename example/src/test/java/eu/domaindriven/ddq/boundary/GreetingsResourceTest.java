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
    public void testEmptyGreetings() {
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
    public void testCreateGreetingAndSalute() {
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
    }

    @Test
    @Order(3)
    public void testEventNotifications() {
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