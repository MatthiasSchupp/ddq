package $ import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import javax.json.Json;
import javax.json.JsonObject;
import java.net.URI;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.Matchers.hasSize;

{package}.boundary;

@QuarkusTest
public class GreetingsResourceTest {

    @TestHTTPResource("/${artifactId}/resources")
    URI resourcesUri;

    @Test
    public void testEmptyGreetings() {
        given()
                .when().get("/resources/greetings")
                .then()
                .statusCode(200)
                .body("_links", hasSize(1))
                .body("_links[0].rel", is("salutes"))
                .body("_links[0].uri", is(resourcesUri + "/greetings/salutes"))
                .body("greetings", hasSize(0));

        given()
                .when().get("/resources/greetings/salutes")
                .then()
                .body("salutes", is(0));
    }

    @Test
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
                .body("_links", hasSize(3))
                .body("_links[0].rel", is("self"))
                .body("_links[0].uri", is(locationHeader))
                .body("_links[1].rel", is("salute"))
                .body("_links[1].uri", is(locationHeader + "/salute"))
                .body("_links[2].rel", is("salutes"))
                .body("_links[2].uri", is(locationHeader + "/salutes"))
                .body("person.name", is(personName))
                .body("salutes", is(0))
                .extract()
                .body()
                .path("_links[1].uri");

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

}