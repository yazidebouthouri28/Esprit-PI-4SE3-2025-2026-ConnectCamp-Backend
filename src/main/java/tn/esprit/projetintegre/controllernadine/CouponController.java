package tn.esprit.projetintegre.controllernadine;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.projetintegre.nadineentities.Coupon;
import tn.esprit.projetintegre.nadineentities.User;
import tn.esprit.projetintegre.servicenadine.CouponService;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    @GetMapping
    public ResponseEntity<List<Coupon>> getActiveCoupons() {
        return ResponseEntity.ok(couponService.getActiveCoupons());
    }

    @PostMapping("/validate")
    public ResponseEntity<Coupon> validate(@RequestParam String code,
                                           @RequestBody User user,
                                           @RequestParam BigDecimal orderAmount) {
        return ResponseEntity.ok(couponService.validate(code, user, orderAmount));
    }

    @PostMapping("/{couponId}/deactivate")
    public ResponseEntity<Void> deactivate(@PathVariable Long couponId) {
        couponService.deactivate(couponId);
        return ResponseEntity.ok().build();
    }
}
