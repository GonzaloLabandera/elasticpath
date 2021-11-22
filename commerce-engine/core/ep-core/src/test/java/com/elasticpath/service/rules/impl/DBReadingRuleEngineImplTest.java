/**
 * Copyright (c) Elastic Path Software Inc., 2015
 */
package com.elasticpath.service.rules.impl;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.Date;

import org.jmock.States;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.jmock.lib.legacy.ClassImposteriser;

import org.drools.core.impl.InternalKnowledgeBase;

import com.elasticpath.cache.SimpleTimeoutCache;
import com.elasticpath.domain.catalog.Catalog;
import com.elasticpath.domain.rules.EpRuleBase;
import com.elasticpath.domain.rules.RuleScenarios;
import com.elasticpath.domain.store.Store;
import com.elasticpath.service.misc.TimeService;
import com.elasticpath.service.rules.RuleService;

/**
 * Test case for {@link DBReadingRuleEngineImpl}.
 */
public class DBReadingRuleEngineImplTest {

	private static final String CODE = "some code";
	private static final long UID = 2000001L;
	private static final int YEAR = 2020;
	private static final String SECOND_STEP = "second-step";

	private DBReadingRuleEngineImpl ruleEngine;

	@Rule
	public final JUnitRuleMockery context = new JUnitRuleMockery() {
		{
			setImposteriser(ClassImposteriser.INSTANCE);
		}
	};

	private RuleService mockRuleService;

	private TimeService mockTimeService;

	private InternalKnowledgeBase mockOldRuleBase;
	
	private InternalKnowledgeBase mockNewRuleBase;
	
	private Store mockStore;
	
	private Catalog mockCatalog;

	@SuppressWarnings("unchecked")
	private final SimpleTimeoutCache<String, Boolean> mockSimpleTimeoutCache = context.mock(SimpleTimeoutCache.class);

	/**
	 * Prepares for tests.
	 * 
	 * @throws Exception in case of any errors
	 */

	@Before
	public void setUp() throws Exception {
		ruleEngine = new DBReadingRuleEngineImpl();
		mockRuleService = context.mock(RuleService.class);
		ruleEngine.setRuleService(mockRuleService);
		mockTimeService = context.mock(TimeService.class);
		ruleEngine.setTimeService(mockTimeService);
		context.checking(new Expectations() {
			{
				allowing(mockTimeService).getCurrentTime();
				will(returnValue(new Date()));
			}
		});

		mockOldRuleBase = context.mock(InternalKnowledgeBase.class, "old rule base");
		mockNewRuleBase = context.mock(InternalKnowledgeBase.class, "new rule base");

		mockStore = context.mock(Store.class);
		context.checking(new Expectations() {
			{
				allowing(mockStore).getCode();
				will(returnValue(CODE));
			}
		});
		
		mockCatalog = context.mock(Catalog.class);
		final Catalog catalog = mockCatalog;
		context.checking(new Expectations() {
			{
				allowing(mockStore).getCatalog();
				will(returnValue(catalog));

				allowing(mockCatalog).getCode();
				will(returnValue(CODE));

				allowing(mockSimpleTimeoutCache).get(CODE);
				will(returnValue(null));

				allowing(mockSimpleTimeoutCache).put(CODE, Boolean.TRUE);
			}
		});

	}

	/**
	 * Test method for {@link DBReadingRuleEngineImpl#getCartRuleBase(Store)} where a rule has not
	 * yet been put into the database.
	 */
	@Test
	public void testCartRuleNotInDB() {
		final Store store = mockStore;
		context.checking(new Expectations() {
			{

				oneOf(mockRuleService).findRuleBaseByScenario(store, null, RuleScenarios.CART_SCENARIO);
				will(returnValue(null));
			}
		});
		assertNotNull(ruleEngine.getCartRuleBase(store));
	}

	/**
	 * Test method for {@link DBReadingRuleEngineImpl#getCartRuleBase(Store)} where a rule is in
	 * the database.
	 */
	@Test
	public void testCartRuleInDB() {
		final Store store = mockStore;

		final InternalKnowledgeBase ruleBase = mockOldRuleBase;
		final EpRuleBase mockEpRuleBase = context.mock(EpRuleBase.class);
		context.checking(new Expectations() {
			{
				allowing(mockEpRuleBase).getRuleBase();
				will(returnValue(ruleBase));


				oneOf(mockRuleService).findRuleBaseByScenario(store, null, RuleScenarios.CART_SCENARIO);
				will(returnValue(mockEpRuleBase));
			}
		});
		assertSame(ruleBase, ruleEngine.getCartRuleBase(store));
	}

	/**
	 * Test method for {@link DBReadingRuleEngineImpl#getCartRuleBase(Store)} where a rule is
	 * cached, but hasn't been updated.
	 */
	@Test
	public void testCartRuleCachedNotUpdated() {
		final Store store = mockStore;

		final InternalKnowledgeBase ruleBase = mockOldRuleBase;
		final EpRuleBase mockEpRuleBase = context.mock(EpRuleBase.class);
		final EpRuleBase epRuleBase = mockEpRuleBase;
		context.checking(new Expectations() {
			{
				allowing(mockEpRuleBase).getRuleBase();
				will(returnValue(ruleBase));

				allowing(mockRuleService).findRuleBaseByScenario(store, null, RuleScenarios.CART_SCENARIO);
				will(returnValue(epRuleBase));
			}
		});
		ruleEngine.getCartRuleBase(store);

		final Date date = new Date();
		context.checking(new Expectations() {
			{
				allowing(epRuleBase).getUidPk();
				will(returnValue(UID));

				allowing(mockEpRuleBase).getLastModifiedDate();
				will(returnValue(date));

				allowing(mockRuleService).getModifiedDateForRuleBase(UID);
				will(returnValue(date));
			}
		});
		assertTrue(ruleEngine.getCartRuleBase(store) instanceof InternalKnowledgeBase);
	}

	/**
	 * Test method for {@link DBReadingRuleEngineImpl#getCartRuleBase(Store)} where a rule is
	 * cached, but it is now updated.
	 */
	@Test
	public void testCartRuleCachedAndUpdated() {
		final Store store = mockStore;
		final States state = context.states("invocation-step");

		final InternalKnowledgeBase ruleBase = mockOldRuleBase;
		final EpRuleBase mockEpRuleBase = context.mock(EpRuleBase.class);
		final EpRuleBase epRuleBase = mockEpRuleBase;
		context.checking(new Expectations() {
			{
				allowing(mockEpRuleBase).getRuleBase();
				will(returnValue(ruleBase));

				allowing(mockRuleService).findRuleBaseByScenario(store, null, RuleScenarios.CART_SCENARIO);
				will(returnValue(epRuleBase));
				when(state.isNot(SECOND_STEP));
			}
		});
		ruleEngine.getCartRuleBase(store);

		state.become(SECOND_STEP);
		final InternalKnowledgeBase updatedRuleBase = mockNewRuleBase;
		final EpRuleBase mockUpdatedEpRuleBase = context.mock(EpRuleBase.class, "updated rule base");
		final Calendar oldCalendar = Calendar.getInstance();
		oldCalendar.set(YEAR, Calendar.JANUARY, 1);
		final Calendar newCalendar = Calendar.getInstance();
		newCalendar.set(YEAR, Calendar.FEBRUARY, 1);
		context.checking(new Expectations() {
			{
				allowing(epRuleBase).getUidPk();
				will(returnValue(UID));

				allowing(mockEpRuleBase).getLastModifiedDate();
				will(returnValue(oldCalendar.getTime()));

				allowing(mockRuleService).getModifiedDateForRuleBase(UID);
				will(returnValue(newCalendar.getTime()));

				allowing(mockUpdatedEpRuleBase).getRuleBase();
				will(returnValue(updatedRuleBase));

				allowing(mockRuleService).findRuleBaseByScenario(store, null, RuleScenarios.CART_SCENARIO);
				will(returnValue(mockUpdatedEpRuleBase));
			}
		});
		assertSame(updatedRuleBase, ruleEngine.getCartRuleBase(store));
	}

	/**
	 * Test method for {@link DBReadingRuleEngineImpl#getCatalogRuleBase(Store)} where a rule has
	 * not yet been put into the database.
	 */
	@Test
	public void testCatalogRuleNotInDB() {
		final Store store = mockStore;
		final Catalog catalog = mockCatalog;
		context.checking(new Expectations() {
			{

				oneOf(mockRuleService).findRuleBaseByScenario(null, catalog, RuleScenarios.CATALOG_BROWSE_SCENARIO);
				will(returnValue(null));
			}
		});
		assertNotNull(ruleEngine.getCatalogRuleBase(store));
	}

	/**
	 * Test method for {@link DBReadingRuleEngineImpl#getCatalogRuleBase(Store)} where a rule is
	 * in the database.
	 */
	@Test
	public void testCatalogRuleInDB() {
		final Store store = mockStore;
		final Catalog catalog = mockCatalog;

		final InternalKnowledgeBase ruleBase = mockOldRuleBase;
		final EpRuleBase mockEpRuleBase = context.mock(EpRuleBase.class);
		final EpRuleBase epRuleBase = mockEpRuleBase;
		context.checking(new Expectations() {
			{
				allowing(mockEpRuleBase).getRuleBase();
				will(returnValue(ruleBase));


				oneOf(mockRuleService).findRuleBaseByScenario(null, catalog, RuleScenarios.CATALOG_BROWSE_SCENARIO);
				will(returnValue(epRuleBase));
			}
		});
		assertSame(ruleBase, ruleEngine.getCatalogRuleBase(store));
	}

	/**
	 * Test method for {@link DBReadingRuleEngineImpl#getCatalogRuleBase(Store)} where a rule is
	 * cached, but hasn't been updated.
	 */
	@Test
	public void testCatalogRuleCachedNotUpdated() {
		final Store store = mockStore;
		final Catalog catalog = mockCatalog;

		final InternalKnowledgeBase ruleBase = mockOldRuleBase;
		final EpRuleBase mockEpRuleBase = context.mock(EpRuleBase.class);
		final EpRuleBase epRuleBase = mockEpRuleBase;
		context.checking(new Expectations() {
			{
				allowing(mockEpRuleBase).getRuleBase();
				will(returnValue(ruleBase));

				allowing(mockRuleService).findRuleBaseByScenario(null, catalog, RuleScenarios.CATALOG_BROWSE_SCENARIO);
				will(returnValue(epRuleBase));
			}
		});
		ruleEngine.getCatalogRuleBase(store);

		final Date date = new Date();
		context.checking(new Expectations() {
			{
				allowing(epRuleBase).getUidPk();
				will(returnValue(UID));

				allowing(epRuleBase).getLastModifiedDate();
				will(returnValue(date));

				allowing(mockRuleService).getModifiedDateForRuleBase(UID);
				will(returnValue(date));
			}
		});
		assertTrue(ruleEngine.getCatalogRuleBase(store) instanceof InternalKnowledgeBase);
	}

	/**
	 * Test method for {@link DBReadingRuleEngineImpl#getCatalogRuleBase(Store)} where a rule is
	 * cached, but it is now updated.
	 */
	@Test
	public void testCatalogRuleCachedAndUpdated() {
		final Store store = mockStore;
		final Catalog catalog = mockCatalog;
		final States state = context.states("invocation-step");

		final InternalKnowledgeBase ruleBase = mockOldRuleBase;
		final EpRuleBase mockEpRuleBase = context.mock(EpRuleBase.class);
		final EpRuleBase epRuleBase = mockEpRuleBase;
		context.checking(new Expectations() {
			{
				allowing(mockEpRuleBase).getRuleBase();
				will(returnValue(ruleBase));

				allowing(mockRuleService).findRuleBaseByScenario(null, catalog, RuleScenarios.CATALOG_BROWSE_SCENARIO);
				will(returnValue(epRuleBase));
				when(state.isNot(SECOND_STEP));
			}
		});
		ruleEngine.getCatalogRuleBase(store);

		state.become(SECOND_STEP);
		final InternalKnowledgeBase updatedRuleBase = mockNewRuleBase;
		final EpRuleBase mockUpdatedEpRuleBase = context.mock(EpRuleBase.class, "updated rule base");
		final Calendar oldCalendar = Calendar.getInstance();
		oldCalendar.set(YEAR, Calendar.JANUARY, 1);
		final Calendar newCalendar = Calendar.getInstance();
		newCalendar.set(YEAR, Calendar.FEBRUARY, 1);
		context.checking(new Expectations() {
			{
				allowing(epRuleBase).getUidPk();
				will(returnValue(UID));

				allowing(epRuleBase).getLastModifiedDate();
				will(returnValue(oldCalendar.getTime()));

				allowing(mockRuleService).getModifiedDateForRuleBase(UID);
				will(returnValue(newCalendar.getTime()));

				allowing(mockUpdatedEpRuleBase).getRuleBase();
				will(returnValue(updatedRuleBase));

				allowing(mockRuleService).findRuleBaseByScenario(null, catalog, RuleScenarios.CATALOG_BROWSE_SCENARIO);
				will(returnValue(mockUpdatedEpRuleBase));
			}
		});
		assertSame(updatedRuleBase, ruleEngine.getCatalogRuleBase(store));
	}
}
