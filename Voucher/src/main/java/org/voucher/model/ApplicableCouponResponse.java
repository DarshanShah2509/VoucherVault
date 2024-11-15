package org.voucher.model;

import lombok.Data;

import java.util.List;

@Data
public class ApplicableCouponResponse {
    private List<Coupon> applicableCoupons;
}
