/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.plugin.payment.provider.capabilities.creation;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Payment instrument creation fields.
 */
public class PaymentInstrumentCreationFields {

	private final List<String> fields;
	private final List<String> blockingFields;
	private final boolean saveable;

	/**
	 * Constructor.
	 *
	 * @param fields   PIC fields
	 * @param saveable if instrument is saveable to profile
	 */
	public PaymentInstrumentCreationFields(final List<String> fields, final boolean saveable) {
		this(fields, Collections.emptyList(), saveable);
	}

	/**
	 * Constructor.
	 *
	 * @param fields         PIC fields
	 * @param blockingFields blocking fields required to create payments instrument
	 * @param saveable       if instrument is saveable to profile
	 */
	public PaymentInstrumentCreationFields(final List<String> fields, final List<String> blockingFields, final boolean saveable) {
		this.fields = Optional.ofNullable(fields)
				.map(Collections::unmodifiableList)
				.orElse(Collections.emptyList());
		this.blockingFields = Optional.ofNullable(blockingFields)
				.map(Collections::unmodifiableList)
				.orElse(Collections.emptyList());
		this.saveable = saveable;
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
	 * Gets blocking fields required to create payments instrument.
	 *
	 * @return blocking fields required to create payments instrument.
	 */
	public List<String> getBlockingFields() {
		return blockingFields;
	}

	/**
	 * Is instrument saveable boolean.
	 *
	 * @return the boolean
	 */
	public boolean isSaveable() {
		return saveable;
	}

}
