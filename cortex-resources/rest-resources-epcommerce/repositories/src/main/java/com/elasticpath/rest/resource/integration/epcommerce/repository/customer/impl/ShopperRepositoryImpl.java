/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.customer.impl;

import java.util.Currency;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import com.google.common.annotations.VisibleForTesting;
import io.reactivex.Single;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.domain.customer.CustomerSession;
import com.elasticpath.domain.shopper.Shopper;
import com.elasticpath.domain.store.Store;
import com.elasticpath.rest.ResourceOperationFailure;
import com.elasticpath.rest.cache.CacheResult;
import com.elasticpath.rest.identity.Subject;
import com.elasticpath.rest.identity.util.SubjectUtil;
import com.elasticpath.rest.resource.ResourceOperationContext;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.ShopperRepository;
import com.elasticpath.service.customer.CustomerSessionService;
import com.elasticpath.service.pricing.SessionPriceListLifecycle;
import com.elasticpath.service.shopper.ShopperService;
import com.elasticpath.service.store.StoreService;
import com.elasticpath.tags.Tag;
import com.elasticpath.tags.TagSet;

/**
 * A repository for {@link Shopper}s.
 */
@Singleton
@Named("shopperRepository")
public class ShopperRepositoryImpl implements ShopperRepository {
	private static final Logger LOG = LogManager.getLogger(ShopperRepositoryImpl.class);

	private final ResourceOperationContext resourceOperationContext;
	private final ShopperService shopperService;
	private final CustomerSessionService customerSessionService;
	private final SessionPriceListLifecycle sessionPriceListLifecycle;
	private final StoreService storeService;
	private final CustomerSessionTagSetFactory tagSetFactory;

	/**
	 * Constructor.
	 *
	 * @param resourceOperationContext  the resource operation context
	 * @param shopperService            the shopper service
	 * @param customerSessionService    the customer session service
	 * @param sessionPriceListLifecycle the price list lifecycle manager.
	 * @param storeService              the store service
	 * @param tagSetFactory             the customer session tag set factory
	 */
	@Inject
	@SuppressWarnings({"checkstyle:parameternumber", "PMD.ExcessiveParameterList"})
	public ShopperRepositoryImpl(
			@Named("resourceOperationContext") final ResourceOperationContext resourceOperationContext,
			@Named("shopperService") final ShopperService shopperService,
			@Named("customerSessionService") final CustomerSessionService customerSessionService,
			@Named("sessionPriceListLifecycle") final SessionPriceListLifecycle sessionPriceListLifecycle,
			@Named("storeService") final StoreService storeService,
			@Named("customerSessionTagSetFactory") final CustomerSessionTagSetFactory tagSetFactory) {

		this.resourceOperationContext = resourceOperationContext;
		this.shopperService = shopperService;
		this.customerSessionService = customerSessionService;
		this.sessionPriceListLifecycle = sessionPriceListLifecycle;
		this.storeService = storeService;
		this.tagSetFactory = tagSetFactory;
	}

	@Override
	public Single<Shopper> findOrCreateShopper() {
		String storeCode = SubjectUtil.getScope(resourceOperationContext.getSubject());
		final String userGuid = resourceOperationContext.getUserIdentifier();
		final String accountSharedId = SubjectUtil.getAccountSharedId(resourceOperationContext.getSubject());
		return findOrCreateShopper(userGuid, accountSharedId, storeCode);
	}

	@Override
	public Single<Shopper> findOrCreateShopper(final String userGuid, final String storeCode) {
		return findOrCreateShopper(userGuid, null, storeCode);
	}

	@Override
	@CacheResult
	public Single<Shopper> findOrCreateShopper(final String userGuid, final String accountSharedId, final String storeCode) {
		Store store = storeService.findStoreWithCode(storeCode);
		if (store == null) {
			return Single.error(ResourceOperationFailure.notFound(String.format("Store %s was not found.", storeCode)));
		}
		Shopper shopper;
		if (accountSharedId == null) {
			try {
				shopper = shopperService.findOrCreateShopper(userGuid, storeCode);
			} catch (EpServiceException ex) {
				return Single.error(ResourceOperationFailure.notFound(ex.getMessage()));
			}
		} else {
			try {
				shopper = shopperService.findOrCreateShopper(userGuid, accountSharedId, storeCode);
			} catch (EpServiceException ex) {
				return Single.error(ResourceOperationFailure.notFound(ex.getMessage()));
			}
		}
		try {
			assignCustomerSessionToShopper(shopper, store);
			return Single.just(shopper);
		} catch (Exception ex) {
			LOG.error(String.format("Error when finding shopper by guid %s", userGuid), ex);
			return Single.error(ResourceOperationFailure.serverError("Server error when finding shopper by guid"));
		}
	}

	private void assignCustomerSessionToShopper(final Shopper shopper, final Store store) {
		Locale locale = findLocaleForCurrentOperation(store);
		Currency currency = findCurrencyForCurrentOperation(store);
		CustomerSession customerSession = customerSessionService.createWithShopper(shopper);
		customerSession.setLocale(locale);
		final TagSet tagSet = tagSetFactory.createTagSet(shopper);
		configurePersonalisedPricing(customerSession, store, currency, tagSet);
	}

	/**
	 * Configure personalised pricing for the session.
	 *
	 * @param customerSession The session.
	 * @param store           The store.
	 * @param currency        The currency
	 * @param tagSet          The tags.
	 */
	protected void configurePersonalisedPricing(final CustomerSession customerSession,
														   final Store store,
														   final Currency currency,
														   final TagSet tagSet) {
		customerSessionService.initializeCustomerSessionForPricing(
				customerSession, store.getCode(), currency);

		for (Map.Entry<String, Tag> tagEntry : tagSet.getTags().entrySet()) {
			customerSession.getCustomerTagSet().addTag(tagEntry.getKey(), tagEntry.getValue());
		}

		sessionPriceListLifecycle.refreshPriceListStack(customerSession, store);
	}

	/**
	 * Find the locale for this session given the current Subject's preferences or the Store's default locale.
	 *
	 * @param store the store
	 * @return the locale locale to use
	 */
	@VisibleForTesting
	Locale findLocaleForCurrentOperation(final Store store) {
		Optional<Locale> localeOptional = Optional.empty();
		Subject subject = resourceOperationContext.getSubject();

		if (subject != null) {
			localeOptional = Optional.ofNullable(SubjectUtil.getLocale(subject));
		}

		return localeOptional
				.orElse(store.getDefaultLocale());
	}

	/**
	 * Find the best currency for this session given the current Subject's preferences and Store's default currency.
	 *
	 * @param store the store
	 * @return the right currency
	 */
	@VisibleForTesting
	Currency findCurrencyForCurrentOperation(final Store store) {
		Optional<Currency> currencyOptional = Optional.empty();
		Subject subject = resourceOperationContext.getSubject();

		if (subject != null) {
			currencyOptional = Optional.ofNullable(SubjectUtil.getCurrency(subject));
		}

		return currencyOptional
				.orElse(store.getDefaultCurrency());
	}
}
