package com.elasticpath.rest.resource.integration.epcommerce.repository.purchase.repositories.lineitems.impl;

import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import io.reactivex.Single;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.order.OrderSku;
import com.elasticpath.rest.definition.purchases.PurchaseIdentifier;
import com.elasticpath.rest.definition.purchases.PurchaseLineItemIdentifier;
import com.elasticpath.rest.definition.purchases.PurchaseLineItemsIdentifier;
import com.elasticpath.rest.definition.purchases.PurchasesIdentifier;
import com.elasticpath.rest.id.type.PathIdentifier;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.resource.integration.epcommerce.repository.order.OrderRepository;

@RunWith(MockitoJUnitRunner.class)
public class DependentPurchaseLineItemRepositoryImplTest {

	private static final String STORE = "testStore";
	private static final String ORDER_ID = "testOrderId";
	private static final String PARENT_ORDER_SKU_GUID = "testParentOrderSkuGuid";
	private static final String CHILD_ORDER_SKU_GUID_1 = "testChildOrderSkuGuid1";
	private static final String CHILD_ORDER_SKU_GUID_2 = "testChildOrderSkuGuid2";
	private static final List<String> DEPENDENT_LINE_ITEM_ID_1 = Arrays.asList(PARENT_ORDER_SKU_GUID, CHILD_ORDER_SKU_GUID_1);
	private static final List<String> DEPENDENT_LINE_ITEM_ID_2 = Arrays.asList(PARENT_ORDER_SKU_GUID, CHILD_ORDER_SKU_GUID_2);
	private static final List<String> PARENT_LINE_ITEM_ID = Collections.singletonList(PARENT_ORDER_SKU_GUID);

	private static final PurchaseLineItemIdentifier DEPENDENT_PURCHASE_LINE_ITEM_IDENTIFIER_1 = PurchaseLineItemIdentifier.builder()
			.withLineItemId(PathIdentifier.of(DEPENDENT_LINE_ITEM_ID_1))
			.withPurchaseLineItems(PurchaseLineItemsIdentifier.builder()
					.withPurchase(PurchaseIdentifier.builder()
							.withPurchaseId(StringIdentifier.of(ORDER_ID))
							.withPurchases(PurchasesIdentifier.builder()
									.withScope(StringIdentifier.of(STORE))
									.build())
							.build())
					.build())
			.build();

	private static final PurchaseLineItemIdentifier DEPENDENT_PURCHASE_LINE_ITEM_IDENTIFIER_2 = PurchaseLineItemIdentifier.builder()
			.withLineItemId(PathIdentifier.of(DEPENDENT_LINE_ITEM_ID_2))
			.withPurchaseLineItems(PurchaseLineItemsIdentifier.builder()
					.withPurchase(PurchaseIdentifier.builder()
							.withPurchaseId(StringIdentifier.of(ORDER_ID))
							.withPurchases(PurchasesIdentifier.builder()
									.withScope(StringIdentifier.of(STORE))
									.build())
							.build())
					.build())
			.build();

	private static final PurchaseLineItemIdentifier PARENT_PURCHASE_LINE_ITEM_IDENTIFIER = PurchaseLineItemIdentifier.builder()
			.withLineItemId(PathIdentifier.of(PARENT_LINE_ITEM_ID))
			.withPurchaseLineItems(PurchaseLineItemsIdentifier.builder()
					.withPurchase(PurchaseIdentifier.builder()
							.withPurchaseId(StringIdentifier.of(ORDER_ID))
							.withPurchases(PurchasesIdentifier.builder()
									.withScope(StringIdentifier.of(STORE))
									.build())
							.build())
					.build())
			.build();

	@InjectMocks
	private DependentPurchaseLineItemRepositoryImpl target;

	@Mock
	private OrderRepository orderRepository;

	@Mock
	private OrderSku childOrderSku1;

	@Mock
	private OrderSku childOrderSku2;

	@Mock
	private OrderSku parentOrderSku;

	@Before
	public void setUp() {

		when(childOrderSku1.getParent()).thenReturn(parentOrderSku);
		when(childOrderSku1.getGuid()).thenReturn(CHILD_ORDER_SKU_GUID_1);

		when(childOrderSku2.getParent()).thenReturn(parentOrderSku);
		when(childOrderSku2.getGuid()).thenReturn(CHILD_ORDER_SKU_GUID_2);

		when(parentOrderSku.getGuid()).thenReturn(PARENT_ORDER_SKU_GUID);
		when(parentOrderSku.getChildren()).thenReturn(Arrays.asList(childOrderSku1, childOrderSku2));

		when(orderRepository.findOrderSku(STORE, ORDER_ID, DEPENDENT_LINE_ITEM_ID_1)).thenReturn(Single.just(childOrderSku1));
		when(orderRepository.findOrderSku(STORE, ORDER_ID, PARENT_LINE_ITEM_ID)).thenReturn(Single.just(parentOrderSku));

	}

	@Test
	public void testFindParentPurchaseLineItem() {

		target.findParentPurchaseLineItem(DEPENDENT_PURCHASE_LINE_ITEM_IDENTIFIER_1)
				.test()
				.assertNoErrors()
				.assertValueCount(1)
				.assertValueAt(0, PARENT_PURCHASE_LINE_ITEM_IDENTIFIER::equals);

	}

	@Test
	public void testFindDependentPurchaseLineItems() {

		target.findDependentPurchaseLineItems(PARENT_PURCHASE_LINE_ITEM_IDENTIFIER)
				.test()
				.assertNoErrors()
				.assertValueCount(2)
				.assertValueAt(0, DEPENDENT_PURCHASE_LINE_ITEM_IDENTIFIER_1::equals)
				.assertValueAt(1, DEPENDENT_PURCHASE_LINE_ITEM_IDENTIFIER_2::equals);

	}

}
