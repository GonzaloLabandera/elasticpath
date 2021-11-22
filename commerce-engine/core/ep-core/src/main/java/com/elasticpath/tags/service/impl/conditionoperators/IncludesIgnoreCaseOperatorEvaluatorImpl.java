/**
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.tags.service.impl.conditionoperators;

import java.util.Collection;

import org.apache.commons.lang3.ObjectUtils;

import com.elasticpath.tags.service.ConditionOperatorEvaluator;

/**
 * Condition evaluator for the "includesIgnoreCase" operator.
 */
public class IncludesIgnoreCaseOperatorEvaluatorImpl implements ConditionOperatorEvaluator {
	@Override
	public String getOperator() {
		return "includesIgnoreCase";
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
		if (value1 instanceof String) {
			String value1Str = ((String) value1).toUpperCase();
			String value2Str = ((String) value2).toUpperCase();
			return value1Str.contains(value2Str);
		} else if (value1 instanceof Collection) {
			Collection value1Collection = (Collection) value1;
			return value1Collection.stream()
					.anyMatch(newValue1 -> ObjectUtils.equals(newValue1, value2));
		}
		return false;
	}
}
