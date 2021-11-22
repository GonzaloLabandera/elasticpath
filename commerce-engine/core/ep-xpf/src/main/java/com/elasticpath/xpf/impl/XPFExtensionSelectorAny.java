/**
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.xpf.impl;

import java.util.List;

import com.elasticpath.xpf.XPFExtensionSelector;

/**
 * Selects extension for any context.
 */
public class XPFExtensionSelectorAny implements XPFExtensionSelector {

	@Override
	public boolean matches(final XPFExtensionSelector xpfExtensionSelector) {
		return true;
	}

	@Override
	public boolean matches(final List<XPFExtensionSelector> others) {
		return true;
	}

	@Override
	public Object getValue() {
		return null;
	}
}
