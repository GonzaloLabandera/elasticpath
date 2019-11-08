/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.catalog.update.processor.connectivity.impl.projection.converter.impl;

import static com.elasticpath.catalog.entity.constants.OfferPropertiesNames.PROPERTY_BUNDLE_PRICING;
import static com.elasticpath.catalog.entity.constants.OfferPropertiesNames.PROPERTY_MINIMUM_ORDER_QUANTITY;
import static com.elasticpath.catalog.entity.constants.OfferPropertiesNames.PROPERTY_NOT_SOLD_SEPARATELY;
import static com.elasticpath.catalog.entity.constants.OfferPropertiesNames.PROPERTY_OFFER_TYPE;

import java.util.Arrays;
import java.util.List;

import com.elasticpath.catalog.entity.Property;
import com.elasticpath.catalog.entity.offer.BundlePricing;
import com.elasticpath.domain.catalog.ProductCharacteristics;

/**
 * Represents a class which implements extracting property logic for {@link com.elasticpath.catalog.entity.offer.Offer}.
 */
public class OfferPropertyExtractor {
	private final String offerType;
	private final boolean notSoldSeparately;
	private final int minOrderQty;
	private final ProductCharacteristics productCharacteristics;
	private final List<Property> propertyList;

	/**
	 * Constructor.
	 *
	 * @param offerType              offer type.
	 * @param notSoldSeparately      not sold separately value.
	 * @param minOrderQty            min Order Qty value.
	 * @param productCharacteristics {@link ProductCharacteristics}.
	 */
	public OfferPropertyExtractor(final String offerType, final boolean notSoldSeparately, final int minOrderQty,
								  final ProductCharacteristics productCharacteristics) {
		this.offerType = offerType;
		this.notSoldSeparately = notSoldSeparately;
		this.minOrderQty = minOrderQty;
		this.productCharacteristics = productCharacteristics;
		this.propertyList = createPropertyList();
	}

	private List<Property> createOfferPropertiesForProductBundle() {
		return Arrays.asList(new Property(PROPERTY_OFFER_TYPE, offerType),
				new Property(PROPERTY_NOT_SOLD_SEPARATELY, String.valueOf(notSoldSeparately)),
				new Property(PROPERTY_MINIMUM_ORDER_QUANTITY, String.valueOf(minOrderQty)),
				new Property(PROPERTY_BUNDLE_PRICING, extractBundlePricingType(productCharacteristics).toString()));
	}

	private List<Property> createOfferPropertiesForNonBundleProduct() {
		return Arrays.asList(new Property(PROPERTY_OFFER_TYPE, offerType),
				new Property(PROPERTY_NOT_SOLD_SEPARATELY, String.valueOf(notSoldSeparately)),
				new Property(PROPERTY_MINIMUM_ORDER_QUANTITY, String.valueOf(minOrderQty)));
	}

	private List<Property> createPropertyList() {
		if (productCharacteristics != null) {
			return createOfferPropertiesForProductBundle();
		}
		return createOfferPropertiesForNonBundleProduct();
	}

	private BundlePricing extractBundlePricingType(final ProductCharacteristics productCharacteristics) {
		if (productCharacteristics.isCalculatedBundle()) {
			return BundlePricing.CALCULATED;
		}
		return BundlePricing.ASSIGNED;
	}

	/**
	 * @return list of Properties.
	 */
	public List<Property> getPropertyList() {
		return propertyList;
	}
}
