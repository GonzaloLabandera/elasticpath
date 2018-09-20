/**
 * Copyright © 2016 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.coupon;

import io.reactivex.Observable;
import io.reactivex.Single;

import com.elasticpath.domain.rules.AppliedCoupon;
import com.elasticpath.domain.rules.Coupon;

/**
 * The facade for {@link Coupon} related operations.
 */
public interface CouponRepository {
	
	/**
	 * True if coupon is valid for system.
	 * @param couponCode coupon code to verify.
	 * @param storeCode  store code to verify coupon is valid within.
	 * @param customerEmail email
	 *
	 * @return true if coupon is valid within store scope, false otherwise.
	 */
	Single<Boolean> isCouponValidInStore(String couponCode, String storeCode, String customerEmail);

	/**
	 * Find the Coupon using the coupon code.
	 *
	 * @param couponCode coupon code to use in look up.
	 * @return Coupon corresponding to coupon code.
	 */
	Single<Coupon> findByCouponCode(String couponCode);

	/**
	 * Get applied coupons for the purchase in the store.
	 *
	 * @param scope store id
	 * @param purchaseId purchase id
	 * @return applied coupons
	 */
	Observable<AppliedCoupon> getAppliedCoupons(String scope, String purchaseId);
}
