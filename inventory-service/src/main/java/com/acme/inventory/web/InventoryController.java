package com.acme.inventory.web;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;

import com.acme.inventory.domain.StockItem;
import com.acme.inventory.service.InventoryService;

import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping
    public List<StockItemResponse> list(@RequestParam(required = false) @Min(0) Integer lowStockThreshold) {
        List<StockItem> items = lowStockThreshold == null
                ? inventoryService.listAll()
                : inventoryService.lowStock(lowStockThreshold);
        return items.stream().map(StockItemResponse::from).collect(Collectors.toList());
    }

    @GetMapping("/{sku}")
    public StockItemResponse get(@PathVariable String sku) {
        return StockItemResponse.from(inventoryService.getBySku(sku));
    }

    @PostMapping("/{sku}/reserve")
    public StockItemResponse reserve(@PathVariable String sku, @Valid @RequestBody QuantityRequest request) {
        return StockItemResponse.from(inventoryService.reserve(sku, request.getQuantity()))
                .withQuantity(request.getQuantity());
    }

    @PostMapping("/{sku}/release")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void release(@PathVariable String sku, @Valid @RequestBody QuantityRequest request) {
        inventoryService.release(sku, request.getQuantity());
    }

    @PutMapping("/{sku}")
    public StockItemResponse restock(@PathVariable String sku, @Valid @RequestBody RestockRequest request) {
        return StockItemResponse.from(inventoryService.restock(sku, request.getProductName(), request.getQuantity(),
                request.getUnitPrice(), request.getCurrency()));
    }

    public static class QuantityRequest {
        @Min(1)
        @Max(1000)
        private int quantity;

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }
    }

    public static class RestockRequest {
        @NotBlank
        private String productName;

        @Min(1)
        private int quantity;

        @NotNull
        @Positive
        private BigDecimal unitPrice;

        @NotBlank
        @Size(min = 3, max = 3)
        private String currency = "USD";

        public String getProductName() {
            return productName;
        }

        public void setProductName(String productName) {
            this.productName = productName;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public BigDecimal getUnitPrice() {
            return unitPrice;
        }

        public void setUnitPrice(BigDecimal unitPrice) {
            this.unitPrice = unitPrice;
        }

        public String getCurrency() {
            return currency;
        }

        public void setCurrency(String currency) {
            this.currency = currency;
        }
    }
}
