/**
 * Copyright (c) Elastic Path Software Inc., 2014
 */
package com.elasticpath.domain.shoppingcart.impl;

import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.hamcrest.Matchers;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Before;
import org.junit.Test;

import com.elasticpath.domain.rules.Rule;
import com.elasticpath.domain.rules.RuleAction;
import com.elasticpath.domain.rules.impl.CartAnySkuAmountDiscountActionImpl;
import com.elasticpath.domain.rules.impl.PromotionRuleImpl;
import com.elasticpath.domain.shipping.ShippingServiceLevel;
import com.elasticpath.domain.shoppingcart.DiscountRecord;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.shoppingcart.ShoppingItem;

/**
 * Unit tests for {@link PromotionRecordContainerImpl}.
 */
public class PromotionRecordContainerImplTest {

	private static final long RULE_ID = 1;
	private static final long ACTION_ID = 2;
	private static final String LINE_ITEM_ID = "lineItemId";

	private Rule rule;

	private RuleAction ruleAction;

	private PromotionRecordContainerImpl promotionRecordContainer;

	@org.junit.Rule
	public final JUnitRuleMockery context = new JUnitRuleMockery() {
		{
			setImposteriser(ClassImposteriser.INSTANCE);
		}
	};

	@Mock
	private DiscountRecord discountRecord;

	@Mock
	private ShoppingCart shoppingCart;

	@Before
	public void setUp() {
		promotionRecordContainer = new PromotionRecordContainerImpl(shoppingCart);

		rule = new PromotionRuleImpl();
		rule.setUidPk(RULE_ID);

		ruleAction = new CartAnySkuAmountDiscountActionImpl();
		ruleAction.setUidPk(ACTION_ID);

		context.checking(new Expectations() {
			{
				allowing(discountRecord).getRuleId();
				will(returnValue(RULE_ID));

				allowing(discountRecord).getActionId();
				will(returnValue(ACTION_ID));
			}
		});
	}

	@Test
	public void testAddDiscountRecord() {
		addDiscountRecord();

		DiscountRecord retrievedDiscountRecord = promotionRecordContainer.getDiscountRecord(rule, ruleAction);

		assertSame(discountRecord, retrievedDiscountRecord);
	}

	@Test
	public void testGetInvalidDiscountRecordReturnsNull() {
		DiscountRecord retrievedDiscountRecord = promotionRecordContainer.getDiscountRecord(rule, ruleAction);

		assertNull(retrievedDiscountRecord);
	}

	@Test
	public void testClearRemovesAllDiscountRecords() {
		addDiscountRecord();

		promotionRecordContainer.clear();
		Collection<DiscountRecord> allDiscountRecords = promotionRecordContainer.getAllDiscountRecords();

		assertEquals("There should be no discount records", 0, allDiscountRecords.size());
	}

	@Test
	public void testGetAllDiscountRecords() {
		addDiscountRecord();

		Collection<DiscountRecord> allDiscountRecords = promotionRecordContainer.getAllDiscountRecords();

		assertSame(discountRecord, allDiscountRecords.iterator().next());
	}

	@Test
	public void testGetAppliedRulesByLineItem() {
		final ShoppingItem shoppingItem = context.mock(ShoppingItem.class);
		final ItemDiscountRecordImpl itemDiscountRecord = new ItemDiscountRecordImpl(shoppingItem, RULE_ID, ACTION_ID, BigDecimal.ONE, 1);

		context.checking(new Expectations() {
			{
				allowing(shoppingItem).getGuid();
				will(returnValue(LINE_ITEM_ID));
			}
		});
		promotionRecordContainer.addDiscountRecord(itemDiscountRecord);

		Collection<Long> appliedRules = promotionRecordContainer.getAppliedRulesByLineItem(LINE_ITEM_ID);

		assertThat(appliedRules, Matchers.containsInAnyOrder(RULE_ID));
	}

	@Test
	public void testGetAppliedRulesByLineItemReturnsEmptyCollection() {
		Collection<Long> appliedRules = promotionRecordContainer.getAppliedRulesByLineItem(LINE_ITEM_ID);

		assertEquals("There should no applied rules for the line item", 0, appliedRules.size());
	}

	@Test
	public void testGetAppliedRulesReturnsSetOfAppliedRuleIds() throws Exception {
		final long ruleId1 = 1L;
		final long ruleId2 = 2L;
		final long ruleId3 = 3L;

		promotionRecordContainer.addDiscountRecord(createDiscountRecord(ruleId1));
		promotionRecordContainer.addDiscountRecord(createDiscountRecord(ruleId2));
		promotionRecordContainer.addDiscountRecord(createDiscountRecord(ruleId3));

		final Collection<Long> appliedRules = promotionRecordContainer.getAppliedRules();

		assertThat(appliedRules, Matchers.containsInAnyOrder(ruleId1, ruleId2, ruleId3));
	}

	@Test
	public void testGetAppliedRulesReturnsEmptyCollectionWhenNoRulesApplied() {
		assertThat("A DiscountRecordContainer should be empty when no rules have been applied",
				promotionRecordContainer.getAppliedRules(), empty());
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testGetAppliedRulesReturnsImmutableCopy() throws Exception {
		promotionRecordContainer.addDiscountRecord(discountRecord);

		final Collection<Long> appliedRules = promotionRecordContainer.getAppliedRules();

		appliedRules.clear();
	}

	@Test(expected = UnsupportedOperationException.class)
	public void verifyGetLimitedUsagePromotionRuleCodesReturnsImmutableCopy() throws Exception {
		promotionRecordContainer.addLimitedUsagePromotionRuleCode("rule code", 1L);

		final Map<String, Long> limitedUsagePromotionRuleCodes = promotionRecordContainer.getLimitedUsagePromotionRuleCodes();

		limitedUsagePromotionRuleCodes.clear();
	}

	@Test
	public void testGetLimitedUsagePromotionRuleCodesReturnsEmptyMapWhenNoRulesApplied() {
		assertEquals("A DiscountRecordContainer should be empty when no rules have been applied",
				Collections.<String, Long>emptyMap(), promotionRecordContainer.getLimitedUsagePromotionRuleCodes());
	}

	@Test
	public void verifyClearClearsLimitedUsagePromotionRuleCodes() throws Exception {
		promotionRecordContainer.addLimitedUsagePromotionRuleCode("rule code", 1L);

		promotionRecordContainer.clear();

		assertEquals("Clearing the record container should clear all Limited Usage Promotion Rule records",
				Collections.<String, Long>emptyMap(), promotionRecordContainer.getLimitedUsagePromotionRuleCodes());
	}

	@Test
	public void verifyRemovingLimitedUSagePromotionRuleCodesWithNullSilentlyNoOps() throws Exception {
		promotionRecordContainer.removeLimitedUsagePromotionRuleCode(null);
	}

	@Test
	public void verifyGetAppliedRulesIgnoresRecordsNotApplicableToCurrentlySelectedShippingServiceLevel() throws Exception {
		final long ruleId1 = 1L;
		final long ruleId2 = 2L;
		final long ruleId3 = 3L;

		final String selectedShippingServiceLevelCode = UUID.randomUUID().toString();
		final String unSelectedShippingServiceLevelCode = UUID.randomUUID().toString();

		final ShippingServiceLevel shippingServiceLevelSelected = createShippingServiceLevel(selectedShippingServiceLevelCode);

		final DiscountRecord discountRecord1 = new ShippingDiscountRecordImpl(selectedShippingServiceLevelCode, ruleId1, ACTION_ID, BigDecimal.ONE);
		final DiscountRecord discountRecord2 = new ShippingDiscountRecordImpl(selectedShippingServiceLevelCode, ruleId2, ACTION_ID, BigDecimal.ONE);
		final DiscountRecord discountRecord3 = new ShippingDiscountRecordImpl(unSelectedShippingServiceLevelCode, ruleId3, ACTION_ID, BigDecimal.ONE);

		promotionRecordContainer.addDiscountRecord(discountRecord1);
		promotionRecordContainer.addDiscountRecord(discountRecord2);
		promotionRecordContainer.addDiscountRecord(discountRecord3);

		context.checking(new Expectations() {
			{
				atLeast(1).of(shoppingCart).getSelectedShippingServiceLevel();
				will(returnValue(shippingServiceLevelSelected));
			}
		});

		final Set<Long> appliedRules = promotionRecordContainer.getAppliedRules();
		assertThat(appliedRules, containsInAnyOrder(ruleId1, ruleId2));
	}

	@Test
	public void verifyGetAppliedRulesForShipmentFindsRecordsForGivenShippingServiceLevel() throws Exception {
		final long ruleId1 = 1L;
		final long ruleId2 = 2L;
		final long ruleId3 = 3L;

		final String shippingServiceLevelCode1 = UUID.randomUUID().toString();
		final String shippingServiceLevelCode2 = UUID.randomUUID().toString();

		final ShippingServiceLevel shippingServiceLevel = createShippingServiceLevel(shippingServiceLevelCode2);

		final DiscountRecord discountRecord1 = new ShippingDiscountRecordImpl(shippingServiceLevelCode1, ruleId1, ACTION_ID, BigDecimal.ONE);
		final DiscountRecord discountRecord2 = new ShippingDiscountRecordImpl(shippingServiceLevelCode1, ruleId2, ACTION_ID, BigDecimal.ONE);
		final DiscountRecord discountRecord3 = new ShippingDiscountRecordImpl(shippingServiceLevelCode2, ruleId3, ACTION_ID, BigDecimal.ONE);

		promotionRecordContainer.addDiscountRecord(discountRecord1);
		promotionRecordContainer.addDiscountRecord(discountRecord2);
		promotionRecordContainer.addDiscountRecord(discountRecord3);

		ignoringSelectedShippingServiceLevelOnCart();

		final Set<Long> appliedRules = promotionRecordContainer.getAppliedRulesByShippingServiceLevel(shippingServiceLevel);
		assertThat(appliedRules, containsInAnyOrder(ruleId3));
	}

	@Test
	public void verifyGetAppliedRulesForShipmentIgnoresRecordsMarkedAsSuperseded() throws Exception {
		final long ruleId1 = 1L;
		final long ruleId2 = 2L;
		final long ruleId3 = 3L;

		final String shippingServiceLevelCode = UUID.randomUUID().toString();

		final ShippingServiceLevel shippingServiceLevel = createShippingServiceLevel(shippingServiceLevelCode);

		final DiscountRecord discountRecord1 = new ShippingDiscountRecordImpl(shippingServiceLevelCode, ruleId1, ACTION_ID, BigDecimal.ONE);
		final DiscountRecord discountRecord2 = new ShippingDiscountRecordImpl(shippingServiceLevelCode, ruleId2, ACTION_ID, BigDecimal.ONE);
		final DiscountRecord discountRecord3 = new ShippingDiscountRecordImpl(shippingServiceLevelCode, ruleId3, ACTION_ID, BigDecimal.ONE);

		((AbstractDiscountRecordImpl) discountRecord1).setSuperceded(true);

		promotionRecordContainer.addDiscountRecord(discountRecord1);
		promotionRecordContainer.addDiscountRecord(discountRecord2);
		promotionRecordContainer.addDiscountRecord(discountRecord3);
		ignoringSelectedShippingServiceLevelOnCart();

		final Set<Long> appliedRules = promotionRecordContainer.getAppliedRulesByShippingServiceLevel(shippingServiceLevel);
		assertThat(appliedRules, containsInAnyOrder(ruleId2, ruleId3));
	}

	private void addDiscountRecord() {
		promotionRecordContainer.addDiscountRecord(discountRecord);
	}

	private DiscountRecord createDiscountRecord(final long ruleId) {
		return new CatalogItemDiscountRecordImpl(ruleId, ACTION_ID, BigDecimal.ONE);
	}

	private ShippingServiceLevel createShippingServiceLevel(final String shippingServiceLevelCode) {
		final ShippingServiceLevel shippingServiceLevel = context.mock(ShippingServiceLevel.class, "SSL " + shippingServiceLevelCode);

		context.checking(new Expectations() {
			{
				allowing(shippingServiceLevel).getCode();
				will(returnValue(shippingServiceLevelCode));
			}
		});

		return shippingServiceLevel;
	}

	private void ignoringSelectedShippingServiceLevelOnCart() {
		context.checking(new Expectations() {
			{
				ignoring(shoppingCart).getSelectedShippingServiceLevel();
			}
		});
	}

}
