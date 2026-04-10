package com.fdm.banking.config;

import com.fdm.banking.repository.UserRepository;
import com.fdm.banking.security.JwtAuthenticationFilter;
import com.fdm.banking.security.ServiceApiKeyFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Security configuration with two security filter chains. (T016)
 *
 * Group 2 import paths documented here per T006:
 *   - com.fdm.banking.security.JwtAuthenticationFilter (Group 2 JWT filter)
 *   - com.fdm.banking.entity.UserEntity (Group 2 user entity)
 *   - com.fdm.banking.entity.RoleEntity (Group 2 role entity)
 *   - com.fdm.banking.entity.CustomerEntity (Group 2 customer entity)
 *   - com.fdm.banking.entity.AccountEntity (Group 2 account entity)
 *   - com.fdm.banking.entity.TransactionEntity (Group 2 transaction entity)
 *   - com.fdm.banking.repository.UserRepository (Group 2 user repository)
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${banking.security.jwt.secret}")
    private String jwtSecret;

    private final UserRepository userRepository;
    private final NotificationsProperties notificationsProperties;

    public SecurityConfig(UserRepository userRepository, NotificationsProperties notificationsProperties) {
        this.userRepository = userRepository;
        this.notificationsProperties = notificationsProperties;
    }

    /**
     * Order 1: Notification endpoints — validated by ServiceApiKeyFilter.
     */
    @Bean
    @Order(1)
    public SecurityFilterChain notificationFilterChain(HttpSecurity http) throws Exception {
        http.securityMatcher("/notifications/**")
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(new ServiceApiKeyFilter(notificationsProperties.getAllowedServiceIds()),
                        UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(authz -> authz.anyRequest().authenticated());
        return http.build();
    }

    /**
     * Order 2: All other endpoints — JWT authenticated.
     */
    @Bean
    @Order(2)
    public SecurityFilterChain jwtFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(new JwtAuthenticationFilter(jwtSecret, userRepository),
                        UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/h2-console/**").permitAll()
                        .anyRequest().authenticated())
                .headers(headers -> headers.frameOptions(fo -> fo.disable())); // H2 console
        return http.build();
    }
}
