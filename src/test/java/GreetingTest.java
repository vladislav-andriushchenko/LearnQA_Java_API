import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Test
    public void testJob() throws InterruptedException {
        String url = "https://playground.learnqa.ru/ajax/api/longtime_job";

        JsonPath responseJobWithoutToken = RestAssured
                .given()
                .when()
                .get("https://playground.learnqa.ru/ajax/api/longtime_job")
                .jsonPath();

        String token = responseJobWithoutToken.get("token");
        int seconds = responseJobWithoutToken.get("seconds");

        JsonPath responseJobWithToken = RestAssured
                .given()
                .queryParam("token", token)
                .get(url)
                .jsonPath();

        String statusValue = responseJobWithToken.get("status");

        int milliseconds = seconds * 1000;

        if (statusValue.equals("Job is NOT ready")) {
            Thread.sleep(milliseconds);
            JsonPath responseJobFinal = RestAssured
                    .given()
                    .queryParam("token", token)
                    .get(url)
                    .jsonPath();

            String statusValueFinal = responseJobFinal.get("status");
            String result = responseJobFinal.get("result");

            if (statusValueFinal.equals("Job is ready")) {
                System.out.println("OK\n status equals: " + statusValueFinal);
            } else {
                System.out.println("The status is incorrect\n Expected: \"Job is ready\"\n But was: " + statusValueFinal);
            }

            if (result != null) {
                System.out.println("OK\n result is not null");
            } else {
                System.out.println("The result is empty");
            }

        } else {
            System.out.println("The status is incorrect\n Expected: \"Job is NOT ready\"\n But was: " + statusValue);
        }

    }

    @Test
    public void testGetPassword() {
        List<String> list = Arrays.asList("password", "123456789", "12345", "12345678", "123456789", "qwerty", "abc123",
                "football", "1234567", "monkey", "111111", "letmein", "1234", "1234567890", "dragon", "baseball", "sunshine",
                "iloveyou", "trustno1", "princess", "adobe123", "123123", "welcome", "login", "admin", "solo", "1q2w3e4r", "master",
                "666666", "photoshop", "qwertyuiop", "ashley", "mustang", "121212", "starwars", "654321", "bailey", "access",
                "flower", "555555", "passw0rd", "shadow", "lovely", "7777777", "michael", "!@#$%^&*", "jesus", "password1",
                "superman", "hello", "charlie", "888888", "696969", "hottie", "freedom", "aa123456", "qazwsx", "ninja", "azerty",
                "loveme", "whatever", "donald", "batman", "zaq1zaq1", "qazwsx", "Football", "000000", "qwerty123", "123qwe");

        String getPasswordUrl = "https://playground.learnqa.ru/ajax/api/get_secret_password_homework";
        String checkCookieUrl = "https://playground.learnqa.ru/ajax/api/check_auth_cookie";
        Map<String, Object> params = new HashMap<>();
        Map<String, String> cookies = new HashMap<>();
        params.put("login", "super_admin");

        for (int i = 0; i <= list.size(); i++) {
            String password = list.get(i);
            params.put("password", password);

            Response responsePassword = RestAssured
                    .given()
                    .body(params)
                    .when()
                    .post(getPasswordUrl)
                    .andReturn();

            String cookie = responsePassword.getCookie("auth_cookie");
            cookies.put("auth_cookie", cookie);

            Response responseCheck = RestAssured
                    .given()
                    .cookies(cookies)
                    .when()
                    .get(checkCookieUrl)
                    .andReturn();

            if (responseCheck.print().equals("You are authorized")) {
                break;
            }
        }
    }
}
