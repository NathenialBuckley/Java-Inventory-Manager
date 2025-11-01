package dev.inventorymanager.controller;

import dev.inventorymanager.dto.DashboardResponse;
import dev.inventorymanager.model.User;
import dev.inventorymanager.repository.UserRepository;
import dev.inventorymanager.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for the Activity Dashboard feature.
 * Provides endpoints to retrieve user-specific activity statistics and insights.
 */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;
    private final UserRepository userRepository;

    /**
     * Constructor injection for required dependencies.
     * @param dashboardService Service for gathering dashboard statistics
     * @param userRepository Repository for user data access
     */
    public DashboardController(DashboardService dashboardService, UserRepository userRepository) {
        this.dashboardService = dashboardService;
        this.userRepository = userRepository;
    }

    /**
     * Retrieves the currently authenticated user from the security context.
     * @return The authenticated User object
     * @throws IllegalStateException if no user is authenticated or user not found
     */
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

    /**
     * GET /api/dashboard
     * Returns comprehensive dashboard statistics for the authenticated user including:
     * - Inventory metrics (total items, value, quantity, low stock count)
     * - Transaction metrics (total, spending, sales, net profit)
     * - Recent activity (recent transactions, top value items, low stock items)
     *
     * @return ResponseEntity containing DashboardResponse with all statistics
     */
    @GetMapping
    public ResponseEntity<DashboardResponse> getDashboard() {
        User currentUser = getCurrentUser();
        DashboardResponse dashboard = dashboardService.getDashboard(currentUser);
        return ResponseEntity.ok(dashboard);
    }
}
