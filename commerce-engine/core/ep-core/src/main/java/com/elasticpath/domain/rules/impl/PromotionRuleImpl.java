/*
 * Copyright (c) Elastic Path Software Inc., 2006
 */
package com.elasticpath.domain.rules.impl;

import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;
import org.apache.openjpa.persistence.DataCache;

import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.EpDomainException;
import com.elasticpath.domain.rules.ImpliedRuleCondition;
import com.elasticpath.domain.rules.Rule;
import com.elasticpath.domain.rules.RuleAction;
import com.elasticpath.domain.rules.RuleCondition;
import com.elasticpath.domain.rules.RuleElement;
import com.elasticpath.domain.rules.RuleElementType;
import com.elasticpath.domain.rules.RuleScenarios;
import com.elasticpath.domain.sellingcontext.SellingContext;

/**
 * Represents a rules engine rule that involves product promotions. Conditions and actions of promotion rules will have the following objects
 * available: -delegate -cart -product
 */
@SuppressWarnings("PMD.CyclomaticComplexity")
@Entity
@Table(name = "TRULE")
@DataCache(enabled = false)
public class PromotionRuleImpl extends AbstractRuleImpl {
	/**
	 * Serial version id.
	 */
	private static final long serialVersionUID = 5000000001L;

	private static final String DROOLS_OR = " || ";

	private static final String SALIENCE = "salience";

	//transient fields
	private String storeCode;
	private Long limitedUseConditionId;

	/**
	 * Default constructor.
	 */
	public PromotionRuleImpl() {
		// No implementation
	}

	/**
	 * Custom constructor used in JPA query - RULE_ID_FIND_BY_COUPON_CODE in rules-orm.xml.
	 *
	 * @param uidPk rule id
	 */
	public PromotionRuleImpl(final long uidPk) {
		setUidPk(uidPk);
	}

	/**
	 * Custom constructor used in JPA query - RULE_FIND_BY_COUPON_CODE in rules-orm.xml.
	 *
	 * @param uidPk rule id
	 * @param storeCode store code
	 * @param isEnabled rule enabled flag
	 * @param startDate rule start date
	 * @param endDate rule end date
	 * @param sellingContext selling contet
	 * @param curentLupNumber current Lup number
	 */
	public PromotionRuleImpl(final long uidPk, final String storeCode, final boolean isEnabled,
							final Date startDate, final Date endDate, final SellingContext sellingContext,
							final long curentLupNumber) {

		this.storeCode = storeCode;

		setUidPk(uidPk);
		setEnabled(isEnabled);
		setStartDate(startDate);
		setEndDate(endDate);
		setSellingContext(sellingContext);
		setCurrentLupNumber(curentLupNumber);
	}

	@Override
	@Transient
	public String getStoreCode() {
		return storeCode;
	}

	/**
	 *
	 * @return true if rule has a limited use condition
	 */
	@Override
	@Transient
	public boolean hasLimitedUseCondition() {
		return limitedUseConditionId != null;
	}

	@Override
	public void setLimitedUseConditionId(final Long limitedUseConditionId) {
		this.limitedUseConditionId = limitedUseConditionId;
	}

	/**
	 * Returns the Drools code corresponding to this rule.
	 *
	 * @return the rule code.
	 * @throws EpDomainException if the rule is not well formed
	 */
	@Override
	@Transient
	@SuppressWarnings("PMD.ConsecutiveLiteralAppends")
	public String getRuleCode() throws EpDomainException {
		validate();
		StringBuilder code = new StringBuilder();
		long startDate = 0;
		long endDate = 0;

		if (this.getStartDate() != null) {
			startDate = this.getStartDate().getTime();
		}
		if (this.getEndDate() != null) {
			endDate = this.getEndDate().getTime();
		}
		
		for (String agenda : this.getAgendaGroups()) {
			// When
			code.append("rule \"" + getCode() + " " + agenda + "\"\n");
			code.append("\t# rule name: " + getName() + "\n");
			code.append("\t" + SALIENCE + " " + getSalience() + "\n");
			code.append("\tagenda-group \"" + agenda + "\"\n");
			code.append("\twhen\n");
			code.append("\t\tdelegate: PromotionRuleDelegate ( )\n");
			code.append("\t\tcurrency: Currency ( )\n\n");

			if (getRuleSet().getScenario() == RuleScenarios.CART_SCENARIO) {
				code.append("\t\tcart: ShoppingCart ( ) \n");
				code.append("\t\tdiscountItemContainer: DiscountItemContainer ( ) \n");
			}
			if (getRuleSet().getScenario() == RuleScenarios.CATALOG_BROWSE_SCENARIO) {
				code.append("\t\tproduct: Product ( )\n\n");
				code.append("\t\tprices: Map ( )\n\n");
			}

			if (getRuleSet().getScenario() == RuleScenarios.CART_SCENARIO) {
				code.append("exists ActiveRule ( ruleId == ").append(getUidPk()).append(")");	// NOPMD -- integration with tagging framework
			}
			if (getRuleSet().getScenario() == RuleScenarios.CATALOG_BROWSE_SCENARIO) {
				//"Enable Date/Time" and "Expiration Date/Time" for catalogue promotion only.
				code.append("\t\teval ( delegate.checkDateRange(\"").append(startDate).append("\",\"").append(endDate).append("\") )\n"); // NOPMD
			}
			code.append("\t\teval ( delegate.checkEnabled(\"").append(this.isEnabled()).append("\") )\n"); // NOPMD
	
			// Conditions and eligibilities
			appendConditions(this.getConditions(), this.getConditionOperator(), code);
	
			// Then
			code.append("\tthen\n");
	
			// Actions
			for (Iterator<RuleAction> actionIter = this.getActionsBySalience().iterator(); actionIter.hasNext();) {
				RuleAction currAction = actionIter.next();
				if (currAction.getAgendaGroup().equals(agenda)) {
					code.append(currAction.getRuleCode());
				}
			}
	
			code.append("end\n");
		}
		
		return code.toString();
	}

	/**
	 * Append a set of RuleConditions or RuleEligibilities to the rule code.
	 *
	 * @param eligOrConditionsToAppend the RuleConditions or RuleEligibilities to append
	 * @param operator the eligibility or condition operator
	 * @param code the StringBuffer to append the RuleConditions or RuleEligibilities to
	 */
	@SuppressWarnings("PMD.NPathComplexity")
	private void appendConditions(final Set<? extends RuleElement> eligOrConditionsToAppend, final boolean operator, final StringBuilder code) {
		if (operator == OR_OPERATOR && !eligOrConditionsToAppend.isEmpty()) {
			code.append("\t\t");
		}

		Set<String> impliedConditionsCode = new HashSet<>();

		boolean isRuleLineEmpty = true;
		boolean conditionsExist = false;

		for (Iterator<? extends RuleElement> conditionIter = eligOrConditionsToAppend.iterator(); conditionIter.hasNext();) {
			RuleElement currCondition = conditionIter.next();

			if (currCondition instanceof ImpliedRuleCondition) {
				impliedConditionsCode.add("\n\n" + currCondition.getRuleCode());
				continue;
			}
			
			if (StringUtils.isEmpty(currCondition.getRuleCode())) {
				continue;
			}
			conditionsExist = true;

			if (operator == Rule.AND_OPERATOR) {
				code.append("\t\teval (").append(currCondition.getRuleCode()).append(")\n");
			} else if (operator == Rule.OR_OPERATOR) {
				if (isRuleLineEmpty) {
					code.append("\t\teval ( ");
					isRuleLineEmpty = false;
				} else {
					code.append(DROOLS_OR);
				}

				code.append(currCondition.getRuleCode().trim());
			}
		}

		if (operator == OR_OPERATOR && conditionsExist) {
			code.append(" )\n\n");
		}
		
		for (String conditionCode : impliedConditionsCode) {
			code.append(getImpliedConditionCode(conditionCode));
		}
	}

	private String getImpliedConditionCode(final String impliedConditionCode) {
		if (StringUtils.isNotEmpty(impliedConditionCode)) {
			return "\t\teval( " + impliedConditionCode + " )";
		}
		return StringUtils.EMPTY;
	}

	/**
	 * Finds <code>RuleCondition</code> representing coupon code for promotion.
	 * 
	 * @return <code>RuleCondition</code> instance of type LIMITED_USE_COUPON_CODE_CONDITION or null if it doesn't exist
	 */
	RuleCondition findPromoCodeCondition() {
		final Set<RuleCondition> conditions = this.getConditions();

		final Iterator<RuleCondition> conditionIterator = conditions.iterator();

		while (conditionIterator.hasNext()) {
			final RuleCondition condition = conditionIterator.next();

			if (RuleElementType.LIMITED_USE_COUPON_CODE_CONDITION.getPropertyKey().equals(condition.getType())) {
				return condition;
			}
		}

		return null;
	}

	/**
	 * Returns the set of agenda groups specified by all actions of this rule.
	 * @return the set of agenda groups or <code>null</code> if no agenda group
	 * is required. The agenda group should be a constant defined on the
	 * <code>RuleAction</code> interface.
	 */
	@Transient
	protected Set<String> getAgendaGroups() {
		Set<String> agendas = new HashSet<>();
		for (RuleAction currAction : this.getActions()) {
			agendas.add(currAction.getAgendaGroup());
		}
		return agendas;
	}

	
	@Override
	@Transient
	public boolean isCouponEnabled() {
		return findPromoCodeCondition() != null;
	}

	@Override
	public void setCouponEnabled(final boolean couponEnabled) {
		RuleCondition couponCondition = findPromoCodeCondition(); 
		if (couponEnabled) {
			if (couponCondition == null) {
				couponCondition = getBean(ContextIdNames.LIMITED_USE_COUPON_CODE_COND);
				addCondition(couponCondition);
			}
		} else {
			if (couponCondition != null) {
				removeCondition(couponCondition);
			}
		}
	}
}
