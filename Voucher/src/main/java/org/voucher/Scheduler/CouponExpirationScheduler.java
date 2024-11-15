package org.voucher.Scheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.voucher.Service.CouponService;

@Component
public class CouponExpirationScheduler {

    private final CouponService couponService;

    @Autowired
    public CouponExpirationScheduler(CouponService couponService) {
        this.couponService = couponService;
    }

    // Run every day at midnight (00:00)
    @Scheduled(cron = "0 0 0 * * ?")
    public void markExpiredCouponsInactive() {
        couponService.deactivateExpiredCoupons();
    }
}
