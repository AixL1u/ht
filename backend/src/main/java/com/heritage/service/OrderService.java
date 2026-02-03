package com.heritage.service;

import com.heritage.domain.Order;
import com.heritage.domain.OrderItem;
import com.heritage.domain.Product;
import com.heritage.repo.OrderItemRepository;
import com.heritage.repo.OrderRepository;
import com.heritage.repo.ProductRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;

    public OrderService(OrderRepository orderRepository, OrderItemRepository orderItemRepository, ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.productRepository = productRepository;
    }

    public static class OrderItemReq {
        public Long productId;
        public Integer quantity;
    }

    @Transactional
    public Order create(Long userId, Long addressId, List<OrderItemReq> items) {
        BigDecimal total = BigDecimal.ZERO;
        for (OrderItemReq req : items) {
            Product p = productRepository.findById(req.productId).orElseThrow();
            if (p.getStock() < req.quantity) {
                throw new IllegalStateException("库存不足");
            }
            p.setStock(p.getStock() - req.quantity);
            productRepository.save(p);
            total = total.add(p.getPrice().multiply(BigDecimal.valueOf(req.quantity)));
        }

        Order o = new Order();
        o.setUserId(userId);
        o.setAddressId(addressId);
        o.setStatus("pending");
        o.setPaymentStatus("unpaid");
        o.setTotalAmount(total);
        o.setCreatedAt(Instant.now());
        o = orderRepository.save(o);

        for (OrderItemReq req : items) {
            Product p = productRepository.findById(req.productId).orElseThrow();
            OrderItem oi = new OrderItem();
            oi.setOrderId(o.getId());
            oi.setProductId(p.getId());
            oi.setQuantity(req.quantity);
            oi.setUnitPrice(p.getPrice());
            orderItemRepository.save(oi);
        }
        return o;
    }

    @Transactional
    public Order updateStatus(Long orderId, String status, String paymentStatus) {
        Order o = orderRepository.findById(orderId).orElseThrow();
        o.setStatus(status);
        if (paymentStatus != null) o.setPaymentStatus(paymentStatus);
        return orderRepository.save(o);
    }
}

