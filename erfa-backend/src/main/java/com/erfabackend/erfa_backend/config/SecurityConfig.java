package com.erfabackend.erfa_backend.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.erfabackend.erfa_backend.service.AppUserDetailsService;
import com.erfabackend.erfa_backend.util.JwtService;

@Configuration
public class SecurityConfig {
    @Autowired
    private JwtService jwtService;
    @Autowired
    private AppUserDetailsService appUserDetailsService;
    @Value("${security.permit-all:false}")
    private boolean permitAll;
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        if (permitAll) {
            http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(reg -> reg.anyRequest().permitAll())
                .formLogin(AbstractHttpConfigurer::disable);
        } else {
            http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(reg -> reg
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                    .requestMatchers(
                        "/api/auth/signup",
                        "/api/auth/login",
                        "/api/auth/verify",
                        "/api/auth/refresh",
                        "/api/auth/resend-verification",
                        "/api/auth/test-email"
                    ).permitAll()
                    .requestMatchers("/api/artists/search").permitAll()
                    .anyRequest().authenticated()
                )
                .formLogin(AbstractHttpConfigurer::disable);

            // JWT filter - construct with UserDetailsService so authorities come from DB
            http.addFilterBefore(new com.erfabackend.erfa_backend.config.JwtAuthenticationFilter(jwtService, appUserDetailsService), UsernamePasswordAuthenticationFilter.class);
        }
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // More flexible origin patterns for development
        configuration.setAllowedOriginPatterns(List.of(
            "http://localhost:*",    // React dev server (usually 3000, 3001, 5173, etc.)
            "http://127.0.0.1:*",   // Alternative localhost
            "https://localhost:*",  // HTTPS localhost
            "https://127.0.0.1:*"   // HTTPS alternative
        ));
        
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L); // Cache preflight for 1 hour
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
