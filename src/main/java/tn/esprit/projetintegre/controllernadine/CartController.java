// CartController.java
package tn.esprit.projetintegre.controllernadine;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.projetintegre.dto.request.CartItemRequest;
import tn.esprit.projetintegre.entities.Product;
import tn.esprit.projetintegre.nadineentities.Cart;
import tn.esprit.projetintegre.nadineentities.User;
import tn.esprit.projetintegre.repositories.UserRepository;
import tn.esprit.projetintegre.servicenadine.CartService;
import tn.esprit.projetintegre.repositories.ProductRepository;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @GetMapping("/user/{userId}")
    public ResponseEntity<Cart> getCart(@PathVariable Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
        return ResponseEntity.ok(cartService.getOrCreateCart(user));
    }

    @PostMapping("/user/{userId}/items")
    public ResponseEntity<Cart> addItem(@PathVariable Long userId,
                                        @Valid @RequestBody CartItemRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Produit introuvable"));
        return ResponseEntity.ok(cartService.addItem(user, product, request.getQuantity()));
    }

    @DeleteMapping("/{cartId}/items/{itemId}")
    public ResponseEntity<Cart> removeItem(@PathVariable Long cartId,
                                           @PathVariable Long itemId) {
        return ResponseEntity.ok(cartService.removeItem(cartId, itemId));
    }

    @PostMapping("/user/{userId}/coupon")
    public ResponseEntity<Cart> applyCoupon(@PathVariable Long userId,
                                            @RequestParam String code) {
        return ResponseEntity.ok(cartService.applyCoupon(userId, code));
    }

    @DeleteMapping("/user/{userId}/clear")
    public ResponseEntity<Void> clearCart(@PathVariable Long userId) {
        cartService.clearCart(userId);
        return ResponseEntity.ok().build();
    }
}