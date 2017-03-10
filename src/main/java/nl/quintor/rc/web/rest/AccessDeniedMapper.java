package nl.quintor.rc.web.rest;

import org.springframework.security.access.AccessDeniedException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * AccessDeniedMapper is instantiated by Jersey directly through the "jersey.config.server.provider.packages" setting
 */
@Provider
public class AccessDeniedMapper implements ExceptionMapper<AccessDeniedException> {
    @Override
    public Response toResponse(AccessDeniedException e) {
        return Response.status(401)
                .build();
    }
}