package lib;

import io.qameta.allure.Step;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.http.Header;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;

public class ApiCoreRequests {
    @Step("Make a GET-request with token and auth cookie")
    public Response makeGetRequest(String url, String token, String cookie) {
        return given()
                .filter(new AllureRestAssured())
                .header(new Header("x-csrf-token", token))
                .cookie("auth_sid", cookie)
                .get(url)
                .andReturn();
    }

    @Step("Make a GET-request with auth cookie only")
    public Response makeGetRequestWithCookie(String url, String cookie) {
        return given()
                .filter(new AllureRestAssured())
                .cookie("auth_sid", cookie)
                .get(url)
                .andReturn();
    }

    @Step("Make a GET-request with token only")
    public Response makeGetRequestWithToken(String url, String token) {
        return given()
                .filter(new AllureRestAssured())
                .header(new Header("x-csrf-token", token))
                .get(url)
                .andReturn();
    }

    @Step("Make a POST-request")
    public Response makePostRequest(String url, Map<String, String> authData) {
        return given()
                .filter(new AllureRestAssured())
                .body(authData)
                .post(url)
                .andReturn();
    }

    @Step("Make a POST-request JsonPath")
    public JsonPath makePostRequestJsonPath(String url, Map<String, String> authData) {
        return given()
                .filter(new AllureRestAssured())
                .body(authData)
                .post(url)
                .jsonPath();
    }

    @Step("Make a PUT-request")
    public Response makePutRequest(String url, Map<String, String> authData) {
        return given()
                .filter(new AllureRestAssured())
                .body(authData)
                .put(url)
                .andReturn();
    }

    @Step("Make a PUT-request with auth data")
    public void makePutRequestAuth(String url, Map<String, String> editData, String header, String cookie) {
        given()
                .filter(new AllureRestAssured())
                .header("x-csrf-token", header)
                .cookie("auth_sid", cookie)
                .body(editData)
                .put(url)
                .andReturn();
    }

    @Step("Create a new user and return id")
    public String createNewUserAndReturnId(String url) {
        Map<String, String> authData = DataGenerator.getRegistrationData();

        JsonPath response = given()
                .filter(new AllureRestAssured())
                .body(authData)
                .post(url)
                .jsonPath();

        return response.get("id");
    }

    @Step("Return user's auth data")
    public Map<String, String> returnAuthDataLoggedUser(String url, Map<String, String> credential) {
        Response response = given()
                .filter(new AllureRestAssured())
                .body(credential)
                .post(url)
                .andReturn();

        String header = response.getHeader("x-csrf-token");
        String cookie = response.getCookie("auth_sid");

        Map<String, String> authData = new HashMap<>();
        authData.put("header", header);
        authData.put("cookie", cookie);

        return authData;
    }
}
