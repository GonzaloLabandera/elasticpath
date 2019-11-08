/**
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.tags.service.impl.conditionoperators;

import com.elasticpath.tags.service.ConditionOperatorEvaluator;

/**
 * Condition evaluator for the "notIncludes" operator.
 */
public class NotIncludesOperatorEvaluatorImpl implements ConditionOperatorEvaluator {
	@Override
	public String getOperator() {
		return "notIncludes";
	}

	@Override
	public boolean evaluate(final Object value1, final Object value2) {
		return !(new IncludesOperatorEvaluatorImpl().evaluate(value1, value2));
	}
}
