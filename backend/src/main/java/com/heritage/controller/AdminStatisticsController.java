package com.heritage.controller;

import com.heritage.domain.*;
import com.heritage.repo.*;
import com.heritage.api.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.Instant;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/statistics")
@CrossOrigin(origins = "http://localhost:5173")
public class AdminStatisticsController {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ActivityRepository activityRepository;
    
    @Autowired
    private HeritageProjectRepository heritageProjectRepository;

    @Autowired
    private HeritageCategoryRepository heritageCategoryRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private ReservationRepository reservationRepository;
    
    @Autowired
    private OrderRepository orderRepository;
    
    /**
     * 获取统计概览数据
     */
    @GetMapping
    public ResponseEntity<?> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userRepository.count());
        stats.put("totalActivities", activityRepository.count());
        stats.put("totalProjects", heritageProjectRepository.count());
        stats.put("totalProducts", productRepository.count());
        stats.put("totalReservations", reservationRepository.count());
        stats.put("totalOrders", orderRepository.count());
        return ResponseEntity.ok(new ApiResponse(0, "success", stats, Instant.now().toEpochMilli()));
    }
    
    /**
     * 获取用户增长趋势数据（按月）
     */
    @GetMapping("/charts/users")
    public ResponseEntity<?> getUsersChart() {
        List<User> users = userRepository.findAll();
        
        // 按创建月份分组统计
        Map<String, Long> monthlyData = users.stream()
            .collect(Collectors.groupingBy(
                u -> {
                    LocalDate date = u.getCreatedAt() != null ? 
                        LocalDate.ofInstant(u.getCreatedAt(), ZoneId.systemDefault()) : LocalDate.now();
                    return date.getYear() + "-" + String.format("%02d", date.getMonthValue());
                },
                Collectors.counting()
            ));
        
        // 生成最近 6 个月的数据
        List<String> months = new ArrayList<>();
        List<Long> values = new ArrayList<>();
        
        for (int i = 5; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusMonths(i);
            String month = date.getYear() + "-" + String.format("%02d", date.getMonthValue());
            months.add(date.getMonthValue() + "月");
            values.add(monthlyData.getOrDefault(month, 0L));
        }
        
        Map<String, Object> chart = new HashMap<>();
        chart.put("months", months);
        chart.put("values", values);
        return ResponseEntity.ok(new ApiResponse(0, "success", chart, Instant.now().toEpochMilli()));
    }
    
    /**
     * 获取活动预约分布数据
     */
    @GetMapping("/charts/activities")
    public ResponseEntity<?> getActivitiesChart() {
        List<Reservation> reservations = reservationRepository.findAll();
        
        Map<String, Long> statusData = reservations.stream()
            .collect(Collectors.groupingBy(
                r -> r.getStatus() != null ? r.getStatus() : "UNKNOWN",
                Collectors.counting()
            ));
        
        List<Map<String, Object>> data = new ArrayList<>();
        statusData.forEach((status, count) -> {
            Map<String, Object> item = new HashMap<>();
            item.put("value", count);
            item.put("name", getStatusLabel(status));
            data.add(item);
        });
        
        Map<String, Object> chart = new HashMap<>();
        chart.put("data", data);
        return ResponseEntity.ok(new ApiResponse(0, "success", chart, Instant.now().toEpochMilli()));
    }
    
    /**
     * 获取商品销售排行数据
     */
    @GetMapping("/charts/products")
    public ResponseEntity<?> getProductsChart() {
        List<Product> products = productRepository.findAll();
        
        // 按销售量排序，取前 5 个
        List<Map<String, Object>> data = products.stream()
            .sorted((a, b) -> Long.compare(b.getSalesCount() != null ? b.getSalesCount() : 0,
                                          a.getSalesCount() != null ? a.getSalesCount() : 0))
            .limit(5)
            .map(p -> {
                Map<String, Object> item = new HashMap<>();
                item.put("name", p.getName());
                item.put("value", p.getSalesCount() != null ? p.getSalesCount() : 0);
                return item;
            })
            .collect(Collectors.toList());
        
        Map<String, Object> chart = new HashMap<>();
        chart.put("data", data);
        return ResponseEntity.ok(new ApiResponse(0, "success", chart, Instant.now().toEpochMilli()));
    }
    
    /**
     * 获取项目分类分布数据
     */
    @GetMapping("/charts/projects")
    public ResponseEntity<?> getProjectsChart() {
        List<HeritageProject> projects = heritageProjectRepository.findAll();
        List<HeritageCategory> categories = heritageCategoryRepository.findAll();
        
        Map<Long, String> categoryMap = categories.stream()
            .collect(Collectors.toMap(HeritageCategory::getId, HeritageCategory::getName));
        
        Map<String, Long> categoryData = projects.stream()
            .collect(Collectors.groupingBy(
                p -> {
                    if (p.getCategoryId() == null) return "未分类";
                    return categoryMap.getOrDefault(p.getCategoryId(), "未知分类");
                },
                Collectors.counting()
            ));
        
        List<Map<String, Object>> data = new ArrayList<>();
        categoryData.forEach((category, count) -> {
            Map<String, Object> item = new HashMap<>();
            item.put("name", category);
            item.put("value", count);
            data.add(item);
        });
        
        Map<String, Object> chart = new HashMap<>();
        chart.put("data", data);
        return ResponseEntity.ok(new ApiResponse(0, "success", chart, Instant.now().toEpochMilli()));
    }
    
    /**
     * 获取订单统计数据
     */
    @GetMapping("/charts/orders")
    public ResponseEntity<?> getOrdersChart() {
        List<Order> orders = orderRepository.findAll();
        
        Map<String, Long> statusData = orders.stream()
            .collect(Collectors.groupingBy(
                o -> o.getStatus() != null ? o.getStatus() : "UNKNOWN",
                Collectors.counting()
            ));
        
        List<Map<String, Object>> data = new ArrayList<>();
        statusData.forEach((status, count) -> {
            Map<String, Object> item = new HashMap<>();
            item.put("value", count);
            item.put("name", getOrderStatusLabel(status));
            data.add(item);
        });
        
        Map<String, Object> chart = new HashMap<>();
        chart.put("data", data);
        return ResponseEntity.ok(new ApiResponse(0, "success", chart, Instant.now().toEpochMilli()));
    }
    
    /**
     * 获取预约状态标签
     */
    private String getStatusLabel(String status) {
        switch (status) {
            case "PENDING": return "待审核";
            case "APPROVED": return "已批准";
            case "REJECTED": return "已拒绝";
            case "CANCELLED": return "已取消";
            default: return status;
        }
    }
    
    /**
     * 获取订单状态标签
     */
    private String getOrderStatusLabel(String status) {
        switch (status) {
            case "PENDING": return "待处理";
            case "PROCESSING": return "处理中";
            case "SHIPPED": return "已发货";
            case "DELIVERED": return "已送达";
            case "CANCELLED": return "已取消";
            default: return status;
        }
    }
}
