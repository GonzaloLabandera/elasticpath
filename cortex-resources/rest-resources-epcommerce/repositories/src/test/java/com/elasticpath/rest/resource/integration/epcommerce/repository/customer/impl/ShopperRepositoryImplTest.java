/*
 * Copyright © 2013 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.customer.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Currency;
import java.util.Locale;

import io.reactivex.Single;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.domain.customer.CustomerSession;
import com.elasticpath.domain.shopper.Shopper;
import com.elasticpath.domain.store.Store;
import com.elasticpath.rest.ResourceOperationFailure;
import com.elasticpath.rest.identity.Subject;
import com.elasticpath.rest.identity.TestSubjectFactory;
import com.elasticpath.rest.resource.ResourceOperationContext;
import com.elasticpath.service.customer.CustomerSessionService;
import com.elasticpath.service.pricing.SessionPriceListLifecycle;
import com.elasticpath.service.shopper.ShopperService;
import com.elasticpath.service.store.StoreService;
import com.elasticpath.tags.Tag;
import com.elasticpath.tags.TagSet;

/**
 * Tests the {@link ShopperRepositoryImpl} class.
 */
@RunWith(MockitoJUnitRunner.class)
public class ShopperRepositoryImplTest {
	private static final String USER_GUID = "userGuid";
	private static final String INVALID_USER_GUID = "invalidUserGuid";
	private static final String ACCOUNT_SHARED_ID = "accountSharedId";
	private static final String INVALID_ACCOUNT_SHARED_ID = "invalidAccountSharedId";
	private static final String STORE_CODE = "storeCode";
	private static final String INVALID_STORE_CODE = "invalidStoreCode";
	private static final String TAG_KEY = "tag";
	private static final Locale LOCALE = Locale.CANADA;
	private static final Currency CURRENCY = Currency.getInstance(LOCALE);

	private TagSet sessionTagSet;

	@Mock
	private Store mockStore;
	@Mock
	private Shopper mockShopper;
	@Mock
	private Shopper mockShopperWithAccount;
	@Mock
	private CustomerSession mockCustomerSession;
	@Mock
	private CustomerSession mockCustomerWithAccountSession;
	@Mock
	private ShopperService shopperService;
	@Mock
	private StoreService storeService;
	@Mock
	private CustomerSessionService customerSessionService;
	@Mock
	private SessionPriceListLifecycle sessionPriceListLifecycle;
	@Mock
	private ResourceOperationContext resourceOperationContext;
	@Mock
	private CustomerSessionTagSetFactory tagSetFactory;

	@InjectMocks
	private ShopperRepositoryImpl shopperRepository;

	public static final String ASSERT_DESCRIPTION = "Expected tag not found in customer session tag set.";

	/**
	 * Setting up tests.
	 */
	@Before
	public void setUp() {
		mockCreateShopper();
		when(storeService.findStoreWithCode(STORE_CODE)).thenReturn(mockStore);
		when(mockStore.getCode()).thenReturn(STORE_CODE);
		when(mockStore.getDefaultLocale()).thenReturn(LOCALE);
		when(mockStore.getDefaultCurrency()).thenReturn(CURRENCY);
	}

	@Test
	public void testFindOrCreateShopperExpectedResultNotNullAndSubjectNotNull() {
		setupSubject(USER_GUID);

		Tag tag = new Tag();
		sessionTagSet.addTag(TAG_KEY, tag);

		shopperRepository.findOrCreateShopper()
				.test()
				.assertNoErrors()
				.assertValue(mockShopper);

		assertEquals(ASSERT_DESCRIPTION, tag, sessionTagSet.getTagValue(TAG_KEY));
		verify(sessionPriceListLifecycle).refreshPriceListStack(mockCustomerSession, mockStore);

		setupSubjectWithAccount(USER_GUID, ACCOUNT_SHARED_ID);
		shopperRepository.findOrCreateShopper()
				.test()
				.assertNoErrors()
				.assertValue(mockShopperWithAccount);

		assertEquals(ASSERT_DESCRIPTION, tag, sessionTagSet.getTagValue(TAG_KEY));
		verify(sessionPriceListLifecycle).refreshPriceListStack(mockCustomerWithAccountSession, mockStore);
	}

	@Test
	public void testFindOrCreateShopperWhenShopperNotFound() {
		setupSubject(INVALID_USER_GUID);

		shopperRepository.findOrCreateShopper()
				.test()
				.assertError(ResourceOperationFailure.notFound("Customer not found."))
				.assertNoValues();

		setupSubjectWithAccount(USER_GUID, INVALID_ACCOUNT_SHARED_ID);

		shopperRepository.findOrCreateShopper()
				.test()
				.assertError(ResourceOperationFailure.notFound("Account not found."))
				.assertNoValues();
	}

	@Test
	public void testFindOrCreateShopperWhenExceptionIsThrown() {
		setupSubject(USER_GUID);
		when(customerSessionService.createWithShopper(mockShopper)).thenThrow(new EpServiceException("Error in customer session service"));

		shopperRepository.findOrCreateShopper()
				.test()
				.assertError(ResourceOperationFailure.serverError("Server error when finding shopper by guid"))
				.assertNoValues();

		setupSubjectWithAccount(USER_GUID, ACCOUNT_SHARED_ID);
		when(customerSessionService.createWithShopper(mockShopperWithAccount)).thenThrow(new EpServiceException("Error in customer session service"));

		shopperRepository.findOrCreateShopper()
				.test()
				.assertError(ResourceOperationFailure.serverError("Server error when finding shopper by guid"))
				.assertNoValues();
	}

	@Test
	public void testFindOrCreateShopperWhenStoreLookupNotFoundExpectedResultIsFailure() {
		Single<Shopper> result = shopperRepository.findOrCreateShopper(USER_GUID, INVALID_STORE_CODE);

		result.test()
				.assertError(ResourceOperationFailure.notFound("Store invalidStoreCode was not found."))
				.assertNoValues();

		result = shopperRepository.findOrCreateShopper(USER_GUID, ACCOUNT_SHARED_ID, INVALID_STORE_CODE);
		result.test()
				.assertError(ResourceOperationFailure.notFound("Store invalidStoreCode was not found."))
				.assertNoValues();
	}

	@Test
	public void testFindLocaleWhenSubjectHasNoLocale() {
		Subject subject = TestSubjectFactory.createWithScopeAndUserId(STORE_CODE, USER_GUID);
		when(resourceOperationContext.getSubject()).thenReturn(subject);
		when(mockStore.getDefaultLocale()).thenReturn(LOCALE);

		Locale actual = shopperRepository.findLocaleForCurrentOperation(mockStore);

		assertEquals(LOCALE, actual);
	}

	@Test
	public void testFindLocaleWhenSubjectHasLocale() {
		setupSubject(USER_GUID);
		when(mockStore.getDefaultLocale()).thenReturn(LOCALE);

		Locale actual = shopperRepository.findLocaleForCurrentOperation(mockStore);

		assertEquals(LOCALE, actual);
	}

	@Test
	public void testFindCurrencyWhenSubjectHasNoCurrency() {
		Subject subject = TestSubjectFactory.createWithScopeAndUserId(STORE_CODE, USER_GUID);
		when(resourceOperationContext.getSubject()).thenReturn(subject);
		when(mockStore.getDefaultCurrency()).thenReturn(CURRENCY);

		Currency actual = shopperRepository.findCurrencyForCurrentOperation(mockStore);

		assertEquals(CURRENCY, actual);
	}

	@Test
	public void testFindCurrencyWhenSubjectHasCurrency() {
		setupSubject(USER_GUID);
		when(mockStore.getDefaultCurrency()).thenReturn(CURRENCY);

		Currency actual = shopperRepository.findCurrencyForCurrentOperation(mockStore);

		assertEquals(CURRENCY, actual);
	}

	private void mockCreateShopper() {
		sessionTagSet = new TagSet();

		when(customerSessionService.createWithShopper(mockShopper))
				.thenReturn(mockCustomerSession);
		when(customerSessionService.createWithShopper(mockShopperWithAccount))
				.thenReturn(mockCustomerWithAccountSession);
		when(mockCustomerSession.getCustomerTagSet())
				.thenReturn(sessionTagSet);
		when(mockCustomerWithAccountSession.getCustomerTagSet())
				.thenReturn(sessionTagSet);
		when(shopperService.findOrCreateShopper(USER_GUID, STORE_CODE))
				.thenReturn(mockShopper);
		when(shopperService.findOrCreateShopper(USER_GUID, ACCOUNT_SHARED_ID, STORE_CODE))
				.thenReturn(mockShopperWithAccount);
		when(shopperService.findOrCreateShopper(INVALID_USER_GUID, STORE_CODE))
				.thenThrow(new EpServiceException("Customer not found."));
		when(shopperService.findOrCreateShopper(USER_GUID, INVALID_ACCOUNT_SHARED_ID, STORE_CODE))
				.thenThrow(new EpServiceException("Account not found."));
		when(tagSetFactory.createTagSet(any()))
				.thenReturn(sessionTagSet);
	}

	private void setupSubject(final String userGuid) {
		Subject subject = TestSubjectFactory.createWithScopeAndUserIdAndLocaleAndCurrency(
				STORE_CODE, userGuid, LOCALE, CURRENCY);

		when(resourceOperationContext.getUserIdentifier()).thenReturn(userGuid);
		when(resourceOperationContext.getSubject()).thenReturn(subject);
	}

	private void setupSubjectWithAccount(final String userGuid, final String accountSharedId) {
		Subject subject = TestSubjectFactory.createWithScopeAndGuidAndAccountIdAndLocaleAndCurrency(
				STORE_CODE, userGuid, accountSharedId, LOCALE, CURRENCY);

		when(resourceOperationContext.getUserIdentifier()).thenReturn(userGuid);
		when(resourceOperationContext.getSubject()).thenReturn(subject);
	}
}
