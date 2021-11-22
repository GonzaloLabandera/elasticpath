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
	public void changeFromSingleSessionToRegisteredCustomer(final Shopper singleSessionShopper,
															final Customer registeredCustomer, final String storeCode) throws EpServiceException {
		final Shopper registeredShopper = shopperService.findOrCreateShopper(registeredCustomer, storeCode);
		registeredShopper.setCustomerSession(singleSessionShopper.getCustomerSession());
		updateCustomerSessionForShopperChange(registeredShopper.getCustomerSession());

		// Call list of CustomerSessionShopperUpdateHandlers.
		handleShopperUpdate(singleSessionShopper, registeredShopper);

		if (!registeredShopper.equals(singleSessionShopper)) {
			shopperService.remove(singleSessionShopper);
		}

		shopperService.save(registeredShopper);
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
	 * Fires shopper updated event.
	 *
	 * @param singleSessionShopper the recently invalidated shopper
	 * @param registeredShopper the new shopper that replaces the invalidated shopper
	 */
	void handleShopperUpdate(final Shopper singleSessionShopper, final Shopper registeredShopper) {
		for (CustomerSessionShopperUpdateHandler handler : customerSessionShopperUpdateHandlers) {
			handler.invalidateShopper(singleSessionShopper, registeredShopper);
		}
	}

	@Override
	public CustomerSession createWithShopper(final Shopper shopper) {
		final CustomerSession customerSession = create();
		shopper.setCustomerSession(customerSession);
		return customerSession;
	}

	@Override
	public void initializeCustomerSessionForPricing(final CustomerSession customerSession, final String storeCode,
															   final Currency currency) {
		TagSet tagSet = getPrototypeBean(ContextIdNames.TAG_SET, TagSet.class);
		tagSet.addTag(SELLING_CHANNEL_TAG, new Tag(storeCode));
		tagSet.addTag(SHOPPING_START_TIME_TAG, new Tag(getTimeService().getCurrentTime().getTime()));
		customerSession.setCustomerTagSet(tagSet);
		customerSession.setCurrency(currency);
	}

	/**
	 * Creates a new {@link CustomerSession}.  Does not persist it.
	 * <p>
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
