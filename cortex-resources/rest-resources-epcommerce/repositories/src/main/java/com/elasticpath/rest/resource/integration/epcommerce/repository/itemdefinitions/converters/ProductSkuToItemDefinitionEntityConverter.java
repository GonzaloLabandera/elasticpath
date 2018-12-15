/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.itemdefinitions.converters;

import java.util.Collection;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.springframework.core.convert.converter.Converter;

import com.elasticpath.domain.attribute.AttributeValue;
import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.rest.definition.base.DetailsEntity;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionEntity;
import com.elasticpath.rest.identity.util.SubjectUtil;
import com.elasticpath.rest.resource.ResourceOperationContext;
import com.elasticpath.rest.resource.integration.epcommerce.transform.AttributeValueTransformer;
import com.elasticpath.rest.util.collection.CollectionUtil;

/**
 * Converter for ItemDefinitionEntity.
 */
@Singleton
@Named
public class ProductSkuToItemDefinitionEntityConverter implements Converter<ProductSku, ItemDefinitionEntity> {

	private final AttributeValueTransformer attributeValueTransformer;

	private final ResourceOperationContext resourceOperationContext;

	/**
	 * Constructor.
	 *
	 * @param attributeValueTransformer attributeValueTransformer
	 * @param resourceOperationContext  resourceOperationContext
	 */
	@Inject
	public ProductSkuToItemDefinitionEntityConverter(
			@Named("attributeValueTransformer") final AttributeValueTransformer attributeValueTransformer,
			@Named("resourceOperationContext") final ResourceOperationContext resourceOperationContext) {
		this.attributeValueTransformer = attributeValueTransformer;
		this.resourceOperationContext = resourceOperationContext;
	}

	@Override
	public ItemDefinitionEntity convert(final ProductSku productSku) {
		Locale locale = SubjectUtil.getLocale(resourceOperationContext.getSubject());
		Product product = productSku.getProduct();

		Collection<DetailsEntity> attributes = createDetailsEntities(productSku, product, locale);
		return ItemDefinitionEntity.builder()
				.withDisplayName(product.getDisplayName(locale))
				.withDetails(attributes)
				.build();
	}

	/**
	 * Create a collection of DetailEntities based on attribute values in the product and product sku.
	 *
	 * @param productSku productSku
	 * @param product    product
	 * @param locale     locale
	 * @return a collection of DetailEntities
	 */
	protected Collection<DetailsEntity> createDetailsEntities(final ProductSku productSku, final Product product, final Locale locale) {
		Collection<AttributeValue> productSkuAttributeValues = productSku.getFullAttributeValues(locale);
		Collection<AttributeValue> productAttributeValues = product.getFullAttributeValues(locale);

		//returns null to void out the attributes field of the entity
		if (CollectionUtil.isEmpty(productSkuAttributeValues) && CollectionUtil.isEmpty(productAttributeValues)) {
			return null;
		}

		Stream<AttributeValue> attributeValues = Stream.concat(productSkuAttributeValues.stream(), productAttributeValues.stream());

		return attributeValues.map(attributeValue -> attributeValueTransformer.transformToEntity(attributeValue, locale))
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
	}
}
