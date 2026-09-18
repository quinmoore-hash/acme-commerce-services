package com.acme.inventory.repository;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;

import com.acme.inventory.domain.StockItem;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StockItemRepository extends JpaRepository<StockItem, Long> {

    Optional<StockItem> findBySku(String sku);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from StockItem s where s.sku = :sku")
    Optional<StockItem> findBySkuForUpdate(@Param("sku") String sku);

    @Query("select s from StockItem s where (s.quantityOnHand - s.quantityReserved) <= :threshold order by s.sku")
    List<StockItem> findLowStock(@Param("threshold") int threshold);
}
