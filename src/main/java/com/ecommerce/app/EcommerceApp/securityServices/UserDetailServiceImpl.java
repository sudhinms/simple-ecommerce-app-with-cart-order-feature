package com.ecommerce.app.EcommerceApp.securityServices;

import com.ecommerce.app.EcommerceApp.entities.UserInfo;
import com.ecommerce.app.EcommerceApp.repositories.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class UserDetailServiceImpl implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserInfo userInfo= userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("user with username : " + email + " not found"));
        return new CustomUserDetails(userInfo);
    }
}
