/**
 * Copyright (c) Elastic Path Software Inc., 2007
 */
package com.elasticpath.service.order;

/**
 * Processor for allocating unallocated quantities on orders
 * with the available quantities from inventory.
 */
public interface OrderAllocationProcessor {

	/**
	 * Allocate outstanding orders for the products with the given SKU code in the specified warehouse.
	 *
	 * @param skuCode the product SKU code
	 * @param warehouseCode the warehouse code
	 */
	void processOutstandingOrders(String skuCode, String warehouseCode);
}
