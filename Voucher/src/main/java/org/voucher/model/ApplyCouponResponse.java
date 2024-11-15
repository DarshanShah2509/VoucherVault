package org.voucher.model;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ApplyCouponResponse {
    private List<Map<String, Object>> items;
    private double totalDiscount;
    private double finalPrice;
}
