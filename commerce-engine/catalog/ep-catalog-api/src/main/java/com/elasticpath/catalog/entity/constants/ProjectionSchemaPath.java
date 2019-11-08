/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.entity.constants;

/**
 * Class contains path to json schemas for {@link com.elasticpath.catalog.entity.Projection}.
 */
public final class ProjectionSchemaPath {
	/**
	 * Path to {@link com.elasticpath.catalog.entity.fieldmetadata.FieldMetadata} json schema.
	 */
	public static final String FIELD_METADATA_SCHEMA_JSON = "/schema/fieldMetadata.schema.json";

	/**
	 * Path to {@link com.elasticpath.catalog.entity.option.Option} json schema.
	 */
	public static final String OPTION_SCHEMA_JSON = "/schema/option.schema.json";

	/**
	 * Path to {@link com.elasticpath.catalog.entity.brand.Brand} json schema.
	 */
	public static final String BRAND_SCHEMA_JSON = "/schema/brand.schema.json";

	/**
	 * Path to {@link com.elasticpath.catalog.entity.attribute.Attribute} json schema.
	 */
	public static final String ATTRIBUTE_SCHEMA_JSON = "/schema/attribute.schema.json";

	/**
	 * Path to {@link com.elasticpath.catalog.entity.offer.Offer} json schema.
	 */
	public static final String OFFER_SCHEMA_JSON = "/schema/offer.schema.json";

	/**
	 * Path to {@link com.elasticpath.catalog.entity.category.Category} json schema.
	 */
	public static final String CATEGORY_SCHEMA_JSON = "/schema/category.schema.json";

	private ProjectionSchemaPath() {
		// Do not instantiate this class
	}
}
