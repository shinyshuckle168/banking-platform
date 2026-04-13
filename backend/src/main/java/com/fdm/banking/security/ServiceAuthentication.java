package com.fdm.banking.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.List;

/**
 * Authentication token for validated service API key callers.
 */
public class ServiceAuthentication extends AbstractAuthenticationToken {

    private final String serviceId;

    public ServiceAuthentication(String serviceId) {
        super(List.of(new SimpleGrantedAuthority("ROLE_SERVICE")));
        this.serviceId = serviceId;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() { return null; }

    @Override
    public Object getPrincipal() { return serviceId; }
}
