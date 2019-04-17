/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.tools.sync.dstmessagelistener.email.converters.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import com.elasticpath.core.messaging.changeset.ChangeSetEventType;
import com.elasticpath.email.EmailDto;
import com.elasticpath.messaging.EventMessage;
import com.elasticpath.tools.sync.dstmessagelistener.email.converters.EmailDtoConverter;

/**
 * Converter bean responsible for converting into <code>{@link EmailDto}</code> objects.
 */
public class EmailDtoConverterImpl implements EmailDtoConverter {

	private static final String SUBJECT_FORMAT = "Change Set Publishing %s for change set [%s]";
	private static final String IMPOSSIBLE_CONVERSION_MESSAGE_FORMAT = "Impossible to convert Event Message of type %s. Expected types are [%s, %s].";

	private static final String EMAIL_ADDRESS_KEY = "emailAddress";
	private static final String CHANGE_SET_PUBLISH_INITIATOR_KEY = "changeSetPublishInitiator";
	private static final String CHANGE_SET_CREATOR_KEY = "changeSetCreator";
	private static final String PUBLISH_SUMMARY_KEY = "publishSummary";

	private String errorEmailAddress;

	private String fromEmailAddress;

	@Override
	public EmailDto fromEventMessage(final EventMessage eventMessage) {
		String successOrFailure;
		final List<String> toEmail = new ArrayList<>(5);

		if (eventMessage.getEventType().equals(ChangeSetEventType.CHANGE_SET_PUBLISHED)) {
			successOrFailure = "SUCCESS";
			toEmail.addAll(createSuccessEmailList(eventMessage));
		} else if (eventMessage.getEventType().equals(ChangeSetEventType.CHANGE_SET_PUBLISH_FAILED)) {
			successOrFailure = "FAILURE";
			toEmail.addAll(createErrorEmailList(eventMessage));
		} else {
			throw new IllegalStateException(String.format(IMPOSSIBLE_CONVERSION_MESSAGE_FORMAT,
					eventMessage.getEventType().toString(),
					ChangeSetEventType.CHANGE_SET_PUBLISHED,
					ChangeSetEventType.CHANGE_SET_PUBLISH_FAILED));
		}

		if (toEmail.isEmpty()) {
			throw new IllegalStateException("No e-mails found to use as destination.");
		}

		String textBody = createEmailBody(eventMessage);

		return EmailDto.builder()
				.withFrom(fromEmailAddress)
				.withTo(toEmail)
				.withSubject(String.format(SUBJECT_FORMAT, successOrFailure, eventMessage.getGuid()))
				.withTextBody(textBody)
				.build();
	}

	/**
	 * Creates an e-mail body describing the outcome of the change set publishing.
	 * @param eventMessage Publishing event message.
	 * @return Created e-mail body.
	 */
	protected String createEmailBody(final EventMessage eventMessage) {
		return (String) eventMessage.getData().get(PUBLISH_SUMMARY_KEY);
	}

	/**
	 * Creates the recipient e-mail list for a failed publishing attempt.
	 * @param eventMessage Publishing event message.
	 * @return E-mail recipient list.
	 */
	protected Collection<String> createErrorEmailList(final EventMessage eventMessage) {
		Collection<String> emailList = createSuccessEmailList(eventMessage);
		emailList.add(getErrorEmailAddress());
		return emailList;
	}

	/**
	 * Creates the recipient e-mail list for a successful publishing attempt.
	 * @param eventMessage Publishing event message.
	 * @return E-mail recipient list.
	 */
	protected Collection<String> createSuccessEmailList(final EventMessage eventMessage) {
		@SuppressWarnings("unchecked")
		Map<String, Object> changeSetPublishInitiator = (Map<String, Object>) eventMessage.getData().get(CHANGE_SET_PUBLISH_INITIATOR_KEY);
		@SuppressWarnings("unchecked")
		Map<String, Object> changeSetCreator = (Map<String, Object>) eventMessage.getData().get(CHANGE_SET_CREATOR_KEY);

		List<String> emailList = new ArrayList<>();

		if (changeSetCreator != null) {
			safelyAddEmail(changeSetCreator, emailList);
		}

		if (changeSetPublishInitiator != null) {
			safelyAddEmail(changeSetPublishInitiator, emailList);
		}

		return emailList;
	}

	@Override
	public void setErrorEmailAddress(final String errorEmailAddress) {
		this.errorEmailAddress = errorEmailAddress;
	}

	@Override
	public void setFromEmailAddress(final String fromEmailAddress) {
		this.fromEmailAddress = fromEmailAddress;
	}

	/**
	 * Provides the configured default e-mail recipient for failed publishing attempts.
	 * @return The error recipient e-mail.
	 */
	protected String getErrorEmailAddress() {
		return errorEmailAddress;
	}

	private void safelyAddEmail(final Map<String, Object> userMapRepresentation, final List<String> targetEmailList) {
		String email = ObjectUtils.defaultIfNull(userMapRepresentation.get(EMAIL_ADDRESS_KEY), "").toString();
		if (StringUtils.isNotEmpty(email)) {
			targetEmailList.add(email);
		}
	}

}
