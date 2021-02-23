/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.service.rules.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.commons.util.SimpleCache;
import com.elasticpath.domain.catalog.Catalog;
import com.elasticpath.domain.rules.RuleScenarios;
import com.elasticpath.domain.sellingcontext.SellingContext;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.service.rules.SellingContextRuleSummary;
import com.elasticpath.domain.shopper.Shopper;
import com.elasticpath.domain.shopper.ShopperMemento;
import com.elasticpath.tags.TagSet;
import com.elasticpath.tags.domain.TagDictionary;
import com.elasticpath.tags.service.ConditionEvaluatorService;

@RunWith(MockitoJUnitRunner.class)
public class RuleEngineRuleStrategyTest {

	private static final String CART_RULE_IDS = "CART_RULE_IDS";

	private static final String CATALOG_CODE = "CATALOG_CODE";

	private static final String STORE_CODE = "STORE_CODE";

	private static final Long RULE_UIDPK = 1L;

	@Mock
	private ConditionEvaluatorService conditionEvaluatorService;

	@Mock
	private RuleEngineDataStrategy dataStrategy;

	@InjectMocks
	private RuleEngineRuleStrategy ruleEngineRuleStrategy;

	@Mock
	private Shopper shopper;

	@Mock
	private ShoppingCart shoppingCart;

	@Mock
	private SimpleCache simpleCache;

	@Mock
	private Catalog catalog;

	@Mock
	private ShopperMemento shopperMemento;

	@Mock
	private SellingContext sellingContext;

	@Mock
	private TagSet tagSet;

	@Before
	public void setup() {
		when(shoppingCart.getShopper()).thenReturn(shopper);
		when(shopper.getCache()).thenReturn(simpleCache);
		when(shopper.getShopperMemento()).thenReturn(shopperMemento);
		when(shopper.getTagSet()).thenReturn(tagSet);

		when(shopperMemento.getStoreCode()).thenReturn(STORE_CODE);

		when(simpleCache.isInvalidated(CART_RULE_IDS)).thenReturn(true);

		when(catalog.getCode()).thenReturn(CATALOG_CODE);

		List<SellingContextRuleSummary> objects =
				Collections.singletonList(new SellingContextRuleSummary(null, RULE_UIDPK, sellingContext, null, null));

		RuleValidationResultEnum ruleValidationResultEnum = RuleValidationResultEnum.SUCCESS;

		when(sellingContext.isSatisfied(conditionEvaluatorService, tagSet, TagDictionary.DICTIONARY_PROMOTIONS_SHOPPER_GUID,
				TagDictionary.DICTIONARY_TIME_GUID)).thenReturn(ruleValidationResultEnum);

		when(dataStrategy.findActiveRuleIdSellingContext(anyInt(), anyString())).thenReturn(objects);
	}

	@Test
	public void testEvaluateApplicableRulesForCatalog() {
		List<Long> result = ruleEngineRuleStrategy.evaluateApplicableRules(catalog, tagSet);

		verify(dataStrategy).findActiveRuleIdSellingContext(anyInt(), eq(CATALOG_CODE));

		assertThat(result.get(0)).isEqualTo(RULE_UIDPK);
	}

	@Test
	public void testEvaluateApplicableRulesForShopper() {
		List<Long> result = ruleEngineRuleStrategy.evaluateApplicableRules(shoppingCart);

		verify(simpleCache).putItem(eq(CART_RULE_IDS), anyList());
		verify(dataStrategy).findActiveRuleIdSellingContext(anyInt(), eq(STORE_CODE));

		assertThat(result.get(0)).isEqualTo(RULE_UIDPK);
	}

	@Test
	public void testEvaluateApplicableRulesForStoreWithValidCache() {

		when(simpleCache.isInvalidated(CART_RULE_IDS)).thenReturn(false);

		ruleEngineRuleStrategy.evaluateApplicableRules(shoppingCart);

		verify(simpleCache).getItem(CART_RULE_IDS);
	}

	@Test
	public void testGetSellingContextForRulesIsNotNull() {

		when(dataStrategy.findActiveRuleIdSellingContext(anyInt(), anyString())).thenReturn(null);

		List<SellingContextRuleSummary> result =
				ruleEngineRuleStrategy.getSellingContextWithRuleUidpk(CATALOG_CODE, RuleScenarios.CATALOG_BROWSE_SCENARIO);

		verify(dataStrategy)
				.findActiveRuleIdSellingContext(eq(RuleScenarios.CATALOG_BROWSE_SCENARIO), eq(CATALOG_CODE));

		assertThat(result).isEmpty();
	}
}
