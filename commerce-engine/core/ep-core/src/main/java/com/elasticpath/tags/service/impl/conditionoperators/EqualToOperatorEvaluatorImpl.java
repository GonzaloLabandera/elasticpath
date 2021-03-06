/**
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.tags.service.impl.conditionoperators;

import org.apache.commons.lang3.ObjectUtils;

import com.elasticpath.tags.service.ConditionOperatorEvaluator;

/**
 * Condition evaluator for the "equalTo" operator.
 */
public class EqualToOperatorEvaluatorImpl implements ConditionOperatorEvaluator {
	@Override
	public String getOperator() {
		return "equalTo";
	}

	@Override
	public boolean evaluate(final Object value1, final Object value2) {
		return ObjectUtils.equals(value1, value2);
	}
}
