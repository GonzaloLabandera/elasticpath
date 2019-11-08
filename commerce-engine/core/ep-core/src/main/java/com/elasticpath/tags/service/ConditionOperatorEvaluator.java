/**
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.tags.service;

/**
 * Strategy interface for condition evaluator for the "contains" operator.
 */
public interface ConditionOperatorEvaluator {
	/**
	 * A string representation of the operator that this strategy class implements.
	 * @return the operator name
	 */
	String getOperator();

	/**
	 * Evaluate value1 and value2 using the correct strategy for the operator.
	 * @param value1 the first operand
	 * @param value2 the second operand
	 * @return the result
	 */
	boolean evaluate(Object value1, Object value2);
}
