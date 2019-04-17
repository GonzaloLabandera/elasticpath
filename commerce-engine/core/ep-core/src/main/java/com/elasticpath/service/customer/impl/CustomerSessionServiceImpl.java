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
import com.elasticpath.domain.customer.CustomerSessionMemento;
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
 *
 * Note: CustomerSessions being returned do not have a ShoppingCart attached to them.
 */
public class CustomerSessionServiceImpl extends AbstractEpPersistenceServiceImpl implements CustomerSessionService {

	private static final String SHOPPING_START_TIME_TAG = "SHOPPING_START_TIME";

	private static final String SELLING_CHANNEL_TAG = "SELLING_CHANNEL";

	private List<CustomerSessionShopperUpdateHandler> customerSessionShopperUpdateHandlers;

	private ShopperService shopperService;

	private TimeService timeService;

	/**
	 * Adds the given customer session.
	 *
	 * @param customerSession the customer session to add
	 * @throws EpServiceException - in case of any errors
	 */
	@Override
	public void add(final CustomerSession customerSession) throws EpServiceException {
		getPersistenceEngine().save(customerSession.getCustomerSessionMemento());
	}

	@Override
	public void handleShopperChangeAndUpdate(final CustomerSession customerSession, final String storeCode) throws EpServiceException {
		final Customer incomingCustomer = customerSession.getShopper().getCustomer();
		changeShopper(customerSession, storeCode, incomingCustomer);
	}

	@Override
	public void changeFromAnonymousToRegisteredCustomer(final CustomerSession customerSession, final Customer registeredCustomer,
														final String storeCode) throws EpServiceException {
		customerSession.setSignedIn(true);
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

		cleanupShopper(invalidatedShopper, customerSession.getShopper());
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

	private void cleanupShopper(final Shopper invalidatedShopper, final Shopper currentShopper) {
		if (!currentShopper.equals(invalidatedShopper)) {
			shopperService.removeIfOrphaned(invalidatedShopper);
		}
	}

	/**
	 * Finds a {@link CustomerSessionMemento} just by its GUID.
	 *
	 * @param guid {@link CustomerSessionMemento} GUID.
	 * @return {@link CustomerSessionMemento} or null.
	 * @throws EpServiceException - when guid is null.
	 */
	private CustomerSessionMemento findMementoByGuid(final String guid) throws EpServiceException {
		if (guid == null) {
			throw new EpServiceException("Cannot retrieve null guid.");
		}

		final List<CustomerSessionMemento> results = getPersistenceEngine().retrieveByNamedQuery("CUSTOMER_SESSION_FIND_BY_GUID", guid);

		if (results.isEmpty()) {
			return null;
		}

		return results.get(0);
	}


	@Override
	public CustomerSession findByGuid(final String guid) throws EpServiceException {
		final CustomerSessionMemento customerSessionMemento = findMementoByGuid(guid);

		if (customerSessionMemento == null) {
			return null;
		}

		return recreatePersistedCustomerSessionWithShopper(customerSessionMemento);
	}

	/**
	 * Load the customer session with the given UID.
	 *
	 * @param customerSessionUid the customer session UID
	 * @return the customer session if exists, otherwise null
	 * @throws EpServiceException - in case of any errors
	 */
	private CustomerSession load(final long customerSessionUid) throws EpServiceException {
		if (customerSessionUid <= 0) {
			return null;
		}

		final CustomerSessionMemento customerSessionMemento = getPersistentBeanFinder().load(
				ContextIdNames.CUSTOMER_SESSION_MEMENTO, customerSessionUid);
		if (customerSessionMemento == null) {
			return null;
		}

		return recreatePersistedCustomerSessionWithShopper(customerSessionMemento);
	}

	@Override
	public Object getObject(final long uid) throws EpServiceException {
		return load(uid);
	}

	/**
	 * Fires customer session updated event.
	 *
	 * @param customerSession the customer session that was updated.
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
		TagSet tagSet = getBean(ContextIdNames.TAG_SET);
		tagSet.addTag(SELLING_CHANNEL_TAG, new Tag(storeCode));
		tagSet.addTag(SHOPPING_START_TIME_TAG, new Tag(getTimeService().getCurrentTime().getTime()));
		customerSession.setCustomerTagSet(tagSet);
		customerSession.setCurrency(currency);
		return customerSession;
	}

	/**
	 * Creates a new {@link CustomerSession}.  Does not persist it.
	 *
	 * WARNING: Does not attach a Shopper to it!
	 *
	 * @return a new {@link CustomerSession} with no {@link Shopper}.
	 */
	private CustomerSession create() {
		final CustomerSession customerSession = createEmpty();
		final CustomerSessionMemento customerSessionMemento = getBean(ContextIdNames.CUSTOMER_SESSION_MEMENTO);
		customerSession.setCustomerSessionMemento(customerSessionMemento);

		return customerSession;
	}

	private CustomerSession recreatePersistedCustomerSessionWithShopper(final CustomerSessionMemento persistentData) {
		final CustomerSession customerSession = createEmpty();
		customerSession.setCustomerSessionMemento(persistentData);

		final Shopper shopper = shopperService.findByPersistedCustomerSessionMemento(persistentData);
		attachShopper(customerSession, shopper);

		return customerSession;
	}

	private CustomerSession createEmpty() {
		return getBean(ContextIdNames.CUSTOMER_SESSION);
	}

	private void attachShopper(final CustomerSession customerSession, final Shopper shopper) {
		customerSession.setShopper(shopper);
		shopper.updateTransientDataWith(customerSession);
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

	protected CustomerSessionService getCustomerSessionService() {
		return getBean(ContextIdNames.CUSTOMER_SESSION_SERVICE);
	}

	/**
	 * Sets the list of CustomerSessionUpdateHandlers.
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
