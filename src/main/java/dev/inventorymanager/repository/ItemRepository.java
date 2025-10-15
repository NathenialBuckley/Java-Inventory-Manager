package dev.inventorymanager.repository;

import dev.inventorymanager.model.Item;
import dev.inventorymanager.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    Optional<Item> findBySku(String sku);
    List<Item> findByUser(User user);
    Optional<Item> findByIdAndUser(Long id, User user);
}
