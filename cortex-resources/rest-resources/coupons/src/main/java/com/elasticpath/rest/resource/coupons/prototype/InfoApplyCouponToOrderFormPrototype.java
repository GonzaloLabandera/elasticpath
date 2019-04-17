/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.coupons.prototype;

import io.reactivex.Single;

import com.elasticpath.rest.ResourceInfo;
import com.elasticpath.rest.definition.coupons.ApplyCouponToOrderFormResource;
import com.elasticpath.rest.resource.coupons.constants.CouponsResourceConstants;

/**
 * Apply Coupon to Order Form prototype for Info operation.
 */
public class InfoApplyCouponToOrderFormPrototype implements ApplyCouponToOrderFormResource.Info {

	private static final Single<ResourceInfo> INFO_SINGLE = Single.just(ResourceInfo.builder()
			.withMaxAge(CouponsResourceConstants.MAX_AGE)
			.build());

	@Override
	public Single<ResourceInfo> onInfo() {
		return INFO_SINGLE;
	}
}
