package dev.inventorymanager.repository;

import dev.inventorymanager.model.Transaction;
import dev.inventorymanager.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByItemIdOrderByTransactionDateDesc(Long itemId);
    List<Transaction> findByUserOrderByTransactionDateDesc(User user);

    @Query("SELECT COALESCE(SUM(t.totalAmount), 0) FROM Transaction t WHERE t.type = 'BUY' AND t.user = :user")
    BigDecimal getTotalSpending(@Param("user") User user);

    @Query("SELECT COALESCE(SUM(t.totalAmount), 0) FROM Transaction t WHERE t.type = 'SELL' AND t.user = :user")
    BigDecimal getTotalSales(@Param("user") User user);
}
