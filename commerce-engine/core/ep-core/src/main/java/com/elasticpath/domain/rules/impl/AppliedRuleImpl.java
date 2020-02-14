/*
 * Copyright (c) Elastic Path Software Inc., 2006
 */
package com.elasticpath.domain.rules.impl;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

import org.apache.openjpa.persistence.DataCache;
import org.apache.openjpa.persistence.ElementDependent;
import org.apache.openjpa.persistence.jdbc.ElementForeignKey;
import org.apache.openjpa.persistence.jdbc.ElementJoinColumn;

import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.commons.constants.GlobalConstants;
import com.elasticpath.domain.impl.AbstractLegacyPersistenceImpl;
import com.elasticpath.domain.rules.AppliedCoupon;
import com.elasticpath.domain.rules.AppliedRule;
import com.elasticpath.domain.rules.Rule;

/**
 * Represents a rule that has been applied to an order.
 *
 * Given the limitation to 4000 characters in some oracle dbs encoding this will ensure the rule code is always 32 characters.
 */
@Entity
@Table(name = AppliedRuleImpl.TABLE_NAME)
@DataCache(enabled = false)
public class AppliedRuleImpl extends AbstractLegacyPersistenceImpl implements AppliedRule {

	/** Serial version id. */
	private static final long serialVersionUID = 5000000001L;
	
	/**
	 * The name of the table & generator to use for persistence.
	 */
	public static final String TABLE_NAME = "TAPPLIEDRULE";
	private String ruleName;
	private String ruleCode;
	private long ruleUid;
	private long uidPk;
	private Set<AppliedCoupon> appliedCoupons;
	private String ruleDescription;
	private String ruleDisplayName;
	private String guid;

	/**
	 * Initialize the <code>AppliedRule</code> from the given <code>Rule</code>.
	 *
	 * @param rule   the <code>Rule</code>
	 * @param locale the <code>Locale</code>
	 */
	@Override
	public void initialize(final Rule rule, final Locale locale) {
		setRuleName(rule.getName());
		setRuleCode(rule.getRuleCode());
		setRuleUid(rule.getUidPk());
		setRuleDisplayName(rule.getDisplayName(locale));
		setRuleDescription(rule.getDescription());
		setAppliedCoupons(new HashSet<>());
		super.initialize();
	}

	/**
	 * Set the name of the applied rule.
	 * @param ruleName the rule name
	 */
	@Override
	public void setRuleName(final String ruleName) {
		this.ruleName = ruleName;
	}

	/**
	 * Get the name of the applied rule.
	 * @return the name of the applied rule
	 */
	@Override
	@Basic
	@Column(name = "RULE_NAME", nullable = false)
	public String getRuleName() {
		return this.ruleName;
	}

	/**
	 * Set the rule engine code for the applied rule.
	 * @param ruleCode the rule code.
	 */
	@Override
	public void setRuleCode(final String ruleCode) {
		this.ruleCode = ruleCode;
	}

	/**
	 * Get the rule engine code for the applied rule.
	 * @return the rule engine code.
	 */
	@Override
	@Lob
	@Column(name = "RULE_CODE", length = GlobalConstants.LONG_TEXT_MAX_LENGTH, nullable = false)
	public String getRuleCode() {
		return this.ruleCode;
	}

	/**
	 * Set the uid of the applied rule.
	 * @param ruleUid the UID
	 */
	@Override
	public void setRuleUid(final long ruleUid) {
		this.ruleUid = ruleUid;
	}

	/**
	 * Get the UID of the applied rule.
	 * @return the UID
	 */
	@Override
	@Basic
	@Column(name = "RULE_UID", nullable = false)
	public long getRuleUid() {
		return this.ruleUid;
	}

	/**
	 * Gets the unique identifier for this domain model object.
	 *
	 * @return the unique identifier.
	 */
	@Override
	@Id
	@Column(name = "UIDPK")
	@GeneratedValue(strategy = GenerationType.TABLE, generator = TABLE_NAME)
	@TableGenerator(name = TABLE_NAME, table = "JPA_GENERATED_KEYS", pkColumnName = "ID",
			valueColumnName = "LAST_VALUE", pkColumnValue = TABLE_NAME, allocationSize = HIGH_CONCURRENCY_ALLOCATION_SIZE)
	public long getUidPk() {
		return this.uidPk;
	}

	/**
	 * Sets the unique identifier for this domain model object.
	 *
	 * @param uidPk the new unique identifier.
	 */
	@Override
	public void setUidPk(final long uidPk) {
		this.uidPk = uidPk;
	}

	@Override
	@OneToMany(targetEntity = AppliedCouponImpl.class, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@ElementJoinColumn(name = "APPLIED_RULE_UID", nullable = false, updatable = false)
	@ElementForeignKey
	@ElementDependent
	public Set<AppliedCoupon> getAppliedCoupons() {
		return this.appliedCoupons;
	}

	public void setAppliedCoupons(final Set<AppliedCoupon> appliedCoupons) {
		this.appliedCoupons = appliedCoupons;
	}

	@Override
	public void addAppliedCoupon(final String couponCode, final int usageCount) {
		AppliedCoupon appliedCoupon = getPrototypeBean(ContextIdNames.APPLIED_COUPON, AppliedCoupon.class);
		appliedCoupon.setCouponCode(couponCode);
		appliedCoupon.setUsageCount(usageCount);
		this.appliedCoupons.add(appliedCoupon);
	}
	/**
	 * @param visitor the visitor to accept for this applied rule.
	 */
	@Override
	public void accept(final Visitor visitor) {
		visitor.visit(this);
	}


	protected void setRuleDescription(final String description) {
		this.ruleDescription = description;
	}

	@Basic
	@Column(name = "RULE_DESCRIPTION")
	@Override
	public String getRuleDescription() {
		return ruleDescription;
	}

	protected void setRuleDisplayName(final String ruleDisplayName) {
		this.ruleDisplayName = ruleDisplayName;
	}

	@Basic
	@Column(name = "RULE_DISPLAY_NAME")
	@Override
	public String getRuleDisplayName() {
		return ruleDisplayName;
	}

	@Basic
	@Column(name = "GUID")
	@Override
	public String getGuid() {
		return guid;
	}

	@Override
	public void setGuid(final String guid) {
		this.guid = guid;
	}

	@Override
	public void initialize() {
		setGuid(UUID.randomUUID().toString());
	}
}
