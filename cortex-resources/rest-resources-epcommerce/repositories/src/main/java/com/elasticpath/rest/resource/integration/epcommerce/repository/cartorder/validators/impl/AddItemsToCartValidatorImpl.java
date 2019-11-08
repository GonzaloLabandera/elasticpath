/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.validators.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import org.apache.commons.lang3.StringUtils;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.domain.store.Store;
import com.elasticpath.rest.ResourceOperationFailure;
import com.elasticpath.rest.advise.Message;
import com.elasticpath.rest.definition.carts.AddItemsToCartFormEntity;
import com.elasticpath.rest.definition.carts.ItemEntity;
import com.elasticpath.rest.definitions.validator.constants.ValidationMessages;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.validators.AddItemsToCartValidator;
import com.elasticpath.rest.resource.integration.epcommerce.repository.store.StoreRepository;
import com.elasticpath.rest.schema.StructuredMessageTypes;
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
	public Completable validate(final AddItemsToCartFormEntity addItemsToCartFormEntity, final String scope) {
		if (addItemsToCartFormEntity == null) {
			Message errorMessage = constructStructuredErrorMessage(ValidationMessages.MISSING_REQUIRED_REQUEST_BODY, MESSAGE_ID, ITEMS_FIELD_NAME);

			return Completable.error(ResourceOperationFailure.badRequestBody(EP_VALIDATION_EXCEPTION_MESSAGE,
					Collections.singletonList(errorMessage)));
		}

		if (addItemsToCartFormEntity.getItems() == null || addItemsToCartFormEntity.getItems().isEmpty()) {
			Message errorMessage = constructStructuredErrorMessage(ValidationMessages.MISSING_REQUIRED_REQUEST_BODY, MESSAGE_ID, ITEMS_FIELD_NAME);

			return Completable.error(ResourceOperationFailure.badRequestBody(EP_VALIDATION_EXCEPTION_MESSAGE,
					Collections.singletonList(errorMessage)));
		}

		Set<String> duplicateSkus = new HashSet<>();
		List<Observable<Message>> errorMessages = new ArrayList<>(addItemsToCartFormEntity.getItems().size() * 2);
		for (ItemEntity entity : addItemsToCartFormEntity.getItems()) {
			checkForDuplicateSkuEntries(entity, addItemsToCartFormEntity.getItems(), duplicateSkus);
			errorMessages.add(validateItemCode(entity, scope));
			errorMessages.add(validateQuantity(entity));
		}

		return Observable.concat(Observable.concat(errorMessages), Observable.fromIterable(duplicateSkus)
				.flatMap(itemCode -> {
					String debugMessage = "Item '"
							+ itemCode + "' has multiple entries. Please combine duplicate items into one entry.";
					return Observable.just(constructStructuredErrorMessage(debugMessage, ITEM_IS_DUPLICATE_ID, CODE_FIELD_NAME, itemCode));
				}))
				.toList()
				.flatMapCompletable(messages -> (messages.isEmpty())
						? Completable.complete()
						: Completable.error(ResourceOperationFailure.badRequestBody(EP_VALIDATION_EXCEPTION_MESSAGE, messages)));
	}

	/**
	 * Construct a Structure Error Message with the field name that contains the error.
	 *
	 * @param debugMessage debugMessage
	 * @param messageId    messageId
	 * @param fieldName    fieldName
	 * @return StructuredErrorMessage
	 */
	protected Message constructStructuredErrorMessage(final String debugMessage, final String messageId, final String fieldName) {
		Map<String, String> errorData = new HashMap<>();
		errorData.put("field-name", fieldName);

		return Message.builder()
				.withType(StructuredMessageTypes.ERROR)
				.withId(messageId)
				.withDebugMessage(debugMessage)
				.withData(errorData)
				.build();
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
	protected Message constructStructuredErrorMessage(final String debugMessage, final String messageId, final String fieldName,
													  final String fieldValue) {
		Map<String, String> errorData = new HashMap<>();
		errorData.put("field-name", fieldName);
		errorData.put("field-value", fieldValue);

		return Message.builder()
				.withType(StructuredMessageTypes.ERROR)
				.withId(messageId)
				.withDebugMessage(debugMessage)
				.withData(errorData)
				.build();
	}

	/**
	 * Check if given ProductSku is not in store.
	 *
	 * @param productSku productSku
	 * @param scope      scope
	 * @return true if SKU is not in store
	 */
	protected Single<Boolean> isSkuNotInStore(final ProductSku productSku, final String scope) {
		return storeRepository.findStoreAsSingle(scope)
				.map(Store::getCatalog)
				.map(catalog -> !productSku.getProduct().isInCatalog(catalog));
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
	 * @param entity entity
	 * @param scope  scope
	 * @return error messages
	 */
	protected Observable<Message> validateItemCode(final ItemEntity entity, final String scope) {
		String itemCode = entity.getCode();

		if (StringUtils.isBlank(itemCode)) {
			return Observable.just(constructStructuredErrorMessage(MISSING_CODE_IN_REQUEST_BODY, MESSAGE_ID, CODE_FIELD_NAME));
		} else {
			ProductSku productSku = productSkuLookup.findBySkuCode(itemCode);

			if (productSku == null) {
				String error = "Item with code '" + itemCode + "' does not exist.";
				return Observable.just(constructStructuredErrorMessage(error, MESSAGE_ID, CODE_FIELD_NAME, itemCode));
			}

			return isSkuNotInStore(productSku, scope)
					.flatMapObservable(isSkuNotInStore -> (isSkuNotInStore)
							? getNotInStoreErrorMessage(itemCode)
							: isSkuConfigurable(productSku, itemCode));
		}
	}

	/**
	 * Get the SKU not in store error message.
	 *
	 * @param itemCode itemCode
	 * @return error message
	 */
	protected Observable<Message> getNotInStoreErrorMessage(final String itemCode) {
		String error = "'" + itemCode + "' is not part of the current store's catalog.";
		return Observable.just(constructStructuredErrorMessage(error, ITEM_NOT_IN_STORE_MESSAGE_ID, CODE_FIELD_NAME, itemCode));
	}

	/**
	 * Check if given productSku is configurable.
	 *
	 * @param productSku productSku
	 * @param itemCode   itemCode
	 * @return error message
	 */
	protected Observable<Message> isSkuConfigurable(final ProductSku productSku, final String itemCode) {
		if (productSku.getProduct().getProductType().isConfigurable()) {
			String error = "Item '" + itemCode + "' is a configurable product. Please add it individually using 'additemtocart' form.";
			return Observable.just(constructStructuredErrorMessage(error, ITEM_IS_CONFIGURABLE, CODE_FIELD_NAME, itemCode));
		}

		return Observable.empty();
	}

	/**
	 * Validate the given entity's quantity.
	 *
	 * @param entity entity
	 * @return error message
	 */
	protected Observable<Message> validateQuantity(final ItemEntity entity) {
		if (entity.getQuantity() == null) {
			return Observable.just(constructStructuredErrorMessage(QUANTITY_FIELD_MISSING_IN_REQUEST_BODY, INVALID_QUANTITY_MESSAGE_ID,
					QUANTITY_FIELD_NAME));
		}

		if (entity.getQuantity() <= 0) {
			return Observable.just(constructStructuredErrorMessage(INVALID_QUANTITY_IN_REQUEST_BODY, INVALID_QUANTITY_MESSAGE_ID,
					QUANTITY_FIELD_NAME));
		}

		return Observable.empty();
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
