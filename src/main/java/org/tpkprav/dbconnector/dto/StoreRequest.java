package org.tpkprav.dbconnector.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.tpkprav.dbconnector.validation.SafeSqlInput;

public record StoreRequest(

        @NotBlank
        @Pattern(
                regexp = "^[A-Za-z0-9]{1,20}$",
                message = "nric must contain only alphanumeric characters (max 20)"
        )
        @SafeSqlInput
        String nric,

        @NotBlank
        @Pattern(
                regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$",
                message = "uuid must be a valid UUID format (xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx)"
        )
        @SafeSqlInput
        String uuid

) {}