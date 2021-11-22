/*
 * Copyright © 2021 Elastic Path Software Inc. All rights reserved.
 */

package com.elasticpath.xpf.converters;

import org.springframework.core.convert.converter.Converter;

import com.elasticpath.base.common.dto.StructuredErrorResolution;
import com.elasticpath.xpf.connectivity.dto.XPFStructuredErrorResolution;

/**
 * Convert XPFStructuredErrorResolution to StructuredErrorResolution.
 */
public class StructuredErrorResolutionConverter implements Converter<XPFStructuredErrorResolution, StructuredErrorResolution> {
	@Override
	public StructuredErrorResolution convert(final XPFStructuredErrorResolution xpfStructuredErrorResolution) {
		if (xpfStructuredErrorResolution == null) {
			return null;
		}
		return new StructuredErrorResolution(xpfStructuredErrorResolution.getDomain(), xpfStructuredErrorResolution.getGuid());
	}
}
