/**
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.tags.service.impl.conditionoperators;

import java.util.Collection;

import com.elasticpath.tags.service.ConditionOperatorEvaluator;

/**
 * Condition evaluator for the "includes" operator.
 */
public class IncludesOperatorEvaluatorImpl implements ConditionOperatorEvaluator {
	@Override
	public String getOperator() {
		return "includes";
	}

	@Override
	@SuppressWarnings("rawtypes")
	public boolean evaluate(final Object value1, final Object value2) {
		if (value1 == null && value2 == null) {
			return true;
		}
		if (value1 == null || value2 == null) {
			return false;
		}
		if (value1 instanceof String) {
			String value1Str = (String) value1;
			String value2Str = (String) value2;
			return value1Str.contains(value2Str);
		} else if (value1 instanceof Collection) {
			Collection value1Collection = (Collection) value1;
			return value1Collection.contains(value2);
		}
		return false;
	}
}
