package com.bank.service;

import com.bank.exception.UnauthorizedException;
import com.bank.model.Account;
import com.bank.security.AuthenticatedUser;
import org.springframework.stereotype.Service;

@Service
public class AuthorizationService {

    public void assertCanAccessCustomer(AuthenticatedUser user, Long customerId) {
        if (user == null) {
            throw new UnauthorizedException("UNAUTHORIZED", "Unauthorized");
        }
        if (user.isAdmin()) {
            return;
        }
        if (user.customerId() == null || !user.customerId().equals(customerId)) {
            throw new UnauthorizedException("UNAUTHORIZED", "Unauthorized");
        }
    }

    public void assertCanAccessAccount(AuthenticatedUser user, Account account) {
        assertCanAccessCustomer(user, account.getCustomer().getCustomerId());
    }
}
