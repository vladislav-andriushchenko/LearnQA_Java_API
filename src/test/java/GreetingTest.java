import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

public class GreetingTest {

    @Test
    public void testGreeting() {
        String name = "Vladislav";
        System.out.println("Hello from " + name);
    }

    @Test
    public void testGetText() {
        Response response = RestAssured
                .get("https://playground.learnqa.ru/api/get_text")
                .andReturn();

        response.prettyPrint();

    }

    @Test
    public void testJsonHomework() {
        JsonPath response = RestAssured
                .given()
                .get("https://playground.learnqa.ru/api/get_json_homework")
                .jsonPath();

        response.prettyPrint();
        String name = response.get("messages[1].message");
        System.out.println(name);
    }

    @Test
    public void testRedirect() {
        Response response = RestAssured
                .given()
                .redirects()
                .follow(false)
                .when()
                .get("https://playground.learnqa.ru/api/long_redirect")
                .andReturn();

        String location = response.getHeader("Location");
        System.out.println(location);
    }

    @Test
    public void testLongRedirect() {
        int redirects = 0;
        String location = "https://playground.learnqa.ru/api/long_redirect";

        while (true) {
            Response response = RestAssured
                    .given()
                    .redirects()
                    .follow(false)
                    .when()
                    .get(location)
                    .andReturn();
            int statusCode = response.getStatusCode();

            if (statusCode == 200) {
                break;
            } else {
                location = response.getHeader("Location");
                redirects += 1;
            }
        }

        System.out.println(redirects);
    }
}
