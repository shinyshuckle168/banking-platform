package com.bank.config;

import com.bank.security.HeaderAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.access.expression.WebExpressionAuthorizationManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, HeaderAuthenticationFilter headerAuthenticationFilter)
            throws Exception {
        http.csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, ex) -> response.sendError(HttpServletResponse.SC_UNAUTHORIZED))
                        .accessDeniedHandler((request, response, ex) -> response.sendError(HttpServletResponse.SC_UNAUTHORIZED)))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/customers/*").hasAuthority("CUSTOMER:DELETE")
                        .requestMatchers(HttpMethod.POST, "/customers/*/accounts").hasAuthority("ACCOUNT:CREATE")
                        .requestMatchers(HttpMethod.POST, "/accounts/*/deposit").hasAuthority("TRANSACTION:DEPOSIT")
                        .requestMatchers(HttpMethod.POST, "/accounts/*/withdraw").hasAuthority("TRANSACTION:WITHDRAW")
                        .requestMatchers(HttpMethod.POST, "/accounts/transfer").hasAuthority("TRANSACTION:TRANSFER")
                        .requestMatchers(HttpMethod.PUT, "/accounts/*").hasAuthority("ACCOUNT:UPDATE")
                        .requestMatchers(HttpMethod.DELETE, "/accounts/*").hasAuthority("ACCOUNT:DELETE")
                        .requestMatchers(HttpMethod.GET, "/accounts/*").hasAuthority("ACCOUNT:READ")
                        .requestMatchers(HttpMethod.GET, "/customers/*/accounts")
                        .access(new WebExpressionAuthorizationManager("hasAuthority('CUSTOMER:READ') and hasAuthority('ACCOUNT:READ')"))
                        .anyRequest().authenticated())
                .addFilterBefore(headerAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
