package dev.inventorymanager;

import dev.inventorymanager.model.Item;
import dev.inventorymanager.model.User;
import dev.inventorymanager.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class InventoryApiIntegrationTest {

    @org.springframework.beans.factory.annotation.Value("${local.server.port}")
    int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private TestRestTemplate authenticatedRestTemplate1;
    private TestRestTemplate authenticatedRestTemplate2;

    @BeforeEach
    public void setup() {
        // Clean up users if they exist
        userRepository.findByUsername("testuser1").ifPresent(userRepository::delete);
        userRepository.findByUsername("testuser2").ifPresent(userRepository::delete);

        // Create test users
        User user1 = new User("testuser1", passwordEncoder.encode("password1"));
        User user2 = new User("testuser2", passwordEncoder.encode("password2"));
        userRepository.save(user1);
        userRepository.save(user2);

        // Create authenticated rest templates
        authenticatedRestTemplate1 = restTemplate.withBasicAuth("testuser1", "password1");
        authenticatedRestTemplate2 = restTemplate.withBasicAuth("testuser2", "password2");
    }

    @Test
    public void basicCreateAndGet() {
        String base = "http://localhost:" + port + "/api/items";

        Item item = new Item("Test Item", "SKU-1", 10, new BigDecimal("9.99"));
        ResponseEntity<Item> created = authenticatedRestTemplate1.postForEntity(base, item, Item.class);

        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Item body = created.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getId()).isNotNull();

        ResponseEntity<Item> fetched = authenticatedRestTemplate1.getForEntity(base + "/" + body.getId(), Item.class);
        assertThat(fetched.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(fetched.getBody()).isNotNull();
        assertThat(fetched.getBody().getName()).isEqualTo("Test Item");
    }

    @Test
    public void usersCannotSeeEachOthersInventory() {
        String base = "http://localhost:" + port + "/api/items";

        // User 1 creates an item
        Item item1 = new Item("User1 Item", "SKU-USER1", 10, new BigDecimal("19.99"));
        ResponseEntity<Item> created1 = authenticatedRestTemplate1.postForEntity(base, item1, Item.class);
        assertThat(created1.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Long item1Id = created1.getBody().getId();

        // User 2 creates an item
        Item item2 = new Item("User2 Item", "SKU-USER2", 5, new BigDecimal("29.99"));
        ResponseEntity<Item> created2 = authenticatedRestTemplate2.postForEntity(base, item2, Item.class);
        assertThat(created2.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // User 2 tries to access User 1's item - should get 404
        ResponseEntity<Item> user2AccessUser1Item = authenticatedRestTemplate2.getForEntity(base + "/" + item1Id, Item.class);
        assertThat(user2AccessUser1Item.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        // User 1 can access their own item
        ResponseEntity<Item> user1AccessOwnItem = authenticatedRestTemplate1.getForEntity(base + "/" + item1Id, Item.class);
        assertThat(user1AccessOwnItem.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(user1AccessOwnItem.getBody().getName()).isEqualTo("User1 Item");

        // User 1 gets list - should only see their own item
        ResponseEntity<Item[]> user1List = authenticatedRestTemplate1.getForEntity(base, Item[].class);
        assertThat(user1List.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(user1List.getBody()).hasSize(1);
        assertThat(user1List.getBody()[0].getName()).isEqualTo("User1 Item");

        // User 2 gets list - should only see their own item
        ResponseEntity<Item[]> user2List = authenticatedRestTemplate2.getForEntity(base, Item[].class);
        assertThat(user2List.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(user2List.getBody()).hasSize(1);
        assertThat(user2List.getBody()[0].getName()).isEqualTo("User2 Item");
    }
}
