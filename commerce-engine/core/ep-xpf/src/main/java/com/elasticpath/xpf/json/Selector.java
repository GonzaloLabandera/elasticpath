/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.xpf.json;

import java.beans.ConstructorProperties;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import com.elasticpath.xpf.exception.InvalidConfigurationException;
import com.elasticpath.xpf.impl.XPFExtensionSelectorAny;
import com.elasticpath.xpf.impl.XPFExtensionSelectorByStoreCode;

/**
 * A class to map configurations from json.
 */
@Data
@SuppressWarnings("PMD.UnusedPrivateField")
public class Selector {
	private final String type;
	private final String value;

	/**
	 * Constructor.
	 *
	 * @param type  the selector type
	 * @param value the store code
	 */
	@ConstructorProperties({"type", "value"})
	public Selector(final String type, final String value) {
		validate(type, value);
		this.type = type;
		this.value = value;
	}

	private void validate(final String type, final String value) {
		if (!XPFExtensionSelectorByStoreCode.class.getSimpleName().equals(type) && !XPFExtensionSelectorAny.class.getSimpleName().equals(type)) {
			throw new InvalidConfigurationException("Wrong extension selector type: " + type);
		}

		if (XPFExtensionSelectorByStoreCode.class.getSimpleName().equals(type) && StringUtils.isEmpty(value)) {
			throw new InvalidConfigurationException("Value for extension selector is absent. Selector type is " + type);
		}
	}
}
