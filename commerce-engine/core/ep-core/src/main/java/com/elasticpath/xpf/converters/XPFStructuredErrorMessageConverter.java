/*
 * Copyright © 2021 Elastic Path Software Inc. All rights reserved.
 */

package com.elasticpath.xpf.converters;

import org.springframework.core.convert.converter.Converter;

import com.elasticpath.base.common.dto.StructuredErrorMessage;
import com.elasticpath.xpf.connectivity.dto.XPFStructuredErrorMessage;

/**
 * Convert StructuredErrorMessages to XPFStructuredErrorMessages.
 */
public class XPFStructuredErrorMessageConverter implements Converter<StructuredErrorMessage, XPFStructuredErrorMessage> {

	private XPFStructuredErrorResolutionConverter xpfStructuredErrorResolutionConverter;

	private XPFStructuredErrorMessageTypeConverter xpfStructuredErrorMessageTypeConverter;

	@Override
	public XPFStructuredErrorMessage convert(final StructuredErrorMessage structuredErrorMessage) {
		return XPFStructuredErrorMessage.builder()
				.withData(structuredErrorMessage.getData())
				.withMessageId(structuredErrorMessage.getMessageId())
				.withDebugMessage(structuredErrorMessage.getDebugMessage())
				.withResolution(xpfStructuredErrorResolutionConverter.convert(structuredErrorMessage.getResolution().orElse(null)))
				.withType(xpfStructuredErrorMessageTypeConverter.convert(structuredErrorMessage.getType()))
				.build();
	}

	public void setXpfStructuredErrorResolutionConverter(final XPFStructuredErrorResolutionConverter xpfStructuredErrorResolutionConverter) {
		this.xpfStructuredErrorResolutionConverter = xpfStructuredErrorResolutionConverter;
	}

	public void setXpfStructuredErrorMessageTypeConverter(final XPFStructuredErrorMessageTypeConverter xpfStructuredErrorMessageTypeConverter) {
		this.xpfStructuredErrorMessageTypeConverter = xpfStructuredErrorMessageTypeConverter;
	}
}
