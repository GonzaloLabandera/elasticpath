/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.provider.payment.service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;

import com.elasticpath.base.common.dto.StructuredErrorMessage;
import com.elasticpath.base.exception.structured.EpStructureErrorMessageException;
import com.elasticpath.base.exception.structured.InvalidBusinessStateException;

/**
 * Payment modules exception sent to a frontend.
 */
public class PaymentsException extends EpStructureErrorMessageException implements InvalidBusinessStateException {

	private static final long serialVersionUID = 5000000001L;
	private static final String PAYMENT_MESSAGE = "Payments failure";

	private final PaymentsExceptionMessageId messageId;

	/**
	 * Constructor.
	 *
	 * @param messageId               business state message id for payment module errors
	 * @param structuredErrorMessages list of structured error messages
	 * @param cause                   cause exception
	 */
	public PaymentsException(final PaymentsExceptionMessageId messageId,
							 final List<StructuredErrorMessage> structuredErrorMessages,
							 final Throwable cause) {
		super(PAYMENT_MESSAGE, Collections.unmodifiableList(structuredErrorMessages), cause);
		this.messageId = messageId;
	}

	/**
	 * Constructor.
	 *
	 * @param messageId business state message id for payment module errors
	 * @param data      additional error data
	 */
	public PaymentsException(final PaymentsExceptionMessageId messageId,
							 final Map<String, String> data) {

		super(PAYMENT_MESSAGE, ImmutableList.of(new StructuredErrorMessage(
				messageId.getKey(),
				messageId.getDefaultDebugMessage(),
				data
		)));
		this.messageId = messageId;
	}

	public PaymentsExceptionMessageId getMessageId() {
		return messageId;
	}
}
