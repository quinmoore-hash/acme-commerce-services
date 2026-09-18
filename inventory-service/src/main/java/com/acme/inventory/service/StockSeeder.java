package com.acme.inventory.service;

import java.math.BigDecimal;

import jakarta.annotation.PostConstruct;

import com.acme.inventory.domain.StockItem;
import com.acme.inventory.repository.StockItemRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
public class StockSeeder {

    private static final Logger log = LoggerFactory.getLogger(StockSeeder.class);

    private final StockItemRepository repository;

    public StockSeeder(StockItemRepository repository) {
        this.repository = repository;
    }

    @PostConstruct
    public void seed() {
        if (repository.count() > 0) {
            return;
        }
        repository.save(new StockItem("SKU-COFFEE-1KG", "Whole bean coffee 1kg", 120, new BigDecimal("18.50"), "USD"));
        repository.save(new StockItem("SKU-MUG-BLUE", "Ceramic mug (blue)", 40, new BigDecimal("7.99"), "USD"));
        repository.save(new StockItem("SKU-GRINDER-X", "Burr grinder", 8, new BigDecimal("129.00"), "USD"));
        log.info("Seeded {} stock items", repository.count());
    }
}
