package com.coursemanagementsystem.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http,
//                                           CustomUserDetailsService userDetailsService) throws Exception {
//
//        http
//                .userDetailsService(userDetailsService)
//                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers("/", "/register", "/courses/**","/admin/**").permitAll()
////                        .requestMatchers("/admin/**").hasRole("ADMIN")
//                        .requestMatchers("/my-courses").hasRole("STUDENT")
//                        .anyRequest().authenticated()
//                )
//                .formLogin(form -> form
//                        .loginPage("/auth/login")
//                        .defaultSuccessUrl("/courses", true)
//                )
//                .logout(logout -> logout.logoutSuccessUrl("/courses"));
//
//        return http.build();
//    }
//
//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder();
//    }
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
            .csrf(csrf -> csrf.disable()) // tránh lỗi khi submit form
            .authorizeHttpRequests(auth -> auth
                    .anyRequest().permitAll() // ✅ cho phép tất cả
            )
            .formLogin(form -> form.disable()) // ❌ tắt login luôn
            .logout(logout -> logout.disable());

    return http.build();
}
}
