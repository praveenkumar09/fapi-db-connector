package org.tpkprav.dbconnector.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class SafeSqlInputValidator implements ConstraintValidator<SafeSqlInput, String> {

    // Matches SQL delimiters, comment sequences, null bytes, hex encoding,
    // and SQL keywords that are used in injection attacks.
    private static final Pattern SQL_INJECTION = Pattern.compile(
        "(?i)(?:" +
        // SQL string/identifier delimiters and statement terminator
        "['\";]|" +
        // Single-line comment sequences
        "--|#|" +
        // Block comment sequences
        "/\\*|\\*/|" +
        // Null bytes (literal and escaped forms)
        "\\x00|\\\\0|%00|" +
        // Hex-encoded payloads
        "0x[0-9a-fA-F]+|" +
        // DML / DDL keywords
        "\\b(?:SELECT|INSERT|UPDATE|DELETE|DROP|TRUNCATE|ALTER|CREATE|REPLACE)\\b|" +
        // Query-manipulation keywords
        "\\b(?:UNION|JOIN|WHERE|HAVING|GROUP\\s+BY|ORDER\\s+BY)\\b|" +
        // Execution keywords
        "\\b(?:EXEC(?:UTE)?|CALL|DECLARE|SET\\s+@)\\b|" +
        // Type-conversion / encoding functions used in blind injection
        "\\b(?:CAST|CONVERT|CHAR|NCHAR|ASCII|ORD|CHR)\\s*\\(|" +
        // Time-based blind injection functions
        "\\b(?:SLEEP|WAITFOR\\s+DELAY|BENCHMARK|PG_SLEEP)\\s*\\(|" +
        // File read/write (MySQL / MSSQL)
        "\\b(?:LOAD_FILE|INTO\\s+OUTFILE|INTO\\s+DUMPFILE)\\b|" +
        // Extended stored procedures (MSSQL)
        "\\bXP_" +
        ")"
    );

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true; // @NotBlank handles null/blank separately
        }
        return !SQL_INJECTION.matcher(value).find();
    }
}