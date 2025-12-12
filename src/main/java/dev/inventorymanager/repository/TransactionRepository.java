package dev.inventorymanager.repository;

import dev.inventorymanager.model.Transaction;
import dev.inventorymanager.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Repository interface for Transaction entity database operations.
 *
 * This interface extends Spring Data JPA's JpaRepository, which provides:
 * - Standard CRUD operations (save, findById, findAll, delete, etc.)
 * - Custom query methods defined by method naming convention
 * - Custom JPQL queries using @Query annotation
 *
 * Spring Data JPA automatically implements this interface at runtime.
 * No implementation class is needed.
 *
 * All methods in this repository respect multi-tenancy - transactions are
 * filtered by user to ensure data isolation.
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    /**
     * Find all transactions for a specific item, ordered by date (newest first).
     *
     * This method uses Spring Data JPA's method naming convention:
     * - findBy: SELECT query
     * - ItemId: WHERE item.id = ?
     * - OrderBy: ORDER BY clause
     * - TransactionDateDesc: ORDER BY transactionDate DESC
     *
     * Generated SQL:
     * SELECT * FROM transactions WHERE item_id = ? ORDER BY transaction_date DESC
     *
     * @param itemId ID of the item
     * @return List of transactions for the item, newest first
     */
    List<Transaction> findByItemIdOrderByTransactionDateDesc(Long itemId);

    /**
     * Find all transactions for a specific user, ordered by date (newest first).
     *
     * Used to display a user's complete transaction history.
     * Ensures users only see their own transactions (multi-tenancy).
     *
     * Generated SQL:
     * SELECT * FROM transactions WHERE user_id = ? ORDER BY transaction_date DESC
     *
     * @param user The user whose transactions to retrieve
     * @return List of user's transactions, newest first
     */
    List<Transaction> findByUserOrderByTransactionDateDesc(User user);

    /**
     * Calculate total spending (sum of all BUY transactions) for a user.
     *
     * This custom JPQL query sums the totalAmount of all BUY transactions.
     * COALESCE returns 0 if there are no transactions (prevents null).
     *
     * Use case: Dashboard analytics, profit calculation
     *
     * @param user The user whose spending to calculate
     * @return Total amount spent on purchases (BUY transactions)
     */
    @Query("SELECT COALESCE(SUM(t.totalAmount), 0) FROM Transaction t WHERE t.type = 'BUY' AND t.user = :user")
    BigDecimal getTotalSpending(@Param("user") User user);

    /**
     * Calculate total sales (sum of all SELL transactions) for a user.
     *
     * This custom JPQL query sums the totalAmount of all SELL transactions.
     * COALESCE returns 0 if there are no transactions (prevents null).
     *
     * Use case: Dashboard analytics, profit calculation
     *
     * @param user The user whose sales to calculate
     * @return Total amount earned from sales (SELL transactions)
     */
    @Query("SELECT COALESCE(SUM(t.totalAmount), 0) FROM Transaction t WHERE t.type = 'SELL' AND t.user = :user")
    BigDecimal getTotalSales(@Param("user") User user);

    /**
     * Count total number of transactions for a user.
     *
     * Uses Spring Data JPA's count method naming convention.
     * Generated SQL: SELECT COUNT(*) FROM transactions WHERE user_id = ?
     *
     * Use case: Dashboard statistics
     *
     * @param user The user whose transactions to count
     * @return Total number of transactions for the user
     */
    Long countByUser(User user);
}
