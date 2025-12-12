package dev.inventorymanager.service;

import dev.inventorymanager.model.*;
import dev.inventorymanager.repository.ItemRepository;
import dev.inventorymanager.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final ItemRepository itemRepository;

    public TransactionService(TransactionRepository transactionRepository, ItemRepository itemRepository) {
        this.transactionRepository = transactionRepository;
        this.itemRepository = itemRepository;
    }

    /**
     * Process a BUY transaction - increases inventory
     * @param item The item to buy
     * @param quantity Quantity to purchase
     * @param pricePerUnit Price per unit
     * @param user The user making the transaction
     * @return The created transaction
     */
    @Transactional
    public Transaction processBuyTransaction(Item item, Integer quantity, BigDecimal pricePerUnit, User user) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }
        if (pricePerUnit.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Price per unit cannot be negative");
        }

        // Record inventory before
        Integer inventoryBefore = item.getQuantity();

        // Update item quantity
        item.setQuantity(item.getQuantity() + quantity);
        itemRepository.save(item);

        // Create transaction record
        Transaction transaction = new Transaction(item, TransactionType.BUY, quantity, pricePerUnit);
        transaction.setUser(user);
        transaction.setInventoryBefore(inventoryBefore);
        transaction.setInventoryAfter(item.getQuantity());
        transaction.setStatus(TransactionStatus.COMPLETED);

        return transactionRepository.save(transaction);
    }

    /**
     * Process a SELL transaction - decreases inventory with validation
     * @param item The item to sell
     * @param quantity Quantity to sell
     * @param pricePerUnit Price per unit
     * @param user The user making the transaction
     * @return The created transaction
     * @throws IllegalArgumentException if insufficient inventory
     */
    @Transactional
    public Transaction processSellTransaction(Item item, Integer quantity, BigDecimal pricePerUnit, User user) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }
        if (pricePerUnit.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Price per unit cannot be negative");
        }

        // Validate sufficient inventory
        if (item.getQuantity() < quantity) {
            throw new IllegalArgumentException(
                String.format("Insufficient inventory. Available: %d, Requested: %d",
                    item.getQuantity(), quantity)
            );
        }

        // Record inventory before
        Integer inventoryBefore = item.getQuantity();

        // Update item quantity
        item.setQuantity(item.getQuantity() - quantity);
        itemRepository.save(item);

        // Create transaction record
        Transaction transaction = new Transaction(item, TransactionType.SELL, quantity, pricePerUnit);
        transaction.setUser(user);
        transaction.setInventoryBefore(inventoryBefore);
        transaction.setInventoryAfter(item.getQuantity());
        transaction.setStatus(TransactionStatus.COMPLETED);

        return transactionRepository.save(transaction);
    }

    /**
     * Process a transaction (buy or sell) based on type
     * @param item The item
     * @param type Transaction type (BUY or SELL)
     * @param quantity Quantity
     * @param pricePerUnit Price per unit
     * @param user The user making the transaction
     * @return The created transaction
     */
    @Transactional
    public Transaction processTransaction(Item item, TransactionType type, Integer quantity, BigDecimal pricePerUnit, User user) {
        return processTransaction(item, type, quantity, pricePerUnit, user, null);
    }

    /**
     * Process a transaction with optional notes
     * @param item The item
     * @param type Transaction type (BUY or SELL)
     * @param quantity Quantity
     * @param pricePerUnit Price per unit
     * @param user The user making the transaction
     * @param notes Optional notes
     * @return The created transaction
     */
    @Transactional
    public Transaction processTransaction(Item item, TransactionType type, Integer quantity,
                                         BigDecimal pricePerUnit, User user, String notes) {
        Transaction transaction;

        if (type == TransactionType.BUY) {
            transaction = processBuyTransaction(item, quantity, pricePerUnit, user);
        } else if (type == TransactionType.SELL) {
            transaction = processSellTransaction(item, quantity, pricePerUnit, user);
        } else {
            throw new IllegalArgumentException("Invalid transaction type: " + type);
        }

        if (notes != null && !notes.isEmpty()) {
            transaction.setNotes(notes);
            transaction = transactionRepository.save(transaction);
        }

        return transaction;
    }
}
