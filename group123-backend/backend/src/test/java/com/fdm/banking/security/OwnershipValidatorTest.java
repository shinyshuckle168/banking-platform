//package com.fdm.banking.security;
//
//import com.fdm.banking.entity.Account;
//import com.fdm.banking.exception.OwnershipException;
//import com.fdm.banking.repository.AccountRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.util.List;
//import java.util.Optional;
//
//import static org.assertj.core.api.Assertions.assertThatThrownBy;
//import static org.assertj.core.api.Assertions.assertThatCode;
//import static org.mockito.Mockito.when;
//
///**
// * Unit tests for OwnershipValidator. (T023)
// */
//@ExtendWith(MockitoExtension.class)
//class OwnershipValidatorTest {
//
//    @Mock
//    private AccountRepository accountRepository;
//
//    @InjectMocks
//    private OwnershipValidator ownershipValidator;
//
//    private Account account;
//
//    @BeforeEach
//    void setUp() {
//        account = new Account();
//        account.setAccountId(1L);
//    }
//
//    @Test
//    void admin_bypassesOwnershipCheck() {
//        UserPrincipal admin = new UserPrincipal("99", "admin", List.of("ADMIN"), List.of(), 999L);
//        assertThatCode(() -> ownershipValidator.assertOwnership(1L, admin)).doesNotThrowAnyException();
//    }
//
//    @Test
//    void customer_ownsAccount_passes() {
//        com.fdm.banking.entity.Customer customer = new com.fdm.banking.entity.Customer();
//        customer.setCustomerId(42L);
//        account.setCustomer(customer);
//        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
//
//        UserPrincipal owner = new UserPrincipal("10", "user", List.of("CUSTOMER"), List.of(), 42L);
//        assertThatCode(() -> ownershipValidator.assertOwnership(1L, owner)).doesNotThrowAnyException();
//    }
//
//    @Test
//    void customer_doesNotOwnAccount_throwsOwnershipException() {
//        com.fdm.banking.entity.Customer customer = new com.fdm.banking.entity.Customer();
//        customer.setCustomerId(42L);
//        account.setCustomer(customer);
//        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
//
//        UserPrincipal other = new UserPrincipal("10", "user", List.of("CUSTOMER"), List.of(), 99L);
//        assertThatThrownBy(() -> ownershipValidator.assertOwnership(1L, other))
//                .isInstanceOf(OwnershipException.class);
//    }
//
//    @Test
//    void accountNotFound_throwsOwnershipException() {
//        when(accountRepository.findById(999L)).thenReturn(Optional.empty());
//
//        UserPrincipal caller = new UserPrincipal("10", "user", List.of("CUSTOMER"), List.of(), 5L);
//        assertThatThrownBy(() -> ownershipValidator.assertOwnership(999L, caller))
//                .isInstanceOf(OwnershipException.class);
//    }
//}
