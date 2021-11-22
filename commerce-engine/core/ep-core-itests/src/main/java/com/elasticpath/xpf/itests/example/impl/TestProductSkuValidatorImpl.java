/**
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.xpf.itests.example.impl;

import java.util.Collection;
import java.util.Collections;

import org.pf4j.Extension;

import com.elasticpath.xpf.annotations.XPFEmbedded;
import com.elasticpath.xpf.connectivity.context.XPFProductSkuValidationContext;
import com.elasticpath.xpf.connectivity.dto.XPFStructuredErrorMessage;
import com.elasticpath.xpf.connectivity.extension.XPFExtensionPointImpl;
import com.elasticpath.xpf.connectivity.extensionpoint.ProductSkuValidator;

/**
 * Embedded implementation of ProductSkuValidator.
 */
@Extension
@XPFEmbedded
public class TestProductSkuValidatorImpl extends XPFExtensionPointImpl implements ProductSkuValidator {

	@Override
	public Collection<XPFStructuredErrorMessage> validate(final XPFProductSkuValidationContext context) {
		return Collections.emptyList();
	}
}
