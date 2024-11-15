package org.voucher.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.voucher.Service.CouponService;
import org.voucher.model.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/coupons")
public class CouponController {

    private final CouponService couponService;

    @Autowired
    public CouponController(CouponService couponService) {
        this.couponService = couponService;
    }

    // Create a new coupon
    @PostMapping
    public ResponseEntity<Coupon> createCoupon(@Valid @RequestBody CouponRequest request) {
        Coupon coupon = new Coupon();
        coupon.setType(request.getType());
        coupon.setDetails(request.getDetails());
        Coupon createdCoupon = couponService.createCoupon(coupon);
        return ResponseEntity.ok(createdCoupon);
    }

    // Get all coupons
    @GetMapping
    public ResponseEntity<List<Coupon>> getAllCoupons() {
        return ResponseEntity.ok(couponService.getAllCoupons());
    }

    // Get a specific coupon by ID
    @GetMapping("/{id}")
    public ResponseEntity<Coupon> getCouponById(@PathVariable String id) {
        return couponService.getCouponById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Update a specific coupon by ID
    @PutMapping("/{id}")
    public ResponseEntity<Coupon> updateCoupon(@PathVariable String id, @Valid @RequestBody CouponRequest request) {
        Coupon coupon = new Coupon();
        coupon.setType(request.getType());
        coupon.setDetails(request.getDetails());

        Coupon updatedCoupon = couponService.updateCoupon(id, coupon);
        return ResponseEntity.ok(updatedCoupon);
    }

    // Delete a specific coupon by ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCoupon(@PathVariable String id) {
        couponService.deleteCoupon(id);
        return ResponseEntity.noContent().build();
    }

    // Fetch all applicable coupons for a given cart
    @PostMapping("/applicable-coupons")
    public ResponseEntity<ApplicableCouponResponse> getApplicableCoupons(@Valid @RequestBody CartRequest cartRequest) {
        List<Coupon> applicableCoupons = couponService.getApplicableCoupons(Map.of(
                "items", cartRequest.getItems(),
                "totalPrice", cartRequest.getTotalPrice()
        ));
        ApplicableCouponResponse response = new ApplicableCouponResponse();
        response.setApplicableCoupons(applicableCoupons);
        return ResponseEntity.ok(response);
    }

    // Apply a specific coupon to the cart
    @PostMapping("/apply-coupon/{id}")
    public ResponseEntity<ApplyCouponResponse> applyCouponToCart(
            @PathVariable String id,
            @Valid @RequestBody CartRequest cartRequest) {

        Map<String, Object> updatedCart = couponService.applyCouponToCart(id, Map.of(
                "items", cartRequest.getItems(),
                "totalPrice", cartRequest.getTotalPrice()
        ));

        ApplyCouponResponse response = new ApplyCouponResponse();
        response.setItems((List<Map<String, Object>>) updatedCart.get("items"));
        response.setTotalDiscount((double) updatedCart.get("totalDiscount"));
        response.setFinalPrice((double) updatedCart.get("finalPrice"));

        return ResponseEntity.ok(response);
    }
}
