/**
 * Copyright © 2016 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.coupon.impl;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;

import com.elasticpath.domain.rules.AppliedCoupon;
import com.elasticpath.domain.rules.Coupon;
import com.elasticpath.rest.cache.CacheResult;
import com.elasticpath.rest.resource.integration.epcommerce.repository.coupon.CouponRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.order.OrderRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.transform.ReactiveAdapter;
import com.elasticpath.service.rules.CouponService;
import com.elasticpath.service.rules.CouponUsageService;

/**
 * The facade for Coupon related operations.
 */
@Singleton
@Named("couponRepository")
public class CouponRepositoryImpl implements CouponRepository {

	/**
	 * Coupon code not found.
	 */
	static final String COUPON_CODE_NOT_FOUND = "Coupon Code Not Found";
	private final CouponService couponService;
	private final CouponUsageService couponUsageService;
	private final ReactiveAdapter reactiveAdapter;
	private final OrderRepository orderRepository;


	/**
	 * Constructor.
	 *
	 * @param couponService      coupon service.
	 * @param couponUsageService coupon service.
	 * @param orderRepository    order repository
	 * @param reactiveAdapter    reactive adapter
	 */
	@Inject
	public CouponRepositoryImpl(
			@Named("couponService") final CouponService couponService,
			@Named("couponUsageService") final CouponUsageService couponUsageService,
			@Named("orderRepository") final OrderRepository orderRepository,
			@Named("reactiveAdapter") final ReactiveAdapter reactiveAdapter) {

		this.couponService = couponService;
		this.couponUsageService = couponUsageService;
		this.orderRepository = orderRepository;
		this.reactiveAdapter = reactiveAdapter;
	}

	@Override
	public Completable validateCoupon(final String couponCode, final String storeCode, final String customerEmail) {
		final Coupon coupon = getByCouponCode(couponCode);
		return reactiveAdapter.fromServiceAsCompletable(() ->
				couponUsageService.ensureValidCouponRuleAndUsage(coupon, couponCode, storeCode, customerEmail));
	}

	@Override
	public Single<Coupon> findByCouponCode(final String couponCode) {
		return reactiveAdapter.fromServiceAsSingle(() -> getByCouponCode(couponCode), COUPON_CODE_NOT_FOUND);
	}

	@Override
	public Observable<AppliedCoupon> getAppliedCoupons(final String scope, final String purchaseId) {
		return orderRepository.findByGuid(scope, purchaseId)
				.flatMapObservable(order -> Observable.fromIterable(order.getAppliedRules()))
				.flatMap(appliedRule -> Observable.fromIterable(appliedRule.getAppliedCoupons()));
	}

	@CacheResult
	private Coupon getByCouponCode(final String couponCode) {
		return couponService.findByCouponCode(couponCode);
	}
}
