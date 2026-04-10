package com.group1.banking.repository;

import com.group1.banking.entity.Customer;
import com.group1.banking.enums.CustomerType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class CustomerRepositoryTest {

    @Autowired
    private CustomerRepository customerRepository;

    @Test
    @DisplayName("Should save and retrieve customer")
    void shouldSaveCustomer() {
        Customer customer = new Customer();
        customer.setName("Tarun");
        customer.setAddress("Toronto, ON");
        customer.setType(CustomerType.PERSON);

        Customer saved = customerRepository.save(customer);

        assertThat(saved.getCustomerId()).isNotNull();

        Customer found = customerRepository.findById(saved.getCustomerId()).orElse(null);

        assertThat(found).isNotNull();
        assertThat(found.getName()).isEqualTo("Tarun");
    }

    @Test
    @DisplayName("Should return all customers")
    void shouldFindAllCustomers() {
        Customer c1 = new Customer();
        c1.setName("User1");
        c1.setAddress("Address1");
        c1.setType(CustomerType.PERSON);

        Customer c2 = new Customer();
        c2.setName("User2");
        c2.setAddress("Address2");
        c2.setType(CustomerType.COMPANY);

        customerRepository.saveAll(List.of(c1, c2));

        List<Customer> customers = customerRepository.findAll();

        assertThat(customers).hasSize(2);
    }

    @Test
    @DisplayName("Should delete customer")
    void shouldDeleteCustomer() {
        Customer customer = new Customer();
        customer.setName("DeleteMe");
        customer.setAddress("Somewhere");
        customer.setType(CustomerType.PERSON);

        Customer saved = customerRepository.save(customer);

        customerRepository.deleteById(saved.getCustomerId());

        boolean exists = customerRepository.findById(saved.getCustomerId()).isPresent();

        assertThat(exists).isFalse();
    }
}