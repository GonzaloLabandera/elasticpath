/**
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.tags.service.impl.conditionoperators;

import com.elasticpath.tags.service.ConditionOperatorEvaluator;

/**
 * Condition evaluator for the "lessThan" operator.
 */
public class LessThanOperatorEvaluatorImpl implements ConditionOperatorEvaluator {
	@Override
	public String getOperator() {
		return "lessThan";
	}

	@Override
	@SuppressWarnings({"rawtypes", "unchecked"})
	public boolean evaluate(final Object value1, final Object value2) {
		if (value1 == null && value2 == null) {
			return true;
		}
		if (value1 == null || value2 == null) {
			return false;
		}

		Object value1Internal = value1;
		Object value2Internal = value2;
		// Convert Integer to Long to avoid cast exception
		if (value1 instanceof Integer) {
			value1Internal = Long.valueOf((Integer) value1);
		}

		if (value2 instanceof Integer) {
			value2Internal = Long.valueOf((Integer) value2);
		}
		Comparable<Comparable> value1Comparable = (Comparable<Comparable>) value1Internal;
		Comparable<Comparable> value2Comparable = (Comparable<Comparable>) value2Internal;
		return value1Comparable.compareTo(value2Comparable) < 0;
	}
}
