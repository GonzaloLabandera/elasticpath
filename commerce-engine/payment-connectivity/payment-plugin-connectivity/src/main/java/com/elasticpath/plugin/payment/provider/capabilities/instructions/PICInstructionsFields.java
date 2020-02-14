/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.plugin.payment.provider.capabilities.instructions;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Payment instrument creation instructions request.
 */
public class PICInstructionsFields {

	private final List<String> fields;
	private final List<String> blockingFields;

	/**
	 * Constructor.
	 *
	 * @param fields List containing dynamic fields provided by payment provider plugin for creating payment instrument.
	 */
	public PICInstructionsFields(final List<String> fields) {
		this(fields, Collections.emptyList());
	}

	/**
	 * Constructor.
	 *
	 * @param fields         List containing dynamic fields provided by payment provider plugin for creating payment instrument.
	 * @param blockingFields blocking fields required to create payments instrument.
	 */
	public PICInstructionsFields(final List<String> fields, final List<String> blockingFields) {
		this.fields = Optional.ofNullable(fields)
				.map(Collections::unmodifiableList)
				.orElse(Collections.emptyList());
		this.blockingFields = blockingFields;
	}

	/**
	 * Gets fields.
	 *
	 * @return the fields
	 */
	public List<String> getFields() {
		return fields;
	}

	/**
	 * Gets blocking fields required to create payments instructions.
	 *
	 * @return blocking fields required to create payments instructions.
	 */
	public List<String> getBlockingFields() {
		return blockingFields;
	}

}
