package com.coursemanagementsystem.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;


@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .userDetailsService(userDetailsService)

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/about", "/contact", "/auth/login", "/auth/register").permitAll()
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/img/**", "/fonts/**", "/uploads/**").permitAll()

                        .requestMatchers("/courses/my-courses").authenticated()
                        .requestMatchers(HttpMethod.GET, "/courses", "/courses/", "/courses/*", "/courses/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/courses/*/reviews", "/enrollments/**", "/lessons/*/complete").authenticated()

                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/lessons/**").authenticated()
                        .requestMatchers("/profile/**").authenticated()
                        .anyRequest().authenticated()
                )

                .sessionManagement(session -> session
                        .invalidSessionUrl("/auth/login?expired=true")
                )

                .formLogin(form -> form
                        .loginPage("/auth/login")
                        .loginProcessingUrl("/auth/login")
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .successHandler(authenticationSuccessHandler())
                        .failureHandler(authenticationFailureHandler())
                        .permitAll()
                )

                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/auth/login?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                );

        return http.build();
    }

    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        return new AuthenticationSuccessHandler() {
            @Override
            public void onAuthenticationSuccess(HttpServletRequest request,
                                                HttpServletResponse response,
                                                Authentication authentication) throws IOException, ServletException {
                boolean isAdmin = authentication.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .anyMatch("ROLE_ADMIN"::equals);

                if (isAdmin) {
                    response.sendRedirect("/admin/course-list");
                } else {
                    response.sendRedirect("/");
                }
            }
        };
    }

    @Bean
    public AuthenticationFailureHandler authenticationFailureHandler() {
        return (request, response, exception) -> {
            if (exception instanceof DisabledException) {
                response.sendRedirect("/auth/login?deleted=true");
            } else {
                response.sendRedirect("/auth/login?error=true");
            }
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}