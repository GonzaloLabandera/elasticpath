/**
 * Copyright (c) Elastic Path Software Inc., 2017
 */
package com.elasticpath.cmclient.policy;

import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Rule;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import com.elasticpath.cmclient.core.BeanLocator;
import com.elasticpath.cmclient.core.helpers.ChangeSetHelper;
import com.elasticpath.commons.beanframework.BeanFactory;

/**
 * Base Class for State Policy Tests.
 */
public class StatePolicyTestBase {

	@Rule
	public final MockitoRule rule = MockitoJUnit.rule();

	@Mock
	private BeanFactory beanFactory;
	@Mock
	private ChangeSetHelper changeSetHelper;

	@Before
	public void setUp() {
		BeanLocator.setBeanFactory(beanFactory);
		when(beanFactory.getSingletonBean(ChangeSetHelper.BEAN_ID, ChangeSetHelper.class)).thenReturn(changeSetHelper);
	}
}
