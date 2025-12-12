package dev.inventorymanager.model;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = true)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status = TransactionStatus.COMPLETED;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private BigDecimal pricePerUnit;

    @Column(nullable = false)
    private BigDecimal totalAmount;

    @Column(nullable = false)
    private LocalDateTime transactionDate;

    @Column(name = "inventory_before")
    private Integer inventoryBefore;

    @Column(name = "inventory_after")
    private Integer inventoryAfter;

    @Column(length = 1000)
    private String notes;

    public Transaction() {
        this.transactionDate = LocalDateTime.now();
    }

    public Transaction(Item item, TransactionType type, Integer quantity, BigDecimal pricePerUnit) {
        this.item = item;
        this.type = type;
        this.quantity = quantity;
        this.pricePerUnit = pricePerUnit;
        this.totalAmount = pricePerUnit.multiply(BigDecimal.valueOf(quantity));
        this.transactionDate = LocalDateTime.now();
        this.status = TransactionStatus.COMPLETED;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }

    public Integer getInventoryBefore() {
        return inventoryBefore;
    }

    public void setInventoryBefore(Integer inventoryBefore) {
        this.inventoryBefore = inventoryBefore;
    }

    public Integer getInventoryAfter() {
        return inventoryAfter;
    }

    public void setInventoryAfter(Integer inventoryAfter) {
        this.inventoryAfter = inventoryAfter;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
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

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDateTime transactionDate) {
        this.transactionDate = transactionDate;
    }
}
