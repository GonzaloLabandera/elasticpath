/**
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.service.rules;

import java.util.Objects;

import com.elasticpath.domain.catalog.Catalog;
import com.elasticpath.domain.sellingcontext.SellingContext;
import com.elasticpath.domain.store.Store;

/**
 * Selling context rule summary.
 */
public class SellingContextRuleSummary {

	private final Long ruleUidPk;

	private final SellingContext sellingContext;

	private final Store store;

	private final Catalog catalog;

	private final Integer scenario;

	/**
	 * Constructor.
	 *
	 * @param scenario       scenario
	 * @param ruleUidPk      rule primary key
	 * @param sellingContext selling context
	 * @param store          store
	 * @param catalog        catalog
	 */
	public SellingContextRuleSummary(final Integer scenario, final Long ruleUidPk, final SellingContext sellingContext, final Store store,
									 final Catalog catalog) {
		this.scenario = scenario;
		this.ruleUidPk = ruleUidPk;
		this.sellingContext = sellingContext;
		this.store = store;
		this.catalog = catalog;
	}

	public Integer getScenario() {
		return scenario;
	}

	public Long getRuleUidPk() {
		return ruleUidPk;
	}

	public SellingContext getSellingContext() {
		return sellingContext;
	}

	public Store getStore() {
		return store;
	}

	public Catalog getCatalog() {
		return catalog;
	}

	@Override
	public boolean equals(final Object object) {
		if (this == object) {
			return true;
		}
		if (object == null || getClass() != object.getClass()) {
			return false;
		}
		SellingContextRuleSummary that = (SellingContextRuleSummary) object;
		return Objects.equals(ruleUidPk, that.ruleUidPk)
				&& Objects.equals(sellingContext, that.sellingContext)
				&& Objects.equals(store, that.store)
				&& Objects.equals(catalog, that.catalog)
				&& Objects.equals(scenario, that.scenario);

	}

	@Override
	public int hashCode() {
		return Objects.hash(ruleUidPk, sellingContext, store, catalog, scenario);
	}
}
