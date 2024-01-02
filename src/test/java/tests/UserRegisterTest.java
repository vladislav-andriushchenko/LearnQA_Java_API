package tests;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import lib.Assertions;
import lib.BaseTestCase;
import lib.DataGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashMap;
import java.util.Map;

@Epic("Registration cases")
@Feature("Register user")
public class UserRegisterTest extends BaseTestCase {

    String url = "https://playground.learnqa.ru/api/user/";

    @Test
    @Description("This test checks the ability to register a user with existing email")
    @DisplayName("Test negative register user with existing email")
    public void testCreateUserWithExistingEmail() {
        String email = "vinkotov@example.com";

        Map<String, String> userData = new HashMap<>();
        userData.put("email", email);
        userData = DataGenerator.getRegistrationData(userData);

        Response responseCreateAuth = RestAssured
                .given()
                .body(userData)
                .post(url)
                .andReturn();

        Assertions.assertResponseCodeEquals(responseCreateAuth, 400);
        Assertions.assertResponseTextEquals(responseCreateAuth, "Users with email '" + email + "' already exists");
    }

    @Test
    @Description("This test successfully register a user")
    @DisplayName("Test positive register user")
    public void testCreateUserSuccessfully() {
        Map<String, String> userData = DataGenerator.getRegistrationData();

        Response responseCreateAuth = RestAssured
                .given()
                .body(userData)
                .post(url)
                .andReturn();

        Assertions.assertResponseCodeEquals(responseCreateAuth, 200);
        Assertions.assertJsonHasField(responseCreateAuth, "id");
    }

    @Test
    @Description("This test checks the ability to register a user with incorrect email")
    @DisplayName("Test negative register user with incorrect email")
    public void testCreateUserWithInvalidEmail() {
        String email = "vinkotovexample.com";

        Map<String, String> userData = new HashMap<>();
        userData.put("email", email);
        userData = DataGenerator.getRegistrationData(userData);

        Response responseCreateAuth = RestAssured
                .given()
                .body(userData)
                .post(url)
                .andReturn();

        Assertions.assertResponseCodeEquals(responseCreateAuth, 400);
    }

    @Description("This test checks the ability to register a user without required field")
    @DisplayName("Test negative register user without required field")
    @ParameterizedTest
    @ValueSource(strings = {"username", "firstName", "lastName", "email", "password"})
    public void testCreateUserWithoutMandatoryFields(String key) {
        Map<String, String> userData = DataGenerator.getRegistrationData();

        userData.remove(key);

        Response responseCreateAuth = RestAssured
                .given()
                .body(userData)
                .post(url)
                .andReturn();

        Assertions.assertResponseCodeEquals(responseCreateAuth, 400);
        Assertions.assertResponseTextEquals(responseCreateAuth,
                "The following required params are missed: " + key);
    }

    @Test
    @Description("This test checks the ability to register a user with short name")
    @DisplayName("Test negative register user with short name")
    public void testCreateUserWithShortName() {
        Map<String, String> userData = new HashMap<>();
        userData.put("firstName", "A");
        userData = DataGenerator.getRegistrationData(userData);

        Response responseCreateAuth = RestAssured
                .given()
                .body(userData)
                .post(url)
                .andReturn();

        Assertions.assertResponseCodeEquals(responseCreateAuth, 400);
        Assertions.assertResponseTextEquals(responseCreateAuth, "The value of 'firstName' field is too short");
    }

    @Test
    @Description("This test checks the ability to register a user with long name")
    @DisplayName("Test negative register user with long name")
    public void testCreateUserWithLongName() {
        Map<String, String> userData = new HashMap<>();
        userData.put("firstName", DataGenerator.getRandomStringByLength(251));
        userData = DataGenerator.getRegistrationData(userData);

        Response responseCreateAuth = RestAssured
                .given()
                .body(userData)
                .post(url)
                .andReturn();

        Assertions.assertResponseCodeEquals(responseCreateAuth, 400);
        Assertions.assertResponseTextEquals(responseCreateAuth, "The value of 'firstName' field is too long");
    }
}
