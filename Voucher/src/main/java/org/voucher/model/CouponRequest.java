package org.voucher.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

@Data
public class CouponRequest {
    private CouponType type;

    @NotNull(message = "Coupon details must not be null")
    private Map<String, Object> details;
}
