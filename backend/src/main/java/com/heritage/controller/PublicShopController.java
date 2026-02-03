package com.heritage.controller;

import com.heritage.domain.*;
import com.heritage.repo.*;
import com.heritage.service.OrderService;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import com.heritage.config.RateLimiterService;

import java.util.List;

@RestController
@RequestMapping("/api")
public class PublicShopController {
    private final ProductCategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final AddressRepository addressRepository;
    private final OrderService orderService;
    private final RateLimiterService rateLimiterService;
    @Value("${app.ratelimit.order.limit:5}")
    private int orderLimit;
    @Value("${app.ratelimit.order.windowMs:60000}")
    private long orderWindowMs;

    public PublicShopController(ProductCategoryRepository categoryRepository, ProductRepository productRepository, OrderRepository orderRepository, OrderItemRepository orderItemRepository, AddressRepository addressRepository, OrderService orderService, RateLimiterService rateLimiterService) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.addressRepository = addressRepository;
        this.orderService = orderService;
        this.rateLimiterService = rateLimiterService;
    }

    @GetMapping("/product-categories")
    public List<ProductCategory> productCategories() { return categoryRepository.findAll(); }

    @GetMapping("/products")
    public List<Product> products() { return productRepository.findAll(); }

    @GetMapping("/products/search")
    public Page<Product> searchProducts(@RequestParam(value = "page", defaultValue = "0") int page,
                                        @RequestParam(value = "size", defaultValue = "10") int size,
                                        @RequestParam(value = "keyword", required = false) String keyword,
                                        @RequestParam(value = "categoryId", required = false) Long categoryId) {
        var pageable = PageRequest.of(Math.max(page, 0), Math.max(size, 1));
        if (categoryId != null && keyword != null && !keyword.isBlank()) {
            return productRepository.findByCategoryIdAndNameContainingIgnoreCase(categoryId, keyword, pageable);
        } else if (categoryId != null) {
            return productRepository.findByCategoryId(categoryId, pageable);
        } else if (keyword != null && !keyword.isBlank()) {
            return productRepository.findByNameContainingIgnoreCase(keyword, pageable);
        } else {
            return productRepository.findAll(pageable);
        }
    }

    @GetMapping("/products/{id}")
    public Product product(@PathVariable Long id) { 
        return productRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("商品不存在: " + id)); 
    }

    @GetMapping("/addresses")
    public List<Address> addresses(@RequestParam("userId") Long userId) { return addressRepository.findByUserId(userId); }

    public static class AddressReq {
        @NotNull public Long userId;
        public String name;
        public String phone;
        public String region;
        public String detail;
    }

    @PostMapping("/addresses")
    public Address createAddress(@RequestBody AddressReq req) {
        Address a = new Address();
        a.setUserId(req.userId);
        a.setName(req.name);
        a.setPhone(req.phone);
        a.setRegion(req.region);
        a.setDetail(req.detail);
        return addressRepository.save(a);
    }

    @PutMapping("/addresses/{id}")
    public Address updateAddress(@PathVariable Long id, @RequestBody AddressReq req) {
        Address a = addressRepository.findById(id).orElseThrow();
        if (req.userId != null) a.setUserId(req.userId);
        a.setName(req.name);
        a.setPhone(req.phone);
        a.setRegion(req.region);
        a.setDetail(req.detail);
        return addressRepository.save(a);
    }

    @DeleteMapping("/addresses/{id}")
    public void deleteAddress(@PathVariable Long id) {
        addressRepository.deleteById(id);
    }

    public static class CreateOrderReq {
        @NotNull public Long userId;
        @NotNull public Long addressId;
        @NotNull public List<OrderService.OrderItemReq> items;
    }

    @PostMapping("/orders")
    public Order createOrder(@RequestBody CreateOrderReq req, HttpServletRequest httpReq) {
        String ip = httpReq.getRemoteAddr();
        String key = "order:" + req.userId + ":" + ip;
        rateLimiterService.check(key, orderLimit, orderWindowMs);
        return orderService.create(req.userId, req.addressId, req.items);
    }

    @GetMapping("/orders")
    public List<Order> orders(@RequestParam("userId") Long userId) { return orderRepository.findByUserId(userId); }

    @GetMapping("/orders/{id}/items")
    public List<OrderItem> orderItems(@PathVariable Long id) { return orderItemRepository.findByOrderId(id); }

    public static class UpdateOrderStatusReq { public String status; public String paymentStatus; }

    @PutMapping("/orders/{id}/status")
    public Order updateOrderStatus(@PathVariable Long id, @RequestBody UpdateOrderStatusReq req) {
        return orderService.updateStatus(id, req.status, req.paymentStatus);
    }
}
