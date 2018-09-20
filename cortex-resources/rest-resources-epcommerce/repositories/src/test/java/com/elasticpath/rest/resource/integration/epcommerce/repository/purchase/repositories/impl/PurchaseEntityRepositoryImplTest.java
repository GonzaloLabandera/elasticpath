/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.purchase.repositories.impl;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;

import io.reactivex.Observable;
import io.reactivex.Single;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.elasticpath.domain.cartorder.CartOrder;
import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.domain.catalogview.StoreProduct;
import com.elasticpath.domain.customer.CustomerSession;
import com.elasticpath.domain.misc.CheckoutResults;
import com.elasticpath.domain.order.Order;
import com.elasticpath.domain.order.OrderPayment;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.shoppingcart.ShoppingCartTaxSnapshot;
import com.elasticpath.domain.shoppingcart.impl.ShoppingItemImpl;
import com.elasticpath.rest.ResourceOperationFailure;
import com.elasticpath.rest.definition.orders.OrderIdentifier;
import com.elasticpath.rest.definition.purchases.PurchaseEntity;
import com.elasticpath.rest.definition.purchases.PurchaseIdentifier;
import com.elasticpath.rest.definition.purchases.PurchasesIdentifier;
import com.elasticpath.rest.form.SubmitResult;
import com.elasticpath.rest.form.SubmitStatus;
import com.elasticpath.rest.id.IdentifierPart;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.resource.integration.epcommerce.repository.availabilities.ItemAvailabilityValidationService;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.CartOrderRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.PricingSnapshotRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.CustomerSessionRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.product.StoreProductRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.purchase.repositories.PurchaseRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.purchase.service.impl.CartHasItemsServiceImpl;

/**
 * Test for the  {@link PurchaseEntityRepositoryImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class PurchaseEntityRepositoryImplTest {

	private static final IdentifierPart<String> SCOPE = StringIdentifier.of("Store");
	private static final IdentifierPart<String> PURCHASE_ID = StringIdentifier.of("1234");
	private static final String ORDER_ID = "Order id";
	private static final String ITEM_GUID = "guid";
	private static final String SKU_CODE = "code";

	@InjectMocks
	private PurchaseEntityRepositoryImpl<PurchaseEntity, PurchaseIdentifier> entityRepository;

	@Mock
	private PricingSnapshotRepository pricingSnapshotRepository;

	@Mock
	private CustomerSessionRepository sessionRepository;

	@Mock
	private PurchaseRepository purchaseRepository;

	@Mock
	private CartOrderRepository cartOrderRepository;

	@Mock
	private StoreProductRepository storeProductRepository;

	@Mock
	private ItemAvailabilityValidationService itemAvailabilityValidationService;

	@InjectMocks
	private CartHasItemsServiceImpl cartHasItemsService;

	@Before
	public void setUp() {
		entityRepository.setCartOrderRepository(cartOrderRepository);
		entityRepository.setCartHasItemsService(cartHasItemsService);
		entityRepository.setItemAvailabilityValidationService(itemAvailabilityValidationService);
		entityRepository.setPricingSnapshotRepository(pricingSnapshotRepository);
	}

	@Test
	public void createPurchaseWithFailure1() {
		PurchaseEntity entity = performCreateOperationSetupWhere(true, true);

		entityRepository.submit(entity, SCOPE)
				.test()
				.assertError(ResourceOperationFailure.serverError(PurchaseEntityRepositoryImpl.NOT_PURCHASABLE));
	}

	@Test
	public void createPurchaseWithFailure2() {
		PurchaseEntity entity = performCreateOperationSetupWhere(true, false);

		entityRepository.submit(entity, SCOPE)
				.test()
				.assertError(ResourceOperationFailure.serverError(PurchaseEntityRepositoryImpl.NOT_PURCHASABLE));
	}

	@Test
	public void createPurchaseWithSuccess() {
		PurchaseEntity entity = performCreateOperationSetupWhere(false, true);

		PurchasesIdentifier purchasesIdentifier = PurchasesIdentifier.builder()
				.withScope(SCOPE)
				.build();
		PurchaseIdentifier purchaseIdentifier = PurchaseIdentifier.builder()
				.withPurchaseId(PURCHASE_ID)
				.withPurchases(purchasesIdentifier)
				.build();

		SubmitResult<PurchaseIdentifier> submitResult = SubmitResult.<PurchaseIdentifier>builder()
				.withIdentifier(purchaseIdentifier)
				.withStatus(SubmitStatus.CREATED)
				.build();

		entityRepository.submit(entity, SCOPE)
				.test()
				.assertValue(submitResult);
	}

	private PurchaseEntity performCreateOperationSetupWhere(final boolean shoppingCartIsEmpty, final boolean allSkusAvailable) {
		CartOrder cartOrder = mock(CartOrder.class);
		StoreProduct storeProduct = mock(StoreProduct.class);
		ProductSku defaultSku = mock(ProductSku.class);
		ShoppingCartTaxSnapshot taxSnapshot = mock(ShoppingCartTaxSnapshot.class);
		CustomerSession customerSession = mock(CustomerSession.class);
		OrderPayment orderPayment = mock(OrderPayment.class);
		CheckoutResults checkoutResults = mock(CheckoutResults.class);
		ShoppingCart shoppingCart = createShoppingCart(shoppingCartIsEmpty);

		when(cartOrderRepository.findByGuidAsSingle(SCOPE.getValue(), ORDER_ID)).thenReturn(Single.just(cartOrder));

		when(purchaseRepository.createNewOrderPaymentEntity()).thenReturn(Single.just(orderPayment));

		when(cartOrderRepository.getEnrichedShoppingCartSingle(SCOPE.getValue(), cartOrder)).thenReturn(Single.just(shoppingCart));

		when(storeProduct.getSkuByGuid(ITEM_GUID)).thenReturn(defaultSku);

		when(defaultSku.getSkuCode()).thenReturn(SKU_CODE);

		when(storeProductRepository.findDisplayableStoreProductWithAttributesBySkuGuid(any(), any()))
				.thenReturn(Single.just(storeProduct));

		when(sessionRepository.findOrCreateCustomerSessionAsSingle()).thenReturn(Single.just(customerSession));

		when(pricingSnapshotRepository.getShoppingCartTaxSnapshot(shoppingCart)).thenReturn(Single.just(taxSnapshot));

		when(storeProduct.isSkuAvailable(any())).thenReturn(allSkusAvailable);

		when(purchaseRepository.checkout(shoppingCart, taxSnapshot, customerSession, orderPayment)).thenReturn(Single.just(checkoutResults));

		when(itemAvailabilityValidationService.validateItemUnavailable(any(OrderIdentifier.class))).thenReturn(Observable.empty());

		Order order = mock(Order.class);
		when(checkoutResults.isOrderFailed()).thenReturn(false);
		when(checkoutResults.getOrder()).thenReturn((order));
		when(order.getGuid()).thenReturn(PURCHASE_ID.getValue());

		return PurchaseEntity.builder()
				.withOrderId(ORDER_ID)
				.build();
	}

	private ShoppingCart createShoppingCart(final boolean empty) {
		ShoppingCart shoppingCart = mock(ShoppingCart.class);
		if (empty) {
			when(shoppingCart.getCartItems()).thenReturn(Collections.emptyList());
		} else {
			ShoppingItemImpl shoppingItem = new ShoppingItemImpl();
			shoppingItem.setSkuGuid(ITEM_GUID);

			when(shoppingCart.getCartItems()).thenReturn(Collections.singletonList(shoppingItem));
		}
		return shoppingCart;
	}
}
