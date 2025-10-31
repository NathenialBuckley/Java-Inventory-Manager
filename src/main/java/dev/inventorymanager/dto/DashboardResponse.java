package dev.inventorymanager.dto;

import dev.inventorymanager.model.Item;
import dev.inventorymanager.model.Transaction;

import java.math.BigDecimal;
import java.util.List;

public class DashboardResponse {

    // Inventory Statistics
    private Long totalItems;
    private BigDecimal totalInventoryValue;
    private Integer totalItemQuantity;
    private Long lowStockItemsCount;

    // Transaction Statistics
    private Long totalTransactions;
    private BigDecimal totalSpending;
    private BigDecimal totalSales;
    private BigDecimal netProfit;

    // Recent Activity
    private List<Transaction> recentTransactions;
    private List<ItemSummary> topValueItems;
    private List<ItemSummary> lowStockItems;

    public DashboardResponse() {}

    // Getters and Setters
    public Long getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(Long totalItems) {
        this.totalItems = totalItems;
    }

    public BigDecimal getTotalInventoryValue() {
        return totalInventoryValue;
    }

    public void setTotalInventoryValue(BigDecimal totalInventoryValue) {
        this.totalInventoryValue = totalInventoryValue;
    }

    public Integer getTotalItemQuantity() {
        return totalItemQuantity;
    }

    public void setTotalItemQuantity(Integer totalItemQuantity) {
        this.totalItemQuantity = totalItemQuantity;
    }

    public Long getLowStockItemsCount() {
        return lowStockItemsCount;
    }

    public void setLowStockItemsCount(Long lowStockItemsCount) {
        this.lowStockItemsCount = lowStockItemsCount;
    }

    public Long getTotalTransactions() {
        return totalTransactions;
    }

    public void setTotalTransactions(Long totalTransactions) {
        this.totalTransactions = totalTransactions;
    }

    public BigDecimal getTotalSpending() {
        return totalSpending;
    }

    public void setTotalSpending(BigDecimal totalSpending) {
        this.totalSpending = totalSpending;
    }

    public BigDecimal getTotalSales() {
        return totalSales;
    }

    public void setTotalSales(BigDecimal totalSales) {
        this.totalSales = totalSales;
    }

    public BigDecimal getNetProfit() {
        return netProfit;
    }

    public void setNetProfit(BigDecimal netProfit) {
        this.netProfit = netProfit;
    }

    public List<Transaction> getRecentTransactions() {
        return recentTransactions;
    }

    public void setRecentTransactions(List<Transaction> recentTransactions) {
        this.recentTransactions = recentTransactions;
    }

    public List<ItemSummary> getTopValueItems() {
        return topValueItems;
    }

    public void setTopValueItems(List<ItemSummary> topValueItems) {
        this.topValueItems = topValueItems;
    }

    public List<ItemSummary> getLowStockItems() {
        return lowStockItems;
    }

    public void setLowStockItems(List<ItemSummary> lowStockItems) {
        this.lowStockItems = lowStockItems;
    }

    // Inner class for item summaries
    public static class ItemSummary {
        private Long id;
        private String name;
        private String sku;
        private Integer quantity;
        private BigDecimal price;
        private BigDecimal totalValue;

        public ItemSummary() {}

        public ItemSummary(Item item) {
            this.id = item.getId();
            this.name = item.getName();
            this.sku = item.getSku();
            this.quantity = item.getQuantity();
            this.price = item.getPrice();
            this.totalValue = item.getPrice() != null && item.getQuantity() != null
                ? item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()))
                : BigDecimal.ZERO;
        }

        // Getters and Setters
        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getSku() {
            return sku;
        }

        public void setSku(String sku) {
            this.sku = sku;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public void setPrice(BigDecimal price) {
            this.price = price;
        }

        public BigDecimal getTotalValue() {
            return totalValue;
        }

        public void setTotalValue(BigDecimal totalValue) {
            this.totalValue = totalValue;
        }
    }
}
