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

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() ||
            authentication.getPrincipal().equals("anonymousUser")) {
            throw new IllegalStateException("User not authenticated");
        }
        String username = authentication.getName();
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalStateException("User not found"));
    }

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

    @PostMapping
    public ResponseEntity<Transaction> create(@RequestBody TransactionRequest request) {
        User currentUser = getCurrentUser();

        // Verify the item belongs to the current user
        Item item = itemRepository.findByIdAndUser(request.getItemId(), currentUser)
                .orElseThrow(() -> new IllegalArgumentException("Item not found or access denied"));

        // Parse transaction type from string to enum
        TransactionType type;
        try {
            type = TransactionType.valueOf(request.getType().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid transaction type. Must be BUY or SELL");
        }

        // Use TransactionService to process the transaction atomically
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

    // DTO for transaction creation
    public static class TransactionRequest {
        private Long itemId;
        private String type;
        private Integer quantity;
        private BigDecimal pricePerUnit;
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
