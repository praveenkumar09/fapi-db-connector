package org.tpkprav.dbconnector;

import io.quarkus.test.junit.QuarkusTestProfile;

import java.util.Map;

public class KarateTestProfile implements QuarkusTestProfile {

    @Override
    public Map<String, String> getConfigOverrides() {
        return Map.of("quarkus.http.test-port", "9092");
    }
}