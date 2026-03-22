// OrderController.java
package tn.esprit.projetintegre.controllernadine;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.projetintegre.nadineentities.Order;
import tn.esprit.projetintegre.enums.OrderStatus;
import tn.esprit.projetintegre.enums.PaymentStatus;
import tn.esprit.projetintegre.services.OrderService;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<Order>> getByUser(@PathVariable Long userId,
                                                 Pageable pageable) {
        return ResponseEntity.ok(orderService.getOrdersByUser(userId, pageable));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<Page<Order>> getByStatus(@PathVariable OrderStatus status,
                                                   Pageable pageable) {
        return ResponseEntity.ok(orderService.getOrdersByStatus(status, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    @GetMapping("/number/{orderNumber}")
    public ResponseEntity<Order> getByNumber(@PathVariable String orderNumber) {
        return ResponseEntity.ok(orderService.getOrderByNumber(orderNumber));
    }

    @PostMapping("/user/{userId}/checkout")
    public ResponseEntity<Order> checkout(
            @PathVariable Long userId,
            @RequestParam String shippingName,
            @RequestParam String shippingPhone,
            @RequestParam String shippingAddress,
            @RequestParam String shippingCity,
            @RequestParam String shippingPostalCode,
            @RequestParam String shippingCountry,
            @RequestParam String paymentMethod,
            @RequestParam(required = false) String notes) {
        return ResponseEntity.ok(orderService.createOrderFromCart(
                userId, shippingName, shippingPhone,
                shippingAddress, shippingCity,
                shippingPostalCode, shippingCountry,
                paymentMethod, notes));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Order> updateStatus(@PathVariable Long id,
                                              @RequestParam OrderStatus status) {
        return ResponseEntity.ok(orderService.updateOrderStatus(id, status));
    }

    @PutMapping("/{id}/payment")
    public ResponseEntity<Order> updatePayment(@PathVariable Long id,
                                               @RequestParam PaymentStatus status,
                                               @RequestParam String transactionId) {
        return ResponseEntity.ok(orderService.updatePaymentStatus(
                id, status, transactionId));
    }

    @GetMapping("/revenue")
    public ResponseEntity<BigDecimal> getTotalRevenue() {
        return ResponseEntity.ok(orderService.getTotalRevenue());
    }
}