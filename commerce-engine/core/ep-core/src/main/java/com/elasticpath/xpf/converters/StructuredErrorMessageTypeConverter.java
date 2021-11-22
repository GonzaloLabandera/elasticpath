/*
 * Copyright © 2021 Elastic Path Software Inc. All rights reserved.
 */

package com.elasticpath.xpf.converters;

import org.springframework.core.convert.converter.Converter;

import com.elasticpath.base.common.dto.StructuredErrorMessageType;
import com.elasticpath.xpf.connectivity.dto.XPFStructuredErrorMessageType;

/**
 * Convert XPFStructuredErrorMessageType to StructuredErrorMessageType.
 */
@SuppressWarnings("checkstyle:magicnumber")
public class StructuredErrorMessageTypeConverter implements Converter<XPFStructuredErrorMessageType, StructuredErrorMessageType> {
	@Override
	public StructuredErrorMessageType convert(final XPFStructuredErrorMessageType xpfStructuredErrorMessageType) {

		switch (xpfStructuredErrorMessageType) {
			case ERROR:
				return StructuredErrorMessageType.ERROR;
			case WARNING:
				return StructuredErrorMessageType.WARNING;
			case INFORMATION:
				return StructuredErrorMessageType.INFORMATION;
			case PROMOTION:
				return StructuredErrorMessageType.PROMOTION;
			case NEEDINFO:
				return StructuredErrorMessageType.NEEDINFO;
			default:
				return null;
		}
	}
}
