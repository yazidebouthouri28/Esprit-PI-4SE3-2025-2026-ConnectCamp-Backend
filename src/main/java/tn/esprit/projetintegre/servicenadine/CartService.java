// CartService.java
package tn.esprit.projetintegre.servicenadine;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.projetintegre.entities.Product;
import tn.esprit.projetintegre.nadineentities.*;
import tn.esprit.projetintegre.repositorynadine.CartItemRepository;
import tn.esprit.projetintegre.repositorynadine.CartRepository;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final CouponService couponService;

    public Cart getOrCreateCart(User user) {
        return cartRepository.findByUserId(user.getId())
                .orElseGet(() -> cartRepository.save(
                        Cart.builder().user(user).build()));
    }

    @Transactional
    public Cart addItem(User user, Product product, Integer quantity) {
        Cart cart = getOrCreateCart(user);

        CartItem item = cartItemRepository
                .findByCartIdAndProductId(cart.getId(), product.getId())
                .orElse(CartItem.builder()
                        .cart(cart)
                        .product(product)
                        .quantity(0)
                        .price(product.getPrice())
                        .build());

        item.setQuantity(item.getQuantity() + quantity);
        cartItemRepository.save(item);
        cart.calculateTotal();
        return cartRepository.save(cart);
    }

    @Transactional
    public Cart removeItem(Long cartId, Long itemId) {
        cartItemRepository.deleteById(itemId);
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new RuntimeException("Panier introuvable"));
        cart.calculateTotal();
        return cartRepository.save(cart);
    }

    @Transactional
    public Cart applyCoupon(Long userId, String code) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Panier introuvable"));
        User user = cart.getUser();

        Coupon coupon = couponService.validate(code, user, cart.getTotalAmount());
        BigDecimal discount = couponService.calculateDiscount(
                coupon, cart.getTotalAmount());

        cart.setDiscountAmount(discount);
        cart.setAppliedCouponCode(code);
        return cartRepository.save(cart);
    }

    @Transactional
    public void clearCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Panier introuvable"));
        cartItemRepository.deleteByCartId(cart.getId());
        cart.setTotalAmount(BigDecimal.ZERO);
        cart.setDiscountAmount(BigDecimal.ZERO);
        cart.setAppliedCouponCode(null);
        cartRepository.save(cart);
    }
}