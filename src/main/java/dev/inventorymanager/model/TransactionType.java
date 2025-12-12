package dev.inventorymanager.model;

/**
 * Enumeration representing the type of inventory transaction.
 *
 * This enum is used to categorize transactions into two main types:
 * - BUY: Purchasing inventory items (increases quantity)
 * - SELL: Selling inventory items (decreases quantity)
 *
 * Using an enum instead of strings ensures type safety and prevents
 * invalid transaction types from being created.
 */
public enum TransactionType {
    /**
     * Represents a purchase transaction that increases inventory quantity.
     * When a BUY transaction is processed, the item's quantity is incremented.
     */
    BUY,

    /**
     * Represents a sale transaction that decreases inventory quantity.
     * When a SELL transaction is processed, the item's quantity is decremented.
     * Validation ensures sufficient inventory exists before allowing the sale.
     */
    SELL
}
