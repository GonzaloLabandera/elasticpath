/*
 * Copyright © 2021 Elastic Path Software Inc. All rights reserved.
 */

package com.elasticpath.xpf.converters;

import org.springframework.core.convert.converter.Converter;

import com.elasticpath.base.common.dto.StructuredErrorResolution;
import com.elasticpath.xpf.connectivity.dto.XPFStructuredErrorResolution;

/**
 * Convert StructuredErrorResolution to XPFStructuredErrorResolution.
 */
public class XPFStructuredErrorResolutionConverter implements Converter<StructuredErrorResolution, XPFStructuredErrorResolution> {
	@Override
	public XPFStructuredErrorResolution convert(final StructuredErrorResolution structuredErrorResolution) {
		if (structuredErrorResolution == null) {
			return null;
		}
		return new XPFStructuredErrorResolution(structuredErrorResolution.getDomain(), structuredErrorResolution.getGuid());
	}
}
