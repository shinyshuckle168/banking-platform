package com.fdm.banking.security;

import java.util.Collection;
import java.util.List;

/**
 * JWT-authenticated user principal (Group 2 compatibility).
 */
public class UserPrincipal {

    private final Long userId;
    private final String username;
    private final String role;
    private final Collection<String> permissions;
    private final Long customerId;

    public UserPrincipal(Long userId, String username, String role,
                         Collection<String> permissions, Long customerId) {
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.permissions = permissions;
        this.customerId = customerId;
    }

    public Long getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getRole() { return role; }
    public Collection<String> getPermissions() { return permissions; }
    public Long getCustomerId() { return customerId; }

    public boolean hasPermission(String permission) {
        return permissions != null && permissions.contains(permission);
    }

    public boolean isAdmin() { return "ADMIN".equals(role); }
    public boolean isCustomer() { return "CUSTOMER".equals(role); }
}
