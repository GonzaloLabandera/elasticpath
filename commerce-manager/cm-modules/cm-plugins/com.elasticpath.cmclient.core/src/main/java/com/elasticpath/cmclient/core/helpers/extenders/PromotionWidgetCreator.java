/**
 * Copyright (c) Elastic Path Software Inc., 2017
 */
package com.elasticpath.cmclient.core.helpers.extenders;

import com.elasticpath.domain.rules.RuleParameter;
import org.eclipse.core.databinding.DataBindingContext;
import com.elasticpath.domain.catalog.Catalog;
import com.elasticpath.domain.rules.Rule;
import com.elasticpath.domain.store.Store;

/**
 * Interface for Promotion Widget Creator.
 */
public interface PromotionWidgetCreator {

    /**
     * Execute rule.
     * @param parent the parent.
     * @param parentWidget the parent widget.
     * @param ruleParameter the rule parameter.
     * @param parentComposite the parent composite.
     * @param bindingContext the binding context.
     * @param policyActionContainer the policy action container.
     * @param rule the rule.
     * @param scenario the scenario.
     * @param store the store.
     * @param catalog the catalog.
     */
	@SuppressWarnings({"checkstyle:parameternumber", "PMD.ExcessiveParameterList"})
	void execute(Object parent, Object parentWidget, RuleParameter ruleParameter,
			Object parentComposite, DataBindingContext bindingContext,
			Object policyActionContainer, Rule rule,
			int scenario, Store store, Catalog catalog);

    /**
     * Check if a given key is valid.
     *
     * @param key the key
     * @return the validity of the key.
     */
    boolean isValid(String key);

}
