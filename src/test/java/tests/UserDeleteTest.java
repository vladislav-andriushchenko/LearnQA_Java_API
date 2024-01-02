package tests;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
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

@Epic("Deletion cases")
@Feature("Deletion")
public class UserDeleteTest extends BaseTestCase {
    String baseUrl = "https://playground.learnqa.ru/api/user/";
    String loginUrl = "https://playground.learnqa.ru/api/user/login";
    String registerUrl = "https://playground.learnqa.ru/api/user/";
    private final ApiCoreRequests apiCoreRequests = new ApiCoreRequests();


    @Test
    @Description("This test checks the ability to delete admin user")
    @DisplayName("Test negative deletion user")
    public void testDeleteAdmin() {
        Map<String, String> userCredentials = new HashMap<>();
        userCredentials.put("email", "vinkotov@example.com");
        userCredentials.put("password", "1234");

        Map<String, String> authData = apiCoreRequests.returnAuthDataLoggedUser(loginUrl, userCredentials);
        String header = authData.get("header");
        String cookie = authData.get("cookie");

        Response response = apiCoreRequests.deleteUserById(baseUrl, header, cookie, "2");

        Assertions.assertResponseCodeEquals(response, 400);
        Assertions.assertResponseTextEquals(response, "Please, do not delete test users with ID 1, 2, 3, 4 or 5.");
    }

    @Test
    @Description("This test successfully delete authorized user")
    @DisplayName("Test positive deletion auth user")
    public void testDeleteUserSuccessfully() {
        //Create a user
        Map<String, String> newUserData = DataGenerator.getRegistrationData();
        JsonPath responseCreateUser = apiCoreRequests.makePostRequestJsonPath(registerUrl, newUserData);
        String newUserId = responseCreateUser.get("id");

        //Get a user's auth data
        Map<String, String> authData = apiCoreRequests.returnAuthDataLoggedUser(loginUrl, newUserData);
        String header = authData.get("header");
        String cookie = authData.get("cookie");

        //Delete a user
        Response deleteUserResponse = apiCoreRequests.deleteUserById(baseUrl, header, cookie, newUserId);

        Assertions.assertResponseCodeEquals(deleteUserResponse, 200);

        //Get deleted user info
        Response responseUserInfo = apiCoreRequests.getUserInfoById(baseUrl, header, cookie, newUserId);

        Assertions.assertResponseCodeEquals(responseUserInfo, 404);
        Assertions.assertResponseTextEquals(responseUserInfo, "User not found");
    }

    @Test
    @Description("This test checks the ability to delete some user by another user")
    @DisplayName("Test negative deletion some user by another user")
    public void testDeleteAnotherUserByLoggedUser() {
        //Create a user 1
        Map<String, String> user1Data = DataGenerator.getRegistrationData();
        apiCoreRequests.makePostRequest(registerUrl, user1Data);

        //Create a user 2
        String user2Id = apiCoreRequests.createNewUserAndReturnId(registerUrl);

        //Get user 1 auth data
        Map<String, String> authData = apiCoreRequests.returnAuthDataLoggedUser(loginUrl, user1Data);
        String header = authData.get("header");
        String cookie = authData.get("cookie");

        //Delete a user2 by user1
        apiCoreRequests.deleteUserById(baseUrl, header, cookie, user2Id);

        Response responseUserInfo = apiCoreRequests.getUserInfoById(baseUrl, header, cookie, user2Id);

        Assertions.assertResponseCodeEquals(responseUserInfo, 200);
        Assertions.assertJsonHasField(responseUserInfo, "username");
    }
}
