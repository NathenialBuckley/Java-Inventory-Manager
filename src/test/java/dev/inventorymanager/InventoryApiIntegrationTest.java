package dev.inventorymanager;

import dev.inventorymanager.model.Item;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class InventoryApiIntegrationTest {

    @org.springframework.beans.factory.annotation.Value("${local.server.port}")
    int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void basicCreateAndGet() {
        String base = "http://localhost:" + port + "/api/items";

        Item item = new Item("Test Item", "SKU-1", 10, new BigDecimal("9.99"));
        ResponseEntity<Item> created = restTemplate.postForEntity(base, item, Item.class);

        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Item body = created.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getId()).isNotNull();

        ResponseEntity<Item> fetched = restTemplate.getForEntity(base + "/" + body.getId(), Item.class);
        assertThat(fetched.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(fetched.getBody()).isNotNull();
        assertThat(fetched.getBody().getName()).isEqualTo("Test Item");
    }
}
