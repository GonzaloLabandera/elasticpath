/**
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.tags.service.impl;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.cache.Cache;
import com.elasticpath.tags.Tag;
import com.elasticpath.tags.TagSet;
import com.elasticpath.tags.domain.Condition;
import com.elasticpath.tags.domain.ConditionalExpression;
import com.elasticpath.tags.domain.LogicalOperator;
import com.elasticpath.tags.domain.LogicalOperatorType;
import com.elasticpath.tags.service.ConditionDSLBuilder;
import com.elasticpath.tags.service.ConditionEvaluatorService;
import com.elasticpath.tags.service.ConditionOperatorEvaluator;

/**
 * ConditionalExpression evaluator using injected Java-based condition operator evaluators.
 * Evaluates conditions strings that are expressed in tag framework DSL format.
 */
public class JavaConditionEvaluatorServiceImpl implements ConditionEvaluatorService {
	private static final Logger LOG = Logger.getLogger(JavaConditionEvaluatorServiceImpl.class);

	private ConditionDSLBuilder conditionDSLBuilder;
	private Map<String, ConditionOperatorEvaluator> conditionOperatorEvaluators;
	private Cache<String, LogicalOperator> decomposedConditionCache;

	@Override
	public boolean evaluateConditionOnTags(final TagSet tags, final ConditionalExpression condition) {
		return evaluateConditionOnMap(tags.getTags(), condition);
	}

	/**
	 * Evaluates the passed ConditionalExpression using the passed tagMap.
	 *
	 * @param tagMap of string tag key to tag values
	 * @param condition the conditional expression to evaluate
	 * @return the condition evaluation result
	 */
	boolean evaluateConditionOnMap(final Map<String, Tag> tagMap, final ConditionalExpression condition) {
		if (condition.getConditionString().isEmpty()) {
			return true;
		}
		LogicalOperator logicalOperatorTree = decomposedConditionCache.get(
				condition.getConditionString(), key -> conditionDSLBuilder.getLogicalOperationTree(key));
		boolean result = evaluateLogicalOperatorOnMap(tagMap, logicalOperatorTree);
		if (LOG.isDebugEnabled()) {
			LOG.debug("Evaluated condition " + condition.getConditionString() + " with tagSet " + tagMap + " for result " + result);
		}
		return result;
	}

	/**
	 * Evaluates the passed LogicalOperator using the passed tagMap.
	 *
	 * @param tagMap of string tag key to tag values
	 * @param logicalOperatorTree the logical operator to evaluate
	 * @return the condition evaluation result
	 */
	protected boolean evaluateLogicalOperatorOnMap(final Map<String, Tag> tagMap, final LogicalOperator logicalOperatorTree) {
		boolean result;
		if (logicalOperatorTree.getOperatorType() == null) {
			return false;
		} else if (logicalOperatorTree.getConditions().isEmpty() && logicalOperatorTree.getLogicalOperators().isEmpty()) {
			return false;
		} else if (logicalOperatorTree.getOperatorType() == LogicalOperatorType.AND) {
			result = true;
		} else {
			result = false;
		}
		for (Condition condition : logicalOperatorTree.getConditions()) {
			boolean thisResult = evaluateConditionOnMap(tagMap, condition);
			result = aggregateResults(logicalOperatorTree.getOperatorType(), result, thisResult);
		}
		for (LogicalOperator logicalOperator : logicalOperatorTree.getLogicalOperators()) {
			boolean thisResult = evaluateLogicalOperatorOnMap(tagMap, logicalOperator);
			result = aggregateResults(logicalOperatorTree.getOperatorType(), result, thisResult);
		}
		return result;
	}

	private boolean aggregateResults(final LogicalOperatorType logicalOperatorType, final boolean result, final boolean thisResult) {
		if (logicalOperatorType == LogicalOperatorType.AND) {
			return result && thisResult;
		} else {
			return result || thisResult;
		}
	}

	/**
	 * Evaluate the condition by retrieving the correct operator evaluator and using the passed tagMap.
	 *
	 * @param tagMap of string tag key to tag values
	 * @param condition the condition to evaluate
	 * @return the condition evaluation result
	 */
	protected boolean evaluateConditionOnMap(final Map<String, Tag> tagMap, final Condition condition) {
		ConditionOperatorEvaluator conditionOperatorEvaluator = conditionOperatorEvaluators.get(condition.getOperator());
		if (conditionOperatorEvaluator == null) {
			throw new EpServiceException("Unrecognized condition operator " + condition.getOperator());
		}
		boolean thisResult;
		Tag tag = tagMap.get(condition.getTagDefinitionString());
		if (tag == null) {
			thisResult = false;
		} else {
			Object value1 = tag.getValue();
			Object value2 = condition.getTagValue();
			thisResult = conditionOperatorEvaluator.evaluate(value1, value2);
		}
		return thisResult;
	}

	protected ConditionDSLBuilder getConditionDSLBuilder() {
		return conditionDSLBuilder;
	}

	public void setConditionDSLBuilder(final ConditionDSLBuilder conditionDSLBuilder) {
		this.conditionDSLBuilder = conditionDSLBuilder;
	}

	protected Collection<ConditionOperatorEvaluator> getConditionOperatorEvaluators() {
		return conditionOperatorEvaluators.values();
	}

	public void setConditionOperatorEvaluators(final Collection<ConditionOperatorEvaluator> conditionOperatorEvaluators) {
		this.conditionOperatorEvaluators = conditionOperatorEvaluators.stream()
				.collect(Collectors.toMap(ConditionOperatorEvaluator::getOperator, Function.identity()));
	}

	protected Cache<String, LogicalOperator> getDecomposedConditionCache() {
		return decomposedConditionCache;
	}

	public void setDecomposedConditionCache(final Cache<String, LogicalOperator> decomposedConditionCache) {
		this.decomposedConditionCache = decomposedConditionCache;
	}
}
