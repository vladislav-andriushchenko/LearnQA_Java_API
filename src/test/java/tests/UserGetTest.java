package tests;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import lib.ApiCoreRequests;
import lib.Assertions;
import lib.BaseTestCase;
import lib.DataGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

@Epic("Get user data cases")
@Feature("Get user data")
public class UserGetTest extends BaseTestCase {
    String loginUrl = "https://playground.learnqa.ru/api/user/login";
    String getUserDataUrl = "https://playground.learnqa.ru/api/user/";
    String registerUrl = "https://playground.learnqa.ru/api/user/";
    private final ApiCoreRequests apiCoreRequests = new ApiCoreRequests();

    @Test
    @Description("This test successfully get not authorized user data")
    @DisplayName("Test positive get not auth user data")
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
    @Description("This test successfully get authorized user data")
    @DisplayName("Test positive get auth user data")
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

    @Test
    @Description("This test checks the ability to get user data using auth data of another user")
    @DisplayName("Test negative get user data by another user")
    public void testGetUserDetailsAuthNotTheSameUser() {
        Map<String, String> user1Data = new HashMap<>();
        user1Data.put("email", "vinkotov@example.com");
        user1Data.put("password", "1234");

        //Login and get auth data for initial user
        Response responseLoginUser = apiCoreRequests.makePostRequest(loginUrl, user1Data);

        String header = this.getHeader(responseLoginUser, "x-csrf-token");
        String cookie = this.getCookie(responseLoginUser, "auth_sid");

        //Create a new user
        Map<String, String> user2Data = DataGenerator.getRegistrationData();
        JsonPath responseCreateUser = apiCoreRequests.makePostRequestJsonPath(registerUrl, user2Data);
        String newUserId = responseCreateUser.get("id");

        //Get a new user info using auth data from initial user
        Response responseGetUserData = apiCoreRequests.makeGetRequest(getUserDataUrl + newUserId, header, cookie);

        Assertions.assertJsonHasField(responseGetUserData, "username");
        Assertions.assertJsonHasNotField(responseGetUserData, "firstName");
        Assertions.assertJsonHasNotField(responseGetUserData, "lastName");
        Assertions.assertJsonHasNotField(responseGetUserData, "email");
    }
}
