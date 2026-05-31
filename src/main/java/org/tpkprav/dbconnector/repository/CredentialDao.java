package org.tpkprav.dbconnector.repository;

import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

public interface CredentialDao {

    @SqlUpdate("INSERT INTO credential_records (nric, uuid) VALUES (:nric, :uuid)")
    void insert(@Bind("nric") String nric, @Bind("uuid") String uuid);
}