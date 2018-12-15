/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.itemdefinitions.converters;

import java.util.Locale;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.springframework.core.convert.converter.Converter;

import com.elasticpath.domain.skuconfiguration.SkuOptionValue;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionOptionValueEntity;
import com.elasticpath.rest.identity.util.SubjectUtil;
import com.elasticpath.rest.resource.ResourceOperationContext;

/**
 * Converter for ItemDefinitionOptionValueEntity.
 */
@Singleton
@Named
public class SkuOptionValueToItemDefinitionOptionValueEntityConverter implements Converter<SkuOptionValue, ItemDefinitionOptionValueEntity> {

	private final ResourceOperationContext resourceOperationContext;

	/**
	 * Constructor.
	 *
	 * @param resourceOperationContext resourceOperationContext
	 */
	@Inject
	public SkuOptionValueToItemDefinitionOptionValueEntityConverter(
			@Named("resourceOperationContext") final ResourceOperationContext resourceOperationContext) {
		this.resourceOperationContext = resourceOperationContext;
	}

	@Override
	public ItemDefinitionOptionValueEntity convert(final SkuOptionValue skuOptionValue) {
		final Locale locale = SubjectUtil.getLocale(resourceOperationContext.getSubject());
		return ItemDefinitionOptionValueEntity.builder()
				.withName(skuOptionValue.getOptionValueKey())
				.withDisplayName(skuOptionValue.getDisplayName(locale, true))
				.build();
	}
}