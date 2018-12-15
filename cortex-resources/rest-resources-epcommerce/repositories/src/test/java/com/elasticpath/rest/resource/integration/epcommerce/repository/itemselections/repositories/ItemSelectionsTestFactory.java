/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.itemselections.repositories;

import com.google.common.collect.ImmutableMap;

import com.elasticpath.rest.definition.itemselections.ItemOptionSelectorChoiceIdentifier;
import com.elasticpath.rest.definition.itemselections.ItemOptionSelectorIdentifier;
import com.elasticpath.rest.id.type.CompositeIdentifier;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.resource.integration.epcommerce.repository.item.ItemRepository;

/**
 * Factory methods for building item selections identifiers.
 */
public final class ItemSelectionsTestFactory {

	private ItemSelectionsTestFactory() {
	}

	/**
	 * Builds an ItemOptionSelectorChoiceIdentifier with test data.
	 *
	 * @param scope         the scope
	 * @param skuCode       the item sku code
	 * @param optionId      the option id
	 * @param optionValueId the option value id
	 * @return ItemOptionSelectorChoiceIdentifier
	 */
	public static ItemOptionSelectorChoiceIdentifier buildItemOptionSelectorChoiceIdentifier(
			final String scope, final String skuCode, final String optionId, final String optionValueId) {
		return ItemOptionSelectorChoiceIdentifier.builder()
				.withItemOptionSelector(buildItemOptionSelectorIdentifier(scope, skuCode, optionId))
				.withOptionValueId(StringIdentifier.of(optionValueId))
				.build();
	}

	/**
	 * Builds an ItemOptionSelectorIdentifier with test data.
	 *
	 * @param scope    the scope
	 * @param skuCode  the item sku code
	 * @param optionId the option id
	 * @return ItemOptionSelectorIdentifier
	 */
	public static ItemOptionSelectorIdentifier buildItemOptionSelectorIdentifier(
			final String scope, final String skuCode, final String optionId) {
		return ItemOptionSelectorIdentifier.builder()
				.withScope(StringIdentifier.of(scope))
				.withItemId(CompositeIdentifier.of(ImmutableMap.of(ItemRepository.SKU_CODE_KEY, skuCode)))
				.withOptionId(StringIdentifier.of(optionId))
				.build();
	}
}
