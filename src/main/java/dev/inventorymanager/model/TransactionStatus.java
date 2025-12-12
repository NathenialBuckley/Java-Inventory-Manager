package dev.inventorymanager.model;

/**
 * Enumeration representing the lifecycle status of a transaction.
 *
 * This enum tracks the current state of a transaction from creation through completion
 * or failure. It enables support for complex workflows like approval processes and
 * transaction reversals.
 *
 * Status Flow:
 * PENDING -> COMPLETED (successful transaction)
 * PENDING -> FAILED (validation or processing failure)
 * COMPLETED -> REVERSED (transaction undone/refunded)
 */
public enum TransactionStatus {
    /**
     * Transaction has been created but not yet processed.
     * Useful for approval workflows or batch processing.
     */
    PENDING,

    /**
     * Transaction has been successfully processed.
     * Inventory quantities have been updated and the transaction is recorded.
     */
    COMPLETED,

    /**
     * Transaction processing failed.
     * Could be due to validation errors, insufficient inventory, or system errors.
     */
    FAILED,

    /**
     * Transaction has been reversed/undone.
     * Typically used for refunds or corrections where inventory needs to be restored.
     */
    REVERSED
}
