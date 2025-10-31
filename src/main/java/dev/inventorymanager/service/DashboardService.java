package dev.inventorymanager.service;

import dev.inventorymanager.dto.DashboardResponse;
import dev.inventorymanager.model.Item;
import dev.inventorymanager.model.Transaction;
import dev.inventorymanager.model.User;
import dev.inventorymanager.repository.ItemRepository;
import dev.inventorymanager.repository.TransactionRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private static final int LOW_STOCK_THRESHOLD = 10;
    private static final int RECENT_TRANSACTIONS_LIMIT = 10;
    private static final int TOP_ITEMS_LIMIT = 5;

    private final ItemRepository itemRepository;
    private final TransactionRepository transactionRepository;

    public DashboardService(ItemRepository itemRepository, TransactionRepository transactionRepository) {
        this.itemRepository = itemRepository;
        this.transactionRepository = transactionRepository;
    }

    public DashboardResponse getDashboard(User user) {
        DashboardResponse dashboard = new DashboardResponse();

        // Inventory Statistics
        dashboard.setTotalItems(itemRepository.countByUser(user));
        dashboard.setTotalInventoryValue(itemRepository.getTotalInventoryValueByUser(user));
        dashboard.setTotalItemQuantity(itemRepository.getTotalQuantityByUser(user));
        dashboard.setLowStockItemsCount(itemRepository.countLowStockItems(user, LOW_STOCK_THRESHOLD));

        // Transaction Statistics
        dashboard.setTotalTransactions(transactionRepository.countByUser(user));
        BigDecimal totalSpending = transactionRepository.getTotalSpending(user);
        BigDecimal totalSales = transactionRepository.getTotalSales(user);
        dashboard.setTotalSpending(totalSpending);
        dashboard.setTotalSales(totalSales);
        dashboard.setNetProfit(totalSales.subtract(totalSpending));

        // Recent Activity
        List<Transaction> recentTransactions = transactionRepository.findByUserOrderByTransactionDateDesc(user);
        dashboard.setRecentTransactions(
            recentTransactions.stream()
                .limit(RECENT_TRANSACTIONS_LIMIT)
                .collect(Collectors.toList())
        );

        // Top Value Items
        List<Item> topItems = itemRepository.findTopValueItems(user);
        dashboard.setTopValueItems(
            topItems.stream()
                .limit(TOP_ITEMS_LIMIT)
                .map(DashboardResponse.ItemSummary::new)
                .collect(Collectors.toList())
        );

        // Low Stock Items
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
