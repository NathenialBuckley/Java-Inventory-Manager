package dev.inventorymanager.service;

import dev.inventorymanager.model.*;
import dev.inventorymanager.repository.ItemRepository;
import dev.inventorymanager.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Service class for processing inventory transactions.
 *
 * This service provides the core business logic for buy and sell operations:
 * - Validates transaction requests (quantity, price, sufficient inventory)
 * - Atomically updates inventory quantities
 * - Creates transaction records with full audit trail
 * - Captures inventory snapshots before and after each transaction
 *
 * All public methods are transactional (@Transactional), meaning:
 * - Changes to Item and Transaction are committed together
 * - If any operation fails, all changes are rolled back
 * - Database consistency is guaranteed
 *
 * Thread Safety: This service is stateless and thread-safe.
 */
@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final ItemRepository itemRepository;

    /**
     * Constructor for dependency injection.
     *
     * @param transactionRepository Repository for transaction persistence
     * @param itemRepository Repository for item persistence
     */
    public TransactionService(TransactionRepository transactionRepository, ItemRepository itemRepository) {
        this.transactionRepository = transactionRepository;
        this.itemRepository = itemRepository;
    }

    /**
     * Process a BUY transaction - increases inventory.
     *
     * This method performs the following atomic operations:
     * 1. Validates the transaction parameters
     * 2. Captures the inventory snapshot before update
     * 3. Increases the item quantity by the purchase amount
     * 4. Creates a transaction record with full audit trail
     * 5. Persists both the updated item and new transaction
     *
     * @param item The item to purchase (must be valid and persisted)
     * @param quantity Number of units to buy (must be positive)
     * @param pricePerUnit Purchase price per unit (must be non-negative)
     * @param user The user making the purchase
     * @return The created and persisted transaction record
     * @throws IllegalArgumentException if quantity <= 0 or pricePerUnit < 0
     */
    @Transactional
    public Transaction processBuyTransaction(Item item, Integer quantity, BigDecimal pricePerUnit, User user) {
        // Validation: Ensure quantity is positive
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }

        // Validation: Ensure price is not negative
        if (pricePerUnit.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Price per unit cannot be negative");
        }

        // Capture inventory snapshot BEFORE making any changes
        // This is critical for audit trail
        Integer inventoryBefore = item.getQuantity();

        // Update the item's quantity - BUY increases inventory
        // Example: 100 + 50 = 150
        item.setQuantity(item.getQuantity() + quantity);
        itemRepository.save(item);

        // Create the transaction record with all details
        Transaction transaction = new Transaction(item, TransactionType.BUY, quantity, pricePerUnit);
        transaction.setUser(user);
        transaction.setInventoryBefore(inventoryBefore);  // What it was
        transaction.setInventoryAfter(item.getQuantity()); // What it is now
        transaction.setStatus(TransactionStatus.COMPLETED);

        // Persist and return the transaction
        return transactionRepository.save(transaction);
    }

    /**
     * Process a SELL transaction - decreases inventory with validation.
     *
     * This method performs the following atomic operations:
     * 1. Validates the transaction parameters
     * 2. Checks if sufficient inventory exists (CRITICAL - prevents overselling)
     * 3. Captures the inventory snapshot before update
     * 4. Decreases the item quantity by the sale amount
     * 5. Creates a transaction record with full audit trail
     * 6. Persists both the updated item and new transaction
     *
     * IMPORTANT: This method prevents overselling by validating inventory availability
     * before processing the sale.
     *
     * @param item The item to sell (must be valid and persisted)
     * @param quantity Number of units to sell (must be positive and <= available)
     * @param pricePerUnit Sale price per unit (must be non-negative)
     * @param user The user making the sale
     * @return The created and persisted transaction record
     * @throws IllegalArgumentException if quantity <= 0, pricePerUnit < 0, or insufficient inventory
     */
    @Transactional
    public Transaction processSellTransaction(Item item, Integer quantity, BigDecimal pricePerUnit, User user) {
        // Validation: Ensure quantity is positive
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }

        // Validation: Ensure price is not negative
        if (pricePerUnit.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Price per unit cannot be negative");
        }

        // CRITICAL VALIDATION: Check for sufficient inventory
        // This prevents selling more than we have in stock
        if (item.getQuantity() < quantity) {
            throw new IllegalArgumentException(
                String.format("Insufficient inventory. Available: %d, Requested: %d",
                    item.getQuantity(), quantity)
            );
        }

        // Capture inventory snapshot BEFORE making any changes
        Integer inventoryBefore = item.getQuantity();

        // Update the item's quantity - SELL decreases inventory
        // Example: 100 - 30 = 70
        item.setQuantity(item.getQuantity() - quantity);
        itemRepository.save(item);

        // Create the transaction record with all details
        Transaction transaction = new Transaction(item, TransactionType.SELL, quantity, pricePerUnit);
        transaction.setUser(user);
        transaction.setInventoryBefore(inventoryBefore);  // What it was
        transaction.setInventoryAfter(item.getQuantity()); // What it is now
        transaction.setStatus(TransactionStatus.COMPLETED);

        // Persist and return the transaction
        return transactionRepository.save(transaction);
    }

    /**
     * Process a transaction (buy or sell) based on type.
     *
     * Convenience method that delegates to processBuyTransaction or processSellTransaction
     * based on the transaction type. This provides a unified interface for transaction processing.
     *
     * @param item The item being transacted
     * @param type Transaction type (BUY or SELL)
     * @param quantity Number of units
     * @param pricePerUnit Price per unit
     * @param user The user making the transaction
     * @return The created transaction
     * @see #processBuyTransaction(Item, Integer, BigDecimal, User)
     * @see #processSellTransaction(Item, Integer, BigDecimal, User)
     */
    @Transactional
    public Transaction processTransaction(Item item, TransactionType type, Integer quantity, BigDecimal pricePerUnit, User user) {
        return processTransaction(item, type, quantity, pricePerUnit, user, null);
    }

    /**
     * Process a transaction with optional notes.
     *
     * This is the main entry point for processing transactions from the controller layer.
     * It routes to the appropriate buy or sell method and adds optional notes to the transaction.
     *
     * Usage Example:
     * <pre>
     * Transaction tx = processTransaction(
     *     item,
     *     TransactionType.SELL,
     *     10,
     *     new BigDecimal("99.99"),
     *     currentUser,
     *     "Sold to Customer #123"
     * );
     * </pre>
     *
     * @param item The item being transacted
     * @param type Transaction type (BUY or SELL)
     * @param quantity Number of units
     * @param pricePerUnit Price per unit
     * @param user The user making the transaction
     * @param notes Optional notes/comments about the transaction (can be null)
     * @return The created and persisted transaction with notes
     * @throws IllegalArgumentException if type is invalid or validation fails
     */
    @Transactional
    public Transaction processTransaction(Item item, TransactionType type, Integer quantity,
                                         BigDecimal pricePerUnit, User user, String notes) {
        Transaction transaction;

        // Route to the appropriate specialized method based on transaction type
        if (type == TransactionType.BUY) {
            transaction = processBuyTransaction(item, quantity, pricePerUnit, user);
        } else if (type == TransactionType.SELL) {
            transaction = processSellTransaction(item, quantity, pricePerUnit, user);
        } else {
            // This should never happen if using the TransactionType enum
            throw new IllegalArgumentException("Invalid transaction type: " + type);
        }

        // Add notes if provided
        if (notes != null && !notes.isEmpty()) {
            transaction.setNotes(notes);
            transaction = transactionRepository.save(transaction);
        }

        return transaction;
    }
}
