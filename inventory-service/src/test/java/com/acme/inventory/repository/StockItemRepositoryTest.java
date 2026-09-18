package com.acme.inventory.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import com.acme.inventory.domain.StockItem;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class StockItemRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private StockItemRepository repository;

    @Test
    void findsLowStockItems() {
        entityManager.persist(new StockItem("SKU-LOW", "Low", 3, new BigDecimal("1.00"), "USD"));
        StockItem reserved = new StockItem("SKU-RESERVED", "Reserved", 10, new BigDecimal("1.00"), "USD");
        reserved.reserve(8);
        entityManager.persist(reserved);
        entityManager.persist(new StockItem("SKU-HIGH", "High", 100, new BigDecimal("1.00"), "USD"));
        entityManager.flush();

        assertThat(repository.findLowStock(3))
                .extracting(StockItem::getSku)
                .containsExactly("SKU-LOW", "SKU-RESERVED");
    }

    @Test
    void auditsTimestamps() {
        StockItem saved = repository.save(new StockItem("SKU-AUDIT", "Audit", 1, new BigDecimal("1.00"), "USD"));

        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }
}
