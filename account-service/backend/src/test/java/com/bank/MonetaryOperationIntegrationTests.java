package com.bank;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bank.model.Account;
import com.bank.model.AccountStatus;
import com.bank.model.AccountType;
import com.bank.model.Customer;
import com.bank.model.CustomerType;
import com.bank.model.Transaction;
import com.bank.model.TransactionDirection;
import com.bank.model.TransactionStatus;
import com.bank.repository.AccountRepository;
import com.bank.repository.CustomerRepository;
import com.bank.repository.IdempotencyRecordRepository;
import com.bank.repository.TransactionRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class MonetaryOperationIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private IdempotencyRecordRepository idempotencyRecordRepository;

    @BeforeEach
    void setUp() {
        idempotencyRecordRepository.deleteAll();
        transactionRepository.deleteAll();
        accountRepository.deleteAll();
        customerRepository.deleteAll();
    }

    @Test
    void deposit_replays_original_success_for_same_caller() throws Exception {
        createCustomer(100L);
        createAccount(1000L, 100L, new BigDecimal("100.00"), AccountType.CHECKING);

        MvcResult firstResult = mockMvc.perform(post("/accounts/1000/deposit")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer demo-token")
                        .header("X-User-Id", "customer-100-user")
                        .header("X-Roles", "CUSTOMER")
                        .header("X-Customer-Id", "100")
                        .header("Idempotency-Key", "deposit-1000-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "amount": 25.00,
                                  "description": "Payroll"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Deposit completed successfully"))
                .andReturn();

        MvcResult replayResult = mockMvc.perform(post("/accounts/1000/deposit")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer demo-token")
                        .header("X-User-Id", "customer-100-user")
                        .header("X-Roles", "CUSTOMER")
                        .header("X-Customer-Id", "100")
                        .header("Idempotency-Key", "deposit-1000-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "amount": 25.00,
                                  "description": "Payroll"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode firstBody = objectMapper.readTree(firstResult.getResponse().getContentAsString());
        JsonNode replayBody = objectMapper.readTree(replayResult.getResponse().getContentAsString());

        assertThat(replayBody).isEqualTo(firstBody);
        assertThat(loadAccount(1000L).getBalance()).isEqualByComparingTo("125.00");
        assertThat(transactionRepository.findAll()).hasSize(1);
        assertThat(idempotencyRecordRepository.findAll()).hasSize(1);
    }

    @Test
    void create_checking_account_initializes_next_check_number_automatically() throws Exception {
        createCustomer(100L);

        mockMvc.perform(post("/customers/100/accounts")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer demo-token")
                        .header("X-User-Id", "customer-100-user")
                        .header("X-Roles", "CUSTOMER")
                        .header("X-Customer-Id", "100")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "accountType": "CHECKING",
                                  "balance": 10.00
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accountType").value("CHECKING"));

        mockMvc.perform(post("/customers/100/accounts")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer demo-token")
                        .header("X-User-Id", "customer-100-user")
                        .header("X-Roles", "CUSTOMER")
                        .header("X-Customer-Id", "100")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "accountType": "CHECKING",
                                  "balance": 10.00,
                                  "interestRate": 0.0500
                                }
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.field").value("interestRate"));
    }

    @Test
    void withdraw_insufficient_funds_persists_failed_transaction_once() throws Exception {
        createCustomer(100L);
        createAccount(1001L, 100L, new BigDecimal("50.00"), AccountType.CHECKING);

        mockMvc.perform(post("/accounts/1001/withdraw")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer demo-token")
                        .header("X-User-Id", "customer-100-user")
                        .header("X-Roles", "CUSTOMER")
                        .header("X-Customer-Id", "100")
                        .header("Idempotency-Key", "withdraw-1001-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "amount": 75.00,
                                  "description": "ATM"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("INSUFFICIENT_FUNDS"));

        mockMvc.perform(post("/accounts/1001/withdraw")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer demo-token")
                        .header("X-User-Id", "customer-100-user")
                        .header("X-Roles", "CUSTOMER")
                        .header("X-Customer-Id", "100")
                        .header("Idempotency-Key", "withdraw-1001-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "amount": 75.00,
                                  "description": "ATM"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("INSUFFICIENT_FUNDS"));

        assertThat(loadAccount(1001L).getBalance()).isEqualByComparingTo("50.00");
        assertThat(idempotencyRecordRepository.findAll()).hasSize(1);
        assertThat(transactionRepository.findAll()).hasSize(1);

        Transaction transaction = transactionRepository.findAll().getFirst();
        assertThat(transaction.getStatus()).isEqualTo(TransactionStatus.FAILED);
        assertThat(transaction.getDirection()).isEqualTo(TransactionDirection.DEBIT);
    }

    @Test
    void transfer_allows_customer_owned_source_and_replays_without_duplicate_mutation() throws Exception {
        createCustomer(100L);
        createCustomer(200L);
        createAccount(1100L, 100L, new BigDecimal("200.00"), AccountType.CHECKING);
        createAccount(2200L, 200L, new BigDecimal("50.00"), AccountType.SAVINGS);

        mockMvc.perform(post("/accounts/transfer")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer demo-token")
                        .header("X-User-Id", "customer-100-user")
                        .header("X-Roles", "CUSTOMER")
                        .header("X-Customer-Id", "100")
                        .header("Idempotency-Key", "transfer-1100-2200-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fromAccountId": 1100,
                                  "toAccountId": 2200,
                                  "amount": 75.00,
                                  "description": "Rent share"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Transfer completed successfully"));

        mockMvc.perform(post("/accounts/transfer")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer demo-token")
                        .header("X-User-Id", "customer-100-user")
                        .header("X-Roles", "CUSTOMER")
                        .header("X-Customer-Id", "100")
                        .header("Idempotency-Key", "transfer-1100-2200-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fromAccountId": 1100,
                                  "toAccountId": 2200,
                                  "amount": 75.00,
                                  "description": "Rent share"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Transfer completed successfully"));

        assertThat(loadAccount(1100L).getBalance()).isEqualByComparingTo("125.00");
        assertThat(loadAccount(2200L).getBalance()).isEqualByComparingTo("125.00");
        assertThat(transactionRepository.findAll()).hasSize(2);
        assertThat(idempotencyRecordRepository.findAll()).hasSize(1);
    }

    @Test
    void transfer_rejects_customer_who_does_not_own_source_account() throws Exception {
        createCustomer(100L);
        createCustomer(200L);
        createAccount(1200L, 200L, new BigDecimal("90.00"), AccountType.CHECKING);
        createAccount(1300L, 100L, new BigDecimal("10.00"), AccountType.CHECKING);

        mockMvc.perform(post("/accounts/transfer")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer demo-token")
                        .header("X-User-Id", "customer-100-user")
                        .header("X-Roles", "CUSTOMER")
                        .header("X-Customer-Id", "100")
                        .header("Idempotency-Key", "transfer-unauthorized-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fromAccountId": 1200,
                                  "toAccountId": 1300,
                                  "amount": 5.00,
                                  "description": "Unauthorized"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));

        assertThat(loadAccount(1200L).getBalance()).isEqualByComparingTo("90.00");
        assertThat(loadAccount(1300L).getBalance()).isEqualByComparingTo("10.00");
        assertThat(transactionRepository.findAll()).isEmpty();
        assertThat(idempotencyRecordRepository.findAll()).hasSize(1);
    }

    private void createCustomer(Long customerId) {
        Customer customer = new Customer();
        customer.setCustomerId(customerId);
        customer.setName("Customer " + customerId);
        customer.setAddress("123 Test Street");
        customer.setType(CustomerType.PERSON);
        customerRepository.save(customer);
    }

    private void createAccount(Long accountId, Long customerId, BigDecimal balance, AccountType accountType) {
        Account account = new Account();
        account.setAccountId(accountId);
        account.setCustomer(customerRepository.findById(customerId).orElseThrow());
        account.setAccountType(accountType);
        account.setStatus(AccountStatus.ACTIVE);
        account.setBalance(balance);
        if (accountType == AccountType.SAVINGS) {
            account.setInterestRate(new BigDecimal("0.0500"));
        }
        accountRepository.save(account);
    }

    private Account loadAccount(Long accountId) {
        return accountRepository.findById(accountId).orElseThrow();
    }
}