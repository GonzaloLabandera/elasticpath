/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.purchase.repositories.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import io.reactivex.Single;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.cartorder.CartOrder;
import com.elasticpath.domain.customer.CustomerSession;
import com.elasticpath.domain.misc.CheckoutResults;
import com.elasticpath.domain.order.Order;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.shoppingcart.ShoppingCartTaxSnapshot;
import com.elasticpath.rest.ResourceOperationFailure;
import com.elasticpath.rest.definition.orders.OrderIdentifier;
import com.elasticpath.rest.definition.purchases.CreatePurchaseFormIdentifier;
import com.elasticpath.rest.definition.purchases.PurchaseFormEntity;
import com.elasticpath.rest.definition.purchases.PurchaseIdentifier;
import com.elasticpath.rest.definition.purchases.PurchasesIdentifier;
import com.elasticpath.rest.form.SubmitResult;
import com.elasticpath.rest.form.SubmitStatus;
import com.elasticpath.rest.id.IdentifierPart;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.resource.ResourceOperationContext;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.CartOrderRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.PricingSnapshotRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.CustomerSessionRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.purchase.repositories.PurchaseRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.purchase.service.impl.CartHasItemsServiceImpl;

/**
 * Test for the {@link PurchaseFormEntityRepositoryImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class PurchaseFormEntityRepositoryImplTest {

	private static final IdentifierPart<String> SCOPE = StringIdentifier.of("Store");
	private static final IdentifierPart<String> PURCHASE_ID = StringIdentifier.of("1234");
	private static final String ORDER_ID = "Order id";

	@InjectMocks
	private PurchaseFormEntityRepositoryImpl<PurchaseFormEntity, PurchaseIdentifier> entityRepository;

	@Mock
	private PricingSnapshotRepository pricingSnapshotRepository;

	@Mock
	private CustomerSessionRepository sessionRepository;

	@Mock
	private PurchaseRepository purchaseRepository;

	@Mock
	private CartOrderRepository cartOrderRepository;

	@Mock
	private ResourceOperationContext resourceOperationContext;

	@InjectMocks
	private CartHasItemsServiceImpl cartHasItemsService;

	@Before
	public void setUp() {
		entityRepository.setCartOrderRepository(cartOrderRepository);
		entityRepository.setCartHasItemsService(cartHasItemsService);
		entityRepository.setPricingSnapshotRepository(pricingSnapshotRepository);
		entityRepository.setResourceOperationContext(resourceOperationContext);
	}

	@Test
	public void createPurchaseWithFailure1() {
		PurchaseFormEntity entity = performCreateOperationSetupWhere(true);

		entityRepository.submit(entity, SCOPE)
				.test()
				.assertError(ResourceOperationFailure.serverError(PurchaseEntityRepositoryImpl.NOT_PURCHASABLE));
	}

	@Test
	public void createPurchaseWithFailure2() {
		PurchaseFormEntity entity = performCreateOperationSetupWhere(true);

		entityRepository.submit(entity, SCOPE)
				.test()
				.assertError(ResourceOperationFailure.serverError(PurchaseEntityRepositoryImpl.NOT_PURCHASABLE));
	}

	@Test
	public void createPurchaseWithFailure3() {
		when(resourceOperationContext.getResourceIdentifier()).thenReturn(Optional.empty());

		entityRepository.submit(null, SCOPE)
				.test()
				.assertError(ResourceOperationFailure.serverError(PurchaseEntityRepositoryImpl.NOT_PURCHASABLE));
	}

	@Test
	public void createPurchaseWithSuccess() {
		PurchaseFormEntity entity = performCreateOperationSetupWhere(false);

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

	private PurchaseFormEntity performCreateOperationSetupWhere(final boolean shoppingCartIsEmpty) {
		CreatePurchaseFormIdentifier createPurchaseFormIdentifier = CreatePurchaseFormIdentifier.builder()
				.withOrder(OrderIdentifier.builder().withOrderId(StringIdentifier.of(ORDER_ID)).withScope(SCOPE).build())
				.build();

		CartOrder cartOrder = mock(CartOrder.class);
		ShoppingCartTaxSnapshot taxSnapshot = mock(ShoppingCartTaxSnapshot.class);
		CustomerSession customerSession = mock(CustomerSession.class);
		CheckoutResults checkoutResults = mock(CheckoutResults.class);
		ShoppingCart shoppingCart = createShoppingCart(shoppingCartIsEmpty);

		when(resourceOperationContext.getResourceIdentifier()).thenReturn(Optional.of(createPurchaseFormIdentifier));

		when(cartOrderRepository.findByGuid(SCOPE.getValue(), ORDER_ID)).thenReturn(Single.just(cartOrder));

		when(cartOrderRepository.getEnrichedShoppingCart(SCOPE.getValue(), cartOrder)).thenReturn(Single.just(shoppingCart));

		when(sessionRepository.findOrCreateCustomerSession()).thenReturn(Single.just(customerSession));

		when(pricingSnapshotRepository.getShoppingCartTaxSnapshot(shoppingCart)).thenReturn(Single.just(taxSnapshot));

		when(purchaseRepository.checkout(shoppingCart, taxSnapshot, customerSession)).thenReturn(Single.just(checkoutResults));

		Order order = mock(Order.class);
		when(checkoutResults.isOrderFailed()).thenReturn(false);
		when(checkoutResults.getOrder()).thenReturn((order));
		when(order.getGuid()).thenReturn(PURCHASE_ID.getValue());

		return PurchaseFormEntity.builder()
				.build();
	}

	private ShoppingCart createShoppingCart(final boolean empty) {
		final ShoppingCart shoppingCart = mock(ShoppingCart.class);

		when(shoppingCart.isEmpty()).thenReturn(empty);

		return shoppingCart;
	}

}
