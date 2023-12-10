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
    public void getJsonHomework() {

        JsonPath response = RestAssured
                .given()
                .get("https://playground.learnqa.ru/api/get_json_homework")
                .jsonPath();

        response.prettyPrint();
        String name = response.get("messages[1].message");
        System.out.println(name);
    }
}
