package dev.inventorymanager.dto;

import dev.inventorymanager.model.Item;
import dev.inventorymanager.model.Transaction;

import java.math.BigDecimal;
import java.util.List;

/**
 * Data Transfer Object (DTO) for the Activity Dashboard response.
 * Contains all statistics and data displayed on the user's dashboard including:
 * - Inventory metrics (items, values, quantities)
 * - Transaction metrics (spending, sales, profit)
 * - Activity lists (recent transactions, top items, low stock alerts)
 */
public class DashboardResponse {

    // Inventory Statistics
    /** Total number of items in the user's inventory */
    private Long totalItems;

    /** Total monetary value of all inventory (sum of price * quantity for all items) */
    private BigDecimal totalInventoryValue;

    /** Total quantity of all items combined */
    private Integer totalItemQuantity;

    /** Number of items that are below the low stock threshold */
    private Long lowStockItemsCount;

    // Transaction Statistics
    /** Total number of transactions (both BUY and SELL) */
    private Long totalTransactions;

    /** Total amount spent on BUY transactions */
    private BigDecimal totalSpending;

    /** Total revenue from SELL transactions */
    private BigDecimal totalSales;

    /** Net profit calculated as totalSales - totalSpending */
    private BigDecimal netProfit;

    // Recent Activity
    /** List of the most recent transactions (up to 10) */
    private List<Transaction> recentTransactions;

    /** List of items with the highest total value (up to 5) */
    private List<ItemSummary> topValueItems;

    /** List of items that are running low on stock (up to 5) */
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

    /**
     * Nested DTO class representing a simplified view of an Item.
     * Used in dashboard lists to avoid circular references and reduce payload size.
     * Includes the calculated total value (price * quantity) for convenience.
     */
    public static class ItemSummary {
        private Long id;
        private String name;
        private String sku;
        private Integer quantity;
        private BigDecimal price;
        /** Calculated total value (price * quantity) */
        private BigDecimal totalValue;

        public ItemSummary() {}

        /**
         * Constructs an ItemSummary from a full Item entity.
         * Automatically calculates the total value during construction.
         * @param item The source Item entity
         */
        public ItemSummary(Item item) {
            this.id = item.getId();
            this.name = item.getName();
            this.sku = item.getSku();
            this.quantity = item.getQuantity();
            this.price = item.getPrice();
            // Calculate total value, defaulting to zero if price or quantity is null
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
