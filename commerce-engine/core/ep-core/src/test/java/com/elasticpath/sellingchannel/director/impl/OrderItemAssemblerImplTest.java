/**
 * Copyright (c) Elastic Path Software Inc., 2016
 */
package com.elasticpath.sellingchannel.director.impl;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Currency;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;

import com.elasticpath.common.dto.OrderItemDto;
import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.commons.tree.impl.TreeNodeMemento;
import com.elasticpath.domain.catalog.DigitalAsset;
import com.elasticpath.domain.catalog.Price;
import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.domain.catalog.impl.PriceImpl;
import com.elasticpath.domain.catalog.impl.ProductSkuImpl;
import com.elasticpath.domain.misc.impl.RandomGuidImpl;
import com.elasticpath.domain.order.Order;
import com.elasticpath.domain.order.OrderShipment;
import com.elasticpath.domain.order.impl.ElectronicOrderShipmentImpl;
import com.elasticpath.domain.order.impl.OrderImpl;
import com.elasticpath.domain.order.impl.OrderSkuImpl;
import com.elasticpath.domain.store.Store;
import com.elasticpath.domain.store.Warehouse;
import com.elasticpath.domain.store.impl.StoreImpl;
import com.elasticpath.domain.store.impl.WarehouseImpl;
import com.elasticpath.money.Money;
import com.elasticpath.money.StandardMoneyFormatter;
import com.elasticpath.sellingchannel.director.impl.OrderItemAssemblerImpl.CopyFunctor;
import com.elasticpath.service.catalog.ProductInventoryManagementService;
import com.elasticpath.service.catalog.ProductSkuLookup;
import com.elasticpath.service.catalog.impl.BundleIdentifierImpl;
import com.elasticpath.service.shoppingcart.PricingSnapshotService;
import com.elasticpath.service.store.StoreService;
import com.elasticpath.test.BeanFactoryExpectationsFactory;


/**
 * Tests {@code OrderItemAssemblerImpl}.
 */
public class OrderItemAssemblerImplTest {

	// Verifying that the PreOrderTraverser is used is done by inspection - it's too simple to test.

	private static final Currency CAD_CURRENCY = Currency.getInstance("CAD");

	private static final int FOUR = 4;

	private static final int UIDPK = 12345;

	private static final int THREE = 3;

	@Rule
	public final JUnitRuleMockery context = new JUnitRuleMockery();
	private BeanFactory beanFactory;
	private BeanFactoryExpectationsFactory expectationsFactory;

	private final ProductInventoryManagementService inventoryManagementService = context.mock(ProductInventoryManagementService.class);
	private final ProductSkuLookup productSkuLookup = context.mock(ProductSkuLookup.class);
	private final StoreService storeService = context.mock(StoreService.class);
	private final PricingSnapshotService pricingSnapshotService = context.mock(PricingSnapshotService.class);
	private Store store;
	private Warehouse warehouse;

	/**
	 * Set up prior to the test.
	 */
	@Before
	public void setUp() {
		warehouse = new WarehouseImpl();
		warehouse.setUidPk(1L);
		warehouse.setCode("warehouse");

		store = new StoreImpl();
		store.setCode("store");
		store.setWarehouses(Collections.singletonList(warehouse));

		beanFactory = context.mock(BeanFactory.class);
		expectationsFactory = new BeanFactoryExpectationsFactory(context, beanFactory);

		expectationsFactory.allowingBeanFactoryGetBean(ContextIdNames.MONEY_FORMATTER, StandardMoneyFormatter.class);


		context.checking(new Expectations() { {
			allowing(storeService).findStoreWithCode(store.getCode()); will(returnValue(store));
		}
		});
	}

	@After
	public void tearDown() {
		expectationsFactory.close();
	}

	/**
	 * Tests that an {@code OrderItemDto} can be created from an {@code OrderSku}. Also verifies that all required fields
	 * are copied.
	 */
	@SuppressWarnings({ "PMD.AvoidDuplicateLiterals", "PMD.DontUseElasticPathImplGetInstance" })
	@Test
	public void testCreateOrderItemDtoNoChild() {
		CopyFunctor functor = new CopyFunctor();

		final OrderSkuImpl orderSku = new OrderSkuImpl();
		orderSku.setGuid("AAA");

		final Order order = new OrderImpl();
		order.setStoreCode(store.getCode());

		final long uidPk = 100001;
		final OrderShipment shipment = new ElectronicOrderShipmentImpl();
		shipment.setUidPk(uidPk);
		shipment.addShipmentOrderSku(orderSku);
		shipment.setOrder(order);

		DigitalAsset digitalAsset = context.mock(DigitalAsset.class);

		final ProductSku productSku = new ProductSkuImpl();
		productSku.setGuid(new RandomGuidImpl().toString());
		orderSku.setSkuGuid(productSku.getGuid());
		orderSku.setDigitalAsset(digitalAsset);
		orderSku.setDisplayName("display");
		orderSku.setUidPk(UIDPK);  // This will be sent to the string encrypter below.
		orderSku.setImage("image.jpg");
		orderSku.setDisplaySkuOptions("skuoptions");
		orderSku.setAllocatedQuantity(FOUR);  // isAllocated() should return true now.
		Price price = new PriceImpl();
		price.setCurrency(CAD_CURRENCY);
		price.setListPrice(Money.valueOf("23.45", CAD_CURRENCY));
		orderSku.enableRecalculation(); // need to do this so that amount get calculated.
		orderSku.setPrice(THREE, price);
		orderSku.setUnitPrice(new BigDecimal("12.34"));
		orderSku.setSkuCode("sku-A");

		context.checking(new Expectations() { {
			allowing(productSkuLookup).findByGuid(productSku.getGuid()); will(returnValue(productSku));
			allowing(inventoryManagementService).getInventory(productSku, warehouse.getUidPk()); will(returnValue(null));
			allowing(pricingSnapshotService).getPricingSnapshotForOrderSku(orderSku); will(returnValue(orderSku));
		} });

		final OrderSkuImplTreeNodeAdapter orderSkuAdapter =
				new OrderSkuImplTreeNodeAdapter(orderSku, shipment, store, new BundleIdentifierImpl(), inventoryManagementService, productSkuLookup,
						pricingSnapshotService);

		TreeNodeMemento<OrderItemDto> rootMemento = functor.processNode(orderSkuAdapter, null, null, 0);
		OrderItemDto dto = rootMemento.getTreeNode();

		assertNotNull("A dto should be returned", dto);

		assertEquals("Output should match input", digitalAsset, dto.getDigitalAsset());
		assertEquals("Output should match input", "display", dto.getDisplayName());
		assertEquals("Output should match input", "image.jpg", dto.getImage());
		assertEquals("Output should match input", "skuoptions", dto.getDisplaySkuOptions());
		assertTrue("Output should match input", dto.isAllocated());
		assertEquals("Output should match input", Money.valueOf("23.45", CAD_CURRENCY), dto.getListPrice());
		assertEquals("Output should match input", Money.valueOf("12.34", CAD_CURRENCY), dto.getUnitPrice());
		assertEquals("Output should match input", Money.valueOf("33.33", CAD_CURRENCY), dto
				.getDollarSavings());
		assertEquals("Output should match input", "sku-A", dto.getSkuCode());
		assertEquals("Output should match input", THREE, dto.getQuantity());
		assertEquals("Output should match input", "37.02 CAD", dto.getTotal().toString());
		assertEquals("Output should match input", productSku, dto.getProductSku());
	}

	/**
	 * Tests that an {@code OrderItemDto} can be created from an {@code OrderSku} and have the child set properly..
	 */

	@Test
	public void testCreateOrderItemDtoOneChild() {
		CopyFunctor functor = new CopyFunctor();

		DigitalAsset digitalAsset = context.mock(DigitalAsset.class);

		final Order order = new OrderImpl();
		order.setStoreCode(store.getCode());

		final OrderSkuImpl orderSku = new OrderSkuImpl();
		orderSku.setGuid("AAA");
		orderSku.setUidPk(UIDPK);
		Price price = new PriceImpl();
		price.setCurrency(CAD_CURRENCY);
		price.setListPrice(Money.valueOf("23.45", CAD_CURRENCY));

		final ProductSku productSku = new ProductSkuImpl();
		productSku.setGuid(new RandomGuidImpl().toString());
		orderSku.setSkuGuid(productSku.getGuid());
		orderSku.setDigitalAsset(digitalAsset);
		orderSku.setDisplayName("display");
		orderSku.setUidPk(UIDPK);  // This will be sent to the string encrypter below.
		orderSku.setImage("image.jpg");
		orderSku.setDisplaySkuOptions("skuoptions");
		orderSku.setAllocatedQuantity(FOUR);  // isAllocated() should return true now.);
		orderSku.setPrice(THREE, price);
		orderSku.setUnitPrice(new BigDecimal("12.34"));
		orderSku.setSkuCode("sku-A");

		final long uidPk = 100001;
		final OrderShipment shipment = new ElectronicOrderShipmentImpl();
		shipment.setUidPk(uidPk);
		shipment.setOrder(order);
		shipment.addShipmentOrderSku(orderSku);

		context.checking(new Expectations() { {
			allowing(productSkuLookup).findByGuid(productSku.getGuid()); will(returnValue(productSku));
			allowing(inventoryManagementService).getInventory(productSku, warehouse.getUidPk()); will(returnValue(null));
			allowing(pricingSnapshotService).getPricingSnapshotForOrderSku(orderSku); will(returnValue(orderSku));
		} });

		OrderItemDto parentDto = new OrderItemDto();
		TreeNodeMemento<OrderItemDto> parentStackMemento = new TreeNodeMemento<>(parentDto);

		final OrderSkuImplTreeNodeAdapter orderSkuAdapter =
				new OrderSkuImplTreeNodeAdapter(orderSku, shipment, store, new BundleIdentifierImpl(), inventoryManagementService, productSkuLookup,
						pricingSnapshotService);

		TreeNodeMemento<OrderItemDto> rootMemento = functor.processNode(orderSkuAdapter, null, parentStackMemento, 0);
		OrderItemDto dto = rootMemento.getTreeNode();

		assertNotNull("A dto should be returned", dto);
		assertEquals("Should have 1 child", 1, parentStackMemento.getTreeNode().getChildren().size());
		assertEquals("The returned dto should have been added as child", dto, parentStackMemento.getTreeNode().getChildren().get(0));

		OrderItemDto childDto = parentStackMemento.getTreeNode().getChildren().get(0);
		assertEquals("Output should match input", digitalAsset, childDto.getDigitalAsset());
		assertEquals("Output should match input", "display", childDto.getDisplayName());
		assertEquals("Output should match input", "image.jpg", childDto.getImage());
		assertEquals("Output should match input", "skuoptions", childDto.getDisplaySkuOptions());
		assertTrue("Output should match input", childDto.isAllocated());
		assertEquals("Output should match input", "sku-A", childDto.getSkuCode());
		assertEquals("Output should match input", THREE, childDto.getQuantity());
		assertEquals("Output should match input", productSku, childDto.getProductSku());

	}

}
