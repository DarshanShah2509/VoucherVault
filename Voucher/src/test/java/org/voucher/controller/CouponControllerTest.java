package org.voucher.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.voucher.Service.CouponService;
import org.voucher.model.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CouponControllerTest {

    @Mock
    private CouponService couponService;

    @InjectMocks
    private CouponController couponController;

    private CouponRequest couponRequest;
    private Coupon coupon;

    @BeforeEach
    void setUp() {
        couponRequest = new CouponRequest();
        couponRequest.setType(CouponType.CART_WISE);
        couponRequest.setDetails(Map.of("threshold", 100, "discount", 10));

        coupon = new Coupon();
        coupon.setId("1");
        coupon.setType(CouponType.CART_WISE);
        coupon.setDetails(Map.of("threshold", 100, "discount", 10));
    }

    @Test
    void testCreateCoupon() {
        when(couponService.createCoupon(any(Coupon.class))).thenReturn(coupon);

        ResponseEntity<Coupon> response = couponController.createCoupon(couponRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(CouponType.CART_WISE, response.getBody().getType());
        verify(couponService, times(1)).createCoupon(any(Coupon.class));
    }

    @Test
    void testGetAllCoupons() {
        List<Coupon> coupons = List.of(coupon);
        when(couponService.getAllCoupons()).thenReturn(coupons);

        ResponseEntity<List<Coupon>> response = couponController.getAllCoupons();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(couponService, times(1)).getAllCoupons();
    }

    @Test
    void testGetCouponById() {
        when(couponService.getCouponById("1")).thenReturn(Optional.of(coupon));

        ResponseEntity<Coupon> response = couponController.getCouponById("1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(CouponType.CART_WISE, response.getBody().getType());
        verify(couponService, times(1)).getCouponById("1");
    }

    @Test
    void testGetCouponById_NotFound() {
        when(couponService.getCouponById("2")).thenReturn(Optional.empty());

        ResponseEntity<Coupon> response = couponController.getCouponById("2");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(couponService, times(1)).getCouponById("2");
    }

    @Test
    void testUpdateCoupon() {
        when(couponService.updateCoupon(eq("1"), any(Coupon.class))).thenReturn(coupon);

        ResponseEntity<Coupon> response = couponController.updateCoupon("1", couponRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(CouponType.CART_WISE, response.getBody().getType());
        verify(couponService, times(1)).updateCoupon(eq("1"), any(Coupon.class));
    }

    @Test
    void testDeleteCoupon() {
        doNothing().when(couponService).deleteCoupon("1");

        ResponseEntity<Void> response = couponController.deleteCoupon("1");

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(couponService, times(1)).deleteCoupon("1");
    }

    @Test
    void testGetApplicableCoupons() {
        CartRequest cartRequest = new CartRequest();
        cartRequest.setItems(List.of(Map.of("itemId", "101", "quantity", 2)));
        cartRequest.setTotalPrice(200.0);

        when(couponService.getApplicableCoupons(any(Map.class))).thenReturn(List.of(coupon));

        ResponseEntity<ApplicableCouponResponse> response = couponController.getApplicableCoupons(cartRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getApplicableCoupons().size());
        verify(couponService, times(1)).getApplicableCoupons(any(Map.class));
    }

    @Test
    void testApplyCouponToCart() {
        CartRequest cartRequest = new CartRequest();
        cartRequest.setItems(List.of(Map.of("itemId", "101", "quantity", 2)));
        cartRequest.setTotalPrice(200.0);

        Map<String, Object> updatedCart = Map.of(
                "items", cartRequest.getItems(),
                "totalDiscount", 20.0,
                "finalPrice", 180.0
        );

        when(couponService.applyCouponToCart(eq("1"), any(Map.class))).thenReturn(updatedCart);

        ResponseEntity<ApplyCouponResponse> response = couponController.applyCouponToCart("1", cartRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(20.0, response.getBody().getTotalDiscount());
        assertEquals(180.0, response.getBody().getFinalPrice());
        verify(couponService, times(1)).applyCouponToCart(eq("1"), any(Map.class));
    }
}
