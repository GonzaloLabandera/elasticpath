/*
 * Copyright © 2013 Elastic Path Software Inc. All rights reserved.
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
import io.reactivex.Completable;
import io.reactivex.Single;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.CustomerSession;
import com.elasticpath.domain.shopper.Shopper;
import com.elasticpath.domain.shopper.ShopperMemento;
import com.elasticpath.domain.store.Store;
import com.elasticpath.rest.cache.CacheRemove;
import com.elasticpath.rest.cache.CacheResult;
import com.elasticpath.rest.chain.Assign;
import com.elasticpath.rest.chain.Ensure;
import com.elasticpath.rest.chain.ExecutionResultChain;
import com.elasticpath.rest.chain.OnFailure;
import com.elasticpath.rest.command.ExecutionResult;
import com.elasticpath.rest.command.ExecutionResultFactory;
import com.elasticpath.rest.identity.ScopePrincipal;
import com.elasticpath.rest.identity.Subject;
import com.elasticpath.rest.identity.util.PrincipalsUtil;
import com.elasticpath.rest.identity.util.SubjectUtil;
import com.elasticpath.rest.resource.ResourceOperationContext;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.CustomerSessionRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.store.StoreRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.transform.ReactiveAdapter;
import com.elasticpath.service.customer.CustomerService;
import com.elasticpath.service.customer.CustomerSessionService;
import com.elasticpath.service.pricing.SessionPriceListLifecycle;
import com.elasticpath.service.shopper.ShopperService;
import com.elasticpath.tags.Tag;
import com.elasticpath.tags.TagSet;

/**
 * Encapsulates operations on customer session.
 */
@Singleton
@Named("customerSessionRepository")
@SuppressWarnings({"PMD.GodClass"})
public class CustomerSessionRepositoryImpl implements CustomerSessionRepository {
	private static final Logger LOG = LoggerFactory.getLogger(CustomerSessionRepositoryImpl.class);

	private static final String CUSTOMER_WAS_NOT_FOUND = "Customer was not found.";

	private final ResourceOperationContext resourceOperationContext;
	private final ShopperService shopperService;
	private final CustomerService customerService;
	private final CustomerSessionService customerSessionService;
	private final SessionPriceListLifecycle sessionPriceListLifecycle;
	private final StoreRepository storeRepository;
	private final CustomerSessionTagSetFactory tagSetFactory;
	private final ReactiveAdapter reactiveAdapter;


	/**
	 * Constructor.
	 *
	 * @param resourceOperationContext  the resource operation context
	 * @param shopperService            the shopper service
	 * @param customerService           the customer service
	 * @param customerSessionService    the customer session service
	 * @param sessionPriceListLifecycle the price list lifecycle manager.
	 * @param storeRepository           the store repository
	 * @param tagSetFactory             the customer session tag set factory
	 * @param reactiveAdapter 			the reactive adapter
	 */
	@Inject
	@SuppressWarnings({"checkstyle:parameternumber", "PMD.ExcessiveParameterList"})
	public CustomerSessionRepositoryImpl(
			@Named("resourceOperationContext") final ResourceOperationContext resourceOperationContext,
			@Named("shopperService") final ShopperService shopperService,
			@Named("customerService") final CustomerService customerService,
			@Named("customerSessionService") final CustomerSessionService customerSessionService,
			@Named("sessionPriceListLifecycle") final SessionPriceListLifecycle sessionPriceListLifecycle,
			@Named("storeRepository") final StoreRepository storeRepository,
			@Named("customerSessionTagSetFactory") final CustomerSessionTagSetFactory tagSetFactory,
			@Named("reactiveAdapter") final ReactiveAdapter reactiveAdapter) {

		this.resourceOperationContext = resourceOperationContext;
		this.shopperService = shopperService;
		this.customerService = customerService;
		this.customerSessionService = customerSessionService;
		this.sessionPriceListLifecycle = sessionPriceListLifecycle;
		this.storeRepository = storeRepository;
		this.tagSetFactory = tagSetFactory;
		this.reactiveAdapter = reactiveAdapter;
	}

	@Override
	public Single<CustomerSession> findOrCreateCustomerSession() {
		String userGuid = resourceOperationContext.getUserIdentifier();
		String accountSharedId = SubjectUtil.getAccountSharedId(resourceOperationContext.getSubject());
		if (accountSharedId != null) {
			return findCustomerSessionByGuidAndAccountSharedIdAsSingle(userGuid, accountSharedId);
		}
		String storeCode = SubjectUtil.getScope(resourceOperationContext.getSubject());
		return findCustomerSessionByGuidAndStoreCodeAsSingle(userGuid, storeCode);
	}

	@Override
	public Single<CustomerSession> createCustomerSessionAsSingle() {
		final String userGuid = resourceOperationContext.getUserIdentifier();
		final String accountSharedId = SubjectUtil.getAccountSharedId(resourceOperationContext.getSubject());
		if (accountSharedId != null) {
			return Single.just(createCustomerSessionByGuidAccountSharedIdAndContext(userGuid, accountSharedId));
		}
		return Single.just(createCustomerSessionByGuidAndContext(userGuid));
	}

	private CustomerSession createCustomerSessionByGuidAndContext(final String userGuid) {

		return createCustomerSessionByGuidAccountSharedIdAndContext(userGuid, null);
	}

	private CustomerSession createCustomerSessionByGuidAccountSharedIdAndContext(final String userGuid, final String accountSharedId) {
		Shopper shopper;
		if (accountSharedId == null) {
			shopper = createShopperByCustomerGuid(userGuid);
		} else {
			shopper = createShopperByCustomerGuidAndAccountSharedId(userGuid, accountSharedId);
		}

		final Subject subject = resourceOperationContext.getSubject();
		Locale  locale = SubjectUtil.getLocale(subject);
		locale = locale == null ? Locale.ENGLISH : locale;
		return createCustomerSession(shopper, locale);
	}

	@Override
	@CacheResult(uniqueIdentifier = "getCustomerSessionByGuidAndScope")
	public ExecutionResult<CustomerSession> findCustomerSessionByGuidAndStoreCode(final String customerGuid, final String storeCode) {
		return new ExecutionResultChain() {
			@Override
			public ExecutionResult<?> build() {
				final Shopper shopper = getShopperByCustomerGuid(customerGuid, storeCode);

				Ensure.notNull(shopper, OnFailure.returnNotFound(CUSTOMER_WAS_NOT_FOUND));

				return createCustomerSession(storeCode, shopper);
			}
		}.execute();
	}

	@Override
	public Single<CustomerSession> findCustomerSessionByGuidAndStoreCodeAsSingle(final String customerGuid, final String storeCode) {
		return reactiveAdapter.fromRepositoryAsSingle(() -> findCustomerSessionByGuidAndStoreCode(customerGuid, storeCode));
	}

	@Override
	public Single<CustomerSession> findCustomerSessionByGuidAndAccountSharedIdAsSingle(final String customerGuid, final String accountSharedId) {
		String storeCode = getStoreCodeFromResourceOperationContext();
		return reactiveAdapter.fromRepositoryAsSingle(() ->
				findCustomerSessionByCustomerGuidAndAccountSharedId(customerGuid, accountSharedId, storeCode));
	}

	private Shopper getShopperByCustomerGuid(final String customerGuid, final String storeCode) {

		Shopper shopper = shopperService.findByCustomerGuidAndStoreCode(customerGuid, storeCode);

		if (shopper == null) {
			shopper = Assign.ifSuccessful(findByCustomerGuidWithoutException(customerGuid));
		}

		return shopper;
	}


	private Shopper createShopperByCustomerGuid(final String customerGuid) {
		return createShopperByCustomerGuidAndAccountSharedId(customerGuid, null);
	}

	private Shopper createShopperByCustomerGuidAndAccountSharedId(final String customerGuid, final String accountSharedId) {
		final Customer customer = customerService.findByGuid(customerGuid);
		String storeCode = getStoreCodeFromResourceOperationContext();

		final Shopper shopper = shopperService.createAndSaveShopper(storeCode);
		ShopperMemento shopperMemento =  shopper.getShopperMemento();
		shopperMemento.setCustomer(customer);
		shopperMemento.setStoreCode(storeCode);

		if (accountSharedId != null) {
			final Customer account = customerService.findBySharedId(accountSharedId);
			shopperMemento.setAccount(account);
		}

		shopperService.save(shopper);
		return shopper;
	}

	// Returns the storecode from the store being accessed, not necessarily the one associated with the customer.
	private String getStoreCodeFromResourceOperationContext() {
		ScopePrincipal scopePrincipal =
				PrincipalsUtil.getFirstPrincipalByType(resourceOperationContext.getSubject().getPrincipals(), ScopePrincipal.class);
		return scopePrincipal.getValue();
	}

	@Override
	@CacheRemove(typesToInvalidate = CustomerSession.class)
	public Completable invalidateCustomerSessionByGuid(final String customerGuid) {
		//this method causes invalidation of CustomerSession instance for given customer guid
		return Completable.complete();
	}

	private ExecutionResult<Shopper> findByCustomerGuidWithoutException(final String customerGuid) {
		try {
			final Customer customer = customerService.findByGuid(customerGuid);
			if (customer == null) {
				return ExecutionResultFactory.createNotFound(
						String.format("Customer not found for given customer guid %s", customerGuid));
			}
			final Shopper shopper = shopperService.findOrCreateShopper(customer, customer.getStoreCode());

			return ExecutionResultFactory.createReadOK(shopper);

		} catch (Exception e) {
			LOG.error(String.format("Error when finding shopper by guid %s", customerGuid), e);
			return ExecutionResultFactory.createServerError("Server error when finding shopper by guid");
		}
	}

	@Override
	@CacheResult(uniqueIdentifier = "getCustomerSessionBySharedId")
	public ExecutionResult<CustomerSession> findCustomerSessionBySharedId(final String storeCode, final String customerSharedId) {
		return new ExecutionResultChain() {
			@Override
			public ExecutionResult<?> build() {
				Shopper shopper = shopperService.findByCustomerSharedIdAndStoreCode(customerSharedId, storeCode);
				if (shopper == null) {
					shopper = Assign.ifSuccessful(findOrCreateShopperWithoutException(customerSharedId, storeCode));
				}

				Ensure.notNull(shopper, OnFailure.returnNotFound(CUSTOMER_WAS_NOT_FOUND));

				return createCustomerSession(storeCode, shopper);
			}
		}.execute();
	}

	@Override
	public ExecutionResult<CustomerSession> findCustomerSessionByCustomerGuidAndAccountSharedId(
			final String customerGuid,
			final String accountSharedId,
			final String storeCode) {
		try {

			Shopper shopper = shopperService.findByCustomerGuidAndAccountSharedIdAndStore(customerGuid, accountSharedId, storeCode);
			if (shopper == null) {
				final Customer customer = customerService.findByGuid(customerGuid);
				if (customer == null) {
					return ExecutionResultFactory.createNotFound(
							String.format("Customer not found for given customer guid id %s", customerGuid));
				}

				ExecutionResult<Shopper> shopperExecutionResult = createShopperByCustomerAndAccountAndStoreCode(customer, accountSharedId, storeCode);
				if (shopperExecutionResult.isFailure()) {
					return ExecutionResultFactory.createNotFound(CUSTOMER_WAS_NOT_FOUND);
				}
				shopper = shopperExecutionResult.getData();
			}

			return createCustomerSession(storeCode, shopper);
		} catch (Exception e) {
			LOG.error(String.format("Error when finding shopper by guid %s", customerGuid), e);
			return ExecutionResultFactory.createServerError("Server error when finding shopper by guid");
		}
	}

	@Override
	public ExecutionResult<CustomerSession> findCustomerSessionByUserIdAndAccountSharedId(
			final String customerSharedId,
			final String accountSharedId,
			final String storeCode) {

		Shopper shopper = shopperService.findByCustomerSharedIdAndAccountSharedIdAndStore(customerSharedId, accountSharedId, storeCode);
		if (shopper == null) {
			final Customer customer = customerService.findBySharedId(customerSharedId, storeCode);
			if (customer == null) {
				return ExecutionResultFactory.createNotFound(
						String.format("Customer not found for given customer shared id %s", customerSharedId));
			}

			ExecutionResult<Shopper> shopperExecutionResult = createShopperByCustomerAndAccountAndStoreCode(customer, accountSharedId, storeCode);
			if (shopperExecutionResult.isFailure()) {
				return ExecutionResultFactory.createNotFound(CUSTOMER_WAS_NOT_FOUND);
			}
			shopper = shopperExecutionResult.getData();
		}

		return createCustomerSession(storeCode, shopper);
	}


	private ExecutionResult<Shopper> createShopperByCustomerAndAccountAndStoreCode(
			final Customer customer,
			final String accountSharedId,
			final String storeCode) {

		final Customer account = customerService.findBySharedId(accountSharedId);
		if (account == null) {
			return ExecutionResultFactory.createNotFound(
					String.format("Account not found for given account shared ID %s and store code %s", accountSharedId, storeCode));
		}

		Shopper shopper = shopperService.findOrCreateShopper(customer, account, storeCode);
		if (shopper == null) {
			return ExecutionResultFactory.createNotFound(CUSTOMER_WAS_NOT_FOUND);
		}
		return ExecutionResultFactory.createReadOK(shopper);
	}

	private ExecutionResult<Shopper> findOrCreateShopperWithoutException(final String customerSharedId, final String storeCode) {
		try {

			final Customer customer = customerService.findBySharedId(customerSharedId, storeCode);

			if (customer == null) {
				return ExecutionResultFactory.createNotFound(
						String.format("Customer not found for given customer shared id %s and store %s", customerSharedId, storeCode));
			}
			final Shopper shopper = shopperService.findOrCreateShopper(customer, storeCode);

			return ExecutionResultFactory.createReadOK(shopper);
		} catch (Exception e) {
			LOG.error(String.format("Error when finding/creating shopper for store %s and customer shared ID %s", storeCode, customerSharedId), e);
			return ExecutionResultFactory.createServerError("Server error when finding/creating shopper by customer ID");
		}
	}

	@CacheResult
	private ExecutionResult<CustomerSession> createCustomerSession(final String storeCode, final Shopper shopper) {
		return new ExecutionResultChain() {
			public ExecutionResult<?> build() {
				ExecutionResult<CustomerSession> result;
				Store store = Assign.ifSuccessful(storeRepository.findStore(storeCode));

				try {
					Locale locale = findLocaleForCurrentOperation(store);
					Currency currency = findCurrencyForCurrentOperation(store);
					CustomerSession customerSession = createCustomerSession(shopper, locale);
					final TagSet tagSet = tagSetFactory.createTagSet(shopper);
					customerSession = configurePersonalisedPricing(customerSession, store, currency, tagSet);
					result = ExecutionResultFactory.createReadOK(customerSession);
				} catch (Exception e) {
					LOG.error("Error when initializing customer session", e);
					result = ExecutionResultFactory.createServerError("Server error when creating customer session");
				}

				return result;
			}
		}.execute();
	}

	/**
	 * Configure personalised pricing for the session.
	 *
	 * @param customerSession The session.
	 * @param store           The store.
	 * @param currency        The currency
	 * @param tagSet          The tags.
	 * @return CustomerSession - The session.
	 */
	protected CustomerSession configurePersonalisedPricing(final CustomerSession customerSession,
														   final Store store,
														   final Currency currency,
														   final TagSet tagSet) {
		CustomerSession customerSessionForPricing = customerSessionService.initializeCustomerSessionForPricing(
				customerSession, store.getCode(), currency);

		for (Map.Entry<String, Tag> tagEntry : tagSet.getTags().entrySet()) {
			customerSessionForPricing.getCustomerTagSet().addTag(tagEntry.getKey(), tagEntry.getValue());
		}

		sessionPriceListLifecycle.refreshPriceListStack(customerSessionForPricing, store);
		return customerSessionForPricing;
	}

	/**
	 * Creates a {@link CustomerSession} given a {@link Shopper}.
	 *
	 * @param shopper the shopper
	 * @param locale  the locale to use for the customer session
	 * @return the created customer session
	 */
	CustomerSession createCustomerSession(final Shopper shopper, final Locale locale) {
		CustomerSession customerSession = customerSessionService.createWithShopper(shopper);
		customerSession.setLocale(locale);
		return customerSession;
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
