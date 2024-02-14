package com.ecommerce.app.EcommerceApp.configuration;

import com.ecommerce.app.EcommerceApp.securityServices.UserDetailServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfiguration {

    @Autowired
    @Qualifier("handlerExceptionResolver")
    private HandlerExceptionResolver exceptionResolver;

    @Bean
    public UserDetailsService userDetailsService() {
        return new UserDetailServiceImpl();
    }

    @Bean
    public JwtAuthenticationFilter authFilter(){
        return new JwtAuthenticationFilter(exceptionResolver);
    }
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity security) throws Exception {
        security
                .csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(customizer->
                        customizer.requestMatchers("/app/register","/app/register-admin","/app/login","/app/home/product/view-one/**"
                                        ,"/app/home/product/view/**","/app/home/category/**","/app/home/product/view-by-category/**","/actuator/mappings")
                                        .permitAll()
                                .requestMatchers("/app/admin/**").hasAuthority("ROLE_ADMIN")
                                .requestMatchers("/app/home/cart/**","/app/home/order/**"
                                        ,"/app/home/order/confirm-address/**","/app/home/cart/add/**")
                                        .hasAuthority("ROLE_USER")
                                .requestMatchers("/app/home/user/**","/app/home/category/**","/app/logout").hasAnyAuthority("ROLE_USER","ROLE_ADMIN"))
                .sessionManagement(customizer->
                        customizer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(authFilter(), UsernamePasswordAuthenticationFilter.class);
        return security.build();
    }


    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailsService());
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        return authenticationProvider;
    }
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

}
