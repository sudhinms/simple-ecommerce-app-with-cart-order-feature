package com.ecommerce.app.EcommerceApp.configuration;

import com.ecommerce.app.EcommerceApp.repositories.ExpiredTokenRepository;
import com.ecommerce.app.EcommerceApp.repositories.OrderRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DbCleaner {
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private ExpiredTokenRepository tokenRepository;

    @Scheduled(cron = "0 0 */3 * * ?")
    @Transactional
    public void deleteExpiredOrders() {
        LocalDateTime expirationTime = LocalDateTime.now().minusHours(3);
        orderRepository.deleteByOrderStatusAndOrderedTimeBefore("NOT_PAYED", expirationTime);
    }

    @Scheduled(cron = "0 0 */3 * * ?")
    @Transactional
    public void deleteExpiredToken() {
        LocalDateTime expirationTime = LocalDateTime.now().minusHours(3);
        tokenRepository.deleteByExpirationTime(expirationTime);
    }
}
