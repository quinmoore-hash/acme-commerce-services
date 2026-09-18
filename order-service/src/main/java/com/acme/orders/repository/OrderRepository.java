package com.acme.orders.repository;

import java.util.List;

import com.acme.orders.domain.Order;
import com.acme.orders.domain.OrderStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByCustomerEmailIgnoreCaseOrderByCreatedAtDesc(String customerEmail);

    List<Order> findByStatus(OrderStatus status);

    @Query("select count(o) from Order o where o.customerEmail = :email and o.status <> com.acme.orders.domain.OrderStatus.CANCELLED")
    long countActiveOrdersFor(@Param("email") String email);
}
