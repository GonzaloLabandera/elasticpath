/*
 * Copyright © 2013 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.functions.Predicate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.base.common.dto.StructuredErrorMessage;
import com.elasticpath.base.exception.structured.InvalidBusinessStateException;
import com.elasticpath.common.dto.ShoppingItemDto;
import com.elasticpath.common.dto.sellingchannel.ShoppingItemDtoFactory;
import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.impl.ElasticPathImpl;
import com.elasticpath.domain.misc.types.ModifierFieldsMapWrapper;
import com.elasticpath.domain.shopper.Shopper;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.shoppingcart.ShoppingItem;
import com.elasticpath.domain.shoppingcart.impl.ShoppingItemImpl;
import com.elasticpath.rest.ResourceOperationFailure;
import com.elasticpath.rest.ResourceStatus;
import com.elasticpath.rest.advise.Message;
import com.elasticpath.rest.identity.Subject;
import com.elasticpath.rest.resource.ResourceOperationContext;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.MultiCartResolutionStrategy;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.MultiCartResolutionStrategyHolder;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.ShopperRepository;
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
	private static final String CART_GUID = "cartGuid";
	private static final String USER_GUID = "userGuid";
	private static final String ACCOUNT_SHARED_ID = "accountSharedId";
	private static final String SKU_CODE = "skuCode";
	private static final String STORE_CODE = "storeCode";

	private static final String PLACEHOLDER_KEY = "Placeholder Key";
	private static final String PLACEHOLDER_VALUE = "Placeholder Value";

	@Mock
	private ShopperRepository shopperRepository;

	@Mock
	private ShoppingCartService shoppingCartService;

	@Mock
	private CartDirectorService cartDirectorService;

	@Mock
	private ShoppingItemDtoFactory shoppingItemDtoFactory;

	@Mock
	private ResourceOperationContext resourceOperationContext;

	@Mock
	private ExceptionTransformer exceptionTransformer;

	@InjectMocks
	private ReactiveAdapterImpl reactiveAdapterImpl;

	@Mock
	private MultiCartResolutionStrategyHolder multiCartResolutionStrategyHolder;

	@Mock
	private MultiCartResolutionStrategy strategy;

	@InjectMocks
	private ShoppingCartRepositoryImpl repository;

	@Mock
	private Subject subject;

	@Mock
	private ShoppingCart cart;

	@Mock
	private Shopper shopper;

	@Mock
	private BeanFactory beanFactory;

	@SuppressWarnings("PMD.DontUseElasticPathImplGetInstance")
	private final ElasticPathImpl elasticPath = (ElasticPathImpl) ElasticPathImpl.getInstance();

	@Before
	public void setUp() {
		elasticPath.setBeanFactory(beanFactory);
		when(multiCartResolutionStrategyHolder.getStrategies()).thenReturn(Collections.singletonList(strategy));
		repository.setReactiveAdapter(reactiveAdapterImpl);
		when(beanFactory.getPrototypeBean(ContextIdNames.MODIFIER_FIELDS_MAP_WRAPPER, ModifierFieldsMapWrapper.class))
				.thenAnswer(invocation -> new ModifierFieldsMapWrapper());
	}

	/**
	 * Test the behaviour of get default shopping cart.
	 */
	@Test
	public void testGetDefaultShoppingCart() {
		when(resourceOperationContext.getSubject()).thenReturn(subject);
		when(strategy.isApplicable(subject)).thenReturn(true);
		when(strategy.getDefaultShoppingCart()).thenReturn(Single.just(cart));
		repository.getDefaultShoppingCart()
				.test()
				.assertNoErrors()
				.assertValue(cart);

	}

	/**
	 * Test the behaviour of get shopping cart when cart not found.
	 */
	@Test
	public void testGetShoppingCartWhenCartNotFound() {
		when(resourceOperationContext.getSubject()).thenReturn(subject);
		when(strategy.isApplicable(subject)).thenReturn(true);
		when(strategy.getShoppingCartSingle(CART_GUID))
				.thenReturn(Single.error(ResourceOperationFailure.notFound()));


		Single<ShoppingCart> result = repository.getShoppingCart(CART_GUID);

		result.test()
				.assertError(ResourceOperationFailure.notFound());
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
		ShoppingItem shoppingItem = mock(ShoppingItem.class, RETURNS_DEEP_STUBS);
		when(shoppingItem.getUidPk()).thenReturn(1L);
		ShoppingItemDto shoppingItemDto = new ShoppingItemDto(SKU_CODE, 2);
		when(shoppingItemDtoFactory.createDto(SKU_CODE, 2)).thenReturn(shoppingItemDto);

		repository.updateCartItem(cart, shoppingItem, SKU_CODE, 2)
				.test()
				.assertNoErrors();

		assertThat(cart.getCartItems(SKU_CODE))
				.isEmpty();

		verify(cartDirectorService).updateCartItem(cart, 1L, shoppingItemDto);
	}

	@Test
	public void shouldNotUpdateCartItemWhenProductIsNotPurchasable() {
		ShoppingItem shoppingItem = mock(ShoppingItem.class, RETURNS_DEEP_STUBS);
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

		repository.removeItemFromCart(cart, "1") //TODO
				.test()
				.assertNoErrors();
		verify(cartDirectorService).removeItemsFromCart(cart, "1");
	}

	@Test
	public void testRemoveAllItemsFromDefaultCartHappyPath() {

		when(resourceOperationContext.getSubject()).thenReturn(subject);
		when(strategy.isApplicable(subject)).thenReturn(true);
		when(strategy.getDefaultShoppingCart()).thenReturn(Single.just(cart));

		repository.removeAllItemsFromDefaultCart()
				.test()
				.assertNoErrors();

		verify(cartDirectorService).clearItems(cart);
	}

	@Test
	public void testRemoveAllItemsFromCartHappyPath() {
		repository.removeAllItemsFromCart(cart)
				.test()
				.assertNoErrors();

		verify(cartDirectorService).clearItems(cart);
	}

	@Test
	public void testFindAllCarts() {
		when(resourceOperationContext.getSubject()).thenReturn(subject);
		when(strategy.isApplicable(subject)).thenReturn(true);
		when(strategy.findAllCarts(USER_GUID, ACCOUNT_SHARED_ID, STORE_CODE, subject))
				.thenReturn(Observable.just(CART_GUID, "OTHER_GUID"));
		repository.findAllCarts(USER_GUID, ACCOUNT_SHARED_ID, STORE_CODE)
				.test()
				.assertNoErrors()
				.assertValueCount(2);
	}

	@Test
	public void testUpdateCartItemWithFields() {
		ShoppingItemDto item = new ShoppingItemDto(SKU_CODE, 1);
		ShoppingItem addedItem = new ShoppingItemImpl();
		addedItem.setUidPk(1L);

		when(shoppingItemDtoFactory.createDto(SKU_CODE, 1)).thenReturn(item);
		when(cartDirectorService.addItemToCart(cart, item)).thenReturn(addedItem);

		Single<ShoppingItem> shoppingItemSingle = repository.addItemToCart(cart, SKU_CODE, 1, Collections.emptyMap());
		shoppingItemSingle.test()
				.assertNoErrors()
				.assertValue(addedItem)
				.assertValue(shoppingItem -> shoppingItem.getModifierFields().getMap().size() == 0);

		addedItem.getModifierFields().put(PLACEHOLDER_KEY, PLACEHOLDER_VALUE);
		repository.updateCartItem(cart, addedItem, SKU_CODE, 1)
				.test()
				.assertNoErrors();
	}

	/**
	 * Test the behaviour of get storecode for shopping cart guid.
	 */
	@Test
	public void testGettingStoreCodeForShoppingCartGuid() {

		when(shoppingCartService.findStoreCodeByCartGuid(CART_GUID)).thenReturn(STORE_CODE);
		repository.findStoreForCartGuid(CART_GUID)
				.test()
				.assertNoErrors()
				.assertValue(STORE_CODE);

	}

	/**
	 * Test the behaviour of get storecode for shopping cart guid.
	 */
	@Test
	public void testGettingStoreCodeForShoppingCartGuidWhenNotFound() {

		when(shoppingCartService.findStoreCodeByCartGuid(CART_GUID)).thenReturn(null);
		repository.findStoreForCartGuid(CART_GUID)
				.test()
				.assertError(isResourceOperationFailureNotFound());

	}

	/**
	 * Test the behaviour of get shopping cart for customer.
	 */
	@Test
	public void testGettingShoppingCartForCustomer() {
		when(resourceOperationContext.getSubject()).thenReturn(subject);
		when(strategy.isApplicable(subject)).thenReturn(true);
		when(shopperRepository.findOrCreateShopper(USER_GUID, STORE_CODE))
				.thenReturn(Single.just(shopper));
		when(strategy.getDefaultCart(shopper))
				.thenReturn(Single.just(cart));

		repository.getDefaultShoppingCartForCustomer(USER_GUID, STORE_CODE)
				.test()
				.assertNoErrors()
				.assertValue(cart);
	}

	private Predicate<Throwable> isResourceOperationFailureNotFound() {
		return throwable -> throwable instanceof ResourceOperationFailure
				&& ResourceStatus.NOT_FOUND.equals(((ResourceOperationFailure) throwable).getResourceStatus());
	}

}
