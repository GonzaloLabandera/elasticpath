/*
 * Copyright © 2021 Elastic Path Software Inc. All rights reserved.
 */

package com.elasticpath.xpf.converters;

import org.springframework.core.convert.converter.Converter;

import com.elasticpath.base.common.dto.StructuredErrorMessage;
import com.elasticpath.xpf.connectivity.dto.XPFStructuredErrorMessage;

/**
 * Covert XPFStructuredErrorMessage to StructuredErrorMessage.
 */
public class StructuredErrorMessageConverter implements Converter<XPFStructuredErrorMessage, StructuredErrorMessage> {

	private StructuredErrorResolutionConverter structuredErrorResolutionConverter;

	private StructuredErrorMessageTypeConverter structuredErrorMessageTypeConverter;

	@Override
	public StructuredErrorMessage convert(final XPFStructuredErrorMessage xpfStructuredErrorMessage) {
		return StructuredErrorMessage.builder()
				.withData(xpfStructuredErrorMessage.getData())
				.withMessageId(xpfStructuredErrorMessage.getMessageId())
				.withDebugMessage(xpfStructuredErrorMessage.getDebugMessage())
				.withResolution(structuredErrorResolutionConverter.convert(xpfStructuredErrorMessage.getResolution().orElse(null)))
				.withType(structuredErrorMessageTypeConverter.convert(xpfStructuredErrorMessage.getType()))
				.build();
	}

	public void setStructuredErrorResolutionConverter(final StructuredErrorResolutionConverter structuredErrorResolutionConverter) {
		this.structuredErrorResolutionConverter = structuredErrorResolutionConverter;
	}

	public void setStructuredErrorMessageTypeConverter(final StructuredErrorMessageTypeConverter structuredErrorMessageTypeConverter) {
		this.structuredErrorMessageTypeConverter = structuredErrorMessageTypeConverter;
	}
}
