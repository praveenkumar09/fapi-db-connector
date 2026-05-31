package org.tpkprav.dbconnector.filter;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import org.jboss.resteasy.reactive.server.ServerRequestFilter;
import org.jboss.resteasy.reactive.server.ServerResponseFilter;
import org.slf4j.MDC;
import org.tpkprav.dbconnector.RequestContext;
import org.tpkprav.dbconnector.logging.AsyncLogger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@ApplicationScoped
public class RequestFilter {

    private static final AsyncLogger log = AsyncLogger.of(RequestFilter.class);

    private static final String REQUEST_ID_HEADER = "X-Request-Id";
    private static final String MDC_REQUEST_ID = "requestId";

    @Inject
    RequestContext requestContext;

    @ServerRequestFilter(preMatching = true)
    public void onRequest(ContainerRequestContext request) throws IOException {
        String requestId = request.getHeaderString(REQUEST_ID_HEADER);
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString();
        }
        requestContext.setRequestId(requestId);
        MDC.put(MDC_REQUEST_ID, requestId);

        InputStream entityStream = request.getEntityStream();
        if (entityStream != null) {
            byte[] bodyBytes = entityStream.readAllBytes();
            request.setEntityStream(new ByteArrayInputStream(bodyBytes));
            String body = bodyBytes.length > 0 ? new String(bodyBytes, StandardCharsets.UTF_8) : "(empty)";
            log.debug("requestId={} --> {} {} body={}", requestId, request.getMethod(), request.getUriInfo().getRequestUri(), body);
        } else {
            log.debug("requestId={} --> {} {}", requestId, request.getMethod(), request.getUriInfo().getRequestUri());
        }
    }

    @ServerResponseFilter
    public void onResponse(ContainerRequestContext request, ContainerResponseContext response) {
        MDC.remove(MDC_REQUEST_ID);
    }
}