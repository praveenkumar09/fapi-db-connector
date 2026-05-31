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
import org.tpkprav.dbconnector.RequestContext;
import org.tpkprav.dbconnector.dto.StoreRequest;
import org.tpkprav.dbconnector.dto.StoreResponse;
import org.tpkprav.dbconnector.logging.AsyncLogger;
import org.tpkprav.dbconnector.repository.CredentialDao;

import java.sql.SQLIntegrityConstraintViolationException;

@Path("/v1/credentials")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CredentialResource {

    private static final AsyncLogger log = AsyncLogger.of(CredentialResource.class);

    @Inject
    Jdbi jdbi;

    @Inject
    RequestContext requestContext;

    @POST
    public Response store(@Valid StoreRequest request) {
        String reqId = requestContext.getRequestId();
        log.debug("requestId={} Storing credential nric={} uuid={}", reqId, maskNric(request.nric()), request.uuid());
        try {
            jdbi.useExtension(CredentialDao.class, dao -> dao.insert(request.nric(), request.uuid()));
            log.info("requestId={} Credential stored nric={} uuid={}", reqId, maskNric(request.nric()), request.uuid());
            return Response.status(Response.Status.CREATED).entity(StoreResponse.stored()).build();
        } catch (UnableToExecuteStatementException e) {
            if (isDuplicateKey(e)) {
                log.warn("requestId={} Duplicate uuid={} rejected", reqId, request.uuid());
                return Response.status(Response.Status.CONFLICT)
                        .entity(new StoreResponse("conflict", "UUID already registered"))
                        .build();
            }
            log.error("requestId={} Failed to store credential uuid={}", reqId, request.uuid(), e);
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