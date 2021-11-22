/*
 * Copyright © 2021 Elastic Path Software Inc. All rights reserved.
 */

package com.elasticpath.xpf.converters;

import org.springframework.core.convert.converter.Converter;

import com.elasticpath.base.common.dto.StructuredErrorMessageType;
import com.elasticpath.xpf.connectivity.dto.XPFStructuredErrorMessageType;

/**
 * Convert StructuredErrorMessageType to XPFStructuredErrorMessageType.
 */
@SuppressWarnings("checkstyle:magicnumber")
public class XPFStructuredErrorMessageTypeConverter implements Converter<StructuredErrorMessageType, XPFStructuredErrorMessageType> {
	@Override
	public XPFStructuredErrorMessageType convert(final StructuredErrorMessageType structuredErrorMessageType) {

		if (structuredErrorMessageType.equals(StructuredErrorMessageType.ERROR)) {
			return XPFStructuredErrorMessageType.ERROR;
		}
		if (structuredErrorMessageType.equals(StructuredErrorMessageType.WARNING)) {
			return XPFStructuredErrorMessageType.WARNING;
		}
		if (structuredErrorMessageType.equals(StructuredErrorMessageType.INFORMATION)) {
			return XPFStructuredErrorMessageType.INFORMATION;
		}
		if (structuredErrorMessageType.equals(StructuredErrorMessageType.PROMOTION)) {
			return XPFStructuredErrorMessageType.PROMOTION;
		}
		if (structuredErrorMessageType.equals(StructuredErrorMessageType.NEEDINFO)) {
			return XPFStructuredErrorMessageType.NEEDINFO;
		}
		return null;
	}
}
