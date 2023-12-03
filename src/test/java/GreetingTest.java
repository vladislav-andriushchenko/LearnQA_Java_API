import org.junit.jupiter.api.Test;
import io.restassured.RestAssured;
import io.restassured.response.Response;

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
}
