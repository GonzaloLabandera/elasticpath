/*
 * Copyright © 2013 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.customer.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.AssertionErrors.assertTrue;

import java.util.Currency;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.CustomerSession;
import com.elasticpath.domain.shopper.Shopper;
import com.elasticpath.domain.shopper.ShopperMemento;
import com.elasticpath.domain.store.Store;
import com.elasticpath.rest.ResourceOperationFailure;
import com.elasticpath.rest.ResourceStatus;
import com.elasticpath.rest.command.ExecutionResult;
import com.elasticpath.rest.command.ExecutionResultFactory;
import com.elasticpath.rest.identity.Subject;
import com.elasticpath.rest.identity.TestSubjectFactory;
import com.elasticpath.rest.resource.ResourceOperationContext;
import com.elasticpath.rest.resource.integration.epcommerce.repository.store.StoreRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.transform.impl.ReactiveAdapterImpl;
import com.elasticpath.service.customer.CustomerService;
import com.elasticpath.service.customer.CustomerSessionService;
import com.elasticpath.service.pricing.SessionPriceListLifecycle;
import com.elasticpath.service.shopper.ShopperService;
import com.elasticpath.tags.Tag;
import com.elasticpath.tags.TagSet;

/**
 * Tests the {@link CustomerSessionRepositoryImpl} class.
 */
@RunWith(MockitoJUnitRunner.class)
public class CustomerSessionRepositoryImplTest {
	private static final String RESULT_SHOULD_BE_A_FAILURE = "Result should be a failure";
	private static final String USER_GUID = "userGuid";
	private static final String CUSTOMER_SHARED_ID = "customerSharedId";
	private static final String ACCOUNT_SHARED_ID = "accountId";
	private static final String STORE_CODE = "StoreCode";
	private static final String TAG_KEY = "tag";
	private static final Locale LOCALE = Locale.CANADA;
	private static final Currency CURRENCY = Currency.getInstance(LOCALE);
	private static final String RESULT_SHOULD_BE_NOT_FOUND_MESSAGE = "Result should be not found message";

	private TagSet sessionTagSet;

	@Mock
	private Store mockStore;
	@Mock
	private Shopper mockShopper;
	@Mock
	private ShopperMemento mockShopperMemento;
	@Mock
	private Shopper mockShopperWithAccount;
	@Mock
	private CustomerSession mockCustomerSession;
	@Mock
	private CustomerSession mockCustomerWithAccountSession;
	@Mock
	private Customer mockCustomer;
	@Mock
	private Customer mockAccount;
	@Mock
	private ShopperService shopperService;
	@Mock
	private CustomerService customerService;
	@Mock
	private CustomerSessionService customerSessionService;
	@Mock
	private SessionPriceListLifecycle sessionPriceListLifecycle;
	@Mock
	private StoreRepository storeRepository;
	@Mock
	private ResourceOperationContext resourceOperationContext;
	@Mock
	private CustomerSessionTagSetFactory tagSetFactory;

	@InjectMocks
	private ReactiveAdapterImpl reactiveAdapter;

	@InjectMocks
	private CustomerSessionRepositoryImpl customerSessionRepository;
	public static final String ASSERT_DESCRIPTION = "Expected tag not found in customer session tag set.";

	/**
	 * Setting up tests.
	 */
	@Before
	public void setUp() {
		customerSessionRepository = new CustomerSessionRepositoryImpl(resourceOperationContext, shopperService, customerService,
				customerSessionService, sessionPriceListLifecycle, storeRepository, tagSetFactory, reactiveAdapter);
		mockCreateCustomerSessionWithShopper();
	}

	@Test
	public void testFindOrCreateCustomerSessionExpectedResultNotNullAndSubjectNotNull() {
		setupSubject();

		Tag tag = new Tag();
		TagSet tagSet = new TagSet();
		tagSet.addTag(TAG_KEY, tag);

		when(resourceOperationContext.getUserIdentifier()).thenReturn(USER_GUID);
		when(shopperService.findByCustomerGuidAndStoreCode(USER_GUID, STORE_CODE)).thenReturn(mockShopper);
		when(storeRepository.findStore(STORE_CODE)).thenReturn(ExecutionResultFactory.createReadOK(mockStore));
		when(mockStore.getCode()).thenReturn(STORE_CODE);
		when(mockStore.getDefaultLocale()).thenReturn(LOCALE);
		when(mockStore.getDefaultCurrency()).thenReturn(CURRENCY);
		when(customerSessionService.initializeCustomerSessionForPricing(mockCustomerSession, STORE_CODE, CURRENCY))
				.thenReturn(mockCustomerSession);
		when(tagSetFactory.createTagSet(any())).thenReturn(tagSet);

		customerSessionRepository.findOrCreateCustomerSession()
				.test()
				.assertNoErrors()
				.assertValue(mockCustomerSession);

		assertEquals(ASSERT_DESCRIPTION, tag, sessionTagSet.getTagValue(TAG_KEY));
		verify(sessionPriceListLifecycle).refreshPriceListStack(mockCustomerSession, mockStore);

		setupSubjectWithAccount();
		when(shopperService.findByCustomerGuidAndAccountSharedIdAndStore(USER_GUID, ACCOUNT_SHARED_ID, STORE_CODE))
				.thenReturn(mockShopperWithAccount);
		when(customerSessionService.initializeCustomerSessionForPricing(mockCustomerWithAccountSession, STORE_CODE, CURRENCY))
				.thenReturn(mockCustomerWithAccountSession);
		customerSessionRepository.findOrCreateCustomerSession()
				.test()
				.assertNoErrors()
				.assertValue(mockCustomerWithAccountSession);

		assertEquals(ASSERT_DESCRIPTION, tag, sessionTagSet.getTagValue(TAG_KEY));
		verify(sessionPriceListLifecycle).refreshPriceListStack(mockCustomerWithAccountSession, mockStore);
	}

	@Test
	public void testFindOrCreateCustomerSessionWhenShopperNotFound() {
		setupSubject();
		when(resourceOperationContext.getUserIdentifier()).thenReturn(USER_GUID);

		when(customerService.findByGuid(USER_GUID)).thenReturn(mockCustomer);

		customerSessionRepository.findOrCreateCustomerSession()
				.test()
				.assertError(ResourceOperationFailure.notFound("Customer was not found."))
				.assertNoValues();

		setupSubjectWithAccount();
		mockCreateCustomerSessionWithShopper();
		when(customerService.findByGuid(USER_GUID)).thenReturn(mockCustomer);

		customerSessionRepository.findOrCreateCustomerSession()
				.test()
				.assertError(ResourceOperationFailure.notFound("Customer was not found."))
				.assertNoValues();
	}

	@Test
	public void testFindOrCreateCustomerSessionWhenExceptionIsThrown() {
		setupSubject();
		when(resourceOperationContext.getUserIdentifier()).thenReturn(USER_GUID);

		when(customerService.findByGuid(USER_GUID)).thenThrow(new EpServiceException("Error in customer service"));

		customerSessionRepository.findOrCreateCustomerSession()
				.test()
				.assertError(ResourceOperationFailure.serverError("Server error when finding shopper by guid"))
				.assertNoValues();

		setupSubjectWithAccount();
		customerSessionRepository.findOrCreateCustomerSession()
				.test()
				.assertError(ResourceOperationFailure.serverError("Server error when finding shopper by guid"))
				.assertNoValues();
	}

	@Test
	public void testCreateCustomerSessionWhenStoreLookupNotFoundExpectedResultIsFailure() {
		setupSubject();

		when(mockCustomer.getGuid()).thenReturn(USER_GUID);

		ExecutionResult<CustomerSession> result = customerSessionRepository.findCustomerSessionByGuidAndStoreCode(mockCustomer.getGuid(), STORE_CODE);

		assertTrue(RESULT_SHOULD_BE_A_FAILURE, result.isFailure());
		assertEquals(RESULT_SHOULD_BE_NOT_FOUND_MESSAGE, ResourceStatus.NOT_FOUND, result.getResourceStatus());

		setupSubjectWithAccount();
		result = customerSessionRepository.findCustomerSessionByCustomerGuidAndAccountSharedId(mockCustomer.getGuid(), ACCOUNT_SHARED_ID, STORE_CODE);
		assertTrue(RESULT_SHOULD_BE_A_FAILURE, result.isFailure());
		assertEquals(RESULT_SHOULD_BE_NOT_FOUND_MESSAGE, ResourceStatus.NOT_FOUND, result.getResourceStatus());
	}

	@Test
	public void testFindCustomerSessionByUserIdAndAccountSharedIdWithResult() {
		setupSubjectWithAccount();
		Tag tag = new Tag();
		TagSet tagSet = new TagSet();
		tagSet.addTag(TAG_KEY, tag);

		mockCreateCustomerSessionWithShopper();
		when(mockCustomer.getSharedId()).thenReturn(CUSTOMER_SHARED_ID);
		when(customerService.findBySharedId(mockCustomer.getSharedId(), STORE_CODE)).thenReturn(mockCustomer);
		when(customerService.findBySharedId(ACCOUNT_SHARED_ID)).thenReturn(mockAccount);
		when(tagSetFactory.createTagSet(any())).thenReturn(tagSet);
		when(mockStore.getCode()).thenReturn(STORE_CODE);
		when(customerSessionService.initializeCustomerSessionForPricing(mockCustomerWithAccountSession, STORE_CODE, CURRENCY))
				.thenReturn(mockCustomerWithAccountSession);

		when(shopperService.findOrCreateShopper(mockCustomer, mockAccount, STORE_CODE)).thenReturn(mockShopperWithAccount);
		when(storeRepository.findStore(STORE_CODE)).thenReturn(ExecutionResultFactory.createReadOK(mockStore));
		ExecutionResult<CustomerSession> result =
				customerSessionRepository.findCustomerSessionByUserIdAndAccountSharedId(
						mockCustomer.getSharedId(),
						ACCOUNT_SHARED_ID,
						STORE_CODE);

		assertEquals(result.getData(), mockCustomerWithAccountSession);
	}

	@Test
	public void testFindCustomerSessionByUserIdAndAccountSharedIdWithCustomerNotFound() {
		setupSubjectWithAccount();
		Tag tag = new Tag();
		TagSet tagSet = new TagSet();
		tagSet.addTag(TAG_KEY, tag);

		mockCreateCustomerSessionWithShopper();
		when(mockCustomer.getSharedId()).thenReturn(CUSTOMER_SHARED_ID);
		when(customerService.findBySharedId(mockCustomer.getSharedId(), STORE_CODE)).thenReturn(null);

		ExecutionResult<CustomerSession> result =
				customerSessionRepository.findCustomerSessionByUserIdAndAccountSharedId(
						mockCustomer.getSharedId(),
						ACCOUNT_SHARED_ID,
						STORE_CODE);

		assertTrue(RESULT_SHOULD_BE_A_FAILURE, result.isFailure());
		assertEquals(RESULT_SHOULD_BE_NOT_FOUND_MESSAGE, ResourceStatus.NOT_FOUND, result.getResourceStatus());
	}

	@Test
	public void testFindCustomerSessionByUserIdAndAccountSharedIdWithAccountNotFound() {
		setupSubjectWithAccount();
		Tag tag = new Tag();
		TagSet tagSet = new TagSet();
		tagSet.addTag(TAG_KEY, tag);

		mockCreateCustomerSessionWithShopper();
		when(mockCustomer.getSharedId()).thenReturn(CUSTOMER_SHARED_ID);
		when(customerService.findBySharedId(mockCustomer.getSharedId(), STORE_CODE)).thenReturn(mockCustomer);
		when(customerService.findBySharedId(ACCOUNT_SHARED_ID)).thenReturn(null);
		ExecutionResult<CustomerSession> result =
				customerSessionRepository.findCustomerSessionByUserIdAndAccountSharedId(
						mockCustomer.getSharedId(),
						ACCOUNT_SHARED_ID,
						STORE_CODE);

		assertTrue(RESULT_SHOULD_BE_A_FAILURE, result.isFailure());
		assertEquals(RESULT_SHOULD_BE_NOT_FOUND_MESSAGE, ResourceStatus.NOT_FOUND, result.getResourceStatus());
	}

	@Test
	public void testFindCustomerSessionByUserIdAndAccountSharedIdWithShopperNull() {
		setupSubjectWithAccount();
		Tag tag = new Tag();
		TagSet tagSet = new TagSet();
		tagSet.addTag(TAG_KEY, tag);

		mockCreateCustomerSessionWithShopper();
		when(mockCustomer.getSharedId()).thenReturn(CUSTOMER_SHARED_ID);
		when(customerService.findBySharedId(mockCustomer.getSharedId(), STORE_CODE)).thenReturn(mockCustomer);
		when(customerService.findBySharedId(ACCOUNT_SHARED_ID)).thenReturn(mockAccount);
		when(shopperService.findOrCreateShopper(mockCustomer, mockAccount, STORE_CODE)).thenReturn(null);

		ExecutionResult<CustomerSession> result =
				customerSessionRepository.findCustomerSessionByUserIdAndAccountSharedId(
						mockCustomer.getSharedId(),
						ACCOUNT_SHARED_ID,
						STORE_CODE);

		assertTrue(RESULT_SHOULD_BE_A_FAILURE, result.isFailure());
		assertEquals(RESULT_SHOULD_BE_NOT_FOUND_MESSAGE, ResourceStatus.NOT_FOUND, result.getResourceStatus());
	}

	@Test
	public void testCreateCustomerSessionWhenExceptionIsThrownExpectedResultIsFailure() {
		setupSubject();
		when(shopperService.findByCustomerGuidAndStoreCode(USER_GUID, STORE_CODE)).thenReturn(mockShopper);
		when(mockCustomer.getGuid()).thenReturn(USER_GUID);
		when(storeRepository.findStore(STORE_CODE)).thenReturn(ExecutionResultFactory.createReadOK(mockStore));
		when(mockStore.getCode()).thenReturn(STORE_CODE);
		when(mockStore.getDefaultLocale()).thenReturn(LOCALE);

		ExecutionResult<CustomerSession> result = customerSessionRepository.findCustomerSessionByGuidAndStoreCode(mockCustomer.getGuid(), STORE_CODE);

		assertTrue(RESULT_SHOULD_BE_A_FAILURE, result.isFailure());
		assertEquals("Result should be a server Error", ResourceStatus.SERVER_ERROR, result.getResourceStatus());
	}

	@Test
	public void testFindLocaleWhenSubjectHasNoLocale() {
		Subject subject = TestSubjectFactory.createWithScopeAndUserId(STORE_CODE, USER_GUID);
		when(resourceOperationContext.getSubject()).thenReturn(subject);
		when(mockStore.getDefaultLocale()).thenReturn(LOCALE);

		Locale actual = customerSessionRepository.findLocaleForCurrentOperation(mockStore);

		assertEquals(LOCALE, actual);
	}

	@Test
	public void testFindLocaleWhenSubjectHasLocale() {
		setupSubject();
		when(mockStore.getDefaultLocale()).thenReturn(LOCALE);

		Locale actual = customerSessionRepository.findLocaleForCurrentOperation(mockStore);

		assertEquals(LOCALE, actual);
	}

	@Test
	public void testFindCurrencyWhenSubjectHasNoCurrency() {
		Subject subject = TestSubjectFactory.createWithScopeAndUserId(STORE_CODE, USER_GUID);
		when(resourceOperationContext.getSubject()).thenReturn(subject);
		when(mockStore.getDefaultCurrency()).thenReturn(CURRENCY);

		Currency actual = customerSessionRepository.findCurrencyForCurrentOperation(mockStore);

		assertEquals(CURRENCY, actual);
	}

	@Test
	public void testFindCurrencyWhenSubjectHasCurrency() {
		setupSubject();
		when(mockStore.getDefaultCurrency()).thenReturn(CURRENCY);

		Currency actual = customerSessionRepository.findCurrencyForCurrentOperation(mockStore);

		assertEquals(CURRENCY, actual);
	}

	@Test
	public void testCreateCustomerSessionAsSingleWhenSubjectHasAccount() {
		setupSubjectWithAccount();
		when(resourceOperationContext.getUserIdentifier()).thenReturn(USER_GUID);
		when(shopperService.createAndSaveShopper(STORE_CODE)).thenReturn(mockShopper);
		when(mockShopper.getShopperMemento()).thenReturn(mockShopperMemento);

		customerSessionRepository.createCustomerSessionAsSingle();

		verify(customerService).findBySharedId(ACCOUNT_SHARED_ID);
		verify(mockShopperMemento).setAccount(mockAccount);
		verify(customerSessionService).createWithShopper(mockShopper);
	}

	@Test
	public void testCreateCustomerSessionAsSingleWhenSubjectHasNoAccount() {
		setupSubject();
		when(resourceOperationContext.getUserIdentifier()).thenReturn(USER_GUID);
		when(shopperService.createAndSaveShopper(STORE_CODE)).thenReturn(mockShopper);
		when(mockShopper.getShopperMemento()).thenReturn(mockShopperMemento);

		customerSessionRepository.createCustomerSessionAsSingle();

		verify(customerService, never()).findBySharedId(ACCOUNT_SHARED_ID);
		verify(mockShopperMemento, never()).setAccount(mockAccount);
		verify(customerSessionService).createWithShopper(mockShopper);
	}

	private void mockCreateCustomerSessionWithShopper() {
		sessionTagSet = new TagSet();

		when(customerService.findByGuid(USER_GUID)).thenReturn(mockCustomer);
		when(customerService.findBySharedId(ACCOUNT_SHARED_ID)).thenReturn(mockAccount);
		when(customerSessionService.createWithShopper(mockShopper)).thenReturn(mockCustomerSession);
		when(customerSessionService.createWithShopper(mockShopperWithAccount)).thenReturn(mockCustomerWithAccountSession);
		when(mockCustomerSession.getCustomerTagSet()).thenReturn(sessionTagSet);
		when(mockCustomerWithAccountSession.getCustomerTagSet()).thenReturn(sessionTagSet);
	}

	private void setupSubject() {
		Subject subject = TestSubjectFactory.createWithScopeAndUserIdAndLocaleAndCurrency(
				STORE_CODE, USER_GUID, LOCALE, CURRENCY);

		when(resourceOperationContext.getSubject()).thenReturn(subject);
	}

	private void setupSubjectWithAccount() {
		Subject subject = TestSubjectFactory.createWithScopeAndGuidAndAccountIdAndLocaleAndCurrency(
				STORE_CODE, USER_GUID, ACCOUNT_SHARED_ID, LOCALE, CURRENCY);

		when(resourceOperationContext.getSubject()).thenReturn(subject);
	}
}
