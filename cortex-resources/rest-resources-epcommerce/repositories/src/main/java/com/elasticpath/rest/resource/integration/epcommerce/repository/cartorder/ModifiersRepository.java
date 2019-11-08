/*
 * Copyright (c) Elastic Path Software Inc., 2016
 */

package com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder;

import java.util.List;
import java.util.Map;

import io.reactivex.Single;

import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.modifier.ModifierField;
import com.elasticpath.domain.modifier.ModifierFieldOption;
import com.elasticpath.domain.modifier.ModifierGroup;
import com.elasticpath.domain.shoppingcart.ShoppingItem;
import com.elasticpath.rest.command.ExecutionResult;
import com.elasticpath.rest.definition.carts.LineItemConfigurationEntity;

/**
 * Modifier Repository layer for integration with resource and backend service.
 */
public interface ModifiersRepository {

	/**
	 * This method retrieves the cart item modifier group corresponding to the given code.
	 *
	 * @param code the code for the cart item modifier group to retrieve
	 * @return the cart item modifier group identified by the code
	 */
	ExecutionResult<ModifierGroup> findModifierGroupByCode(String code);

	/**
	 * This method retrieves the cart item modifier field corresponding to the given group code and field code.
	 *
	 * @param modifierFieldCode the code for the cart item modifier field to retrieve
	 * @param modifierGroupCode the code for the cart item modifier Group  from which the field need to be retrieved
	 * @return the cart item modifier field identified by the code
	 */
	ExecutionResult<ModifierField> findModifierFieldBy(String modifierFieldCode, String modifierGroupCode);

	/**
	 * This method retrieves the cart item modifier field Option corresponding to the group code, field code and option value.
	 *
	 * @param modifierOptionValue the option value for which modifierOptionValue to retrieve
	 * @param modifierFieldCode   the code for the cart item modifier field from which the option value to retrieve
	 * @param modifierGroupCode   the code for the cart item modifier Group  from which the field need to be retrieved
	 * @return the cart item modifier field identified by the code
	 */
	ExecutionResult<ModifierFieldOption> findModifierFieldOptionBy(String modifierOptionValue,
		String modifierFieldCode, String modifierGroupCode);

	/**
	 * Find modifier values for a given cart line item.
	 * The returned map will contain entries for all applicable modifier fields (including those with empty values).
	 *
	 *
	 * @param cartId the shopping cart ID.
	 * @param shoppingItemGuid the shopping cart line item GUID
	 * @return map of fields to values
	 */
	Single<Map<ModifierField, String>> findModifierValues(String cartId, String shoppingItemGuid);

	/**
	 * Find modifier values for a given purchase line item.
	 * The returned map will contain entries for all applicable modifier fields (including those with empty values).
	 *
	 * @param storeCode            the store code
	 * @param purchaseGuid         the purchase GUID
	 * @param purchaseLineItemGuid the purchase line item GUID
	 * @return map of fields to values
	 */
	Single<Map<ModifierField, String>> findPurchaseItemModifierValues(String storeCode, String purchaseGuid,
		String purchaseLineItemGuid);

	/**
	 * Get the LineItemConfigurationEntity for an item.
	 *
	 * @param itemId the item id
	 * @return LineItemConfigurationEntity
	 */
	Single<LineItemConfigurationEntity> getConfiguration(String itemId);

	/**
	 * Find cart item modifier by product.
	 *
	 * @param product the product
	 * @return the list of cart item modifier.
	 */
	List<ModifierField> findModifiersByProduct(Product product);

	/**
	 * Find missing required fields in given shopping item.
	 *
	 * @param shoppingItem the shopping item.
	 * @return the list of cart item modifier fields which are missing.
	 */
	List<String> findMissingRequiredFieldCodesByShoppingItem(ShoppingItem shoppingItem);

}
