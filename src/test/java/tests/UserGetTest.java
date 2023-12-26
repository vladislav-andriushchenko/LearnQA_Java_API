package tests;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import lib.Assertions;
import lib.BaseTestCase;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class UserGetTest extends BaseTestCase {

    @Test
    public void testGetUserDataNotAuth() {
        Response responseGetUserData = RestAssured
                .get("https://playground.learnqa.ru/api/user/2")
                .andReturn();

        System.out.println(responseGetUserData.asString());

        Assertions.assertJsonHasField(responseGetUserData, "username");
        Assertions.assertJsonHasNotField(responseGetUserData, "firstName");
        Assertions.assertJsonHasNotField(responseGetUserData, "lastName");
        Assertions.assertJsonHasNotField(responseGetUserData, "email");
    }

    @Test
    public void testGetUserDetailsAuthSameUser() {
        Map<String, String> userData = new HashMap<>();
        userData.put("email", "vinkotov@example.com");
        userData.put("password", "1234");

        String url = "https://playground.learnqa.ru/api/user/login";
        Response responseGetAuth = RestAssured
                .given()
                .body(userData)
                .post(url)
                .andReturn();

        String header = this.getHeader(responseGetAuth, "x-csrf-token");
        String cookie = this.getCookie(responseGetAuth, "auth_sid");

        Response responseUserData = RestAssured
                .given()
                .header("x-csrf-token", header)
                .cookie("auth_sid", cookie)
                .get("https://playground.learnqa.ru/api/user/2")
                .andReturn();

        String[] expectedFields = {"username", "firstName", "lastName", "email"};

        Assertions.assertJsonHasFields(responseUserData, expectedFields);
    }

}