/**
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.tags.service.impl.conditionoperators;

import com.elasticpath.tags.service.ConditionOperatorEvaluator;

/**
 * Condition evaluator for the "equalsIgnoreCase" operator.
 */
public class EqualsIgnoreCaseOperatorEvaluatorImpl implements ConditionOperatorEvaluator {
	@Override
	public String getOperator() {
		return "equalsIgnoreCase";
	}

	@Override
	public boolean evaluate(final Object value1, final Object value2) {
		if (value1 == null && value2 == null) {
			return true;
		}
		if (value1 == null || value2 == null) {
			return false;
		}
		String value1Str = (String) value1;
		String value2Str = (String) value2;
		return value1Str.equalsIgnoreCase(value2Str);
	}
}
