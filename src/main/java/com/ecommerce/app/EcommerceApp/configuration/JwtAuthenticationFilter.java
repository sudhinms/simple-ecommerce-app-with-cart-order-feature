package com.ecommerce.app.EcommerceApp.configuration;

import com.ecommerce.app.EcommerceApp.repositories.ExpiredTokenRepository;
import com.ecommerce.app.EcommerceApp.securityServices.UserDetailServiceImpl;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.nio.file.AccessDeniedException;

@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    @Autowired
    private UserDetailServiceImpl userDetailService;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private ExpiredTokenRepository tokenRepository;
    private final HandlerExceptionResolver exceptionResolver;

    @Autowired
    public JwtAuthenticationFilter(HandlerExceptionResolver exceptionResolver) {
        this.exceptionResolver = exceptionResolver;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        String token = null;
        String username = null;
        try {
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
                username = jwtService.extractUsernameFromToken(token);
                if(tokenRepository.findByToken(token).isPresent()){
                    throw new AccessDeniedException("Access denied. Try login");
                }
            }

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailService.loadUserByUsername(username);
                if (!jwtService.validateToken(token, userDetails)) {
                    throw new JwtException("Invalid Token........");
                } else {
                    UsernamePasswordAuthenticationToken authenticationToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                }

            }

            filterChain.doFilter(request, response);
        }
        catch (Exception e){
            exceptionResolver.resolveException(request,response,null,e);
        }
    }
}
