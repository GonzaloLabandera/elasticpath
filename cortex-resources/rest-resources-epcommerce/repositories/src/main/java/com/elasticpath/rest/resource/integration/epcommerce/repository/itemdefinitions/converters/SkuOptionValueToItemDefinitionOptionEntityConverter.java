/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.itemdefinitions.converters;

import java.util.Locale;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.springframework.core.convert.converter.Converter;

import com.elasticpath.domain.skuconfiguration.SkuOption;
import com.elasticpath.domain.skuconfiguration.SkuOptionValue;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionOptionEntity;
import com.elasticpath.rest.id.util.Base32Util;
import com.elasticpath.rest.identity.util.SubjectUtil;
import com.elasticpath.rest.resource.ResourceOperationContext;

/**
 * Converter for ItemDefinitionOptionEntity.
 */
@Singleton
@Named
public class SkuOptionValueToItemDefinitionOptionEntityConverter implements Converter<SkuOptionValue, ItemDefinitionOptionEntity> {

	private final ResourceOperationContext resourceOperationContext;

	/**
	 * Constructor.
	 *
	 * @param resourceOperationContext resourceOperationContext
	 */
	@Inject
	public SkuOptionValueToItemDefinitionOptionEntityConverter(
			@Named("resourceOperationContext") final ResourceOperationContext resourceOperationContext) {
		this.resourceOperationContext = resourceOperationContext;
	}

	@Override
	public ItemDefinitionOptionEntity convert(final SkuOptionValue skuOptionValue) {
		final Locale locale = SubjectUtil.getLocale(resourceOperationContext.getSubject());
		final SkuOption skuOption = skuOptionValue.getSkuOption();

		return ItemDefinitionOptionEntity.builder()
				.withName(skuOption.getOptionKey())
				.withDisplayName(skuOption.getDisplayName(locale, true))
				.withOptionId(Base32Util.encode(skuOption.getGuid()))
				.withOptionValueId(skuOptionValue.getOptionValueKey())
				.build();
	}
}
