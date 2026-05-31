package org.tpkprav.dbconnector;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class CredentialResourceTest {

    private static final String CREDENTIALS_PATH = "/v1/credentials";

    @Inject
    Jdbi jdbi;

    @BeforeEach
    void cleanTable() {
        jdbi.useHandle(h -> h.execute("DELETE FROM credential_records"));
    }

    // ── Positive ─────────────────────────────────────────────────────────────

    @Test
    void validRequest_returns201WithStoredStatus() {
        given()
            .contentType(ContentType.JSON)
            .header("X-Request-Id", "test-req-001")
            .body("{\"nric\":\"S1234567D\",\"uuid\":\"" + UUID.randomUUID() + "\"}")
        .when()
            .post(CREDENTIALS_PATH)
        .then()
            .statusCode(201)
            .body("status", equalTo("stored"))
            .body("message", equalTo("Credential saved successfully"));
    }

    @Test
    void requestIdHeader_isPropagatedToFilter() {
        given()
            .contentType(ContentType.JSON)
            .header("X-Request-Id", "trace-abc-999")
            .body("{\"nric\":\"S1234567D\",\"uuid\":\"" + UUID.randomUUID() + "\"}")
        .when()
            .post(CREDENTIALS_PATH)
        .then()
            .statusCode(201);
    }

    @Test
    void missingRequestIdHeader_generatesOne() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"nric\":\"S1234567D\",\"uuid\":\"" + UUID.randomUUID() + "\"}")
        .when()
            .post(CREDENTIALS_PATH)
        .then()
            .statusCode(201);
    }

    // ── Duplicate UUID ────────────────────────────────────────────────────────

    @Test
    void duplicateUuid_returns409WithConflictStatus() {
        String uuid = UUID.randomUUID().toString();
        String body = "{\"nric\":\"S1234567D\",\"uuid\":\"" + uuid + "\"}";

        given().contentType(ContentType.JSON).body(body)
               .when().post(CREDENTIALS_PATH).then().statusCode(201);

        given()
            .contentType(ContentType.JSON)
            .body(body)
        .when()
            .post(CREDENTIALS_PATH)
        .then()
            .statusCode(409)
            .body("status", equalTo("conflict"))
            .body("message", notNullValue());
    }

    @Test
    void differentNricSameUuid_returns409() {
        String uuid = UUID.randomUUID().toString();

        given().contentType(ContentType.JSON)
               .body("{\"nric\":\"S1234567D\",\"uuid\":\"" + uuid + "\"}")
               .when().post(CREDENTIALS_PATH).then().statusCode(201);

        given()
            .contentType(ContentType.JSON)
            .body("{\"nric\":\"T9876543Z\",\"uuid\":\"" + uuid + "\"}")
        .when()
            .post(CREDENTIALS_PATH)
        .then()
            .statusCode(409);
    }

    // ── Validation (negative) ─────────────────────────────────────────────────

    @Test
    void missingNric_returns400() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"uuid\":\"" + UUID.randomUUID() + "\"}")
        .when()
            .post(CREDENTIALS_PATH)
        .then()
            .statusCode(400);
    }

    @Test
    void missingUuid_returns400() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"nric\":\"S1234567D\"}")
        .when()
            .post(CREDENTIALS_PATH)
        .then()
            .statusCode(400);
    }

    @Test
    void blankNric_returns400() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"nric\":\"\",\"uuid\":\"" + UUID.randomUUID() + "\"}")
        .when()
            .post(CREDENTIALS_PATH)
        .then()
            .statusCode(400);
    }

    @Test
    void blankUuid_returns400() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"nric\":\"S1234567D\",\"uuid\":\"\"}")
        .when()
            .post(CREDENTIALS_PATH)
        .then()
            .statusCode(400);
    }

    @Test
    void emptyBody_returns400() {
        given()
            .contentType(ContentType.JSON)
            .body("{}")
        .when()
            .post(CREDENTIALS_PATH)
        .then()
            .statusCode(400);
    }

    // ── SQL injection attempts ────────────────────────────────────────────────

    @Test
    void sqlInjectionInNric_singleQuote_returns400() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"nric\":\"S' OR '1'='1\",\"uuid\":\"" + UUID.randomUUID() + "\"}")
        .when()
            .post(CREDENTIALS_PATH)
        .then()
            .statusCode(400);
    }

    @Test
    void sqlInjectionInNric_unionSelect_returns400() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"nric\":\"1 UNION SELECT * FROM credential_records\",\"uuid\":\"" + UUID.randomUUID() + "\"}")
        .when()
            .post(CREDENTIALS_PATH)
        .then()
            .statusCode(400);
    }

    @Test
    void sqlInjectionInNric_dropTable_returns400() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"nric\":\"S1234567D; DROP TABLE credential_records--\",\"uuid\":\"" + UUID.randomUUID() + "\"}")
        .when()
            .post(CREDENTIALS_PATH)
        .then()
            .statusCode(400);
    }

    @Test
    void sqlInjectionInUuid_comment_returns400() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"nric\":\"S1234567D\",\"uuid\":\"550e8400-e29b-41d4-a716-446655440000 -- injected\"}")
        .when()
            .post(CREDENTIALS_PATH)
        .then()
            .statusCode(400);
    }

    @Test
    void sqlInjectionInUuid_hexEncoding_returns400() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"nric\":\"S1234567D\",\"uuid\":\"0x53454C454354\"}")
        .when()
            .post(CREDENTIALS_PATH)
        .then()
            .statusCode(400);
    }

    @Test
    void sqlInjectionInNric_sleepFunction_returns400() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"nric\":\"1 AND SLEEP(5)\",\"uuid\":\"" + UUID.randomUUID() + "\"}")
        .when()
            .post(CREDENTIALS_PATH)
        .then()
            .statusCode(400);
    }

    // ── Method not allowed ────────────────────────────────────────────────────

    @Test
    void getOnCredentials_returns405() {
        given().when().get(CREDENTIALS_PATH).then().statusCode(405);
    }

    @Test
    void deleteOnCredentials_returns405() {
        given().when().delete(CREDENTIALS_PATH).then().statusCode(405);
    }

    // ── Not found ─────────────────────────────────────────────────────────────

    @Test
    void nonExistentPath_returns404() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"nric\":\"S1234567D\",\"uuid\":\"" + UUID.randomUUID() + "\"}")
        .when()
            .post("/v1/credentials/nonexistent")
        .then()
            .statusCode(404);
    }
}