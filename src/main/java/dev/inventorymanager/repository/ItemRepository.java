package dev.inventorymanager.repository;

import dev.inventorymanager.model.Item;
import dev.inventorymanager.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    Optional<Item> findBySku(String sku);
    List<Item> findByUser(User user);
    Optional<Item> findByIdAndUser(Long id, User user);

    // Dashboard queries
    Long countByUser(User user);

    @Query("SELECT COALESCE(SUM(i.quantity), 0) FROM Item i WHERE i.user = :user")
    Integer getTotalQuantityByUser(@Param("user") User user);

    @Query("SELECT COALESCE(SUM(i.price * i.quantity), 0) FROM Item i WHERE i.user = :user")
    BigDecimal getTotalInventoryValueByUser(@Param("user") User user);

    @Query("SELECT COUNT(i) FROM Item i WHERE i.user = :user AND i.quantity < :threshold")
    Long countLowStockItems(@Param("user") User user, @Param("threshold") Integer threshold);

    @Query("SELECT i FROM Item i WHERE i.user = :user AND i.quantity < :threshold ORDER BY i.quantity ASC")
    List<Item> findLowStockItems(@Param("user") User user, @Param("threshold") Integer threshold);

    @Query("SELECT i FROM Item i WHERE i.user = :user ORDER BY (i.price * i.quantity) DESC")
    List<Item> findTopValueItems(@Param("user") User user);
}
