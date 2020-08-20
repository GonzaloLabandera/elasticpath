/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.importexport.api.models;

import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * DTO object for marshalling the Integration Server API import response.
 */
@XmlRootElement(name = "summary")
@XmlAccessorType(XmlAccessType.NONE)
public class SummaryDto {

	@XmlElementWrapper(name = "failures")
	@XmlElement(name = "message")
	private List<MessageDto> failures;

	@XmlElementWrapper(name = "warnings")
	@XmlElement(name = "message")
	private List<MessageDto> warnings;

	@XmlElementWrapper(name = "comments")
	@XmlElement(name = "message")
	private List<MessageDto> comments;

	@XmlElementWrapper(name = "objectCounters")
	@XmlElement(name = "entry")
	private List<SummaryEntryDto> objectCounters;

	/**
	 * Default constructor.
	 */
	public SummaryDto() {
		// Do nothing
	}

	/**
	 * Constructor.
	 * @param failures the list of failure messages
	 * @param warnings the list of warning messages
	 * @param comments the list of comment messages
	 * @param objectCounters the list of object counters
	 */
	public SummaryDto(final List<MessageDto> failures,
					  final List<MessageDto> warnings,
					  final List<MessageDto> comments,
					  final List<SummaryEntryDto> objectCounters) {
		this.failures = failures;
		this.warnings = warnings;
		this.comments = comments;
		this.objectCounters = objectCounters;
	}

	public List<SummaryEntryDto> getObjectCounters() {
		return objectCounters;
	}

	public List<MessageDto> getFailures() {
		return failures;
	}

	public List<MessageDto> getWarnings() {
		return warnings;
	}

	public List<MessageDto> getComments() {
		return comments;
	}
}
