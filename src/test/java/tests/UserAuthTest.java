package tests;

import io.qameta.allure.*;
import io.restassured.response.Response;
import lib.ApiCoreRequests;
import lib.Assertions;
import lib.BaseTestCase;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashMap;
import java.util.Map;

@Epic("Authorization cases")
@Feature("Authorization")
public class  UserAuthTest extends BaseTestCase {
    String cookie;
    String header;
    int userIdOnAuth;

    private final ApiCoreRequests apiCoreRequests = new ApiCoreRequests();

    @BeforeEach
    public void loginUser() {
        String url = "https://playground.learnqa.ru/api/user/login";
        Map<String, String> authData = new HashMap<>();
        authData.put("email", "vinkotov@example.com");
        authData.put("password", "1234");

        Response responseGetAuth = apiCoreRequests.makePostRequest(url, authData);

        this.cookie = this.getCookie(responseGetAuth, "auth_sid");
        this.header = this.getHeader(responseGetAuth, "x-csrf-token");
        this.userIdOnAuth = responseGetAuth.jsonPath().getInt("user_id");
    }

    @Test
    @Tags({@Tag("Regression"), @Tag("Authorization")})
    @Severity(SeverityLevel.CRITICAL)
    @Description("This test successfully authorize user by email and password")
    @DisplayName("Test positive auth user")
    public void testAuthUser() {
        String url = "https://playground.learnqa.ru/api/user/auth";

        Response responseCheckAuth = apiCoreRequests.makeGetRequest(url, this.header, this.cookie);

        Assertions.assertJsonByName(responseCheckAuth, "user_id", this.userIdOnAuth);
    }

    @Description("This test checks authorization status w/o sending auth cookie or token")
    @DisplayName("Test negative auth user")
    @Severity(SeverityLevel.CRITICAL)
    @Tags({@Tag("Regression"), @Tag("Authorization")})
    @ParameterizedTest
    @ValueSource(strings = {"cookie", "headers"})
    public void testNegativeAuthUser(String condition) {
        String url = "https://playground.learnqa.ru/api/user/auth";

        if (condition.equals("cookie")) {
            Response responseForCheck = apiCoreRequests.makeGetRequestWithCookie(url, this.cookie);
            Assertions.assertJsonByName(responseForCheck, "user_id", 0);
        } else if (condition.equals("headers")) {
            Response responseForCheck = apiCoreRequests.makeGetRequestWithToken(url, this.header);
            Assertions.assertJsonByName(responseForCheck, "user_id", 0);
        } else {
            throw new IllegalArgumentException("Condition value is known: " + condition);
        }

    }
}
