/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.service.rules.impl;

import java.util.Date;

import org.kie.api.KieBase;

import com.elasticpath.domain.catalog.Catalog;
import com.elasticpath.domain.rules.EpRuleBase;
import com.elasticpath.domain.store.Store;

/**
 * Immutable implementation of EpRuleBase used for caching when there is no TRULESTORAGE record
 * found in the database.
 */
public class EpRuleBaseNotPresentImpl implements EpRuleBase {
	private final Date lastModifiedDate;
	private final KieBase kieBase;

	/**
	 * Constructor.
	 * @param lastModifiedDate last modified date
	 * @param kieBase the SOLR knowledge base
	 */
	public EpRuleBaseNotPresentImpl(final Date lastModifiedDate, final KieBase kieBase) {
		this.lastModifiedDate = lastModifiedDate;
		this.kieBase = kieBase;
	}

	@Override
	public long getUidPk() {
		return 0;
	}

	@Override
	public void setUidPk(final long uidPk) {
		// No-op
	}

	@Override
	public int getScenarioId() {
		return 0;
	}

	@Override
	public void setScenarioId(final int ruleScenarioId) {
		// No-op
	}

	@Override
	public Store getStore() {
		return null;
	}

	@Override
	public void setStore(final Store store) {
		// No-op
	}

	@Override
	public Catalog getCatalog() {
		return null;
	}

	@Override
	public void setCatalog(final Catalog catalog) {
		// No-op
	}

	@Override
	public KieBase getRuleBase() {
		return kieBase;
	}

	@Override
	public void setRuleBase(final KieBase ruleBase) {
		// No-op
	}

	@Override
	public Date getLastModifiedDate() {
		return lastModifiedDate;
	}

	@Override
	public boolean isPersisted() {
		return false;
	}
}
