/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder;

import io.reactivex.Completable;
import io.reactivex.Observable;

import com.elasticpath.common.dto.ShoppingItemDto;
import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.rest.advise.Message;
import com.elasticpath.rest.definition.carts.LineItemEntity;

/**
 * Validation Service for cart items.
 */
public interface ShoppingItemValidationService {

	/**
	 * Check if the item is purchasable.
	 *
	 * @param scope  scope
	 * @param itemId item id
	 * @return the execution result with the boolean result
	 */
	Observable<Message> validateItemPurchasable(String scope, String itemId);

	/**
	 * Validate if an item is purchasable.
	 *
	 * @param scope      the scope
	 * @param productSku the product sku
	 * @return the structured error message, if any.
	 */
	Observable<Message> validateItemPurchasable(String scope, ProductSku productSku);

	/**
	 * Validate quantity.
	 *
	 * @param lineItemEntity line item entity
	 * @return the structured error message, if any.
	 */
	Completable validateQuantity(LineItemEntity lineItemEntity);

	/**
	 * Validate quantity.
	 *
	 * @param lineItemEntity  line item entity
	 * @param minimumQuantity the minimum allowed quantity
	 * @return the structured error message, if any.
	 */
	Completable validateQuantity(LineItemEntity lineItemEntity, int minimumQuantity);

	/**
	 * Validates {@link com.elasticpath.common.dto.ShoppingItemDto}.
	 *
	 * @param shoppingItemDto the {@link com.elasticpath.common.dto.ShoppingItemDto}
	 * @return SUCCESS if shopping item meets validation criteria BAD_REQUEST otherwise
	 */
	Completable validate(ShoppingItemDto shoppingItemDto);
}
