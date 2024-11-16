package org.voucher.Service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.voucher.model.Coupon;
import org.voucher.model.CouponType;
import org.voucher.repository.CouponRepository;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CouponServiceTest {

    @Mock
    private CouponRepository couponRepository;

    @InjectMocks
    private CouponService couponService;

    private Coupon coupon;

    @BeforeEach
    void setUp() {
        coupon = new Coupon();
        coupon.setId("1");
        coupon.setType(CouponType.CART_WISE);
        coupon.setDetails(Map.of("threshold", 100, "discount", 10));
        coupon.setCreationDate(LocalDate.now());
        coupon.setExpirationDate(LocalDate.now().plusDays(30));
        coupon.setActive(true);
    }

    @Test
    void testCreateCoupon() {
        when(couponRepository.save(any(Coupon.class))).thenReturn(coupon);

        Coupon createdCoupon = couponService.createCoupon(coupon);

        assertNotNull(createdCoupon);
        assertEquals(CouponType.CART_WISE, createdCoupon.getType());
        assertTrue(createdCoupon.isActive());
        verify(couponRepository, times(1)).save(any(Coupon.class));
    }

    @Test
    void testDeactivateExpiredCoupons() {
        Coupon expiredCoupon = new Coupon();
        expiredCoupon.setId("2");
        expiredCoupon.setExpirationDate(LocalDate.now().minusDays(1));
        expiredCoupon.setActive(true);

        when(couponRepository.findAll()).thenReturn(List.of(coupon, expiredCoupon));
        when(couponRepository.save(any(Coupon.class))).thenReturn(expiredCoupon);

        couponService.deactivateExpiredCoupons();

        assertFalse(expiredCoupon.isActive());
        verify(couponRepository, times(1)).save(expiredCoupon);
    }

    @Test
    void testGetAllCoupons() {
        when(couponRepository.findAll()).thenReturn(List.of(coupon));

        List<Coupon> allCoupons = couponService.getAllCoupons();

        assertEquals(1, allCoupons.size());
        verify(couponRepository, times(1)).findAll();
    }

    @Test
    void testGetCouponById() {
        when(couponRepository.findById("1")).thenReturn(Optional.of(coupon));

        Optional<Coupon> foundCoupon = couponService.getCouponById("1");

        assertTrue(foundCoupon.isPresent());
        assertEquals(CouponType.CART_WISE, foundCoupon.get().getType());
        verify(couponRepository, times(1)).findById("1");
    }

    @Test
    void testGetCouponById_NotFound() {
        when(couponRepository.findById("2")).thenReturn(Optional.empty());

        Optional<Coupon> foundCoupon = couponService.getCouponById("2");

        assertFalse(foundCoupon.isPresent());
        verify(couponRepository, times(1)).findById("2");
    }

    @Test
    void testUpdateCoupon() {
        when(couponRepository.findById("1")).thenReturn(Optional.of(coupon));
        when(couponRepository.save(any(Coupon.class))).thenReturn(coupon);

        coupon.setType(CouponType.PRODUCT_WISE);
        Coupon updatedCoupon = couponService.updateCoupon("1", coupon);

        assertEquals(CouponType.PRODUCT_WISE, updatedCoupon.getType());
        verify(couponRepository, times(1)).save(coupon);
    }

    @Test
    void testUpdateCoupon_NotFound() {
        when(couponRepository.findById("2")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            couponService.updateCoupon("2", coupon);
        });

        assertEquals("Coupon not found", exception.getMessage());
        verify(couponRepository, never()).save(any(Coupon.class));
    }

    @Test
    void testDeleteCoupon() {
        doNothing().when(couponRepository).deleteById("1");

        couponService.deleteCoupon("1");

        verify(couponRepository, times(1)).deleteById("1");
    }

    @Test
    void testGetAllActiveCoupons() {
        Coupon inactiveCoupon = new Coupon();
        inactiveCoupon.setId("2");
        inactiveCoupon.setActive(false);

        when(couponRepository.findAll()).thenReturn(List.of(coupon, inactiveCoupon));

        List<Coupon> activeCoupons = couponService.getAllActiveCoupons();

        assertEquals(1, activeCoupons.size());
        assertTrue(activeCoupons.get(0).isActive());
        verify(couponRepository, times(1)).findAll();
    }

    @Test
    void testGetApplicableCoupons() {
        Map<String, Object> cart = Map.of("totalPrice", 200.0);
        when(couponRepository.findAll()).thenReturn(List.of(coupon));

        List<Coupon> applicableCoupons = couponService.getApplicableCoupons(cart);

        assertEquals(1, applicableCoupons.size());
        verify(couponRepository, times(1)).findAll();
    }

    @Test
    void testApplyCouponToCart() {
        Map<String, Object> cart = new HashMap<>();
        cart.put("totalPrice", 200.0);

        when(couponRepository.findById("1")).thenReturn(Optional.of(coupon));

        Map<String, Object> updatedCart = couponService.applyCouponToCart("1", cart);

        assertNotNull(updatedCart);
        assertEquals(20.0, updatedCart.get("totalDiscount"));
        assertEquals(180.0, updatedCart.get("finalPrice"));
        verify(couponRepository, times(1)).findById("1");
    }

    @Test
    void testApplyCouponToCart_CouponNotFound() {
        Map<String, Object> cart = new HashMap<>();
        cart.put("totalPrice", 200.0);

        when(couponRepository.findById("2")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            couponService.applyCouponToCart("2", cart);
        });

        assertEquals("Coupon not found", exception.getMessage());
        verify(couponRepository, times(1)).findById("2");
    }

    @Test
    void testApplyCouponToCart_ExpiredCoupon() {
        coupon.setExpirationDate(LocalDate.now().minusDays(1));

        when(couponRepository.findById("1")).thenReturn(Optional.of(coupon));

        Map<String, Object> cart = new HashMap<>();
        cart.put("totalPrice", 200.0);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            couponService.applyCouponToCart("1", cart);
        });

        assertEquals("Coupon is either inactive or expired", exception.getMessage());
    }
}
