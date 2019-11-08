/*
 * Copyright (c) Elastic Path Software Inc., 2014
 */
package com.elasticpath.persistence.impl;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.elasticpath.domain.customer.impl.CustomerImpl;
import com.elasticpath.persistence.openjpa.routing.HDSSupportBean;
import com.elasticpath.persistence.openjpa.routing.HDSSupportBeanImpl;
import com.elasticpath.persistence.openjpa.util.QueryUtil;

/**
 * Unit test for the {@code EntityModifiedListener} class.
 */
public class EntityModifiedListenerTest {

	private final EntityModifiedListener entityModifiedListener = new EntityModifiedListener();

	@Before
	public void init() {
		entityModifiedListener.setQueryUtil(new QueryUtil());
	}

	/**
	 * Test if HDSSuportBean stores modified entity's class name if HDS feature is ON.
	 */
	@Test
	public void shouldInformHDSBeanWhenEntityIsModifiedAndHDSFeatureEnabled() {
		String entitySimpleName = "CustomerImpl";

		HDSSupportBean hdsSupportBean = new HDSSupportBeanImpl();
		hdsSupportBean.setHdsSupportEnabled(true);

		entityModifiedListener.setHdsSupportBean(hdsSupportBean);

		entityModifiedListener.endSingleOperation(new CustomerImpl(), null);

		assertThat(hdsSupportBean.isHdsSupportEnabled())
			.isTrue();
		assertThat(hdsSupportBean.getModifiedEntities())
			.contains(entitySimpleName);
	}

	/**
	 * Test that HDSSuportBean doesn't store modified entity's class name if HDS feature is OFF.
	 */
	@Test
	public void shouldNeverInformHDSBeanWhenEntityIsModifiedAndHDSFeatureEnabled() {
		HDSSupportBean hdsSupportBean = new HDSSupportBeanImpl();
		entityModifiedListener.setHdsSupportBean(hdsSupportBean);

		entityModifiedListener.endSingleOperation(new CustomerImpl(), null);

		assertThat(hdsSupportBean.isHdsSupportEnabled())
			.isFalse();
	}

}
