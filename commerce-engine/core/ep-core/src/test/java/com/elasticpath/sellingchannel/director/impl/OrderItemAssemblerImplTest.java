/**
 * Copyright (c) Elastic Path Software Inc., 2016
 */
package com.elasticpath.sellingchannel.director.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Currency;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.common.dto.OrderItemDto;
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
import com.elasticpath.sellingchannel.director.impl.OrderItemAssemblerImpl.CopyFunctor;
import com.elasticpath.service.catalog.ProductInventoryManagementService;
import com.elasticpath.service.catalog.ProductSkuLookup;
import com.elasticpath.service.catalog.impl.BundleIdentifierImpl;
import com.elasticpath.service.shoppingcart.PricingSnapshotService;


/**
 * Tests {@code OrderItemAssemblerImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class OrderItemAssemblerImplTest {

	// Verifying that the PreOrderTraverser is used is done by inspection - it's too simple to test.

	private static final Currency CAD_CURRENCY = Currency.getInstance("CAD");

	private static final int FOUR = 4;

	private static final int UIDPK = 12345;

	private static final int THREE = 3;

	@Mock
	private ProductInventoryManagementService inventoryManagementService;

	@Mock
	private ProductSkuLookup productSkuLookup;

	@Mock
	private PricingSnapshotService pricingSnapshotService;

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

		DigitalAsset digitalAsset = mock(DigitalAsset.class);

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

		when(productSkuLookup.findByGuid(productSku.getGuid())).thenReturn(productSku);
		when(inventoryManagementService.getInventory(productSku, warehouse.getUidPk())).thenReturn(null);
		when(pricingSnapshotService.getPricingSnapshotForOrderSku(orderSku)).thenReturn(orderSku);

		final OrderSkuImplTreeNodeAdapter orderSkuAdapter =
			new OrderSkuImplTreeNodeAdapter(orderSku, shipment, store, new BundleIdentifierImpl(), inventoryManagementService, productSkuLookup,
				pricingSnapshotService);

		TreeNodeMemento<OrderItemDto> rootMemento = functor.processNode(orderSkuAdapter, null, null, 0);
		OrderItemDto dto = rootMemento.getTreeNode();

		assertThat(dto).isNotNull();

		assertThat(dto.getDigitalAsset()).isEqualTo(digitalAsset);
		assertThat(dto.getDisplayName()).isEqualTo("display");
		assertThat(dto.getImage()).isEqualTo("image.jpg");
		assertThat(dto.getDisplaySkuOptions()).isEqualTo("skuoptions");
		assertThat(dto.isAllocated()).isTrue();
		assertThat(dto.getListPrice()).isEqualTo(Money.valueOf("23.45", CAD_CURRENCY));
		assertThat(dto.getUnitPrice()).isEqualTo(Money.valueOf("12.34", CAD_CURRENCY));
		assertThat(dto.getDollarSavings()).isEqualTo(Money.valueOf("33.33", CAD_CURRENCY));
		assertThat(dto.getSkuCode()).isEqualTo("sku-A");
		assertThat(dto.getQuantity()).isEqualTo(THREE);
		assertThat(dto.getTotal().toString()).isEqualTo("37.02 CAD");
		assertThat(dto.getProductSku()).isEqualTo(productSku);
	}

	/**
	 * Tests that an {@code OrderItemDto} can be created from an {@code OrderSku} and have the child set properly..
	 */

	@Test
	public void testCreateOrderItemDtoOneChild() {
		CopyFunctor functor = new CopyFunctor();

		DigitalAsset digitalAsset = mock(DigitalAsset.class);

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

		when(productSkuLookup.findByGuid(productSku.getGuid())).thenReturn(productSku);
		when(inventoryManagementService.getInventory(productSku, warehouse.getUidPk())).thenReturn(null);
		when(pricingSnapshotService.getPricingSnapshotForOrderSku(orderSku)).thenReturn(orderSku);

		OrderItemDto parentDto = new OrderItemDto();
		TreeNodeMemento<OrderItemDto> parentStackMemento = new TreeNodeMemento<>(parentDto);

		final OrderSkuImplTreeNodeAdapter orderSkuAdapter =
			new OrderSkuImplTreeNodeAdapter(orderSku, shipment, store, new BundleIdentifierImpl(), inventoryManagementService, productSkuLookup,
				pricingSnapshotService);

		TreeNodeMemento<OrderItemDto> rootMemento = functor.processNode(orderSkuAdapter, null, parentStackMemento, 0);
		OrderItemDto dto = rootMemento.getTreeNode();

		assertThat(dto).isNotNull();
		assertThat(parentStackMemento.getTreeNode().getChildren())
			.as("The returned dto should have been added as child")
			.containsOnly(dto);

		OrderItemDto childDto = parentStackMemento.getTreeNode().getChildren().get(0);
		assertThat(childDto.getDigitalAsset()).isEqualTo(digitalAsset);
		assertThat(childDto.getDisplayName()).isEqualTo("display");
		assertThat(childDto.getImage()).isEqualTo("image.jpg");
		assertThat(childDto.getDisplaySkuOptions()).isEqualTo("skuoptions");
		assertThat(childDto.isAllocated()).isTrue();
		assertThat(childDto.getSkuCode()).isEqualTo("sku-A");
		assertThat(childDto.getQuantity()).isEqualTo(THREE);
		assertThat(childDto.getProductSku()).isEqualTo(productSku);

	}

}
