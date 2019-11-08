/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.selenium.domainobjects;

import java.util.HashMap;
import java.util.Map;

/**
 * Cart Item Modifier Group Field.
 */
public class CartItemModiferGroupField {
	private final Map<String, String> names = new HashMap<>();
	private final Map<String, Map<String, String>> options = new HashMap<>();
	private String fieldCode;
	private String fieldName;
	private String fieldType;
	private String shortTextSize;
	private String optionValue;
	private String optionName;
	private boolean isRequired;
	private String oldFieldType;

	public String getFieldCode() {
		return fieldCode;
	}

	public void setFieldCode(final String fieldCode) {
		this.fieldCode = fieldCode;
	}

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(final String fieldName) {
		this.fieldName = fieldName;
	}

	public String getFieldType() {
		return fieldType;
	}

	public void setFieldType(final String fieldType) {
		this.fieldType = fieldType;
	}

	public String getShortTextSize() {
		return shortTextSize;
	}

	public void setShortTextSize(final String shortTextSize) {
		this.shortTextSize = shortTextSize;
	}

	public String getOptionValue() {
		return optionValue;
	}

	public void setOptionValue(final String optionValue) {
		this.optionValue = optionValue;
	}

	public String getOptionName() {
		return optionName;
	}

	public void setOptionName(final String optionName) {
		this.optionName = optionName;
	}

	public boolean isFieldRequired() {
		return isRequired;
	}

	public void setRequired(final boolean required) {
		isRequired = required;
	}

	public void setName(final String language, final String name) {
		names.put(language, name);
	}

	public Map<String, Map<String, String>> getOptions() {
		return options;
	}

	public Map<String, String> getNames() {
		return names;
	}

	public String getName(final String language) {
		return names.get(language);
	}

	public void setOption(final String optionCode, final String language, final String localizedName) {
		Map<String, String> optionLocalizedName = options.get(optionCode);
		if (optionLocalizedName == null) {
			Map<String, String> names = new HashMap<>();
			names.put(language, localizedName);
			options.put(optionCode, names);
		} else {
			optionLocalizedName.put(language, localizedName);
			options.put(optionCode, optionLocalizedName);
		}
	}

	public String getOldFieldType() {
		return oldFieldType;
	}

	public void setOldFieldType(final String oldFieldType) {
		this.oldFieldType = oldFieldType;
	}
}
