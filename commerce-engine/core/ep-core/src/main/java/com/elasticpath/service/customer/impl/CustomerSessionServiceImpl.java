/*
 * Copyright (c) Elastic Path Software Inc., 2006
 */
package com.elasticpath.service.customer.impl;

import java.util.Currency;
import java.util.List;

import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.CustomerSession;
import com.elasticpath.domain.shopper.Shopper;
import com.elasticpath.service.customer.CustomerSessionService;
import com.elasticpath.service.customer.CustomerSessionShopperUpdateHandler;
import com.elasticpath.service.impl.AbstractEpPersistenceServiceImpl;
import com.elasticpath.service.misc.TimeService;
import com.elasticpath.service.shopper.ShopperService;
import com.elasticpath.tags.Tag;
import com.elasticpath.tags.TagSet;

/**
 * Service for retrieving and saving CustomerSessions.
 * <p>
 * Note: CustomerSessions being returned do not have a ShoppingCart attached to them.
 */
public class CustomerSessionServiceImpl extends AbstractEpPersistenceServiceImpl implements CustomerSessionService {

	private static final String SHOPPING_START_TIME_TAG = "SHOPPING_START_TIME";

	private static final String SELLING_CHANNEL_TAG = "SELLING_CHANNEL";

	private List<CustomerSessionShopperUpdateHandler> customerSessionShopperUpdateHandlers;

	private ShopperService shopperService;

	private TimeService timeService;

	@Override
	public void changeFromAnonymousToRegisteredCustomer(final CustomerSession customerSession, final Customer registeredCustomer,
														final String storeCode) throws EpServiceException {
		changeShopper(customerSession, storeCode, registeredCustomer);
	}

	private void changeShopper(final CustomerSession customerSession, final String storeCode, final Customer incomingCustomer) {
		final Shopper invalidatedShopper = customerSession.getShopper();
		final Shopper currentShopper = shopperService.findOrCreateShopper(incomingCustomer, storeCode);
		customerSession.setShopper(currentShopper);

		currentShopper.updateTransientDataWith(customerSession);
		updateCustomerSessionForShopperChange(customerSession);

		// Call list of CustomerSessionShopperUpdateHandlers.
		handleShopperUpdate(customerSession, invalidatedShopper);

		if (!currentShopper.equals(invalidatedShopper)) {
			shopperService.remove(invalidatedShopper);
		}

		shopperService.save(customerSession.getShopper());
	}

	/**
	 * Updates the {@link CustomerSession} in case of a Shopper change.
	 *
	 * @param customerSession a {@link CustomerSession}
	 */
	protected void updateCustomerSessionForShopperChange(final CustomerSession customerSession) {
		// Nothing needs to be done in the stock implementation.
		customerSession.setPriceListStackValid(false);
	}

	@Override
	public Object getObject(final long uid) throws EpServiceException {
		throw new UnsupportedOperationException("Not supported");
	}

	/**
	 * Fires customer session updated event.
	 *
	 * @param customerSession    the customer session that was updated.
	 * @param invalidatedShopper the recently invalidated shopping context.
	 */
	void handleShopperUpdate(final CustomerSession customerSession, final Shopper invalidatedShopper) {
		for (CustomerSessionShopperUpdateHandler handler : customerSessionShopperUpdateHandlers) {
			handler.invalidateShopper(customerSession, invalidatedShopper);
		}
	}

	@Override
	public CustomerSession createWithShopper(final Shopper shopper) {
		final CustomerSession customerSession = create();

		customerSession.setShopper(shopper);
		shopper.updateTransientDataWith(customerSession);

		return customerSession;
	}

	@Override
	public CustomerSession initializeCustomerSessionForPricing(final CustomerSession customerSession, final String storeCode,
															   final Currency currency) {
		TagSet tagSet = getPrototypeBean(ContextIdNames.TAG_SET, TagSet.class);
		tagSet.addTag(SELLING_CHANNEL_TAG, new Tag(storeCode));
		tagSet.addTag(SHOPPING_START_TIME_TAG, new Tag(getTimeService().getCurrentTime().getTime()));
		customerSession.setCustomerTagSet(tagSet);
		customerSession.setCurrency(currency);
		return customerSession;
	}

	/**
	 * Creates a new {@link CustomerSession}.  Does not persist it.
	 * <p>
	 * WARNING: Does not attach a Shopper to it!
	 *
	 * @return a new {@link CustomerSession} with no {@link Shopper}.
	 */
	private CustomerSession create() {
		return createEmpty();
	}

	private CustomerSession createEmpty() {
		return getPrototypeBean(ContextIdNames.CUSTOMER_SESSION, CustomerSession.class);
	}

	// Settings/Getters.
	// ------------------

	/**
	 * Sets shopperService.
	 *
	 * @param shopperService the service to set.
	 */
	public void setShopperService(final ShopperService shopperService) {
		this.shopperService = shopperService;
	}

	/**
	 * Sets the list of CustomerSessionUpdateHandlers.
	 *
	 * @param customerSessionShopperUpdateHandlers listeners to set
	 */
	public void setCustomerSessionUpdateHandlers(
			final List<CustomerSessionShopperUpdateHandler> customerSessionShopperUpdateHandlers) {
		this.customerSessionShopperUpdateHandlers = customerSessionShopperUpdateHandlers;
	}

	public void setTimeService(final TimeService timeService) {
		this.timeService = timeService;
	}

	protected TimeService getTimeService() {
		return timeService;
	}
}
