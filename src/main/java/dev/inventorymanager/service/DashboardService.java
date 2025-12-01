package dev.inventorymanager.service;

import dev.inventorymanager.dto.DashboardResponse;
import dev.inventorymanager.model.Item;
import dev.inventorymanager.model.Transaction;
import dev.inventorymanager.model.User;
import dev.inventorymanager.repository.ItemRepository;
import dev.inventorymanager.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service layer for the Activity Dashboard feature.
 * Aggregates data from multiple repositories to provide comprehensive dashboard statistics.
 */
@Service
public class DashboardService {

    // Items with quantity below this threshold are considered "low stock"
    private static final int LOW_STOCK_THRESHOLD = 10;

    // Number of recent transactions to display on the dashboard
    private static final int RECENT_TRANSACTIONS_LIMIT = 10;

    // Number of top items to display in various lists (top value, low stock)
    private static final int TOP_ITEMS_LIMIT = 5;

    private final ItemRepository itemRepository;
    private final TransactionRepository transactionRepository;

    /**
     * Constructor injection for required repositories.
     * @param itemRepository Repository for item data access
     * @param transactionRepository Repository for transaction data access
     */
    public DashboardService(ItemRepository itemRepository, TransactionRepository transactionRepository) {
        this.itemRepository = itemRepository;
        this.transactionRepository = transactionRepository;
    }

    /**
     * Gathers and aggregates all dashboard statistics for a specific user.
     * This method performs multiple database queries to collect:
     * - Inventory metrics (counts, values, quantities)
     * - Transaction metrics (spending, sales, profit)
     * - Activity lists (recent transactions, top items, low stock alerts)
     *
     * @param user The user whose dashboard data should be retrieved
     * @return DashboardResponse containing all aggregated statistics
     */
    public DashboardResponse getDashboard(User user) {
        DashboardResponse dashboard = new DashboardResponse();

        // Inventory Statistics - Calculate aggregate metrics for all user's items
        dashboard.setTotalItems(itemRepository.countByUser(user));
        dashboard.setTotalInventoryValue(itemRepository.getTotalInventoryValueByUser(user));
        dashboard.setTotalItemQuantity(itemRepository.getTotalQuantityByUser(user));
        dashboard.setLowStockItemsCount(itemRepository.countLowStockItems(user, LOW_STOCK_THRESHOLD));

        // Transaction Statistics - Calculate financial metrics from user's transactions
        dashboard.setTotalTransactions(transactionRepository.countByUser(user));
        BigDecimal totalSpending = transactionRepository.getTotalSpending(user);
        BigDecimal totalSales = transactionRepository.getTotalSales(user);
        dashboard.setTotalSpending(totalSpending);
        dashboard.setTotalSales(totalSales);
        // Net profit = total sales - total spending
        dashboard.setNetProfit(totalSales.subtract(totalSpending));

        // Recent Activity - Get the most recent transactions for the user
        List<Transaction> recentTransactions = transactionRepository.findByUserOrderByTransactionDateDesc(user);
        dashboard.setRecentTransactions(
            recentTransactions.stream()
                .limit(RECENT_TRANSACTIONS_LIMIT)
                .collect(Collectors.toList())
        );

        // Top Value Items - Items with highest total value (price * quantity)
        List<Item> topItems = itemRepository.findTopValueItems(user);
        dashboard.setTopValueItems(
            topItems.stream()
                .limit(TOP_ITEMS_LIMIT)
                .map(DashboardResponse.ItemSummary::new)
                .collect(Collectors.toList())
        );

        // Low Stock Items - Items that need to be reordered soon
        List<Item> lowStockItems = itemRepository.findLowStockItems(user, LOW_STOCK_THRESHOLD);
        dashboard.setLowStockItems(
            lowStockItems.stream()
                .limit(TOP_ITEMS_LIMIT)
                .map(DashboardResponse.ItemSummary::new)
                .collect(Collectors.toList())
        );

        return dashboard;
    }
}
