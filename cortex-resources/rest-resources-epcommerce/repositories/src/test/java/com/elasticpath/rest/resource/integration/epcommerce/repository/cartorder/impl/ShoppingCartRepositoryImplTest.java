/*
 * Copyright © 2013 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.impl;

import static java.util.Arrays.asList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.functions.Predicate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.elasticpath.common.dto.ShoppingItemDto;
import com.elasticpath.common.dto.StructuredErrorMessage;
import com.elasticpath.common.dto.sellingchannel.ShoppingItemDtoFactory;
import com.elasticpath.commons.exception.InvalidBusinessStateException;
import com.elasticpath.domain.customer.CustomerSession;
import com.elasticpath.domain.shopper.Shopper;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.shoppingcart.ShoppingItem;
import com.elasticpath.domain.shoppingcart.impl.ShoppingItemImpl;
import com.elasticpath.rest.ResourceOperationFailure;
import com.elasticpath.rest.ResourceStatus;
import com.elasticpath.rest.advise.Message;
import com.elasticpath.rest.command.ExecutionResult;
import com.elasticpath.rest.command.ExecutionResultFactory;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.ShoppingItemValidationService;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.CustomerSessionRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.sku.ProductSkuRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.transform.ExceptionTransformer;
import com.elasticpath.rest.resource.integration.epcommerce.repository.transform.impl.ReactiveAdapterImpl;
import com.elasticpath.sellingchannel.ProductUnavailableException;
import com.elasticpath.sellingchannel.director.CartDirectorService;
import com.elasticpath.service.shoppingcart.ShoppingCartService;

/**
 * Test for {@link ShoppingCartRepositoryImpl}.
 */
@SuppressWarnings({"deprecation", "PMD.TooManyMethods"})
@RunWith(MockitoJUnitRunner.class)
public class ShoppingCartRepositoryImplTest {
	private static final String CART_GUID = "cart";
	private static final String USER_GUID = "user";
	private static final String SKU_CODE = "sku";
	private static final String STORE_CODE = "store";

	private static final String SUCCESSFUL_OPERATION = "The operation should have been successful";
	public static final String CART_ASSERTION_ERROR =
			"The result should be the cart returned by the core service";
	public static final String PLACEHOLDER_KEY = "Placeholder Key";
	public static final String PLACEHOLDER_VALUE = "Placeholder Value";

	@Mock
	private ShoppingCartService shoppingCartService;

	@Mock
	private CartDirectorService cartDirectorService;

	@Mock
	private CartPostProcessor cartPostProcessor;

	@Mock
	private Shopper mockShopper;

	@Mock
	private CustomerSessionRepository customerSessionRepository;

	@Mock
	private ShoppingItemValidationService shoppingItemValidationService;

	@Mock
	private ProductSkuRepository productSkuRepository;

	@Mock
	private ShoppingItemDtoFactory shoppingItemDtoFactory;

	@Mock
	private ExceptionTransformer exceptionTransformer;

	@InjectMocks
	private ReactiveAdapterImpl reactiveAdapterImpl;

	private ShoppingCartRepositoryImpl repository;

	@Before
	public void setUp() {
		repository = new ShoppingCartRepositoryImpl(shoppingCartService, cartDirectorService, customerSessionRepository,
				shoppingItemDtoFactory, cartPostProcessor, reactiveAdapterImpl, shoppingItemValidationService, productSkuRepository);
		given(shoppingItemValidationService.validate(any(ShoppingItemDto.class))).willReturn(Completable.complete());
	}

	/**
	 * Test the behaviour of get default shopping cart.
	 */
	@Test
	public void testGetDefaultShoppingCart() {
		CustomerSession mockCustomerSession = createMockCustomerSession();
		ShoppingCart cart = createMockShoppingCart();

		expectCartPostProcessing(cart);
		when(shoppingCartService.findOrCreateByCustomerSession(mockCustomerSession)).thenReturn(cart);
		when(shoppingCartService.saveIfNotPersisted(cart)).thenReturn(cart);

		repository.getDefaultShoppingCart()
				.test()
				.assertNoErrors()
				.assertValue(cart);

		verifyPostProcess(cart, mockCustomerSession);
	}

	/**
	 * Test the behaviour of get default shopping cart when cart not found.
	 */
	@Test
	public void testGetDefaultShoppingCartWhenCartNotFound() {
		CustomerSession mockCustomerSession = createMockCustomerSession();

		when(mockCustomerSession.getShopper()).thenReturn(mockShopper);
		when(shoppingCartService.findOrCreateByShopper(mockShopper)).thenReturn(null);

		repository.getDefaultShoppingCart()
				.test()
				.assertError(isResourceOperationFailureNotFound());
	}

	private Predicate<Throwable> isResourceOperationFailureNotFound() {
		return throwable -> throwable instanceof ResourceOperationFailure
				&& ResourceStatus.NOT_FOUND.equals(((ResourceOperationFailure) throwable).getResourceStatus());
	}

	/**
	 * Test the behaviour of get shopping cart.
	 */
	@Test
	public void testGetShoppingCart() {
		CustomerSession mockCustomerSession = createMockCustomerSession();
		ShoppingCart cart = createMockShoppingCart();

		expectCartPostProcessing(cart);
		when(shoppingCartService.findByGuid(CART_GUID)).thenReturn(cart);

		ExecutionResult<ShoppingCart> result = repository.getShoppingCart(CART_GUID);

		verifyPostProcess(cart, mockCustomerSession);
		assertThat(result.isSuccessful())
				.as(SUCCESSFUL_OPERATION)
				.isTrue();
		assertThat(result.getData())
				.as(CART_ASSERTION_ERROR)
				.isEqualTo(cart);
	}

	/**
	 * Test the behaviour of get shopping cart when cart not found.
	 */
	@Test
	public void testGetShoppingCartWhenCartNotFound() {
		createMockCustomerSession();

		when(shoppingCartService.findByGuid(CART_GUID)).thenReturn(null);

		ExecutionResult<ShoppingCart> result = repository.getShoppingCart(CART_GUID);
		assertThat(result.isFailure())
				.as("The operation should have failed")
				.isTrue();
		assertThat(result.getResourceStatus())
				.as("The status should be NOT FOUND")
				.isEqualTo(ResourceStatus.NOT_FOUND);
	}

	@Test
	public void givenValidCartAndStoreVerifyShoppingCartExists() {
		when(shoppingCartService.shoppingCartExistsForStore(CART_GUID, STORE_CODE)).thenReturn(true);

		repository.verifyShoppingCartExistsForStore(CART_GUID, STORE_CODE)
				.test()
				.assertValue(true);
	}

	@Test
	public void testVerifyShoppingCartExistsThrowsException() {
		when(shoppingCartService.shoppingCartExistsForStore(CART_GUID, STORE_CODE)).thenReturn(false);

		repository.verifyShoppingCartExistsForStore(CART_GUID, STORE_CODE)
				.test()
				.assertValue(false);
	}

	@Test
	public void testAddItemToCartHappyPath() {
		ShoppingCart cart = createMockShoppingCart();
		ShoppingItemDto item = new ShoppingItemDto(SKU_CODE, 1);
		ShoppingItem addedItem = new ShoppingItemImpl();
		addedItem.setUidPk(1L);

		when(shoppingItemDtoFactory.createDto(SKU_CODE, 1)).thenReturn(item);
		when(cartDirectorService.addItemToCart(cart, item)).thenReturn(addedItem);

		repository.addItemToCart(cart, SKU_CODE, 1, Collections.emptyMap())
				.test()
				.assertNoErrors()
				.assertValue(addedItem);
	}

	@Test
	public void testUpdateCartItemHappyPath() {
		ShoppingCart cart = createMockShoppingCart();
		ShoppingItem shoppingItem = mock(ShoppingItem.class);
		when(shoppingItem.getUidPk()).thenReturn(1L);
		ShoppingItemDto shoppingItemDto = new ShoppingItemDto(SKU_CODE, 2);
		when(shoppingItemDtoFactory.createDto(SKU_CODE, 2)).thenReturn(shoppingItemDto);

		repository.updateCartItem(cart, shoppingItem, SKU_CODE, 2)
				.test()
				.assertNoErrors();

		assertThat(cart.getCartItem(SKU_CODE))
				.isNull();

		verify(cartDirectorService).updateCartItem(cart, 1L, shoppingItemDto);
	}

	@Test
	public void shouldNotUpdateCartItemWhenProductIsNotPurchasable() {
		ShoppingCart cart = createMockShoppingCart();
		ShoppingItem shoppingItem = mock(ShoppingItem.class);
		when(shoppingItem.getUidPk()).thenReturn(1L);
		ShoppingItemDto shoppingItemDto = new ShoppingItemDto(SKU_CODE, 2);
		when(shoppingItemDtoFactory.createDto(SKU_CODE, 2)).thenReturn(shoppingItemDto);

		String productNotPurchasableError = "error message";
		StructuredErrorMessage structuredErrorMessage = mock(StructuredErrorMessage.class);
		when(cartDirectorService.updateCartItem(any(ShoppingCart.class), anyLong(), any(ShoppingItemDto.class)))
				.thenThrow(
						new ProductUnavailableException(
								productNotPurchasableError,
								Collections.singletonList(structuredErrorMessage)
						)
				);
		Message mockMessage = mock(Message.class);
		when(exceptionTransformer.getResourceOperationFailure(any(InvalidBusinessStateException.class)))
				.thenReturn(ResourceOperationFailure.stateFailure(productNotPurchasableError, Collections.singletonList(mockMessage)));

		repository.updateCartItem(cart, shoppingItem, SKU_CODE, 2)
				.test()
				.assertError(ResourceOperationFailure.class)
				.assertError(throwable -> {
					if (throwable instanceof ResourceOperationFailure) {
						List<Message> messages = ((ResourceOperationFailure) throwable).getMessages();
						return messages.contains(mockMessage);
					}
					return false;
				});

		verify(cartDirectorService).updateCartItem(cart, 1L, shoppingItemDto);
	}

	@Test
	public void testRemoveItemFromCartHappyPath() {
		ShoppingCart cart = createMockShoppingCart();

		repository.removeItemFromCart(cart, "1") //TODO
				.test()
				.assertNoErrors();
		verify(cartDirectorService).removeItemsFromCart(cart, "1");
	}

	@Test
	public void testRemoveAllItemsFromCartHappyPath() {
		ShoppingCart cart = createMockShoppingCart();
		CustomerSession mockCustomerSession = createMockCustomerSession();

		expectCartPostProcessing(cart);
		when(shoppingCartService.findOrCreateByCustomerSession(mockCustomerSession)).thenReturn(cart);
		when(shoppingCartService.saveIfNotPersisted(cart)).thenReturn(cart);


		repository.removeAllItemsFromCart()
				.test()
				.assertNoErrors();

		verify(cartDirectorService).clearItems(cart);
	}

	@Test
	public void testFindAllCarts() {
		when(shoppingCartService.findByCustomerAndStore(USER_GUID, STORE_CODE.toUpperCase(Locale.getDefault())))
				.thenReturn(asList(CART_GUID, "OTHER_GUID"));

		repository.findAllCarts(USER_GUID, STORE_CODE)
				.test()
				.assertNoErrors()
				.assertValueCount(2);
	}

	@Test
	public void testUpdateCartItemWithFields() {
		ShoppingCart cart = createMockShoppingCart();
		ShoppingItemDto item = new ShoppingItemDto(SKU_CODE, 1);
		ShoppingItem addedItem = new ShoppingItemImpl();
		addedItem.setUidPk(1L);

		when(shoppingItemDtoFactory.createDto(SKU_CODE, 1)).thenReturn(item);
		when(cartDirectorService.addItemToCart(cart, item)).thenReturn(addedItem);

		Single<ShoppingItem> shoppingItemSingle = repository.addItemToCart(cart, SKU_CODE, 1, Collections.emptyMap());
		shoppingItemSingle.test()
				.assertNoErrors()
				.assertValue(addedItem)
				.assertValue(shoppingItem -> shoppingItem.getFields().size() == 0);

		addedItem.setFieldValue(PLACEHOLDER_KEY, PLACEHOLDER_VALUE);
		repository.updateCartItem(cart, addedItem, SKU_CODE, 1)
				.test()
				.assertNoErrors();
	}


	private ShoppingCart createMockShoppingCart() {
		ShoppingCart cart = mock(ShoppingCart.class);
		when(cart.getGuid()).thenReturn(CART_GUID);
		when(cart.getShopper()).thenReturn(mockShopper);

		return cart;
	}

	private CustomerSession createMockCustomerSession() {
		CustomerSession mockCustomerSession = mock(CustomerSession.class);

		when(customerSessionRepository.findOrCreateCustomerSession()).thenReturn(ExecutionResultFactory.createReadOK(mockCustomerSession));
		when(customerSessionRepository.findOrCreateCustomerSessionAsSingle()).thenReturn(Single.just(mockCustomerSession));
		when(mockCustomerSession.getShopper()).thenReturn(mockShopper);

		return mockCustomerSession;
	}

	private void expectCartPostProcessing(final ShoppingCart cart) {
		when(mockShopper.getCurrentShoppingCart()).thenReturn(cart);
	}

	private void verifyPostProcess(final ShoppingCart cart,
								   final CustomerSession customerSession) {
		verify(cartPostProcessor, atLeastOnce()).postProcessCart(cart, cart.getShopper(), customerSession);
	}
}
