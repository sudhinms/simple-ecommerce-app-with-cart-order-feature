package com.ecommerce.app.EcommerceApp.repositories;

import com.ecommerce.app.EcommerceApp.entities.OrderPrimaryKey;
import com.ecommerce.app.EcommerceApp.entities.Orders;
import com.ecommerce.app.EcommerceApp.services.OrderIdGeneratingService;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Orders,Long> {
    List<Orders> findByUserInfoId(Long userId);

    @Modifying
    @Query("DELETE FROM Orders o WHERE o.status = :orderStatus AND o.orderDateTime <= :expirationTime")
    void deleteByOrderStatusAndOrderedTimeBefore(@Param("orderStatus") String orderStatus, @Param("expirationTime") LocalDateTime expirationTime);

    List<Orders> findByUserInfoIdAndStatus(long userId, String orderStatus);

    @Query("SELECT o FROM Orders o WHERE EXTRACT(MONTH FROM o.orderDateTime) BETWEEN :startMonth AND :endMonth")
    List<Orders> findByOrderDateTimeBetweenMonths(@Param("startMonth") int startMonth, @Param("endMonth") int endMonth);

    @Query("SELECT MAX(o.id.orderId) FROM Orders o" )
    Long findMaxId();

    @Query ("select case when count (o)> 0 then true else false end from Orders o where o.orderKeyId.orderId = :orderId")
    boolean existsOrderByOrderId(@Param ("orderId") long orderId);
    Optional<Orders> findByOrderKeyId(OrderPrimaryKey orderPrimaryKey);

    void deleteByUserInfoId(long id);

    List<Orders> findByAddressId(long addressId);

    List<Orders> findByUserInfoIdAndPaymentStatus(long id, String status);
}
