/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.catalog.update.processor.connectivity.impl.projection.converter.impl;

import java.util.List;

import com.elasticpath.catalog.entity.attribute.Attribute;
import com.elasticpath.catalog.entity.brand.Brand;
import com.elasticpath.catalog.entity.option.Option;

/**
 * Contains a {@link Brand},  a list of {@link Option}, a list of {@link Attribute} data for converting Offer.
 */
public class TranslationExtractorData {

	private final Brand brand;
	private final List<Option> options;
	private final List<Attribute> attributes;

	/**
	 * Constructor.
	 *
	 * @param brand      {@link Brand}.
	 * @param options    list of       {@link Option}.
	 * @param attributes list of {@link Attribute} .
	 */
	public TranslationExtractorData(final Brand brand, final List<Option> options, final List<Attribute> attributes) {
		this.brand = brand;
		this.options = options;
		this.attributes = attributes;
	}

	public Brand getBrand() {
		return brand;
	}

	public List<Option> getOptions() {
		return options;
	}

	public List<Attribute> getAttributes() {
		return attributes;
	}
}
