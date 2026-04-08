package com.fdm.banking.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.List;

/**
 * Authentication token for JWT-authenticated users.
 */
public class JwtAuthenticationToken extends AbstractAuthenticationToken {

    private final UserPrincipal principal;

    public JwtAuthenticationToken(UserPrincipal principal) {
        super(List.of(new SimpleGrantedAuthority("ROLE_" + principal.getRole())));
        this.principal = principal;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() { return null; }

    @Override
    public UserPrincipal getPrincipal() { return principal; }
}
