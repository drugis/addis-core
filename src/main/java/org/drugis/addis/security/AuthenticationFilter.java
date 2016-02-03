package org.drugis.addis.security;

import org.apache.jena.ext.com.google.common.base.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by connor on 9/10/15.
 */
public class AuthenticationFilter extends GenericFilterBean {

  private final static Logger logger = LoggerFactory.getLogger(AuthenticationFilter.class);
  private AuthenticationManager authenticationManager;
  private AccessDeniedHandler accessDeniedHandler = new AccessDeniedHandlerImpl();

  public AuthenticationFilter(AuthenticationManager authenticationManager) {
    this.authenticationManager = authenticationManager;
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    HttpServletRequest httpRequest = asHttp(request);
    HttpServletResponse httpResponse = asHttp(response);

    Optional<String> applicationKey = Optional.fromNullable(httpRequest.getHeader("X-Auth-Application-Key"));

    try {
      if (applicationKey.isPresent()) {
        logger.debug("Trying to authenticate user by X-Application-Key method. ApplicationKey: {}", applicationKey);
        processApplicationKeyAuthentication(applicationKey);
      }
      logger.debug("AuthenticationFilter is passing request down the filter chain");
      chain.doFilter(request, response);
    } catch (InternalAuthenticationServiceException internalAuthenticationServiceException) {
      SecurityContextHolder.clearContext();
      logger.error("Internal authentication service exception", internalAuthenticationServiceException);
      httpResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    } catch (AuthenticationException authenticationException) {
      SecurityContextHolder.clearContext();
      httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, authenticationException.getMessage());
      accessDeniedHandler.handle(httpRequest, httpResponse, new AccessDeniedException(authenticationException.getMessage()));
    }
  }

  private HttpServletRequest asHttp(ServletRequest request) {
    return (HttpServletRequest) request;
  }

  private HttpServletResponse asHttp(ServletResponse response) {
    return (HttpServletResponse) response;
  }

  private void processApplicationKeyAuthentication(Optional<String> applicationKey) {
    Authentication resultOfAuthentication = tryToAuthenticateWithApplicationKey(applicationKey);
    SecurityContextHolder.getContext().setAuthentication(resultOfAuthentication);
  }

  private Authentication tryToAuthenticateWithApplicationKey(Optional<String> applicationKey) {
    PreAuthenticatedAuthenticationToken requestAuthentication = new PreAuthenticatedAuthenticationToken(applicationKey, null);
    return tryToAuthenticate(requestAuthentication);
  }

  private Authentication tryToAuthenticate(Authentication requestAuthentication) {
    Authentication responseAuthentication = authenticationManager.authenticate(requestAuthentication);
    if (responseAuthentication == null || !responseAuthentication.isAuthenticated()) {
      throw new InternalAuthenticationServiceException("Unable to authenticate Domain User for provided credentials");
    }
    logger.debug("User successfully authenticated");
    return responseAuthentication;
  }
}

