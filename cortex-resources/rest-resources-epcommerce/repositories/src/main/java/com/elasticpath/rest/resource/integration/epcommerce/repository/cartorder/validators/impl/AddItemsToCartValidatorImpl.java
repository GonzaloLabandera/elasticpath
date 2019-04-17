/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.validators.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elasticpath.base.common.dto.StructuredErrorMessage;
import com.elasticpath.base.exception.structured.EpValidationException;
import com.elasticpath.domain.catalog.Catalog;
import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.domain.store.Store;
import com.elasticpath.rest.definition.carts.AddItemsToCartFormEntity;
import com.elasticpath.rest.definition.carts.ItemEntity;
import com.elasticpath.rest.definitions.validator.constants.ValidationMessages;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.validators.AddItemsToCartValidator;
import com.elasticpath.rest.resource.integration.epcommerce.repository.store.StoreRepository;
import com.elasticpath.service.catalog.ProductSkuLookup;

/**
 * Implementation for AddItemsToCartValidator.
 */
@Component(service = AddItemsToCartValidator.class)
public class AddItemsToCartValidatorImpl implements AddItemsToCartValidator {

	private ProductSkuLookup productSkuLookup;

	private StoreRepository storeRepository;

	private static final String MISSING_CODE_IN_REQUEST_BODY = "Code field is either missing or missing a value";

	private static final String ITEM_NOT_IN_STORE_MESSAGE_ID = "item.not.in.store.catalog";

	private static final String ITEM_IS_CONFIGURABLE = "item.is.configurable";

	private static final String ITEM_IS_DUPLICATE_ID = "item.is.duplicate";

	private static final String CODE_FIELD_NAME = "code";

	private static final String INVALID_QUANTITY_IN_REQUEST_BODY = "'quantity' value must be greater than or equal to '1'.";

	private static final String INVALID_QUANTITY_MESSAGE_ID = "field.invalid.minimum.value";

	private static final String QUANTITY_FIELD_MISSING_IN_REQUEST_BODY = "Quantity field is missing or has invalid value";

	private static final String QUANTITY_FIELD_NAME = "quantity";

	private static final String EP_VALIDATION_EXCEPTION_MESSAGE = "There is something wrong with the constructed JSON payload";

	private static final String ITEMS_FIELD_NAME = "items";

	private static final String MESSAGE_ID = "item.not.available";

	@Override
	public void validate(final AddItemsToCartFormEntity addItemsToCartFormEntity, final String scope) {
		Set<StructuredErrorMessage> errorMessagesCollected = new HashSet<>();

		if (addItemsToCartFormEntity == null) {
			errorMessagesCollected.add(
					constructStructuredErrorMessage(ValidationMessages.MISSING_REQUIRED_REQUEST_BODY, MESSAGE_ID, ITEMS_FIELD_NAME)
			);
			throw new EpValidationException(EP_VALIDATION_EXCEPTION_MESSAGE, new ArrayList<>(errorMessagesCollected));
		}

		if (addItemsToCartFormEntity.getItems() == null || addItemsToCartFormEntity.getItems().isEmpty()) {
			errorMessagesCollected.add(
					constructStructuredErrorMessage(ValidationMessages.MISSING_REQUIRED_REQUEST_BODY, MESSAGE_ID, ITEMS_FIELD_NAME)
			);
			throw new EpValidationException(EP_VALIDATION_EXCEPTION_MESSAGE, new ArrayList<>(errorMessagesCollected));
		}

		Set<String> duplicateSkus = new HashSet<>();

		for (ItemEntity entity : addItemsToCartFormEntity.getItems()) {
			checkForDuplicateSkuEntries(entity, addItemsToCartFormEntity.getItems(), duplicateSkus);
			validateItemCode(entity, scope, errorMessagesCollected);
			validateQuantity(entity, errorMessagesCollected);
		}

		duplicateSkus.forEach(itemCode -> {
			String debugMessage = "Item '"
					+ itemCode + "' has multiple entries. Please combine duplicate items into one entry.";
			errorMessagesCollected.add(constructStructuredErrorMessage(debugMessage, ITEM_IS_DUPLICATE_ID, CODE_FIELD_NAME, itemCode));
		});

		if (!errorMessagesCollected.isEmpty()) {
			throw new EpValidationException(EP_VALIDATION_EXCEPTION_MESSAGE, new ArrayList<>(errorMessagesCollected));
		}
	}

	/**
	 * Construct a Structure Error Message with the field name that contains the error.
	 *
	 * @param debugMessage debugMessage
	 * @param messageId    messageId
	 * @param fieldName    fieldName
	 * @return StructuredErrorMessage
	 */
	protected StructuredErrorMessage constructStructuredErrorMessage(final String debugMessage, final String messageId, final String fieldName) {
		Map<String, String> errorData = new HashMap<>();
		errorData.put("field-name", fieldName);
		return new StructuredErrorMessage(messageId, debugMessage, errorData);
	}

	/**
	 * Construct a Structure Error Message with the field name and field value that contains the error.
	 *
	 * @param debugMessage debugMessage
	 * @param messageId    messageId
	 * @param fieldName    fieldName
	 * @param fieldValue   fieldValue
	 * @return StructuredErrorMessage
	 */
	protected StructuredErrorMessage constructStructuredErrorMessage(final String debugMessage, final String messageId, final String fieldName,
																	 final String fieldValue) {
		Map<String, String> errorData = new HashMap<>();
		errorData.put("field-name", fieldName);
		errorData.put("field-value", fieldValue);
		return new StructuredErrorMessage(messageId, debugMessage, errorData);
	}

	/**
	 * Check if given ProductSku is not in store.
	 *
	 * @param productSku productSku
	 * @param scope      scope
	 * @return true if SKU is not in store
	 */
	protected boolean isSkuNotInStore(final ProductSku productSku, final String scope) {
		Store store = storeRepository.findStoreAsSingle(scope).blockingGet();
		Catalog catalog = store.getCatalog();
		Product product = productSku.getProduct();
		return !product.isInCatalog(catalog, true);
	}

	/**
	 * Check if multiple entities has the given entity's item code.
	 *
	 * @param entity        entity
	 * @param items         items
	 * @param duplicateSkus duplicateSkus
	 */
	protected void checkForDuplicateSkuEntries(final ItemEntity entity, final List<ItemEntity> items,
											   final Set<String> duplicateSkus) {
		String itemCode = entity.getCode();
		long count = items.stream().filter(itemEntity -> itemCode.equals(itemEntity.getCode())).count();
		if (count > 1) {
			duplicateSkus.add(itemCode);
		}
	}

	/**
	 * Validate the given entity's item code.
	 *
	 * @param entity                 entity
	 * @param scope                  scope
	 * @param errorMessagesCollected errorMessagesCollected
	 */
	protected void validateItemCode(final ItemEntity entity, final String scope, final Set<StructuredErrorMessage> errorMessagesCollected) {
		String itemCode = entity.getCode();

		if (StringUtils.isBlank(itemCode)) {
			errorMessagesCollected.add(constructStructuredErrorMessage(MISSING_CODE_IN_REQUEST_BODY, MESSAGE_ID, CODE_FIELD_NAME));
		} else {
			ProductSku productSku = productSkuLookup.findBySkuCode(itemCode);

			if (productSku == null) {
				String error = "Item with code '" + itemCode + "' does not exist.";
				errorMessagesCollected.add(constructStructuredErrorMessage(error, MESSAGE_ID, CODE_FIELD_NAME, itemCode));
			} else if (isSkuNotInStore(productSku, scope)) {
				String error = "'" + itemCode + "' is not part of the current store's catalog.";
				errorMessagesCollected.add(constructStructuredErrorMessage(error, ITEM_NOT_IN_STORE_MESSAGE_ID, CODE_FIELD_NAME, itemCode));
			} else if (productSku.getProduct().getProductType().isConfigurable()) {
				String error = "Item '" + itemCode + "' is a configurable product. Please add it individually using 'additemtocart' form.";
				errorMessagesCollected.add(constructStructuredErrorMessage(error, ITEM_IS_CONFIGURABLE, CODE_FIELD_NAME, itemCode));
			}
		}
	}

	/**
	 * Validate the given entity's quantity.
	 *
	 * @param entity                 entity
	 * @param errorMessagesCollected errorMessagesCollected
	 */
	protected void validateQuantity(final ItemEntity entity, final Set<StructuredErrorMessage> errorMessagesCollected) {
		if (entity.getQuantity() == null) {
			errorMessagesCollected.add(constructStructuredErrorMessage(QUANTITY_FIELD_MISSING_IN_REQUEST_BODY, INVALID_QUANTITY_MESSAGE_ID,
					QUANTITY_FIELD_NAME));
			return;
		}

		if (entity.getQuantity() <= 0) {
			errorMessagesCollected.add(constructStructuredErrorMessage(INVALID_QUANTITY_IN_REQUEST_BODY, INVALID_QUANTITY_MESSAGE_ID,
					QUANTITY_FIELD_NAME));
		}
	}

	@Reference
	public void setProductSkuLookup(final ProductSkuLookup productSkuLookup) {
		this.productSkuLookup = productSkuLookup;
	}

	@Reference
	public void setStoreRepository(final StoreRepository storeRepository) {
		this.storeRepository = storeRepository;
	}
}
