/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.itemdefinitions.converters;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.springframework.core.convert.converter.Converter;

import com.elasticpath.domain.attribute.AttributeValue;
import com.elasticpath.domain.catalog.BundleConstituent;
import com.elasticpath.domain.catalog.ConstituentItem;
import com.elasticpath.domain.catalog.Product;
import com.elasticpath.rest.definition.base.DetailsEntity;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionComponentEntity;
import com.elasticpath.rest.identity.util.SubjectUtil;
import com.elasticpath.rest.resource.ResourceOperationContext;
import com.elasticpath.rest.resource.integration.epcommerce.transform.AttributeValueTransformer;

/**
 * Converter for ItemDefinitionComponentEntity.
 */
@Singleton
@Named
public class BundleConstituentToItemDefinitionComponentEntityConverter implements Converter<BundleConstituent, ItemDefinitionComponentEntity> {

	private final AttributeValueTransformer attributeValueTransformer;

	private final ResourceOperationContext resourceOperationContext;

	/**
	 * Constructor.
	 *
	 * @param attributeValueTransformer attributeValueTransformer
	 * @param resourceOperationContext  resourceOperationContext
	 */
	@Inject
	public BundleConstituentToItemDefinitionComponentEntityConverter(
			@Named("attributeValueTransformer") final AttributeValueTransformer attributeValueTransformer,
			@Named("resourceOperationContext") final ResourceOperationContext resourceOperationContext) {
		this.attributeValueTransformer = attributeValueTransformer;
		this.resourceOperationContext = resourceOperationContext;
	}

	@Override
	public ItemDefinitionComponentEntity convert(final BundleConstituent bundleConstituent) {
		Locale locale = SubjectUtil.getLocale(resourceOperationContext.getSubject());
		return ItemDefinitionComponentEntity.builder()
				.withDetails(createDetailsEntities(bundleConstituent.getConstituent(), locale))
				.withDisplayName(bundleConstituent.getConstituent().getProduct().getDisplayName(locale))
				.withQuantity(bundleConstituent.getQuantity())
				.build();
	}

	/**
	 * Create {@link DetailsEntity} objects from the passed {@link ConstituentItem}.
	 *
	 * @param constituentItem the constituent item to use to create the details entity
	 * @param locale the locale to use when retrieving attributes
	 * @return a collection of {@link DetailsEntity}s
	 */
	protected Collection<DetailsEntity> createDetailsEntities(final ConstituentItem constituentItem, final Locale locale) {
		Product product = constituentItem.getProduct();
		List<AttributeValue> productAttributeValues = product.getFullAttributeValues(locale);
		Stream<AttributeValue> attributeValues = productAttributeValues.stream();

		if (constituentItem.isProductSku()) {
			List<AttributeValue> productSkuAttributeValues = constituentItem.getProductSku().getFullAttributeValues(locale);
			attributeValues = Stream.concat(attributeValues, productSkuAttributeValues.stream());
		}

		return attributeValues.map(attributeValue -> attributeValueTransformer.transformToEntity(attributeValue, locale))
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
	}
}
