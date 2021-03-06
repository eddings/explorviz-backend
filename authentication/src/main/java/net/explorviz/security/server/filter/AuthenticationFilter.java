package net.explorviz.security.server.filter;

import java.io.IOException;
import java.lang.reflect.Method;
import javax.annotation.Priority;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.Provider;
import net.explorviz.security.services.TokenService;
import net.explorviz.shared.security.AuthenticatedUserDetails;
import net.explorviz.shared.security.TokenBasedSecurityContext;
import net.explorviz.shared.security.annotations.Secured;
import net.explorviz.shared.security.model.TokenDetails;
import net.explorviz.shared.security.model.User;

/**
 * This filter is responsible for the authentication of the authentication web service. Depending on
 * the annotated resource class method, it checks if the requests contains a Authorization Header,
 * therefore requires authentication and validates the used token, e.g., for refreshment.
 */
@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthenticationFilter implements ContainerRequestFilter {

  private static final String FAILED_AUTH_MESSAGE = "Could not be authenticated";

  @Context
  private ResourceInfo resourceInfo;

  // @Inject
  // private UserService userService;

  @Inject
  private TokenService tokenService;

  @Override
  public void filter(final ContainerRequestContext requestContext) throws IOException {

    final Method method = this.resourceInfo.getResourceMethod();



    if (method.getName().equals("apply")) {
      // TODO where does the apply message come from?
      // It is only called, if the request is not issued with curl but the frontend
      return;
    }

    if (method.isAnnotationPresent(PermitAll.class)) {
      // nothing to do, resource class method requires no authentication
      return;
    }

    if (method.isAnnotationPresent(RolesAllowed.class)) {
      return;
    }

    if (method.isAnnotationPresent(Secured.class)) {
      final String authorizationHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
      if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
        final String authenticationToken = authorizationHeader.substring(7);
        this.handleTokenBasedAuthentication(authenticationToken, requestContext);
        return;
      }
    }

    // non-annotated classes cannot be accessed
    throw new ForbiddenException(FAILED_AUTH_MESSAGE);

  }

  private void handleTokenBasedAuthentication(final String authenticationToken,
      final ContainerRequestContext requestContext) {

    // prepare securityContext for usage in resource

    final TokenDetails tokenDetails = this.tokenService.parseToken(authenticationToken);

    // TODO real authentication with DB requests
    if (tokenDetails.getUsername().equals("admin")) {

      final User user = new User(tokenDetails.getUsername());

      final AuthenticatedUserDetails authenticatedUserDetails =
          new AuthenticatedUserDetails(user.getUsername(), user.getRoles());

      final boolean isSecure = requestContext.getSecurityContext().isSecure();
      final SecurityContext securityContext =
          new TokenBasedSecurityContext(authenticatedUserDetails, tokenDetails, isSecure);

      requestContext.setSecurityContext(securityContext);

      return;
    }

    throw new ForbiddenException(FAILED_AUTH_MESSAGE);
  }
}
