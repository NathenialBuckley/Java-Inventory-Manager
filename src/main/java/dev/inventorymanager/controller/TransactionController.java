package dev.inventorymanager.controller;

import dev.inventorymanager.model.Item;
import dev.inventorymanager.model.Transaction;
import dev.inventorymanager.repository.ItemRepository;
import dev.inventorymanager.repository.TransactionRepository;
import org.springframework.http.ResponseEntity;
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

    public TransactionController(TransactionRepository transactionRepository, ItemRepository itemRepository) {
        this.transactionRepository = transactionRepository;
        this.itemRepository = itemRepository;
    }

    @GetMapping
    public List<Transaction> list() {
        return transactionRepository.findAll();
    }

    @GetMapping("/item/{itemId}")
    public List<Transaction> getByItem(@PathVariable Long itemId) {
        return transactionRepository.findByItemIdOrderByTransactionDateDesc(itemId);
    }

    @GetMapping("/summary")
    public ResponseEntity<Map<String, BigDecimal>> getSummary() {
        Map<String, BigDecimal> summary = new HashMap<>();
        summary.put("totalSpending", transactionRepository.getTotalSpending());
        summary.put("totalSales", transactionRepository.getTotalSales());

        BigDecimal spending = summary.get("totalSpending");
        BigDecimal sales = summary.get("totalSales");
        summary.put("netProfit", sales.subtract(spending));

        return ResponseEntity.ok(summary);
    }

    @PostMapping
    public ResponseEntity<Transaction> create(@RequestBody TransactionRequest request) {
        Item item = itemRepository.findById(request.getItemId())
                .orElseThrow(() -> new IllegalArgumentException("Item not found"));

        Transaction transaction = new Transaction(
                item,
                request.getType(),
                request.getQuantity(),
                request.getPricePerUnit()
        );

        Transaction saved = transactionRepository.save(transaction);
        return ResponseEntity.ok(saved);
    }

    // DTO for transaction creation
    public static class TransactionRequest {
        private Long itemId;
        private String type;
        private Integer quantity;
        private BigDecimal pricePerUnit;

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
    }
}
