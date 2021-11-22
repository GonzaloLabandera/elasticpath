/**
 * Copyright (c) Elastic Path Software Inc., 2013
 */
package com.elasticpath.tools.sync.merge.configuration.impl;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.rules.RuleSet;
import com.elasticpath.domain.rules.RuleSetLoadTuner;
import com.elasticpath.persistence.api.Persistable;
import com.elasticpath.service.rules.RuleSetService;
import com.elasticpath.tools.sync.exception.SyncToolConfigurationException;

/**
 * 
 * The rule set locator class.
 *
 */
public class RuleSetLocatorImpl extends AbstractEntityLocator {
	
	private RuleSetService ruleSetService;

	private BeanFactory beanFactory;

	/**
	 * @param ruleSetService the ruleSetService to set
	 */
	public void setRuleSetService(final RuleSetService ruleSetService) {
		this.ruleSetService = ruleSetService;
	}

	public void setBeanFactory(final BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	@Override
	public boolean isResponsibleFor(final Class<?> clazz) {
		return RuleSet.class.isAssignableFrom(clazz);
	}

	@Override
	public Persistable locatePersistence(final String guid, final Class<?> clazz)
			throws SyncToolConfigurationException {
		RuleSetLoadTuner ruleSetLoadTuner = beanFactory.getPrototypeBean(ContextIdNames.RULE_SET_LOAD_TUNER, RuleSetLoadTuner.class);
		return ruleSetService.findByName(guid, ruleSetLoadTuner);
	}

}
