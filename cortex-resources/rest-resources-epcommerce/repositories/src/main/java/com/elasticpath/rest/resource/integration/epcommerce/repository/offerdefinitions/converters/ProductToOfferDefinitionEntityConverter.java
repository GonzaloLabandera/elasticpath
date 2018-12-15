/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.offerdefinitions.converters;

import java.util.Collection;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.springframework.core.convert.converter.Converter;

import com.elasticpath.domain.attribute.AttributeValue;
import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.catalog.impl.ProductImpl;
import com.elasticpath.rest.definition.base.DetailsEntity;
import com.elasticpath.rest.definition.offerdefinitions.OfferDefinitionEntity;
import com.elasticpath.rest.identity.util.SubjectUtil;
import com.elasticpath.rest.resource.ResourceOperationContext;
import com.elasticpath.rest.resource.integration.epcommerce.transform.AttributeValueTransformer;
import com.elasticpath.rest.util.collection.CollectionUtil;

/**
 * Converter for ProductImpl.
 */
@Singleton
@Named
public class ProductToOfferDefinitionEntityConverter implements Converter<ProductImpl, OfferDefinitionEntity> {

	private final AttributeValueTransformer attributeValueTransformer;

	private final ResourceOperationContext resourceOperationContext;

	/**
	 * Constructor.
	 *
	 * @param attributeValueTransformer attributeValueTransformer
	 * @param resourceOperationContext  resourceOperationContext
	 */
	@Inject
	public ProductToOfferDefinitionEntityConverter(
			@Named("attributeValueTransformer") final AttributeValueTransformer attributeValueTransformer,
			@Named("resourceOperationContext") final ResourceOperationContext resourceOperationContext) {
		this.attributeValueTransformer = attributeValueTransformer;
		this.resourceOperationContext = resourceOperationContext;
	}

	@Override
	public OfferDefinitionEntity convert(final ProductImpl product) {
		Locale locale = SubjectUtil.getLocale(resourceOperationContext.getSubject());
		Collection<DetailsEntity> attributes = createDetailsEntities(product, locale);
		return OfferDefinitionEntity.builder()
				.withDisplayName(product.getDisplayName(locale))
				.withDetails(attributes)
				.build();
	}

	/**
	 * Create a collection of DetailEntities based on attribute values in the product and product sku.
	 *
	 * @param product product
	 * @param locale  locale
	 * @return a collection of DetailEntities
	 */
	protected Collection<DetailsEntity> createDetailsEntities(final Product product, final Locale locale) {

		Collection<AttributeValue> productAttributeValues = product.getFullAttributeValues(locale);
		if (CollectionUtil.isEmpty(productAttributeValues)) {
			return null;
		}
		return productAttributeValues.stream().map(attributeValue -> attributeValueTransformer.transformToEntity(attributeValue, locale))
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
	}
}
