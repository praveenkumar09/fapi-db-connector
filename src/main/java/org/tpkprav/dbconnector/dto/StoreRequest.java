package org.tpkprav.dbconnector.dto;

import jakarta.validation.constraints.NotBlank;

public record StoreRequest(
        @NotBlank String nric,
        @NotBlank String uuid
) {}