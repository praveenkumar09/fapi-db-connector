package org.tpkprav.dbconnector;

import com.intuit.karate.Results;
import com.intuit.karate.Runner;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@QuarkusTest
@TestProfile(KarateTestProfile.class)
class CredentialApiKarateTest {

    @Inject
    Jdbi jdbi;

    @Test
    void runKarateFeatures() {
        jdbi.useHandle(h -> h.execute("DELETE FROM credential_records"));

        System.setProperty("quarkus.http.test-port",
                String.valueOf(io.restassured.RestAssured.port));

        Results results = Runner
                .path("classpath:karate")
                .outputCucumberJson(true)
                .parallel(1);

        Assertions.assertEquals(0, results.getFailCount(), results.getErrorMessages());
    }
}