package dev.inventorymanager.repository;

import dev.inventorymanager.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByItemIdOrderByTransactionDateDesc(Long itemId);

    @Query("SELECT COALESCE(SUM(t.totalAmount), 0) FROM Transaction t WHERE t.type = 'BUY'")
    BigDecimal getTotalSpending();

    @Query("SELECT COALESCE(SUM(t.totalAmount), 0) FROM Transaction t WHERE t.type = 'SELL'")
    BigDecimal getTotalSales();
}
