/**
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.commons.enums;

import java.util.ArrayList;
import java.util.List;

/**
 * Enum implementing a representation of reason for Invalid Catalog Code.
 */
public enum InvalidCatalogCodeMessageImpl implements InvalidCatalogCodeMessage {

	/**
	 * A message code indicating required catalog code.
	 */
	VALUE_REQUIRED_CODE_MESSAGE("EpValidatorFactory_ValueRequired"),

	/**
	 * A message code indicating max length exceeded.
	 */
	MAX_LENGTH_CODE_MESSAGE ("EpValidatorFactory_MaxCharLength"),

	/**
	 * A message code indicating no spaces are allowed.
	 */
	NO_SPACES_CODE_MESSAGE("EpValidatorFactory_NoSpace"),

	/**
	 * A message code indicating invalid Catalog Code.
	 */
	INVALID_CATALOG_CODE_MESSAGE("EpValidatorFactory_CatalogCode"),

	/**
	 * A message code indicating invalid Category Code.
	 */
	INVALID_CATEGORY_CODE_MESSAGE("EpValidatorFactory_CategoryCode"),

	/**
	 * A message code indicating invalid Product Code.
	 */
	INVALID_PRODUCT_CODE_MESSAGE("EpValidatorFactory_ProductCode"),

	/**
	 * A message code indicating invalid SKU Code.
	 */
	INVALID_SKU_CODE_MESSAGE("EpValidatorFactory_SkuCode"),

	/**
	 * A message code indicating invalid Brand Code.
	 */
	INVALID_BRAND_CODE_MESSAGE("EpValidatorFactory_BrandCode");


	private final String messageCode;
	private final List<String> parameters;

	/**
	 * Constructor.
	 *
	 * @param messageCode to be find in <code>CoreMessages</code>
	 */
	InvalidCatalogCodeMessageImpl(final String messageCode) {
		this.messageCode = messageCode;
		parameters = new ArrayList<>();
	}

	@Override
	public String getMessageCode() {
		return messageCode;
	}

	@Override
	public List<String> getParameters() {
		return parameters;
	}

	@Override
	public void addParameter(final String parameter) {
		parameters.add(parameter);
	}

}
