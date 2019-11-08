/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.domain.catalogview.impl;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.testing.EqualsTester;
import org.junit.Test;

import com.elasticpath.common.dto.SkuInventoryDetails;
import com.elasticpath.domain.catalog.Availability;
import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.domain.catalog.impl.ProductSkuImpl;
import com.elasticpath.domain.catalogview.StoreProductSku;
import com.elasticpath.service.catalogview.impl.InventoryMessage;

/**
 * Test class for {@link StoreProductSkuFactoryImpl}.
 */
public class StoreProductSkuFactoryImplTest {

	private final StoreProductSkuFactoryImpl storeProductSkuFactory = new StoreProductSkuFactoryImpl();

	@Test
	public void verifyProxyDelegatesToProductSku() {
		final String skuCode = "MySku123";

		final ProductSku productSku = createProductSku(skuCode);

		final StoreProductSku storeProductSku = storeProductSkuFactory.createStoreProductSku(productSku, null);

		assertThat(storeProductSku.getSkuCode())
				.isEqualTo(skuCode);
	}

	@Test
	public void verifyProxyDelegatesToPerStoreAvailability() {
		final Availability skuAvailability = Availability.AVAILABLE_FOR_PRE_ORDER;

		final PerStoreProductSkuAvailabilityImpl perStoreSkuAvailability = createPerStoreProductSkuAvailability(
				skuAvailability,
				null,
				null,
				true,
				true);

		final StoreProductSku storeProductSku = storeProductSkuFactory.createStoreProductSku(null, perStoreSkuAvailability);

		assertThat(storeProductSku.getSkuAvailability())
				.isEqualTo(skuAvailability);
	}

	@Test
	public void verifyEquals() {
		final SkuInventoryDetails inventoryDetails = new SkuInventoryDetails();

		final PerStoreProductSkuAvailabilityImpl perStoreSkuAvailability1 = createPerStoreProductSkuAvailability(
				Availability.AVAILABLE,
				inventoryDetails,
				InventoryMessage.IN_STOCK,
				true,
				true);

		final ProductSku productSku1 = createProductSku("skuCode");
		final ProductSku productSku2 = createProductSku("differentSkuCode");

		final PerStoreProductSkuAvailabilityImpl perStoreSkuAvailability2 = createPerStoreProductSkuAvailability(
				Availability.AVAILABLE_FOR_PRE_ORDER,
				inventoryDetails,
				InventoryMessage.AVAILABLE_FOR_PREORDER,
				true,
				false);

		new EqualsTester()
				.addEqualityGroup(storeProductSkuFactory.createStoreProductSku(productSku1, perStoreSkuAvailability1))
				.addEqualityGroup(storeProductSkuFactory.createStoreProductSku(productSku1, perStoreSkuAvailability2))
				.addEqualityGroup(storeProductSkuFactory.createStoreProductSku(productSku2, perStoreSkuAvailability1))
				.addEqualityGroup(storeProductSkuFactory.createStoreProductSku(productSku2, perStoreSkuAvailability2))
				.testEquals();
	}

	protected ProductSku createProductSku(final String skuCode) {
		final ProductSku productSku = new ProductSkuImpl();
		productSku.setSkuCode(skuCode);
		return productSku;
	}

	private PerStoreProductSkuAvailabilityImpl createPerStoreProductSkuAvailability(final Availability skuAvailability,
																					final SkuInventoryDetails inventoryDetails,
																					final InventoryMessage messageCode,
																					final boolean productSkuDisplayable,
																					final boolean productSkuAvailable) {
		final PerStoreProductSkuAvailabilityImpl perStoreSkuAvailability = new PerStoreProductSkuAvailabilityImpl();
		perStoreSkuAvailability.setInventoryDetails(inventoryDetails);
		perStoreSkuAvailability.setProductSkuDisplayable(productSkuDisplayable);
		perStoreSkuAvailability.setProductSkuAvailable(productSkuAvailable);
		perStoreSkuAvailability.setMessageCode(messageCode);
		perStoreSkuAvailability.setSkuAvailability(skuAvailability);
		return perStoreSkuAvailability;
	}

}