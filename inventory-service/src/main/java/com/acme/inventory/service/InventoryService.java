package com.acme.inventory.service;

import java.math.BigDecimal;
import java.util.List;

import com.acme.common.error.ResourceNotFoundException;
import com.acme.inventory.domain.StockItem;
import com.acme.inventory.repository.StockItemRepository;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryService {

    private final StockItemRepository repository;

    public InventoryService(StockItemRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<StockItem> listAll() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "stock", key = "#sku")
    public StockItem getBySku(String sku) {
        return repository.findBySku(sku).orElseThrow(() -> new ResourceNotFoundException("StockItem", sku));
    }

    @Transactional(readOnly = true)
    public List<StockItem> lowStock(int threshold) {
        return repository.findLowStock(threshold);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    @CacheEvict(cacheNames = "stock", key = "#sku")
    public StockItem reserve(String sku, int quantity) {
        StockItem item = repository.findBySkuForUpdate(sku)
                .orElseThrow(() -> new ResourceNotFoundException("StockItem", sku));
        item.reserve(quantity);
        return repository.save(item);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    @CacheEvict(cacheNames = "stock", key = "#sku")
    public StockItem release(String sku, int quantity) {
        StockItem item = repository.findBySkuForUpdate(sku)
                .orElseThrow(() -> new ResourceNotFoundException("StockItem", sku));
        item.release(quantity);
        return repository.save(item);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    @CacheEvict(cacheNames = "stock", key = "#sku")
    public StockItem restock(String sku, String productName, int quantity, BigDecimal unitPrice, String currency) {
        StockItem item = repository.findBySkuForUpdate(sku)
                .orElseGet(() -> new StockItem(sku, productName, 0, unitPrice, currency));
        item.restock(quantity);
        return repository.save(item);
    }
}
