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

import io.reactivex.Observable;
import io.reactivex.Single;
import org.apache.commons.lang3.StringUtils;

import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.modifier.ModifierField;
import com.elasticpath.domain.modifier.ModifierFieldOption;
import com.elasticpath.domain.modifier.ModifierGroup;
import com.elasticpath.domain.shoppingcart.ShoppingItem;
import com.elasticpath.rest.ResourceOperationFailure;
import com.elasticpath.rest.definition.carts.LineItemConfigurationEntity;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.ModifiersRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.ShoppingCartRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.order.OrderRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.sku.ProductSkuRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.transform.ReactiveAdapter;
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
	private final ReactiveAdapter reactiveAdapter;

	/**
	 * Constructor of service contexts repository.
	 *
	 * @param modifierService        a {@link ModifierService}.
	 * @param shoppingCartRepository a {@link ShoppingCartRepository}.
	 * @param productSkuRepository   a {@link ProductSkuRepository}.
	 * @param orderRepository        a {@link OrderRepository}.
	 * @param reactiveAdapter        a {@link ReactiveAdapter}.
	 */
	@Inject
	public ModifiersRepositoryImpl(
			@Named("modifierService") final ModifierService modifierService,
			@Named("shoppingCartRepository") final ShoppingCartRepository shoppingCartRepository,
			@Named("productSkuRepository") final ProductSkuRepository productSkuRepository,
			@Named("orderRepository") final OrderRepository orderRepository,
			@Named("reactiveAdapter") final ReactiveAdapter reactiveAdapter) {

		this.modifierService = modifierService;
		this.shoppingCartRepository = shoppingCartRepository;
		this.productSkuRepository = productSkuRepository;
		this.orderRepository = orderRepository;
		this.reactiveAdapter = reactiveAdapter;
	}

	@Override
	public Single<ModifierGroup> findModifierGroupByCode(final String modifierGroupCode) {
		checkNotNull(modifierGroupCode);
		return reactiveAdapter.fromServiceAsSingle(() -> modifierService.findModifierGroupByCode(modifierGroupCode))
				.onErrorResumeNext(Single.error(ResourceOperationFailure.notFound(MODIFIERGROUP_NOT_FOUND_MESSAGE)));
	}

	@Override
	public Single<ModifierField> findModifierFieldBy(final String modifierFieldCode,
													 final String modifierGroupCode) {
		checkNotNull(modifierGroupCode);
		checkNotNull(modifierFieldCode);

		return getModifierField(modifierFieldCode, modifierGroupCode)
				.onErrorResumeNext(Single.error(ResourceOperationFailure.notFound(MODIFIERFIELD_NOT_FOUND_MESSAGE)));
	}

	@Override
	public Single<ModifierFieldOption> findModifierFieldOptionBy(final String modifierOptionValue,
																 final String modifierFieldCode, final String modifierGroupCode) {
		checkNotNull(modifierGroupCode);
		checkNotNull(modifierFieldCode);
		checkNotNull(modifierOptionValue);

		return getModifierField(modifierFieldCode, modifierGroupCode)
				.onErrorResumeNext(Single.error(ResourceOperationFailure.notFound(MODIFIERFIELDOPTION_NOT_FOUND_MESSAGE)))
				.map(modifierField -> getModifierFieldOption(modifierOptionValue, modifierField))
				.onErrorResumeNext(Single.error(ResourceOperationFailure.notFound(MODIFIERFIELDOPTION_NOT_FOUND_MESSAGE)));
	}

	@Override
	public Single<Map<ModifierField, String>> findModifierValues(final String cartId, final String shoppingItemGuid) {
		return getShoppingItem(cartId, shoppingItemGuid)
				.flatMap(shoppingItem -> getFieldValues(shoppingItem.getSkuGuid(), shoppingItem.getFields()));

	}

	@Override
	public Single<Map<ModifierField, String>> findPurchaseItemModifierValues(final String storeCode, final String purchaseGuid,
																			 final String purchaseLineItemGuid) {
		return orderRepository.findByGuid(storeCode, purchaseGuid)
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
		return productSkuRepository.getProductSkuWithAttributesByGuid(skuGuid)
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

	private Single<ModifierField> getModifierField(final String modifierFieldCode, final String modifierGroupCode) {
		return reactiveAdapter.fromServiceAsSingle(() -> modifierService.findModifierGroupByCode(modifierGroupCode))
				.flatMapObservable(modifierGroup -> Observable.fromIterable(modifierGroup.getModifierFields())
						.filter(modifierField -> StringUtils.equals(modifierFieldCode, modifierField.getCode())))
				.firstOrError();
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
				.flatMap(productSku -> findModifiersByProduct(productSku.getProduct()))
				.map(this::buildLineItemConfigurationEntity);
	}

	private LineItemConfigurationEntity buildLineItemConfigurationEntity(final List<ModifierField> fields) {
		LineItemConfigurationEntity.Builder configBuilder = LineItemConfigurationEntity.builder();
		fields.forEach(field -> configBuilder.addingProperty(field.getCode(), ""));
		return configBuilder.build();
	}

	@Override
	public Single<List<ModifierField>> findModifiersByProduct(final Product product) {
		return reactiveAdapter.fromServiceAsSingle(() -> modifierService.findModifierFieldsByProductType(product.getProductType()));
	}

	@Override
	public Observable<String> findMissingRequiredFieldCodesByShoppingItem(final ShoppingItem shoppingItem) {
		return productSkuRepository.getProductSkuWithAttributesByGuid(shoppingItem.getSkuGuid())
				.flatMap(productSku -> findModifiersByProduct(productSku.getProduct()))
				.flatMapObservable(Observable::fromIterable)
				.filter(ModifierField::isRequired)
				.map(ModifierField::getCode)
				.filter(code -> StringUtils.isBlank(shoppingItem.getFields().get(code)));
	}

}
