/*
 * Copyright (c) Elastic Path Software Inc., 2016
 */

package com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.impl;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import io.reactivex.Single;
import org.apache.commons.lang3.StringUtils;

import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.domain.modifier.ModifierField;
import com.elasticpath.domain.modifier.ModifierFieldOption;
import com.elasticpath.domain.modifier.ModifierGroup;
import com.elasticpath.domain.shoppingcart.ShoppingItem;
import com.elasticpath.rest.chain.Assign;
import com.elasticpath.rest.chain.ExecutionResultChain;
import com.elasticpath.rest.chain.OnFailure;
import com.elasticpath.rest.command.ExecutionResult;
import com.elasticpath.rest.command.ExecutionResultFactory;
import com.elasticpath.rest.definition.carts.LineItemConfigurationEntity;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.ModifiersRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.ShoppingCartRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.order.OrderRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.sku.ProductSkuRepository;
import com.elasticpath.service.modifier.ModifierService;

/**
 * Implementation of {@link ModifiersRepository}.
 */
@Singleton
@Named("modifiersRepository")
public class ModifiersRepositoryImpl implements ModifiersRepository {

	private static final String MODIFIERGROUP_NOT_FOUND_MESSAGE = "ModifierGroup not found";

	private static final String MODIFIERFIELD_NOT_FOUND_MESSAGE = "ModifierField not found";

	private static final String MODIFIERFIELDOPTION_NOT_FOUND_MESSAGE = "ModifierFieldOption not found";

	private final ModifierService modifierService;
	private final ShoppingCartRepository shoppingCartRepository;
	private final ProductSkuRepository productSkuRepository;
	private final OrderRepository orderRepository;

	/**
	 * Constructor of service contexts repository.
	 *
	 * @param modifierService a {@link ModifierService}.
	 * @param shoppingCartRepository  a {@link ShoppingCartRepository}.
	 * @param productSkuRepository    a {@link ProductSkuRepository}.
	 * @param orderRepository         a {@link OrderRepository}.
	 */
	@Inject
	public ModifiersRepositoryImpl(
			@Named("modifierService")
			final ModifierService modifierService,
			@Named("shoppingCartRepository")
			final ShoppingCartRepository shoppingCartRepository,
			@Named("productSkuRepository")
			final ProductSkuRepository productSkuRepository,
			@Named("orderRepository")
			final OrderRepository orderRepository) {

		this.modifierService = modifierService;
		this.shoppingCartRepository = shoppingCartRepository;
		this.productSkuRepository = productSkuRepository;
		this.orderRepository = orderRepository;
	}

	@Override
	public ExecutionResult<ModifierGroup> findModifierGroupByCode(final String modifierGroupCode) {
		checkNotNull(modifierGroupCode);

		return new ExecutionResultChain() {
			@Override
			public ExecutionResult<?> build() {
				ModifierGroup modifierGroup = Assign.ifNotNull(
					modifierService.findModifierGroupByCode(modifierGroupCode),
					OnFailure.returnNotFound(MODIFIERGROUP_NOT_FOUND_MESSAGE));
				return ExecutionResultFactory.createReadOK(modifierGroup);
			}
		}.execute();
	}

	@Override
	public ExecutionResult<ModifierField> findModifierFieldBy(final String modifierFieldCode,
			final String modifierGroupCode) {
		checkNotNull(modifierGroupCode);
		checkNotNull(modifierFieldCode);

		return new ExecutionResultChain() {
			@Override
			public ExecutionResult<?> build() {
				ModifierField modifierField = Assign.ifNotNull(
					getModifierField(modifierFieldCode, modifierGroupCode),
					OnFailure.returnNotFound(MODIFIERFIELD_NOT_FOUND_MESSAGE));
				return ExecutionResultFactory.createReadOK(modifierField);
			}
		}.execute();
	}

	@Override
	public ExecutionResult<ModifierFieldOption> findModifierFieldOptionBy(final String modifierOptionValue,
			final String modifierFieldCode, final String modifierGroupCode) {
		checkNotNull(modifierGroupCode);
		checkNotNull(modifierFieldCode);
		checkNotNull(modifierOptionValue);

		return new ExecutionResultChain() {
			@Override
			public ExecutionResult<?> build() {
				ModifierField modifierField = Assign.ifNotNull(
					getModifierField(modifierFieldCode, modifierGroupCode),
					OnFailure.returnNotFound(MODIFIERFIELDOPTION_NOT_FOUND_MESSAGE));
				ModifierFieldOption modifierFieldOption = Assign.ifNotNull(
					getModifierFieldOption(modifierOptionValue, modifierField),
					OnFailure.returnNotFound(MODIFIERFIELDOPTION_NOT_FOUND_MESSAGE));
				return ExecutionResultFactory.createReadOK(modifierFieldOption);
			}
		}.execute();
	}

	@Override
	public Single<Map<ModifierField, String>> findModifierValues(final String cartId, final String shoppingItemGuid) {
		return getShoppingItem(cartId, shoppingItemGuid)
				.flatMap(shoppingItem -> getFieldValues(shoppingItem.getSkuGuid(), shoppingItem.getFields()));

	}

	@Override
	public Single<Map<ModifierField, String>> findPurchaseItemModifierValues(final String storeCode, final String purchaseGuid,
			final String purchaseLineItemGuid) {
		return orderRepository.findByGuidAsSingle(storeCode, purchaseGuid)
				.map(order -> order.getOrderSkuByGuid(purchaseLineItemGuid))
				.flatMap(orderSku -> getFieldValues(orderSku.getSkuGuid(), orderSku.getFields()));
	}

	private Single<Map<ModifierField, String>> getFieldValues(final String skuGuid, final Map<String, String> itemData) {
		return getModifierGroups(skuGuid)
				.map(modifierGroups -> getModifierFieldValues(itemData, getModifierFields(modifierGroups)));
	}

	private Single<ShoppingItem> getShoppingItem(final String cartId, final String cartItemGuid) {
		return shoppingCartRepository.getShoppingCart(cartId)
				.map(shoppingCart -> shoppingCart.getCartItemByGuid(cartItemGuid));
	}

	private Set<ModifierField> getModifierFields(final Set<ModifierGroup> modifierGroups) {
		return modifierGroups.stream().map(ModifierGroup::getModifierFields)
			.flatMap(Set::stream)
			.collect(Collectors.toCollection(LinkedHashSet::new));
	}

	private Single<Set<ModifierGroup>> getModifierGroups(final String skuGuid) {
		return productSkuRepository.getProductSkuWithAttributesByGuidAsSingle(skuGuid)
				.map(productSku -> productSku.getProduct().getProductType().getModifierGroups());
	}

	private Map<ModifierField, String> getModifierFieldValues(final Map<String, String> itemData,
			final Set<ModifierField> allModifierFields) {
		Map<ModifierField, String> matchingModifierFieldData = new LinkedHashMap<>(allModifierFields.size());
		for (ModifierField modField : allModifierFields) {
			String key = modField.getCode();
			matchingModifierFieldData.put(modField, itemData.getOrDefault(key, StringUtils.EMPTY));
		}
		return matchingModifierFieldData;
	}

	private ModifierField getModifierField(final String modifierFieldCode, final String modifierGroupCode) {
		ModifierField modifierField = null;

		ModifierGroup modifierGroup = modifierService.findModifierGroupByCode(modifierGroupCode);
		for (ModifierField field : modifierGroup.getModifierFields()) {
			if (modifierField == null && StringUtils.equals(modifierFieldCode, field.getCode())) {
				modifierField = field;
			}
		}
		return modifierField;
	}

	private ModifierFieldOption getModifierFieldOption(final String modifierOptionValue,
			final ModifierField modifierField) {
		ModifierFieldOption modifierFieldOption = null;

		for (ModifierFieldOption option : modifierField.getModifierFieldOptions()) {
			if (modifierFieldOption == null && StringUtils.equals(modifierOptionValue, option.getValue())) {
				modifierFieldOption = option;
			}
		}
		return modifierFieldOption;
	}

	@Override
	public Single<LineItemConfigurationEntity> getConfiguration(final String itemId) {
		return productSkuRepository.getProductSkuWithAttributesByCode(itemId)
				.map(this::buildLineItemConfigurationEntity);
	}

	private LineItemConfigurationEntity buildLineItemConfigurationEntity(final ProductSku productSku) {
		List<ModifierField> fields = findModifiersByProduct(productSku.getProduct());

		LineItemConfigurationEntity.Builder configBuilder = LineItemConfigurationEntity.builder();
		if (fields != null) {
			fields.forEach(field -> configBuilder.addingProperty(field.getCode(), ""));
		}
		return configBuilder.build();
	}

	@Override
	public List<ModifierField> findModifiersByProduct(final Product product) {

		return modifierService.findModifierFieldsByProductType(product.getProductType());

	}

	@Override
	public List<String> findMissingRequiredFieldCodesByShoppingItem(final ShoppingItem shoppingItem) {

		// find fields by given sku guid.
		ProductSku productSku = productSkuRepository.getProductSkuWithAttributesByGuidAsSingle(shoppingItem.getSkuGuid()).blockingGet();
		List<ModifierField> fields = findModifiersByProduct(productSku.getProduct());

		// find and return the missed code.
		return fields.stream()
			.filter(ModifierField::isRequired)
			.map(ModifierField::getCode)
			.filter(code -> StringUtils.isBlank(shoppingItem.getFields().get(code)))
			.collect(Collectors.toList());

	}

}
