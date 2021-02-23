/**
 * Copyright (c) Elastic Path Software Inc., 2015
 */
package com.elasticpath.service.shoppingcart.actions.impl;

import java.util.Set;
import java.util.stream.Collectors;

import com.elasticpath.base.exception.EpSystemException;
import com.elasticpath.domain.rules.AppliedRule;
import com.elasticpath.service.rules.CouponUsageService;
import com.elasticpath.service.shoppingcart.actions.PostCaptureCheckoutActionContext;
import com.elasticpath.service.shoppingcart.actions.ReversiblePostCaptureCheckoutAction;

/**
 * CheckoutAction to assign coupons to customers who are entitled to receive a coupon as a result of a promotion.
 */
public class ProcessCouponCustomerAssignmentsCheckoutAction implements ReversiblePostCaptureCheckoutAction {

	private CouponUsageService couponUsageService;

	@Override
	public void execute(final PostCaptureCheckoutActionContext context) throws EpSystemException {
		Set<AppliedRule> appliedRules = context.getOrder().getAppliedRules();
		Set<Long> ruleUids = appliedRules.stream()
				.map(AppliedRule::getRuleUid)
				.collect(Collectors.toSet());
		couponUsageService.processCouponCustomerAssignments(ruleUids, context.getCustomer().getEmail());
	}

	@Override
	public void rollback(final PostCaptureCheckoutActionContext context)
	throws EpSystemException {
		// NO OP
	}

	protected CouponUsageService getCouponUsageService() {
		return couponUsageService;
	}

	public void setCouponUsageService(final CouponUsageService couponUsageService) {
		this.couponUsageService = couponUsageService;
	}
}