import io.restassured.RestAssured;
import io.restassured.http.Headers;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GreetingTest {

    private static Stream<Arguments> testData() {
        return Stream.of(
                Arguments.of("Mozilla/5.0 (Linux; U; Android 4.0.2; en-us; Galaxy Nexus Build/ICL53F) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30",
                        "Mobile", "No", "Android"),
                Arguments.of("Mozilla/5.0 (iPad; CPU OS 13_2 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) CriOS/91.0.4472.77 Mobile/15E148 Safari/604.1",
                        "Mobile", "Chrome", "iOS"),
                Arguments.of("Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)",
                        "Googlebot", "Unknown", "Unknown"),
                Arguments.of("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.77 Safari/537.36 Edg/91.0.100.0",
                        "Web", "Chrome", "No"),
                Arguments.of("Mozilla/5.0 (iPad; CPU iPhone OS 13_2_3 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.0.3 Mobile/15E148 Safari/604.1",
                        "Mobile", "No", "iPhone")
        );
    }

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
                System.out.println("The correct password is: " + password);
                break;
            }
        }
    }

    @Test
    public void testLength() {
        String example = "1234567890qwerty";

        assertTrue(example.length() > 15, "The length should be longer that 15 characters: " + example.length());
    }

    @Test
    public void testCookie() {
        String url = "https://playground.learnqa.ru/api/homework_cookie";

        Response response = RestAssured
                .when()
                .get(url)
                .andReturn();

        Map<String, String> cookies = response.getCookies();

        assertTrue(cookies.containsKey("HomeWork"), "Response doesn't have 'HomeWork' cookie");
        assertTrue(cookies.containsValue("hw_value"), "The cookie's value doesn't have 'hw_value' value\n" +
                "Actual value is: " + cookies.get("HomeWork"));
    }

    @Test
    public void testHeader() {
        String url = "https://playground.learnqa.ru/api/homework_header";

        Response response = RestAssured
                .when()
                .get(url)
                .andReturn();

        Headers headers = response.getHeaders();

        assertTrue(headers.hasHeaderWithName("x-secret-homework-header"), "Response doesn't have 'x-secret-homework-header' header");
        assertEquals(headers.getValue("x-secret-homework-header"), "Some secret value",
                "The header doesn't have 'Some secret value' value: " + headers.getValue("x-secret-homework-header"));

    }

    @ParameterizedTest
    @MethodSource("testData")
    public void testUserAgent(String userAgent, String expectedPlatform, String expectedBrowser, String expectedDevice) {
        String url = "https://playground.learnqa.ru/ajax/api/user_agent_check";

        JsonPath response = RestAssured
                .given()
                .header("User-Agent", userAgent)
                .when()
                .get(url)
                .jsonPath();

        String actualPlatform = response.get("platform");
        String actualBrowser = response.get("browser");
        String actualDevice = response.get("device");

        assertEquals(expectedPlatform, actualPlatform, "Response doesn't have a required Platform");
        assertEquals(expectedBrowser, actualBrowser, "Response doesn't have a required Browser");
        assertEquals(expectedDevice, actualDevice, "Response doesn't have a required Device");
    }
}
