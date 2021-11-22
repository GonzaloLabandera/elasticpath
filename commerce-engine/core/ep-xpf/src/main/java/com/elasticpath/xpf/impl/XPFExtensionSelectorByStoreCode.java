/**
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.xpf.impl;

import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.elasticpath.xpf.XPFExtensionSelector;

/**
 * Selects extension by store code.
 */
public class XPFExtensionSelectorByStoreCode implements XPFExtensionSelector {

	private final String storeCode;

	/**
	 * Constructor with store code.
	 *
	 * @param storeCode the store code
	 */
	public XPFExtensionSelectorByStoreCode(final String storeCode) {
		this.storeCode = storeCode;
	}

	@Override
	public boolean matches(final XPFExtensionSelector xpfExtensionSelector) {
		if (this.getValue() == null || xpfExtensionSelector.getValue() == null) {
			return false;
		}
		return this.getValue().toString().equalsIgnoreCase(xpfExtensionSelector.getValue().toString());
	}

	@Override
	public boolean matches(final List<XPFExtensionSelector> others) {
		return others.stream().anyMatch(xpfExtensionSelector -> xpfExtensionSelector.matches(this));
	}

	@Override
	public Object getValue() {
		return this.storeCode;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}

		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}

		XPFExtensionSelectorByStoreCode that = (XPFExtensionSelectorByStoreCode) obj;

		return new EqualsBuilder()
				.append(getValue(), that.getValue())
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
				.append(getValue())
				.toHashCode();
	}
}
