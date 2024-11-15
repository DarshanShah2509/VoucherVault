package org.voucher.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class CartRequest {
    @NotNull(message = "Items list must not be null")
    private List<Map<String, Object>> items;

    @PositiveOrZero(message = "Total price must be a positive value")
    private double totalPrice;
}
