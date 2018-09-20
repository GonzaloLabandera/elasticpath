/**
 * Copyright (c) Elastic Path Software Inc., 2016
 */
package com.elasticpath.domain.coupon.specifications;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.elasticpath.domain.rules.Coupon;
import com.elasticpath.domain.rules.Rule;
import com.elasticpath.domain.specifications.Specification;
import com.elasticpath.service.rules.CouponUsageService;
import com.elasticpath.service.rules.RuleService;

/**
 * Coupon validity in cart specification.
 */
public class ValidCouponUseSpecification implements Specification<PotentialCouponUse> {

	private static final Logger LOG = Logger.getLogger(ValidCouponUseSpecification.class.getName());

	private RuleService ruleService;

	private CouponUsageService couponUsageService;

	@Override
	public boolean isSatisfiedBy(final PotentialCouponUse potentialCouponUse) {
		Coupon coupon = potentialCouponUse.getCoupon();
		if (coupon == null) {
			return false;
		}
		String code = coupon.getCouponCode();
		if (StringUtils.isEmpty(code)) {
			return false;
		}

		Rule rule = ruleService.findByPromoCode(code);
		if (!ruleService.isRuleValid(rule, potentialCouponUse.getStoreCode())) {
			LOG.debug(String.format("Promotion code '%s' is not valid", code));
			return false;
		}

		if (!isCouponUsageValidForRule(potentialCouponUse, rule.hasLimitedUseCondition())) {
			LOG.debug(String.format("Promotion code '%s' usage is not valid", code));
			return false;
		}

		return true;
	}

	private boolean isCouponUsageValidForRule(final PotentialCouponUse potentialCouponUse, final boolean hasLimitedUseCondition) {
			return !hasLimitedUseCondition || couponUsageService.isValidCouponUsage(potentialCouponUse.getCustomerEmailAddress(),
																						potentialCouponUse.getCoupon(), null);

	}

	public RuleService getRuleService() {
		return ruleService;
	}

	public void setRuleService(final RuleService ruleService) {
		this.ruleService = ruleService;
	}

	public CouponUsageService getCouponUsageService() {
		return couponUsageService;
	}

	public void setCouponUsageService(final CouponUsageService couponUsageService) {
		this.couponUsageService = couponUsageService;
	}
}
