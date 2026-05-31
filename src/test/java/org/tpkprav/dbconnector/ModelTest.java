package org.tpkprav.dbconnector;

import org.junit.jupiter.api.Test;
import org.tpkprav.dbconnector.dto.StoreRequest;
import org.tpkprav.dbconnector.dto.StoreResponse;

import static org.junit.jupiter.api.Assertions.*;

class ModelTest {

    // ── StoreRequest ─────────────────────────────────────────────────────────

    @Test
    void storeRequest_recordAccessors() {
        StoreRequest req = new StoreRequest("S1234567D", "some-uuid");
        assertEquals("S1234567D", req.nric());
        assertEquals("some-uuid", req.uuid());
    }

    @Test
    void storeRequest_equality() {
        StoreRequest a = new StoreRequest("S1234567D", "u1");
        StoreRequest b = new StoreRequest("S1234567D", "u1");
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void storeRequest_inequality_differentNric() {
        assertNotEquals(new StoreRequest("S1234567D", "u1"), new StoreRequest("T9876543Z", "u1"));
    }

    @Test
    void storeRequest_inequality_differentUuid() {
        assertNotEquals(new StoreRequest("S1234567D", "u1"), new StoreRequest("S1234567D", "u2"));
    }

    @Test
    void storeRequest_toString_containsFieldValues() {
        String s = new StoreRequest("S1234567D", "my-uuid").toString();
        assertTrue(s.contains("S1234567D"));
        assertTrue(s.contains("my-uuid"));
    }

    // ── StoreResponse ─────────────────────────────────────────────────────────

    @Test
    void storeResponse_stored_factoryReturnsCorrectValues() {
        StoreResponse resp = StoreResponse.stored();
        assertEquals("stored", resp.status());
        assertEquals("Credential saved successfully", resp.message());
    }

    @Test
    void storeResponse_recordAccessors() {
        StoreResponse resp = new StoreResponse("conflict", "UUID already registered");
        assertEquals("conflict", resp.status());
        assertEquals("UUID already registered", resp.message());
    }

    @Test
    void storeResponse_equality() {
        assertEquals(new StoreResponse("stored", "ok"), new StoreResponse("stored", "ok"));
    }

    @Test
    void storeResponse_inequality_differentStatus() {
        assertNotEquals(new StoreResponse("stored", "ok"), new StoreResponse("error", "ok"));
    }

    @Test
    void storeResponse_stored_notNull() {
        assertNotNull(StoreResponse.stored());
    }
}