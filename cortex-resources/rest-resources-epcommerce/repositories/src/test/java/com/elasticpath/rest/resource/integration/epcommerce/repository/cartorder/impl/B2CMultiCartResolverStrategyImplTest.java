/*
 * Copyright © 2013 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.impl;

import static com.elasticpath.rest.resource.integration.epcommerce.repository.ResourceTestConstants.CART_GUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Locale;

import io.reactivex.Single;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.modifier.ModifierField;
import com.elasticpath.domain.modifier.ModifierGroup;
import com.elasticpath.domain.shopper.Shopper;
import com.elasticpath.domain.shoppingcart.CartType;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.store.Store;
import com.elasticpath.rest.identity.Subject;
import com.elasticpath.rest.resource.ResourceOperationContext;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.CartPostProcessor;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.ShopperRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.transform.ExceptionTransformer;
import com.elasticpath.rest.resource.integration.epcommerce.repository.transform.impl.ReactiveAdapterImpl;
import com.elasticpath.service.shoppingcart.MulticartItemListTypeLocationProvider;
import com.elasticpath.service.shoppingcart.ShoppingCartService;
import com.elasticpath.service.store.StoreService;

/**
 * Test for {@link B2CMultiCartResolutionStrategyImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class B2CMultiCartResolverStrategyImplTest {

	private static final String CUSTOMER_GUID = "customerGuid";
	private static final String ACCOUNT_SHARED_ID = "accountSharedId";
	private static final String STORE_CODE = "storeCode";

	@Mock
	private ShoppingCartService shoppingCartService;

	@Mock
	private CartPostProcessor cartPostProcessor;

	@Mock
	private ShopperRepository shopperRepository;

	@InjectMocks
	private ReactiveAdapterImpl reactiveAdapterImpl;

	private B2CMultiCartResolutionStrategyImpl strategy;

	@Mock
	private Subject subject;

	@Mock
	private StoreService storeService;

	@Mock
	private ExceptionTransformer exceptionTransformer;

	@Mock
	private ResourceOperationContext resourceOperationContext;

	@Mock
	private MulticartItemListTypeLocationProvider multicartItemListTypeLocationProvider;

	@Before
	public void setUp() {
		strategy = spy(new B2CMultiCartResolutionStrategyImpl());

		strategy.setReactiveAdapter(reactiveAdapterImpl);
		strategy.setReactiveAdapter(reactiveAdapterImpl);
		strategy.setCartPostProcessor(cartPostProcessor);
		strategy.setShopperRepository(shopperRepository);
		strategy.setExceptionTransformer(exceptionTransformer);
		strategy.setShoppingCartService(shoppingCartService);
		strategy.setStoreService(storeService);
		strategy.setResourceOperationContext(resourceOperationContext);
		strategy.setMulticartItemListTypeLocationProvider(multicartItemListTypeLocationProvider);
		when(storeService.getCartTypeNamesForStore(STORE_CODE))
				.thenReturn(Collections.singletonList("SHOPPING_CART"));
		when(multicartItemListTypeLocationProvider.getMulticartItemListTypeForStore(STORE_CODE)).thenReturn("SHOPPING_CART");

	}


	@Test
	public void testGetValidCartTypeForStrategy() {
		assertThat(strategy.getValidCartTypeForStrategy(STORE_CODE)).isEqualTo("SHOPPING_CART");
	}


	@Test
	public void testIsApplicable() {
		boolean applicable = strategy.isApplicable(subject);
		assertThat(applicable).isTrue();
	}

	@Test
	public void testIsApplicableWhenSubjectHasNoMetadata() {
		boolean applicable = strategy.isApplicable(subject);
		assertThat(applicable).isTrue();
	}

	@Test
	public void testIsApplicableWithNoSubject() {
		boolean applicable = strategy.isApplicable(null);
		assertThat(applicable).isTrue();
	}

	@Test
	public void testSupportsCreateWithRegistedCustomer() {
		Shopper mockShopper = mock(Shopper.class);
		Customer mockCustomer = mock(Customer.class);
		when(mockShopper.getCustomer()).thenReturn(mockCustomer);
		when(mockCustomer.isRegistered()).thenReturn(true);

		boolean result = strategy.supportsCreate(subject, mockShopper, STORE_CODE);
		assertThat(result).isTrue();
	}


	@Test
	public void testSupportsCreateWithNonRegisteredCustomer() {
		Shopper mockShopper = mock(Shopper.class);
		Customer mockCustomer = mock(Customer.class);
		when(mockShopper.getCustomer()).thenReturn(mockCustomer);
		when(mockCustomer.isRegistered()).thenReturn(false);

		boolean result = strategy.supportsCreate(subject, mockShopper, STORE_CODE);
		assertThat(result).isFalse();

	}

	@Test
	public void testSupportsCreateWithInvalidCartType() {
		Shopper mockShopper = mock(Shopper.class);
		when(storeService.getCartTypeNamesForStore(STORE_CODE))
				.thenReturn(Collections.singletonList("Invalid"));

		boolean result = strategy.supportsCreate(subject, mockShopper, STORE_CODE);
		assertThat(result).isFalse();
	}

	@Test
	public void testSupportsCreateWithNoCartType() {
		Shopper mockShopper = mock(Shopper.class);
		when(storeService.getCartTypeNamesForStore(STORE_CODE)).thenReturn(Collections.emptyList());

		boolean result = strategy.supportsCreate(subject, mockShopper, STORE_CODE);
		assertThat(result).isFalse();
	}

	@Test
	public void testFindAllCarts() {
		when(shoppingCartService.findByCustomerAndStore(CUSTOMER_GUID, ACCOUNT_SHARED_ID, STORE_CODE.toUpperCase(Locale.getDefault())))
				.thenReturn(Collections.singletonList(CART_GUID));

		strategy.findAllCarts(CUSTOMER_GUID, ACCOUNT_SHARED_ID, STORE_CODE, null)
				.test()
				.assertNoErrors()
				.assertValueCount(1)
				.assertValue(CART_GUID);

	}

	@Test
	public void testGetShoppingCart() {

		ShoppingCart mockCart = mockExistingCart();
		Shopper shopper = mock(Shopper.class);

		when(shopperRepository.findOrCreateShopper())
				.thenReturn(Single.just(shopper));
		when(shoppingCartService.findByGuid(CART_GUID))
				.thenReturn(mockCart);

		strategy.getShoppingCartSingle(CART_GUID)
				.test()
				.assertNoErrors()
				.assertValue(mockCart);

	}

	@Test
	public void testGetModifierFields() {

		ModifierField modifierField = mock(ModifierField.class);
		CartType cartType = mock(CartType.class);
		Store store = mock(Store.class);
		ModifierGroup modifierGroup = mock(ModifierGroup.class);

		when(store.getShoppingCartTypes()).thenReturn(Collections.singletonList(cartType));
		when(cartType.getModifiers()).thenReturn(Collections.singletonList(modifierGroup));
		when(modifierGroup.getModifierFields()).thenReturn(Collections.singleton(modifierField));

		when(storeService.findStoreWithCode(STORE_CODE)).thenReturn(store);

		assertThat(strategy.getModifierFields(STORE_CODE)).containsExactly(modifierField);
	}

	/**
	 * Test the behaviour of get default shopping cart guid.
	 */
	@Test
	public void testGetDefaultShoppingCartGuid() {
		Shopper shopper = mock(Shopper.class);

		when(shopperRepository.findOrCreateShopper())
				.thenReturn(Single.just(shopper));
		when(shoppingCartService.findOrCreateDefaultCartGuidByShopper(shopper))
				.thenReturn(CART_GUID);
		strategy.getDefaultShoppingCartGuid()
				.test()
				.assertNoErrors()
				.assertValue(CART_GUID);
	}

	private ShoppingCart mockExistingCart() {
		ShoppingCart mockCart = mock(ShoppingCart.class);
		when(shoppingCartService.findByGuid(CART_GUID))
				.thenReturn(mockCart);
		return mockCart;
	}

}
