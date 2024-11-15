package org.voucher.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.Map;

@Data
@Document(collection = "coupons")
public class Coupon {
    @Id
    private String id;
    private CouponType type; // cart-wise, product-wise, bxgy
    private Map<String, Object> details; // Stores dynamic details based on type
    private boolean isActive;
    private LocalDate creationDate;
    private LocalDate expirationDate;
}
