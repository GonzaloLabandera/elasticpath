/**
 * Copyright (c) Elastic Path Software Inc., 2011
 */
package com.elasticpath.test.integration.inventory;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.elasticpath.domain.catalog.AvailabilityCriteria;
import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.domain.store.impl.WarehouseImpl;
import com.elasticpath.inventory.InventoryDto;
import com.elasticpath.inventory.dao.InventoryDao;
import com.elasticpath.inventory.domain.Inventory;
import com.elasticpath.inventory.domain.impl.InventoryImpl;
import com.elasticpath.service.catalog.ProductSkuService;
import com.elasticpath.test.db.DbTestCase;
import com.elasticpath.test.integration.DirtiesDatabase;

/**
 * Tests the implementation of {@link InventoryDao}.
 */
public class InventoryDaoImplTest extends DbTestCase {

	private static final String SKU_CODE1 = "skuCode1";
	private static final String SKU_CODE2 = "skuCode2";
	private static final String SKU_CODE3 = "skuCode3";	
	private static final long WAREHOUSE_10 = 10L;
	private static final long WAREHOUSE_100 = 100L;
	private static final long WAREHOUSE_1000 = 1000L;		
	@Autowired
	private InventoryDao inventoryDao;
	
	@Autowired
	private ProductSkuService productSkuService;

	/**
	 * Sets up the test case.
	 */
	@Before
	public void setUp() throws Exception {
		Date restockDate = getRestockDate();
		addInventory(SKU_CODE1, WAREHOUSE_10, 30, 3, 20, 10, 11, restockDate, false);
		addInventory(SKU_CODE2, WAREHOUSE_10, 14, 5, 60, 12, 13, restockDate, false);
		addInventory(SKU_CODE3, WAREHOUSE_100, 14, 5, 60, 12, 13, restockDate, false);
		addInventory(SKU_CODE3, WAREHOUSE_1000, 14, 5, 60, 12, 13, restockDate, true);
	}
	
	/**
	 * Test the retrieving low stock functionality for the inventory dao. 
	 */
	@DirtiesDatabase
	@Test
	public void testFindLowStockInventories() {
		List<InventoryDto> lowStockInventories = findLowStockInventories(WAREHOUSE_10, SKU_CODE1, SKU_CODE2);
		assertThat(lowStockInventories).hasSize(1);
		InventoryDto dto = lowStockInventories.get(0);
		int expectedQuantityOnHand = 14;
		int expectedAllocatedQuantity = 5;
		verifyQuantitiesForInventory(dto, expectedQuantityOnHand, expectedAllocatedQuantity);
	}
	
	/**
	 * Test the retrieving low stock functionality for the inventory dao with empty sku code list.
	 */
	@DirtiesDatabase
	@Test
	public void testFindLowStockInventoriesWithEmptySkuCodeList() {
		List<InventoryDto> lowStockInventories = findLowStockInventories(WAREHOUSE_10);
		assertThat(lowStockInventories).hasSize(1);
		InventoryDto dto = lowStockInventories.get(0);
		int expectedQuantityOnHand = 14;
		int expectedAllocatedQuantity = 5;
		verifyQuantitiesForInventory(dto, expectedQuantityOnHand, expectedAllocatedQuantity);
	}

	/**
	 * Test deletion of inventory.
	 */
	@DirtiesDatabase
	@Test
	public void testRemove() {
		Inventory inventoryToDelete = inventoryDao.getInventory(SKU_CODE1, WAREHOUSE_10);
		inventoryDao.remove(inventoryToDelete);

		Inventory deletedInventory = inventoryDao.getInventory(SKU_CODE1, WAREHOUSE_10);
		assertThat(deletedInventory)
			.as("This Inventory should be deleted.")
			.isNull();

		Inventory remainingInventory = inventoryDao.getInventory(SKU_CODE2, WAREHOUSE_10);
		assertThat(remainingInventory)
			.as("This Inventory should not be deleted.")
			.isNotNull();
	}

	/**
	 * Test getting inventories by sku code.
	 */
	@DirtiesDatabase
	@Test
	public void testGetInventoriesForSku() {
		Map<Long, Inventory> inventoryMap = inventoryDao.getInventoriesForSku(SKU_CODE3);
		int expectedInventoryCount = 2;
		assertThat(inventoryMap)
			.as("Map should contain " + expectedInventoryCount + " entries")
			.hasSize(expectedInventoryCount);

		Inventory inventory = inventoryMap.get(WAREHOUSE_100);
		assertThat(inventory.getSkuCode()).isEqualTo(SKU_CODE3);

		inventory = inventoryMap.get(WAREHOUSE_1000);
		assertThat(inventory.getSkuCode()).isEqualTo(SKU_CODE3);
	}
	
	/**
	 * Testing getting a map of inventories keyed by sku code.
	 */
	@DirtiesDatabase
	@Test
	public void testGetInventoryMap() {
		Set<String> skuCodeSet = new HashSet<>();
		skuCodeSet.add(SKU_CODE1);
		skuCodeSet.add(SKU_CODE2);

		Map<String, Inventory> inventoryMap = inventoryDao.getInventoryMap(skuCodeSet, WAREHOUSE_10);
		assertThat(inventoryMap).hasSize(2);

		Inventory inventory = inventoryMap.get(SKU_CODE1);
		assertThat(inventoryDao.getInventory(SKU_CODE1, WAREHOUSE_10))
			.as("Inventory for " + SKU_CODE1 + "does not match the one from the map.")
			.isEqualTo(inventory);

		inventory = inventoryMap.get(SKU_CODE2);
		assertThat(inventoryDao.getInventory(SKU_CODE2, WAREHOUSE_10))
			.as("Inventory for " + SKU_CODE2 + "does not match the one from the map.")
			.isEqualTo(inventory);
	}
	
	private void verifyQuantitiesForInventory(InventoryDto dto, int quantityOnHand, int allocatedQuantity) {
		assertThat(dto.getQuantityOnHand())
			.as("On hand quantity doesn't match calculated value")
			.isEqualTo(14);
		assertThat(dto.getAllocatedQuantity())
			.as("Allocated quantity doesn't match calculated value")
			.isEqualTo(5);
	}

	private void addInventory(final String skuCode, final long warehouseUid, final int quantityOnHand,
			final int allocatedQuantity, final int reorderMinimum, final int reservedQuantity,
			final int reorderQuantity, final Date restockDate, final boolean skuExists) {
		InventoryImpl inventory = new InventoryImpl();
		inventory.setSkuCode(skuCode);
		inventory.setWarehouseUid(warehouseUid);
		inventory.setReorderMinimum(reorderMinimum);
		inventory.setReservedQuantity(reservedQuantity);
		inventory.setReorderQuantity(reorderQuantity);
		inventory.setRestockDate(restockDate);
		inventory.setQuantityOnHand(quantityOnHand);
		inventory.setAllocatedQuantity(allocatedQuantity);
		inventoryDao.saveOrUpdate(inventory);

		if (!skuExists) {
			WarehouseImpl warehouse = new WarehouseImpl();
			warehouse.setUidPk(warehouseUid);

			final Product product = persisterFactory.getCatalogTestPersister().createDefaultProductWithSkuAndInventory(scenario.getCatalog(),
					scenario.getCategory(), warehouse);
			ProductSku productSku = product.getDefaultSku();
			productSku.setSkuCode(skuCode);
			product.setAvailabilityCriteria(AvailabilityCriteria.AVAILABLE_WHEN_IN_STOCK);
			productSkuService.saveOrUpdate(productSku);
		}
	}
	
	private Date getRestockDate() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.YEAR, 2011);
		calendar.set(Calendar.MONTH, 4);
		calendar.set(Calendar.DATE, 31);
		return calendar.getTime();
	}
	
	private List<InventoryDto> findLowStockInventories(long warehouseUid, String ... skuCodes) {
		Set<String> skuCodeSet = ImmutableSet.copyOf(skuCodes);
		List<InventoryDto> inventories = inventoryDao.findLowStockInventories(skuCodeSet, warehouseUid);
		return inventories;
	}
	
}
