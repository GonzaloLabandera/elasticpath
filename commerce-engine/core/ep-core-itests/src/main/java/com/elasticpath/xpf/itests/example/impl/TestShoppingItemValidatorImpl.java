/**
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.xpf.itests.example.impl;

import java.util.Collection;
import java.util.Collections;

import org.pf4j.Extension;

import com.elasticpath.xpf.annotations.XPFEmbedded;
import com.elasticpath.xpf.connectivity.context.XPFShoppingItemValidationContext;
import com.elasticpath.xpf.connectivity.dto.XPFStructuredErrorMessage;
import com.elasticpath.xpf.connectivity.extension.XPFExtensionPointImpl;
import com.elasticpath.xpf.connectivity.extensionpoint.ShoppingItemValidator;

/**
 * Embedded implementation of an ShoppingItemValidator.
 */
@Extension
@XPFEmbedded
public class TestShoppingItemValidatorImpl extends XPFExtensionPointImpl implements ShoppingItemValidator {
	@Override
	public Collection<XPFStructuredErrorMessage> validate(final XPFShoppingItemValidationContext context) {
		return Collections.emptyList();
	}
}
