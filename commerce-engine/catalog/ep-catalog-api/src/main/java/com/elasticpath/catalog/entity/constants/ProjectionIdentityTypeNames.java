/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.entity.constants;

/**
 * Class contains identity type constants for {@link com.elasticpath.catalog.entity.Projection}.
 */
public final class ProjectionIdentityTypeNames {
	/**
	 * Identity type for {@link com.elasticpath.catalog.entity.fieldmetadata.FieldMetadata}.
	 */
	public static final String FIELD_METADATA_IDENTITY_TYPE = "fieldMetadata";


	/**
	 * Identity type for {@link com.elasticpath.catalog.entity.option.Option}.
	 */
	public static final String OPTION_IDENTITY_TYPE = "option";

	/**
	 * Identity type for {@link com.elasticpath.catalog.entity.brand.Brand}.
	 */
	public static final String BRAND_IDENTITY_TYPE = "brand";

	/**
	 * Identity type for {@link com.elasticpath.catalog.entity.attribute.Attribute}.
	 */
	public static final String ATTRIBUTE_IDENTITY_TYPE = "attribute";

	/**
	 * Identity type for {@link com.elasticpath.catalog.entity.offer.Offer}.
	 */
	public static final String OFFER_IDENTITY_TYPE = "offer";

	/**
	 * Identity type for {@link com.elasticpath.catalog.entity.category.Category}.
	 */
	public static final String CATEGORY_IDENTITY_TYPE = "category";

	private ProjectionIdentityTypeNames() {
		// Do not instantiate this class
	}
}
