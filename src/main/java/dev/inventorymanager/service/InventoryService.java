package dev.inventorymanager.service;

import dev.inventorymanager.model.Item;
import dev.inventorymanager.model.User;
import dev.inventorymanager.repository.ItemRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class InventoryService {

    private final ItemRepository repository;

    public InventoryService(ItemRepository repository) {
        this.repository = repository;
    }

    public Item create(Item item, User user) {
        // basic validation
        if (item.getName() == null || item.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("name is required");
        }
        if (item.getSku() == null || item.getSku().trim().isEmpty()) {
            throw new IllegalArgumentException("sku is required");
        }
        item.setUser(user);
        return repository.save(item);
    }

    public List<Item> list(User user) {
        return repository.findByUser(user);
    }

    public Optional<Item> get(Long id, User user) {
        return repository.findByIdAndUser(id, user);
    }

    public Item update(Long id, Item updated, User user) {
        return repository.findByIdAndUser(id, user).map(existing -> {
            existing.setName(updated.getName());
            existing.setSku(updated.getSku());
            existing.setQuantity(updated.getQuantity());
            existing.setPrice(updated.getPrice());
            return repository.save(existing);
        }).orElseThrow(() -> new IllegalArgumentException("item not found"));
    }

    public void delete(Long id, User user) {
        repository.findByIdAndUser(id, user).ifPresent(repository::delete);
    }
}
