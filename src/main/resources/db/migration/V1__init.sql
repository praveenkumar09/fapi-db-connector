CREATE TABLE IF NOT EXISTS credential_records (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    nric       VARCHAR(9)  NOT NULL,
    uuid       VARCHAR(36) NOT NULL,
    created_at TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_credential_uuid UNIQUE (uuid)
);