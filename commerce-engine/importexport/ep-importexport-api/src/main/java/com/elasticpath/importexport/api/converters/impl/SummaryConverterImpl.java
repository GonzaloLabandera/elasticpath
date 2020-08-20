/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.importexport.api.converters.impl;

import java.util.List;
import java.util.stream.Collectors;

import com.elasticpath.importexport.api.converters.SummaryConverter;
import com.elasticpath.importexport.api.models.MessageDto;
import com.elasticpath.importexport.api.models.SummaryDto;
import com.elasticpath.importexport.api.models.SummaryEntryDto;
import com.elasticpath.importexport.common.summary.Summary;
import com.elasticpath.importexport.common.util.Message;
import com.elasticpath.importexport.common.util.MessageResolver;

/**
 * Converter class for converting a Summary object to a SummaryDto object.
 */
public class SummaryConverterImpl implements SummaryConverter {
	private MessageResolver messageResolver;

	@Override
	public SummaryDto convert(final Summary summary) {
		List<MessageDto> failures = summary.getFailures().stream()
				.map(this::convertMessage)
				.collect(Collectors.toList());
		List<MessageDto> warnings = summary.getWarnings().stream()
				.map(this::convertMessage)
				.collect(Collectors.toList());
		List<MessageDto> comments = summary.getComments().stream()
				.map(this::convertMessage)
				.collect(Collectors.toList());
		List<SummaryEntryDto> objectCounters = summary.getCounters().entrySet().stream()
				.map(entry -> new SummaryEntryDto(entry.getKey().name(), entry.getValue()))
				.collect(Collectors.toList());
		return new SummaryDto(failures, warnings, comments, objectCounters);
	}

	/**
	 * Convert Message object to MessageDto object.
	 * @param message the Message object
	 * @return the MessageDto object
	 */
	protected MessageDto convertMessage(final Message message) {
		String jobTypeName = null;
		if (message.getJobType() != null) {
			jobTypeName = message.getJobType().name();
		}
		return new MessageDto(jobTypeName, message.getCode(), message.getParams(), messageResolver.resolve(message), message.getException());
	}

	protected MessageResolver getMessageResolver() {
		return messageResolver;
	}

	public void setMessageResolver(final MessageResolver messageResolver) {
		this.messageResolver = messageResolver;
	}
}
