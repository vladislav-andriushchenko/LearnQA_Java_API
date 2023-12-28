package tests;

import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import lib.ApiCoreRequests;
import lib.Assertions;
import lib.BaseTestCase;
import lib.DataGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class UserEditTest extends BaseTestCase {
    String loginUrl = "https://playground.learnqa.ru/api/user/login";
    String getUserDataUrl = "https://playground.learnqa.ru/api/user/";
    String registerUrl = "https://playground.learnqa.ru/api/user/";
    private final ApiCoreRequests apiCoreRequests = new ApiCoreRequests();

    private static Stream<Arguments> testData() {
        return Stream.of(
                Arguments.of("email", "@", "Email without '@'"),
                Arguments.of("firstName", DataGenerator.getRandomStringByLength(1), "Very short name")
        );
    }

    @Test
    public void testEditJustCreatedTest() {
        Map<String, String> userData = DataGenerator.getRegistrationData();

        JsonPath responseCreateAuth = RestAssured
                .given()
                .body(userData)
                .post("https://playground.learnqa.ru/api/user/")
                .jsonPath();

        String userId = responseCreateAuth.getString("id");

        Map<String, String> authData = new HashMap<>();
        authData.put("email", userData.get("email"));
        authData.put("password", userData.get("password"));

        Response responseGetAuth = RestAssured
                .given()
                .body(authData)
                .post("https://playground.learnqa.ru/api/user/login/")
                .andReturn();

        String newName = "Changed Name";
        Map<String, String> editData = new HashMap<>();
        editData.put("firstName", newName);

        RestAssured.given()
                .header("x-csrf-token", this.getHeader(responseGetAuth, "x-csrf-token"))
                .cookie("auth_sid", this.getCookie(responseGetAuth, "auth_sid"))
                .body(editData)
                .put("https://playground.learnqa.ru/api/user/" + userId);

        Response responseUserData = RestAssured
                .given()
                .header("x-csrf-token", this.getHeader(responseGetAuth, "x-csrf-token"))
                .cookie("auth_sid", this.getCookie(responseGetAuth, "auth_sid"))
                .get("https://playground.learnqa.ru/api/user/" + userId)
                .andReturn();

        Assertions.assertJsonByName(responseUserData, "firstName", newName);
    }

    @Test
    public void testEditNotAuthUser() {
        //Create a new user
        Map<String, String> newUserData = DataGenerator.getRegistrationData();
        JsonPath responseCreateUser = apiCoreRequests.makePostRequestJsonPath(registerUrl, newUserData);
        String newUserId = responseCreateUser.get("id");

        //Edit user data
        String newName = "Changed Name";
        Map<String, String> editData = new HashMap<>();
        editData.put("firstName", newName);

        Response response = apiCoreRequests.makePutRequest(getUserDataUrl + newUserId, editData);

        Assertions.assertResponseCodeEquals(response, 400);
        Assertions.assertResponseTextEquals(response, "Auth token not supplied");
    }

    @Test
    public void testEditAnotherUser() {
        //Create a new user 1
        Map<String, String> newUser1Data = DataGenerator.getRegistrationData();
        apiCoreRequests.makePostRequest(registerUrl, newUser1Data);

        //Create a new user 2
        String newUser2Id = apiCoreRequests.createNewUserAndReturnId(registerUrl);

        //Login as user1 and get auth data
        Map<String, String> userCredentials = new HashMap<>();
        userCredentials.put("email", newUser1Data.get("email"));
        userCredentials.put("password", newUser1Data.get("password"));

        Response responseLoginUser = apiCoreRequests.makePostRequest(loginUrl, userCredentials);
        String cookie = this.getCookie(responseLoginUser, "auth_sid");
        String header = this.getHeader(responseLoginUser, "x-csrf-token");

        //Edit user2 info using user1 auth data
        String newName = "Changed Name";
        Map<String, String> editData = new HashMap<>();
        editData.put("firstName", newName);

        apiCoreRequests.makePutRequestAuth
                (getUserDataUrl + newUser2Id, editData, header, cookie);

        //Get user2 info
        Response responseGetEditedData = apiCoreRequests.makeGetRequest(getUserDataUrl + newUser2Id, header, cookie);

        Assertions.assertJsonHasField(responseGetEditedData, "username");
        Assertions.assertJsonHasNotField(responseGetEditedData, "firstName");
        Assertions.assertJsonHasNotField(responseGetEditedData, "lastName");
        Assertions.assertJsonHasNotField(responseGetEditedData, "email");
    }

    @ParameterizedTest(name = "{index} {2}")
    @MethodSource("testData")
    public void testSetInvalidDataAuthUser(String key, String value, String ignoredNameTest) {
        //Create a new user
        Map<String, String> newUserData = DataGenerator.getRegistrationData();
        JsonPath responseCreate = apiCoreRequests.makePostRequestJsonPath(registerUrl, newUserData);
        String userId = responseCreate.get("id");

        //Login as user and get auth data
        Map<String, String> userCredentials = new HashMap<>();
        userCredentials.put("email", newUserData.get("email"));
        userCredentials.put("password", newUserData.get("password"));

        Response responseLoginUser = apiCoreRequests.makePostRequest(loginUrl, userCredentials);
        String cookie = this.getCookie(responseLoginUser, "auth_sid");
        String header = this.getHeader(responseLoginUser, "x-csrf-token");

        //Edit user info, set invalid data
        String newValue = userCredentials.replace(key, value);
        Map<String, String> editData = new HashMap<>();
        editData.put(key, newValue);

        apiCoreRequests.makePutRequestAuth
                (getUserDataUrl + userId, editData, header, cookie);

        //Get user info
        Response responseGetEditedData = apiCoreRequests.makeGetRequest(getUserDataUrl + userId, header, cookie);

        Assertions.assertJsonByName(responseGetEditedData, key, newUserData.get(key));
    }

}
