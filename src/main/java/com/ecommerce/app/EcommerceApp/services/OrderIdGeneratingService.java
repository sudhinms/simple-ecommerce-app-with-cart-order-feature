package com.ecommerce.app.EcommerceApp.services;

import com.ecommerce.app.EcommerceApp.repositories.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderIdGeneratingService {

    @Autowired
    private OrderRepository orderRepository;

    public long getNextAvailableId(){
        Long maxId=orderRepository.findMaxId();
        if(maxId == null){
            return 1;
        }
        long nextId=maxId+1;
        while (orderRepository.existsOrderByOrderId(nextId))
        {
            nextId++;
        }
        return  nextId;
    }
}
