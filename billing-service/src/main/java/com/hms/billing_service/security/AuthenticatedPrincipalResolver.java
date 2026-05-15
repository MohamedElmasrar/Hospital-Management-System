package com.hms.billing_service.security;

import com.hms.billing_service.enums.UserRole;
import java.util.UUID;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

public class AuthenticatedPrincipalResolver implements HandlerMethodArgumentResolver {

  @Override
  public boolean supportsParameter(MethodParameter parameter) {
    return parameter.getParameterType().equals(AuthenticatedPrincipal.class);
  }

  @Override
  public AuthenticatedPrincipal resolveArgument(
      MethodParameter parameter,
      ModelAndViewContainer mavContainer,
      NativeWebRequest webRequest,
      WebDataBinderFactory binderFactory) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (!(authentication instanceof JwtAuthenticationToken token)) {
      return null;
    }
    Jwt jwt = token.getToken();
    String subject = jwt.getSubject();
    if (subject == null) {
      return null;
    }
    UUID userId = UUID.fromString(subject);
    String roleClaim = jwt.getClaimAsString("role");
    UserRole userRole = roleClaim != null ? UserRole.valueOf(roleClaim) : null;
    return new AuthenticatedPrincipal(userId, userRole);
  }
}
