/*
 * Copyright (c) Elastic Path Software Inc., 2006
 */
package com.elasticpath.service.customer.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Currency;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.ElasticPath;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.CustomerSession;
import com.elasticpath.domain.customer.impl.CustomerImpl;
import com.elasticpath.domain.customer.impl.CustomerSessionImpl;
import com.elasticpath.domain.shopper.Shopper;
import com.elasticpath.domain.shopper.ShopperMemento;
import com.elasticpath.domain.shopper.impl.ShopperImpl;
import com.elasticpath.domain.shopper.impl.ShopperMementoImpl;
import com.elasticpath.service.customer.CustomerSessionShopperUpdateHandler;
import com.elasticpath.service.misc.TimeService;
import com.elasticpath.service.shopper.ShopperService;
import com.elasticpath.tags.Tag;
import com.elasticpath.tags.TagSet;

/**
 * Test <code>CustomerSessionServiceImpl</code>.
 */
@RunWith(MockitoJUnitRunner.class)
public class CustomerSessionServiceImplTest {
	private static final String STORE_CODE = "STORE_CODE";

	private static final String SHOPPING_START_TIME_TAG = "SHOPPING_START_TIME";

	private static final String SELLING_CHANNEL_TAG = "SELLING_CHANNEL";

	@Mock
	private ElasticPath elasticPath;
	@Mock
	private ShopperService shopperService;
	@Mock
	private TimeService timeService;
	@Mock
	private CustomerSessionShopperUpdateHandler cartMerger;
	@Mock
	private CustomerSessionShopperUpdateHandler wishlistMerger;
	@Mock
	private CustomerSessionShopperUpdateHandler customerConsentMerger;

	private final List<CustomerSessionShopperUpdateHandler> updateHandlers = new ArrayList<>();

	@InjectMocks
	private CustomerSessionServiceImpl customerSessionService;

	/**
	 * Sets up the test cases.
	 *
	 * @throws Exception in case of error
	 */
	@Before
	public void setUp() throws Exception {
		updateHandlers.add(cartMerger);
		updateHandlers.add(wishlistMerger);
		updateHandlers.add(customerConsentMerger);

		customerSessionService.setCustomerSessionUpdateHandlers(updateHandlers);
	}

	/**
	 * Test initializing customer session for pricing.
	 */
	@Test
	public void testInitializeCustomerSessionForPricing() {
		final Date shoppingStartTime = new Date();
		final Currency currency = Currency.getInstance("CAD");

		when(timeService.getCurrentTime()).thenReturn(shoppingStartTime);
		when(elasticPath.getPrototypeBean(ContextIdNames.TAG_SET, TagSet.class)).thenReturn(new TagSet());

		final CustomerSession customerSession = new CustomerSessionImpl();
		customerSessionService.initializeCustomerSessionForPricing(customerSession, STORE_CODE, currency);

		TagSet tagSet = customerSession.getCustomerTagSet();
		assertNotNull("TagSet should not be null", tagSet);

		Tag storeTag = tagSet.getTagValue(SELLING_CHANNEL_TAG);
		assertEquals("Store tag is incorrect.", STORE_CODE, storeTag.getValue());

		Tag startTimeTag = tagSet.getTagValue(SHOPPING_START_TIME_TAG);
		assertEquals("Shopping start time tag is incorrect.", shoppingStartTime.getTime(), startTimeTag.getValue());

		assertEquals("Currency is incorrect.", currency, customerSession.getCurrency());
	}

	@Test
	public void shouldDeleteAnonymousShopperWhenAnonymousAndRegisteredShoppersAreDifferent() {
		CustomerSession anonymousCustomerSession = new CustomerSessionImpl();
		Shopper anonymousShopper = createShopper("anonymousShopperGuid");
		anonymousCustomerSession.setShopper(anonymousShopper);

		Customer registeredCustomer = new CustomerImpl();
		Shopper registeredShopper = createShopper("registerShopperGuid");

		when(shopperService.findOrCreateShopper(registeredCustomer, STORE_CODE)).thenReturn(registeredShopper);

		customerSessionService.changeFromAnonymousToRegisteredCustomer(anonymousCustomerSession, registeredCustomer, STORE_CODE);

		verify(shopperService).remove(anonymousShopper);
		verify(shopperService).save(registeredShopper);
		verify(cartMerger).invalidateShopper(anonymousCustomerSession, anonymousShopper);
		verify(wishlistMerger).invalidateShopper(anonymousCustomerSession, anonymousShopper);
		verify(customerConsentMerger).invalidateShopper(anonymousCustomerSession, anonymousShopper);
	}

	@Test
	public void shouldNotDeleteAnonymousShopperWhenAnonymousAndRegisteredShoppersAreSame() {
		CustomerSession anonymousCustomerSession = new CustomerSessionImpl();
		Shopper anonymousShopper = createShopper("anonymousShopperGuid");
		anonymousCustomerSession.setShopper(anonymousShopper);

		Customer registeredCustomer = new CustomerImpl();
		Shopper registeredShopper = anonymousShopper;

		when(shopperService.findOrCreateShopper(registeredCustomer, STORE_CODE)).thenReturn(registeredShopper);

		customerSessionService.changeFromAnonymousToRegisteredCustomer(anonymousCustomerSession, registeredCustomer, STORE_CODE);

		verify(shopperService, never()).remove(anonymousShopper);
		verify(shopperService).save(registeredShopper);
		verify(cartMerger).invalidateShopper(anonymousCustomerSession, anonymousShopper);
		verify(wishlistMerger).invalidateShopper(anonymousCustomerSession, anonymousShopper);
		verify(customerConsentMerger).invalidateShopper(anonymousCustomerSession, anonymousShopper);
	}

	private Shopper createShopper(final String shopperGuid) {
		Shopper shopper = new ShopperImpl();
		ShopperMemento shopperMemento = new ShopperMementoImpl();
		shopper.setShopperMemento(shopperMemento);
		shopper.setGuid(shopperGuid);

		return shopper;
	}
}
