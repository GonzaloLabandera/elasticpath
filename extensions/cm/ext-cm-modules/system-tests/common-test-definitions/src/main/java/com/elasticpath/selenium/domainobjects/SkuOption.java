package com.elasticpath.selenium.domainobjects;

import java.util.HashMap;
import java.util.Map;

/**
 * Sku option class state object.
 */
public class SkuOption {
	/*
	 * Key - skuOption value code;
	 * Value - a map where a key is a language (as it is written in UI) and a value is a localized sku option value name
	 */
	private final Map<String, Map<String, String>> skuOptionValues = new HashMap<>();
	private final Map<String, String> names = new HashMap<>();
	private final Map<String, String> oldNames = new HashMap<>();
	private final Map<String, Map<String, String>> skuOptionValuesOldNames = new HashMap<>();
	private String code;

	public String getCode() {
		return code;
	}

	public void setCode(final String code) {
		this.code = code;
	}

	public String getName(final String language) {
		return names.get(language);
	}

	public void setName(final String language, final String name) {
		names.put(language, name);
	}

	public String getOldName(final String language) {
		return oldNames.get(language);
	}

	public void setOldName(final String language, final String name) {
		oldNames.put(language, name);
	}

	public void addSkuOptionValue(final String language, final String valueCode, final String valueDisplayName) {
		addSkuOptionValueName(this.skuOptionValues, language, valueCode, valueDisplayName);
	}

	public void addSkuOptionValueOldName(final String language, final String valueCode, final String valueDisplayName) {
		addSkuOptionValueName(this.skuOptionValuesOldNames, language, valueCode, valueDisplayName);
	}

	public Map<String, Map<String, String>> getSkuOptionValues() {
		return skuOptionValues;
	}

	public Map<String, String> getSkuOptionValueName(final String valueCode) {
		return skuOptionValues.get(valueCode);
	}

	public Map<String, String> getSkuOptionValueOldName(final String valueCode) {
		return skuOptionValuesOldNames.get(valueCode);
	}

	/**
	 * Get sku option value code by partial match. Returns first matched sku option value code.
	 * If none of the codes match returns empty string.
	 *
	 * @param partialCode sku value code for partial match
	 * @return first matched sku option value code. If none of the codes match return empty string
	 */
	public String getSkuOptionValueCodeByPartialCode(final String partialCode) {
		return skuOptionValues.entrySet().stream()
				.filter(entry -> entry.getKey().startsWith(partialCode))
				.findFirst()
				.map(Map.Entry::getKey)
				.orElse("");
	}

	/**
	 * Adds sku option value localized name.
	 *
	 * @param skuOptionValues  collection of existing sku option values.
	 * @param language         localization.
	 * @param valueCode        sku option value code.
	 * @param valueDisplayName sku option value localized name.
	 */
	private void addSkuOptionValueName(
			final Map<String, Map<String, String>> skuOptionValues, final String language, final String valueCode, final String valueDisplayName) {
		Map<String, String> skuOptionValueLocalizedName = skuOptionValues.get(valueCode);
		if (skuOptionValueLocalizedName == null) {
			Map<String, String> skuValueName = new HashMap<>();
			skuValueName.put(language, valueDisplayName);
			skuOptionValues.put(valueCode, skuValueName);
		} else {
			skuOptionValueLocalizedName.put(language, valueDisplayName);
			skuOptionValues.put(valueCode, skuOptionValueLocalizedName);
		}
	}
}
