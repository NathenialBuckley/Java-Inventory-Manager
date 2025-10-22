package dev.inventorymanager.controller;

import dev.inventorymanager.model.Item;
import dev.inventorymanager.model.User;
import dev.inventorymanager.repository.UserRepository;
import dev.inventorymanager.service.InventoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/items")
public class InventoryController {

    private final InventoryService service;
    private final UserRepository userRepository;

    public InventoryController(InventoryService service, UserRepository userRepository) {
        this.service = service;
        this.userRepository = userRepository;
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
    public List<Item> list() {
        return service.list(getCurrentUser());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Item> get(@PathVariable Long id) {
        return service.get(id, getCurrentUser()).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Item> create(@RequestBody Item item) {
        Item created = service.create(item, getCurrentUser());
        return ResponseEntity.created(URI.create("/api/items/" + created.getId())).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Item> update(@PathVariable Long id, @RequestBody Item item) {
        try {
            Item updated = service.update(id, item, getCurrentUser());
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id, getCurrentUser());
        return ResponseEntity.noContent().build();
    }
}
