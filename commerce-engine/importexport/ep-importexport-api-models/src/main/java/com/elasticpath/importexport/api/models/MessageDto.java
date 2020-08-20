/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.importexport.api.models;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * DTO object for marshalling the message portion of the Integration Server API import response.
 */
@XmlRootElement(name = "message")
@XmlAccessorType(XmlAccessType.NONE)
public class MessageDto {
	@XmlElement(name = "jobType")
	private String jobType;

	@XmlElement(name = "code", required = true)
	private String code;

	@XmlElementWrapper(name = "params")
	@XmlElement(name = "param")
	private String[] params;

	@XmlElement(name = "message")
	private String message;

	@XmlElement(name = "exception")
	private String exceptionMessage;

	/**
	 * Default constructor.
	 */
	public MessageDto() {
		// Do nothing
	}

	/**
	 * Constructor.
	 *
	 * @param jobType the job type
	 * @param code the error code
	 * @param params the error parameters
	 * @param message the error message
	 * @param exception the exception that occurred (optional)
	 */
	public MessageDto(final String jobType, final String code, final String[] params, final String message, final Throwable exception) {
		this.jobType = jobType;
		this.code = code;
		this.params = new String[params.length];
		System.arraycopy(params, 0, this.params, 0, params.length);
		this.message = message;
		if (exception != null) {
			this.exceptionMessage = exception.getMessage();
		}
	}

	public String getCode() {
		return code;
	}

	public String getJobType() {
		return jobType;
	}

	/**
	 * Get message parameters.
	 * @return message parameters array
	 */
	public String[] getParams() {
		return params; // NOPMD
	}

	public String getMessage() {
		return message;
	}

	public String getExceptionMessage() {
		return exceptionMessage;
	}
}
