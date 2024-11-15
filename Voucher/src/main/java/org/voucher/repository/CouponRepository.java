package org.voucher.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import org.voucher.model.Coupon;

@Repository
public interface CouponRepository extends MongoRepository<Coupon, String> {
}
