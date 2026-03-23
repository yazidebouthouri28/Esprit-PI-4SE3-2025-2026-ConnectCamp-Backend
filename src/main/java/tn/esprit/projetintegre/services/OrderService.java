package tn.esprit.projetintegre.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.projetintegre.entities.*;
import tn.esprit.projetintegre.enums.OrderStatus;
import tn.esprit.projetintegre.enums.PaymentStatus;
import tn.esprit.projetintegre.exception.InsufficientStockException;
import tn.esprit.projetintegre.exception.ResourceNotFoundException;
import tn.esprit.projetintegre.repositories.OrderRepository;
import tn.esprit.projetintegre.repositories.ProductRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CartService cartService;
    private final UserService userService;

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Page<Order> getOrdersByUser(Long userId, Pageable pageable) {
        return orderRepository.findByUserId(userId, pageable);
    }

    public Page<Order> getOrdersByStatus(OrderStatus status, Pageable pageable) {
        return orderRepository.findByStatus(status, pageable);
    }

    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
    }

    public Order getOrderByNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with number: " + orderNumber));
    }

    @Transactional
    public Order createOrderFromCart(Long userId, String shippingName, String shippingPhone,
                                     String shippingAddress, String shippingCity,
                                     String shippingPostalCode, String shippingCountry,
                                     String paymentMethod, String notes) {
        User user = userService.getUserById(userId);
        Cart cart = cartService.getCartByUserId(userId);

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new IllegalStateException("Cart is empty");
        }

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();

            // Null-safe price from cart item
            BigDecimal itemPrice = cartItem.getPrice() != null
                    ? cartItem.getPrice()
                    : product.getPrice();

            if (product.getStockQuantity() < cartItem.getQuantity()) {
                throw new InsufficientStockException(
                        "Insufficient stock for product: " + product.getName());
            }

            BigDecimal lineTotal = itemPrice.multiply(BigDecimal.valueOf(cartItem.getQuantity()));

            OrderItem orderItem = OrderItem.builder()
                    .product(product)
                    .productName(product.getName())
                    .productSku(product.getSku())
                    .productThumbnail(product.getThumbnail())
                    .quantity(cartItem.getQuantity())
                    .unitPrice(itemPrice)
                    .totalPrice(lineTotal)
                    .build();

            orderItems.add(orderItem);
            subtotal = subtotal.add(lineTotal);

            product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
            product.setSalesCount(product.getSalesCount() + cartItem.getQuantity());
            productRepository.save(product);
        }

        BigDecimal shippingCost = calculateShippingCost(subtotal);
        BigDecimal taxAmount = subtotal.multiply(BigDecimal.valueOf(0.19));

        // ── NULL-SAFE cart fields ──────────────────────────────────────────────
        BigDecimal discountAmount = cart.getDiscountAmount() != null
                ? cart.getDiscountAmount()
                : BigDecimal.ZERO;

        BigDecimal totalAmount = subtotal
                .add(shippingCost)
                .add(taxAmount)
                .subtract(discountAmount);   // safe now

        Order order = Order.builder()
                .user(user)
                .subtotal(subtotal)
                .shippingCost(shippingCost)
                .taxAmount(taxAmount)
                .discountAmount(discountAmount)
                .totalAmount(totalAmount)
                .status(OrderStatus.PENDING)
                .paymentStatus(PaymentStatus.PENDING)
                .paymentMethod(paymentMethod)
                .shippingName(shippingName)
                .shippingPhone(shippingPhone)
                .shippingAddress(shippingAddress)
                .shippingCity(shippingCity)
                .shippingPostalCode(shippingPostalCode)
                .shippingCountry(shippingCountry)
                .couponCode(cart.getAppliedCouponCode())
                .notes(notes)
                .build();

        order = orderRepository.save(order);

        for (OrderItem item : orderItems) {
            item.setOrder(order);
        }
        order.setItems(orderItems);
        order = orderRepository.save(order);

        cartService.clearCart(userId);

        int loyaltyPoints = totalAmount.intValue() / 10;
        userService.addLoyaltyPoints(userId, loyaltyPoints);

        return order;
    }
    @Transactional
    public Order updateOrderStatus(Long orderId, OrderStatus status) {
        Order order = getOrderById(orderId);
        order.setStatus(status);

        switch (status) {
            case SHIPPED:
                order.setShippedAt(LocalDateTime.now());
                break;
            case DELIVERED:
                order.setDeliveredAt(LocalDateTime.now());
                break;
            case CANCELLED:
                order.setCancelledAt(LocalDateTime.now());
                // Restore stock
                for (OrderItem item : order.getItems()) {
                    Product product = item.getProduct();
                    product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
                    productRepository.save(product);
                }
                break;
            default:
                break;
        }

        return orderRepository.save(order);
    }

    @Transactional
    public Order updatePaymentStatus(Long orderId, PaymentStatus status, String transactionId) {
        Order order = getOrderById(orderId);
        order.setPaymentStatus(status);
        order.setPaymentTransactionId(transactionId);
        if (status == PaymentStatus.COMPLETED) {
            order.setPaidAt(LocalDateTime.now());
            order.setStatus(OrderStatus.CONFIRMED);
        }
        return orderRepository.save(order);
    }

    private BigDecimal calculateShippingCost(BigDecimal subtotal) {
        if (subtotal.compareTo(BigDecimal.valueOf(100)) >= 0) {
            return BigDecimal.ZERO; // Free shipping for orders over 100
        }
        return BigDecimal.valueOf(7); // Fixed shipping cost
    }

    public BigDecimal getTotalRevenue() {
        BigDecimal revenue = orderRepository.getTotalRevenue();
        return revenue != null ? revenue : BigDecimal.ZERO;
    }
}
