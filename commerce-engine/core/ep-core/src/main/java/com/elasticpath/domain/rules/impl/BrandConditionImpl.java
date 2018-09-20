/*
 * Copyright (c) Elastic Path Software Inc., 2006
 */
package com.elasticpath.domain.rules.impl;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

import org.apache.openjpa.persistence.DataCache;

import com.elasticpath.domain.EpDomainException;
import com.elasticpath.domain.rules.RuleCondition;
import com.elasticpath.domain.rules.RuleElementType;
import com.elasticpath.domain.rules.RuleExceptionType;
import com.elasticpath.domain.rules.RuleParameter;
import com.elasticpath.domain.rules.RuleScenarios;

/**
 * Rule condition that checks the brand of a product.
 */
@Entity
@DiscriminatorValue("brandCondition")
@DataCache(enabled = false)
public class BrandConditionImpl extends AbstractRuleElementImpl implements RuleCondition {
	/**
	 * Serial version id.
	 */
	private static final long serialVersionUID = 5000000001L;
	
	private static final RuleElementType RULE_ELEMENT_TYPE = RuleElementType.BRAND_CONDITION;

	private static final String[] PARAMETER_KEYS = new String[] { RuleParameter.BRAND_CODE_KEY };

	/** Set of <code>RuleExcetion</code> allowed for this <code>RuleAction</code>. */
	private static final RuleExceptionType[] ALLOWED_EXCEPTIONS = new RuleExceptionType[] { };

	private static final int EXPECTED_RULE_CODE_LENGTH = 61;

	/**
	 * Returns the <code>RuleElementType</code> associated with this <code>RuleElement</code> subclass. The <code>RuleElementType</code>'s
	 * property key must match this class' discriminator-value and the spring context bean id for this <code>RuleElement</code> implementation.
	 * 
	 * @return the <code>RuleElementType</code> associated with this <code>RuleElement</code> subclass.
	 */
	@Override
	@Transient
	public RuleElementType getElementType() {
		return RULE_ELEMENT_TYPE;
	}

	/**
	 * Returns the kind of this <code>RuleElement</code> (e.g. eligibility, condition, action).
	 *
	 * @return the kind
	 */
	@Override
	@Transient
	protected String getElementKind() {
		return CONDITION_KIND;
	}

	/**
	 * Check if this rule element is valid in the specified scenario.
	 *
	 * @param scenarioId the Id of the scenario to check (defined in RuleScenarios)
	 * @return true if the rule element is applicable in the given scenario
	 */
	@Override
	public boolean appliesInScenario(final int scenarioId) {
		return scenarioId == RuleScenarios.CATALOG_BROWSE_SCENARIO;
	}

	/**
	 * Return the array of the allowed <code>RuleException</code> types for the rule.
	 *
	 * @return an array of <code>RuleExceptionType</code> of the allowed <code>RuleException</code> types for the rule.
	 */
	@Override
	@Transient
	public RuleExceptionType[] getAllowedExceptions() {
		return ALLOWED_EXCEPTIONS.clone();
	}

	/**
	 * Returns the Drools code corresponding to this rule condition.
	 *
	 * @return the rule code.
	 * @throws EpDomainException if the object model is not well formed
	 */
	@Override
	@Transient
	public String getRuleCode() throws EpDomainException {
		validate();
		StringBuilder sbf = new StringBuilder(EXPECTED_RULE_CODE_LENGTH);
		sbf.append(" delegate.catalogBrandIs(product, "); //NOPMD - Split for readability
		sbf.append("true, \"");
		sbf.append(this.getParamValue(RuleParameter.BRAND_CODE_KEY));
		sbf.append("\", \"").append(this.getExceptionStr());
		sbf.append("\") ");
		return sbf.toString();
	}

	/**
	 * Return the array of the required parameter keys for the rule.
	 *
	 * @return an array of String of the required parameter keys for the rule.
	 */
	@Override
	@Transient
	public String[] getParameterKeys() {
		return PARAMETER_KEYS.clone();
	}
}
