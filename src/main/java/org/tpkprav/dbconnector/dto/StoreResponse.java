package org.tpkprav.dbconnector.dto;

public record StoreResponse(String status, String message) {

    public static StoreResponse stored() {
        return new StoreResponse("stored", "Credential saved successfully");
    }
}