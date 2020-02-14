/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.service.shoppingcart.actions.impl;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.customer.impl.CustomerSessionImpl;
import com.elasticpath.domain.event.OrderEventHelper;
import com.elasticpath.domain.order.Order;
import com.elasticpath.service.shoppingcart.actions.CheckoutActionContext;
import com.elasticpath.service.shoppingcart.actions.FinalizeCheckoutActionContext;
import com.elasticpath.tags.Tag;
import com.elasticpath.tags.TagSet;

/**
 * Test for {@link LogShoppingContextDateOverriddenAction}.
 */
@RunWith(MockitoJUnitRunner.class)
public class LogShoppingContextDateOverriddenActionTest {

	@Mock
	private Order order;

	@Mock
	private OrderEventHelper orderEventHelper;

	@InjectMocks
	private LogShoppingContextDateOverriddenAction fixture;

	private FinalizeCheckoutActionContext checkoutContext;

	/**
	 * Tag name.
	 */
	private static final String SHOPPING_DATE_OVERRIDE_KEY = "SHOPPING_CONTEXT_DATE_OVERRIDE";

	/**
	 * Expected note value.
	 */
	private static final String EXPECTED_NOTE = "This order was placed with the SHOPPING_CONTEXT_DATE_OVERRIDE trait specified with a value of "
			+ "date_value";

	@Test
	public void testOrderNoteCreated() {
		createCheckoutContext(true);

		fixture.execute(checkoutContext);

		verify(orderEventHelper).logOrderNote(order, EXPECTED_NOTE);
	}

	@Test
	public void testOrderNoteNotCreated() {
		createCheckoutContext(false);

		fixture.execute(checkoutContext);

		verify(orderEventHelper, never()).logOrderNote(order, EXPECTED_NOTE);
	}

	private TagSet createTagSet(final boolean withOverrideTag) {
		TagSet tags = new TagSet();

		if (withOverrideTag) {
			tags.addTag(SHOPPING_DATE_OVERRIDE_KEY, new Tag("date_value"));
		}

		return tags;
	}

	private void createCheckoutContext(final boolean withOverrideTag) {
		CustomerSessionImpl customerSession = new CustomerSessionImpl();
		customerSession.setCustomerTagSet(createTagSet(withOverrideTag));
		CheckoutActionContext checkoutActionContext = new CheckoutActionContextImpl(
				null, null, customerSession, false, false, null, null
		);
		checkoutActionContext.setOrder(order);
		checkoutContext = new FinalizeCheckoutActionContextImpl(checkoutActionContext);
	}
}
