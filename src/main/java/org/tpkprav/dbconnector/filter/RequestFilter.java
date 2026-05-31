package org.tpkprav.dbconnector.filter;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import org.jboss.resteasy.reactive.server.ServerRequestFilter;
import org.jboss.resteasy.reactive.server.ServerResponseFilter;
import org.slf4j.MDC;
import org.tpkprav.dbconnector.RequestContext;

import java.util.UUID;

@ApplicationScoped
public class RequestFilter {

    private static final String REQUEST_ID_HEADER = "X-Request-Id";
    private static final String MDC_REQUEST_ID = "requestId";

    @Inject
    RequestContext requestContext;

    @ServerRequestFilter(preMatching = true)
    public void onRequest(ContainerRequestContext request) {
        String requestId = request.getHeaderString(REQUEST_ID_HEADER);
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString();
        }
        requestContext.setRequestId(requestId);
        MDC.put(MDC_REQUEST_ID, requestId);
    }

    @ServerResponseFilter
    public void onResponse(ContainerRequestContext request, ContainerResponseContext response) {
        MDC.remove(MDC_REQUEST_ID);
    }
}