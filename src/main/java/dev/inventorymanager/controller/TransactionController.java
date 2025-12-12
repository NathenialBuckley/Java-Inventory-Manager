package dev.inventorymanager.controller;

import dev.inventorymanager.model.*;
import dev.inventorymanager.repository.ItemRepository;
import dev.inventorymanager.repository.TransactionRepository;
import dev.inventorymanager.repository.UserRepository;
import dev.inventorymanager.service.TransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for managing inventory transactions.
 *
 * This controller provides endpoints for:
 * - Creating new buy/sell transactions (POST)
 * - Viewing transaction history (GET)
 * - Getting transaction summaries and analytics (GET /summary)
 * - Viewing item-specific transaction history (GET /item/{itemId})
 *
 * All endpoints require authentication. Transactions are user-isolated,
 * meaning users can only see and create transactions for their own items.
 *
 * Base URL: /api/transactions
 *
 * Authentication: Required (Spring Security)
 * Authorization: USER role (default for authenticated users)
 */
@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionRepository transactionRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final TransactionService transactionService;

    public TransactionController(TransactionRepository transactionRepository,
                                ItemRepository itemRepository,
                                UserRepository userRepository,
                                TransactionService transactionService) {
        this.transactionRepository = transactionRepository;
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.transactionService = transactionService;
    }

    /**
     * Helper method to get the currently authenticated user.
     *
     * Extracts the user from Spring Security's authentication context.
     * This ensures that all operations are performed in the context of
     * the logged-in user, maintaining multi-tenancy isolation.
     *
     * @return The currently authenticated User entity
     * @throws IllegalStateException if user is not authenticated or not found in database
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Check if user is authenticated
        if (authentication == null || !authentication.isAuthenticated() ||
            authentication.getPrincipal().equals("anonymousUser")) {
            throw new IllegalStateException("User not authenticated");
        }

        // Get username from authentication and fetch User entity from database
        String username = authentication.getName();
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalStateException("User not found"));
    }

    /**
     * Get all transactions for the current user.
     *
     * Returns the authenticated user's complete transaction history,
     * ordered by date (newest first).
     *
     * Endpoint: GET /api/transactions
     * Authentication: Required
     *
     * Example Response:
     * [
     *   {
     *     "id": 123,
     *     "type": "SELL",
     *     "quantity": 10,
     *     "pricePerUnit": 50.00,
     *     "totalAmount": 500.00,
     *     "inventoryBefore": 100,
     *     "inventoryAfter": 90,
     *     "status": "COMPLETED",
     *     "transactionDate": "2025-12-12T10:30:00",
     *     "item": {...},
     *     "notes": "Sold to Customer ABC"
     *   }
     * ]
     *
     * @return List of transactions ordered by date descending
     */
    @GetMapping
    public List<Transaction> list() {
        return transactionRepository.findByUserOrderByTransactionDateDesc(getCurrentUser());
    }

    @GetMapping("/item/{itemId}")
    public List<Transaction> getByItem(@PathVariable Long itemId) {
        return transactionRepository.findByItemIdOrderByTransactionDateDesc(itemId);
    }

    @GetMapping("/summary")
    public ResponseEntity<Map<String, BigDecimal>> getSummary() {
        User currentUser = getCurrentUser();
        Map<String, BigDecimal> summary = new HashMap<>();
        summary.put("totalSpending", transactionRepository.getTotalSpending(currentUser));
        summary.put("totalSales", transactionRepository.getTotalSales(currentUser));

        BigDecimal spending = summary.get("totalSpending");
        BigDecimal sales = summary.get("totalSales");
        summary.put("netProfit", sales.subtract(spending));

        return ResponseEntity.ok(summary);
    }

    /**
     * Create a new buy or sell transaction.
     *
     * This is the main endpoint for processing inventory transactions.
     * It performs the following operations atomically:
     * 1. Validates user authentication and item ownership
     * 2. Parses and validates the transaction type
     * 3. Delegates to TransactionService which:
     *    - Validates quantity and price
     *    - Checks inventory availability (for SELL)
     *    - Updates item quantity
     *    - Creates transaction record with audit trail
     * 4. Returns the created transaction
     *
     * Endpoint: POST /api/transactions
     * Authentication: Required
     * Content-Type: application/json
     *
     * Request Body Example:
     * {
     *   "itemId": 1,
     *   "type": "SELL",
     *   "quantity": 10,
     *   "pricePerUnit": 50.00,
     *   "notes": "Sold to Customer ABC"
     * }
     *
     * Success Response (200 OK):
     * {
     *   "id": 123,
     *   "type": "SELL",
     *   "quantity": 10,
     *   "pricePerUnit": 50.00,
     *   "totalAmount": 500.00,
     *   "inventoryBefore": 100,
     *   "inventoryAfter": 90,
     *   "status": "COMPLETED",
     *   "transactionDate": "2025-12-12T10:30:00",
     *   "item": {...},
     *   "user": {...},
     *   "notes": "Sold to Customer ABC"
     * }
     *
     * Error Responses:
     * - 400 Bad Request: Invalid data (negative quantity, invalid type, insufficient inventory)
     * - 401 Unauthorized: Not authenticated
     * - 403 Forbidden: Item doesn't belong to current user
     *
     * @param request Transaction request containing itemId, type, quantity, pricePerUnit, and optional notes
     * @return ResponseEntity with the created transaction
     * @throws IllegalArgumentException if validation fails
     */
    @PostMapping
    public ResponseEntity<Transaction> create(@RequestBody TransactionRequest request) {
        // Get the authenticated user
        User currentUser = getCurrentUser();

        // Security check: Verify the item belongs to the current user
        // This prevents users from creating transactions for other users' items
        Item item = itemRepository.findByIdAndUser(request.getItemId(), currentUser)
                .orElseThrow(() -> new IllegalArgumentException("Item not found or access denied"));

        // Parse transaction type from string to enum
        // Accepts "BUY", "SELL", "buy", "sell" (case-insensitive)
        TransactionType type;
        try {
            type = TransactionType.valueOf(request.getType().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid transaction type. Must be BUY or SELL");
        }

        // Use TransactionService to process the transaction atomically
        // This ensures inventory and transaction record are updated together
        Transaction transaction = transactionService.processTransaction(
                item,
                type,
                request.getQuantity(),
                request.getPricePerUnit(),
                currentUser,
                request.getNotes()
        );

        return ResponseEntity.ok(transaction);
    }

    /**
     * Data Transfer Object (DTO) for creating transactions.
     *
     * This class defines the structure of the JSON request body
     * for creating new transactions via POST /api/transactions
     *
     * All fields except 'notes' are required.
     */
    public static class TransactionRequest {
        /** ID of the item being transacted */
        private Long itemId;

        /** Type of transaction: "BUY" or "SELL" (case-insensitive) */
        private String type;

        /** Number of units to buy or sell (must be positive) */
        private Integer quantity;

        /** Price per unit for this transaction (must be non-negative) */
        private BigDecimal pricePerUnit;

        /** Optional notes/comments about the transaction */
        private String notes;

        public Long getItemId() {
            return itemId;
        }

        public void setItemId(Long itemId) {
            this.itemId = itemId;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }

        public BigDecimal getPricePerUnit() {
            return pricePerUnit;
        }

        public void setPricePerUnit(BigDecimal pricePerUnit) {
            this.pricePerUnit = pricePerUnit;
        }

        public String getNotes() {
            return notes;
        }

        public void setNotes(String notes) {
            this.notes = notes;
        }
    }
}
