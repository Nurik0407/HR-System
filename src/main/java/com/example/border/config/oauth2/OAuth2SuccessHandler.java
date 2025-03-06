package com.example.border.config.oauth2;

import com.example.border.config.helper.CustomUserDetailsService;
import com.example.border.config.jwt.JwtTokenUtil;
import com.example.border.model.dto.auth.AuthResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private static final Logger logger = LoggerFactory.getLogger(OAuth2SuccessHandler.class);

    private final JwtTokenUtil jwtTokenUtil;
    private final CustomUserDetailsService customUserDetailsService;
    private final ObjectMapper objectMapper;

    public OAuth2SuccessHandler(JwtTokenUtil jwtTokenUtil, CustomUserDetailsService customUserDetailsService, ObjectMapper objectMapper) {
        this.jwtTokenUtil = jwtTokenUtil;
        this.customUserDetailsService = customUserDetailsService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        if (authentication.getPrincipal() instanceof OAuth2User oAuth2User) {
            try {
                String email = oAuth2User.getAttribute("email");
                if (email == null) {
                    logger.error("Email not found in OAuth2User attributes.");
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().write("{\"error\": \"Email attribute missing\"}");
                    return;
                }

                UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);
                String accessToken = jwtTokenUtil.generateToken(userDetails);

                response.setContentType("application/json");
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write(objectMapper.writeValueAsString(new AuthResponse(accessToken)));

                logger.info("Authentication successful for user: {}", email);

            } catch (Exception e) {
                logger.error("Error processing authentication success", e);
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("{\"error\": \"Internal Server Error\"}");
            }
        } else {
            logger.error("Authentication principal is not of type OAuth2User");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\": \"Unauthorized\"}");
        }
    }
}
