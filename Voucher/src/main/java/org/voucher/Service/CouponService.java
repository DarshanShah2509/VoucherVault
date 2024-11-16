package org.voucher.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.voucher.model.Coupon;
import org.voucher.repository.CouponRepository;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CouponService {

    private final CouponRepository couponRepository;

    @Autowired
    public CouponService(CouponRepository couponRepository) {
        this.couponRepository = couponRepository;
    }

    // CRUD Operations
    public Coupon createCoupon(Coupon coupon) {
        coupon.setCreationDate(LocalDate.now());
        coupon.setExpirationDate(LocalDate.now().plusMonths(2));
        coupon.setActive(true);
        return couponRepository.save(coupon);
    }

    public void deactivateExpiredCoupons() {
        List<Coupon> allCoupons = couponRepository.findAll();
        LocalDate today = LocalDate.now();

        for (Coupon coupon : allCoupons) {
            if (coupon.getExpirationDate().isBefore(today) && coupon.isActive()) {
                coupon.setActive(false);
                couponRepository.save(coupon);
            }
        }
    }

    public List<Coupon> getAllCoupons() {
        return couponRepository.findAll();
    }

    public Optional<Coupon> getCouponById(String id) {
        return couponRepository.findById(id);
    }

    public Coupon updateCoupon(String id, Coupon coupon) {
        Optional<Coupon> existing = couponRepository.findById(id);
        if (existing.isPresent()) {
            coupon.setId(id);
            coupon.setCreationDate(existing.get().getCreationDate());
            coupon.setExpirationDate(existing.get().getExpirationDate());
            coupon.setActive(existing.get().isActive());
        } else {
            throw new RuntimeException("Coupon not found");
        }
        return couponRepository.save(coupon);
    }

    public void deleteCoupon(String id) {
        couponRepository.deleteById(id);
    }

    public List<Coupon> getAllActiveCoupons() {
        return couponRepository.findAll().stream()
                .filter(coupon -> coupon.isActive() && coupon.getExpirationDate().isAfter(LocalDate.now()))
                .collect(Collectors.toList());
    }

    // Fetch all applicable coupons for a given cart
    public List<Coupon> getApplicableCoupons(Map<String, Object> cart) {
        List<Coupon> activeCoupons = getAllActiveCoupons();
        return activeCoupons.stream()
                .filter(coupon -> isCouponApplicable(cart, coupon))
                .collect(Collectors.toList());
    }

    public Map<String, Object> applyCouponToCart(String couponId, Map<String, Object> cart) {
        Optional<Coupon> couponOpt = couponRepository.findById(couponId);

        if (couponOpt.isPresent()) {
            Coupon coupon = couponOpt.get();
            if (coupon.isActive() && coupon.getExpirationDate().isAfter(LocalDate.now())) {
                return calculateDiscount(cart, coupon);
            } else {
                throw new RuntimeException("Coupon is either inactive or expired");
            }
        }
        throw new RuntimeException("Coupon not found");
    }

    // Helper Method to Check Applicability
    private boolean isCouponApplicable(Map<String, Object> cart, Coupon coupon) {
        return switch (coupon.getType()) {
            case CART_WISE -> {
                double cartTotal = (double) cart.get("totalPrice");
                Map<String, Object> cartDetails = coupon.getDetails();
                yield cartTotal > (int) cartDetails.get("threshold");
            }
            case PRODUCT_WISE -> {
                List<Map<String, Object>> items = (List<Map<String, Object>>) cart.get("items");
                Map<String, Object> productDetails = coupon.getDetails();
                yield items.stream()
                        .anyMatch(item -> item.get("product_id").equals(productDetails.get("product_id")));
            }
            case BXGY -> checkBxGyApplicability(cart, coupon.getDetails());
        };
    }

    // Helper Method to Apply Discounts
    private Map<String, Object> calculateDiscount(Map<String, Object> cart, Coupon coupon) {
        return switch (coupon.getType()) {
            case CART_WISE -> applyCartWiseDiscount(cart, coupon);
            case PRODUCT_WISE -> applyProductWiseDiscount(cart, coupon);
            case BXGY -> applyBxGyDiscount(cart, coupon);
        };
    }

    private Map<String, Object> applyCartWiseDiscount(Map<String, Object> cart, Coupon coupon) {
        HashMap<String, Object> res = new HashMap<>(cart);
        double cartTotal = (double) cart.get("totalPrice");
        Map<String, Object> details = coupon.getDetails();
        double discount = (int) details.get("discount");
        double discountAmount = cartTotal * (discount / 100);

        res.put("totalDiscount", discountAmount);
        res.put("finalPrice", cartTotal - discountAmount);
        return res;
    }

    private Map<String, Object> applyProductWiseDiscount(Map<String, Object> cart, Coupon coupon) {
        List<Map<String, Object>> items = (List<Map<String, Object>>) cart.get("items");
        Map<String, Object> details = coupon.getDetails();
        int productId = (int) details.get("product_id");
        double discount = (int) details.get("discount");

        items.forEach(item -> {
            if (item.get("product_id").equals(productId)) {
                double price = (int) item.get("price");
                double discountAmount = price * (discount / 100);
                item.put("total_discount", discountAmount);
            }
        });
        return cart;
    }

    private boolean checkBxGyApplicability(Map<String, Object> cart, Map<String, Object> details) {
        List<Map<String, Object>> buyProducts = (List<Map<String, Object>>) details.get("buy_products");
        List<Map<String, Object>> cartItems = (List<Map<String, Object>>) cart.get("items");
        for (Map<String, Object> buyProduct : buyProducts) {
            int productId = (int) buyProduct.get("product_id");
            long count = cartItems.stream().filter(item -> item.get("product_id").equals(productId)).count();
            if (count < (int) buyProduct.get("quantity")) {
                return false;
            }
        }
        return true;
    }

    private Map<String, Object> applyBxGyDiscount(Map<String, Object> cart, Coupon coupon) {
        List<Map<String, Object>> getProducts = (List<Map<String, Object>>) coupon.getDetails().get("get_products");
        List<Map<String, Object>> items = (List<Map<String, Object>>) cart.get("items");
        int repetitionLimit = (int) coupon.getDetails().get("repetition_limit");

        for (int i = 0; i < repetitionLimit; i++) {
            getProducts.forEach(freeProduct -> {
                Map<String, Object> newItem = new HashMap<>();
                newItem.put("product_id", freeProduct.get("product_id"));
                newItem.put("price", 0);
                items.add(newItem);
            });
        }
        return cart;
    }
}
