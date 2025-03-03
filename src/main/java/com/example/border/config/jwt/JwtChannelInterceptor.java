package com.example.border.config.jwt;

import com.example.border.exception.UnauthorizedAccessException;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

@Component
public class JwtChannelInterceptor implements ChannelInterceptor {

    Logger log = LoggerFactory.getLogger(JwtChannelInterceptor.class);
    private final JwtTokenUtil jwtTokenUtil;
    private final UserDetailsService userDetailsService;

    public JwtChannelInterceptor(JwtTokenUtil jwtTokenUtil, UserDetailsService userDetailsService) {
        this.jwtTokenUtil = jwtTokenUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public Message<?> preSend(@NonNull Message<?> message,@NonNull MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String token = accessor.getFirstNativeHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
                String email = jwtTokenUtil.getUsernameFromToken(token);
                if (email != null) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                    log.info("Validating token for user: {}", email);
                    boolean isValid = jwtTokenUtil.validateToken(token, userDetails);
                    log.info("Token valid: {}", isValid);
                    if (isValid) {
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(
                                        userDetails, null, userDetails.getAuthorities());
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        accessor.setUser(authentication);
                    } else {
                        throw new UnauthorizedAccessException("Invalid token");
                    }
                } else {
                    throw new UnauthorizedAccessException("Token doesn't contain a valid user");
                }
            }else {
                throw new UnauthorizedAccessException("Authorization token missing or invalid");
            }
        }
        return message;
    }
}
