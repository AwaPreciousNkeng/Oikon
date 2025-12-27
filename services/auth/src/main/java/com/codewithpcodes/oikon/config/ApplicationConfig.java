package com.codewithpcodes.oikon.config;

import com.codewithpcodes.oikon.auditing.ApplicationAuditAware;
import com.codewithpcodes.oikon.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

@Configuration
@RequiredArgsConstructor
public class ApplicationConfig {

    private final UserRepository repository;

    /**
     * Defines the strategy for retrieving user details from the database.
     * Overrides the default in-memory user details service.
     *
     * @return UserDetailsService implementation.
     */
    @Bean
    public UserDetailsService userDetailsService() {
        // Use a lambda to fetch the user by email (username) from the repository
        return username -> repository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    @Bean
    public AuthenticationProvider authenticationProvider(
            UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder
    ) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }


    /**
     * Defines the AuditorAware for Spring Data JPA Auditing.
     * This is used to automatically populate the 'created by' and 'last modified by' fields.
     *
     * @return AuditorAware implementation (ApplicationAuditAware).
     */
    @Bean
    public AuditorAware<UUID> auditorAware() {
        return new ApplicationAuditAware();
    }

    /**
     * Exposes the AuthenticationManager bean, which is crucial for performing authentication,
     * typically used in the AuthenticationController (login/register).
     *
     * @param config The AuthenticationConfiguration injected by Spring.
     * @return The AuthenticationManager.
     * @throws Exception if configuration fails.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Defines the PasswordEncoder bean, using BCrypt, the recommended hashing algorithm.
     *
     * @return BCryptPasswordEncoder instance.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
