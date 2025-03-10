package com.example.border.config;

import com.example.border.config.jwt.JwtRequestFilter;
import com.example.border.config.oauth2.CustomOAuth2UserService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtRequestFilter jwtRequestFilter;
    private final AuthenticationSuccessHandler authenticationSuccessHandler;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final String[] AUTH_WHITE_LIST = {
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/api/v1/auth/**",
            "/oauth/**",
            "/login/oauth2/code/google",
            "/api/v1/auth/select-role",

            "/api/v1/vacancies/**",
            "/ws/**",
            "/api/v1/auth/reset-password/**",

            "/css/**","/js/**","/images/**"
    };

    public SecurityConfig(JwtRequestFilter jwtRequestFilter,
                          AuthenticationSuccessHandler authenticationSuccessHandler,
                          @Lazy CustomOAuth2UserService customOAuth2UserService) {
        this.jwtRequestFilter = jwtRequestFilter;
        this.authenticationSuccessHandler = authenticationSuccessHandler;
        this.customOAuth2UserService = customOAuth2UserService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(AUTH_WHITE_LIST).permitAll()
                        .requestMatchers("/api/v1/applicant").hasAnyAuthority("ADMIN", "APPLICANT")
                        .requestMatchers("/api/v1/vacancy/applications").hasAnyAuthority("ADMIN", "APPLICANT")

                        .requestMatchers("/api/v1/favorites").hasAnyAuthority("ADMIN", "EMPLOYER")
                        .requestMatchers("/api/v1/employers/**").hasAnyAuthority("ADMIN", "EMPLOYER")

                        .anyRequest().authenticated())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                        )
                        .successHandler(authenticationSuccessHandler)
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .permitAll())
                .exceptionHandling(e -> e
                        .defaultAuthenticationEntryPointFor(((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json");
                            response.setCharacterEncoding("UTF-8");
                            response.getWriter().write("{\"message\": \"Unauthorized: Доступ запрещён\"}");
                        }), new AntPathRequestMatcher("/api/**")))
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManagerBean(AuthenticationConfiguration configuration)
            throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
