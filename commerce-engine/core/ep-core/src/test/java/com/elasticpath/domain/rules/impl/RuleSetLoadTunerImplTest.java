/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.domain.rules.impl;

import static com.elasticpath.persistence.support.FetchFieldConstants.RULES;
import static org.mockito.Mockito.verify;

import junit.framework.TestCase;
import org.apache.openjpa.persistence.FetchPlan;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.rules.RuleSetLoadTuner;

/**
 * Test <code>RuleSetLoadTunerImpl</code>.
 */
@RunWith(MockitoJUnitRunner.class)
public class RuleSetLoadTunerImplTest extends TestCase {
	@Mock
	private FetchPlan mockFetchPlan;

	/**
	 * Test method for 'com.elasticpath.domain.rules.impl.RuleSetLoadTunerImpl.contains(RuleSetLoadTuner)'.
	 */
	@Test
	public void testContains() {
		final RuleSetLoadTuner loadTuner1 = new RuleSetLoadTunerImpl();
		final RuleSetLoadTuner loadTuner2 = new RuleSetLoadTunerImpl();

		// Always contains a <code>null<code> tuner.
		assertTrue(loadTuner1.contains(null));

		// Empty load tuner contains each other.
		assertTrue(loadTuner1.contains(loadTuner2));
		assertTrue(loadTuner2.contains(loadTuner1));

		// Load tuner 1 has more flags set than load tuner 2
		loadTuner1.setLoadingRules(true);
		assertTrue(loadTuner1.contains(loadTuner2));
		assertFalse(loadTuner2.contains(loadTuner1));
	}

	/**
	 * Test method for 'com.elasticpath.domain.rules.impl.RuleSetLoadTunerImpl.merge(RuleSetLoadTuner)'.
	 */
	@Test
	public void testMerge() {
		final RuleSetLoadTuner loadTuner1 = new RuleSetLoadTunerImpl();
		final RuleSetLoadTuner loadTuner2 = new RuleSetLoadTunerImpl();

		// Merge null doesn't change anything
		loadTuner1.merge(null);
		assertTrue(loadTuner1.contains(loadTuner2));
		assertTrue(loadTuner2.contains(loadTuner1));

		// Load tuner 1 contains 2, we will just return load tuner 1
		loadTuner1.setLoadingRules(true);
		RuleSetLoadTuner loadTuner3 = loadTuner2.merge(loadTuner1);
		assertSame(loadTuner3, loadTuner1);
	}

	@Test
	public void shouldConfigureWithLazyFieldsOnly() {
		final RuleSetLoadTuner loadTuner = new RuleSetLoadTunerImpl();

		loadTuner.setLoadingRules(true);

		loadTuner.configure(mockFetchPlan);

		verify(mockFetchPlan).addField(RuleSetImpl.class, RULES);
	}
}