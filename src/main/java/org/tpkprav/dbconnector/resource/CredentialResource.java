package org.tpkprav.dbconnector.resource;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.UnableToExecuteStatementException;
import org.jboss.logging.Logger;
import org.tpkprav.dbconnector.dto.StoreRequest;
import org.tpkprav.dbconnector.dto.StoreResponse;
import org.tpkprav.dbconnector.repository.CredentialDao;

import java.sql.SQLIntegrityConstraintViolationException;

@Path("/v1/credentials")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CredentialResource {

    private static final Logger log = Logger.getLogger(CredentialResource.class);

    @Inject
    Jdbi jdbi;

    @POST
    public Response store(@Valid StoreRequest request) {
        log.debugf("Storing credential nric=%s uuid=%s", maskNric(request.nric()), request.uuid());
        try {
            jdbi.useExtension(CredentialDao.class, dao -> dao.insert(request.nric(), request.uuid()));
            log.infof("Credential stored nric=%s uuid=%s", maskNric(request.nric()), request.uuid());
            return Response.status(Response.Status.CREATED).entity(StoreResponse.stored()).build();
        } catch (UnableToExecuteStatementException e) {
            if (isDuplicateKey(e)) {
                log.warnf("Duplicate uuid=%s rejected", request.uuid());
                return Response.status(Response.Status.CONFLICT)
                        .entity(new StoreResponse("conflict", "UUID already registered"))
                        .build();
            }
            log.errorf(e, "Failed to store credential uuid=%s", request.uuid());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new StoreResponse("error", "Failed to store credential"))
                    .build();
        }
    }

    private static boolean isDuplicateKey(UnableToExecuteStatementException e) {
        Throwable cause = e.getCause();
        while (cause != null) {
            if (cause instanceof SQLIntegrityConstraintViolationException sqlEx) {
                return sqlEx.getMessage() != null && sqlEx.getMessage().contains("Duplicate entry");
            }
            cause = cause.getCause();
        }
        return false;
    }

    private static String maskNric(String nric) {
        if (nric == null || nric.length() < 4) return "****";
        return "*".repeat(nric.length() - 4) + nric.substring(nric.length() - 4);
    }
}