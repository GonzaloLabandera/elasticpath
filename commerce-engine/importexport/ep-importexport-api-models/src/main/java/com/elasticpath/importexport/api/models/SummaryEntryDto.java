/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.importexport.api.models;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * DTO object for marshalling the entry portion of the Integration Server API import response.
 */
@XmlRootElement(name = "entry")
@XmlAccessorType(XmlAccessType.NONE)
public class SummaryEntryDto {
	@XmlElement(name = "jobType", required = true)
	private String jobType;

	@XmlElement(name = "counter", required = true)
	private Integer counter;

	/**
	 * Default constructor.
	 */
	public SummaryEntryDto() {
		// Do nothing
	}

	/**
	 * Constructor.
	 * @param jobType the job type
	 * @param counter the counter
	 */
	public SummaryEntryDto(final String jobType, final Integer counter) {
		this.jobType = jobType;
		this.counter = counter;
	}

	public String getJobType() {
		return jobType;
	}

	public Integer getCounter() {
		return counter;
	}
}
