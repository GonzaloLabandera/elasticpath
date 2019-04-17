/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.tools.sync.dstmessagelistener.email.converters.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.common.collect.ImmutableList;

import org.assertj.core.api.SoftAssertions;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.core.messaging.changeset.ChangeSetEventType;
import com.elasticpath.email.EmailDto;
import com.elasticpath.messaging.EventMessage;
import com.elasticpath.messaging.impl.EventMessageImpl;

/**
 * Unit test for class {@link EmailDtoConverterImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class EmailDtoConverterImplTest {

	private static final String CHANGESET_GUID = "CS_GUID1234";
	private static final String SUCCESS = "SUCCESS";
	private static final String FROM_EMAIL = "no-reply@elasticpath.com";
	private static final String ADMIN_EMAIL = "user@domain.com";
	private static final String CREATOR_EMAIL = "creator@email.com";
	private static final String PUBLISHER_EMAIL = "publisher@email.com";
	private static final String FAILURE = "FAILURE";
	private static final String SYNC_SUCCESS_DETAILS_KEY = "syncSuccessDetails";
	private static final String SYNC_FAILURE_DETAILS_KEY = "syncFailureDetails";
	private static final String SYNC_RESULTS_KEY = "syncResults";
	private static final String EMAIL_ADDRESS_KEY = "emailAddress";
	private static final String CHANGE_SET_CREATOR_KEY = "changeSetCreator";
	private static final String CHANGE_SET_PUBLISH_INITIATOR_KEY = "changeSetPublishInitiator";
	private static final String PUBLISH_SUMMARY_KEY = "publishSummary";
	private static final String PUBLISH_SUMMARY_SUCCESS_MESSAGE = "Publish succeeded. 5 entities were synchronised successfully. "
			+ "0 entities failed to synchronise.";

	@InjectMocks
	private EmailDtoConverterImpl emailDtoConverter;

	@Before
	public void setUp() {
		emailDtoConverter.setErrorEmailAddress(ADMIN_EMAIL);
		emailDtoConverter.setFromEmailAddress(FROM_EMAIL);
	}

	@Test
	public void testConversionOfTopLevelAttributesOnSuccess() {
		// Given
		EventMessage eventMessage = givenEventMessage(ChangeSetEventType.CHANGE_SET_PUBLISHED);

		// When
		EmailDto emailDto = emailDtoConverter.fromEventMessage(eventMessage);

		// Then
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(emailDto.getSubject())
				.isEqualTo(String.format("Change Set Publishing %s for change set [%s]", SUCCESS, CHANGESET_GUID));
		softly.assertThat(emailDto.getTextBody()).contains(PUBLISH_SUMMARY_SUCCESS_MESSAGE);
		softly.assertAll();
	}

	@Test
	public void testConversionOfTopLevelAttributesOnFailure() {
		// Given
		EventMessage eventMessage = givenEventMessage(ChangeSetEventType.CHANGE_SET_PUBLISH_FAILED);

		// When
		EmailDto emailDto = emailDtoConverter.fromEventMessage(eventMessage);

		// Then
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(emailDto.getSubject())
				.isEqualTo(String.format("Change Set Publishing %s for change set [%s]", FAILURE, CHANGESET_GUID));
		softly.assertThat(emailDto.getTextBody()).contains(PUBLISH_SUMMARY_SUCCESS_MESSAGE);
		softly.assertAll();
	}

	@Test
	public void testInvalidEventTypeError() {
		// Given
		EventMessage eventMessage = givenEventMessage(ChangeSetEventType.CHANGE_SET_READY_FOR_PUBLISH);

		// When
		Throwable throwable = catchThrowable(() -> emailDtoConverter.fromEventMessage(eventMessage));

		// Then
		assertThat(throwable)
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining(String.format("Impossible to convert Event Message of type %s. Expected types are [%s, %s].",
						ChangeSetEventType.CHANGE_SET_READY_FOR_PUBLISH.toString(),
						ChangeSetEventType.CHANGE_SET_PUBLISHED.toString(),
						ChangeSetEventType.CHANGE_SET_PUBLISH_FAILED.toString()));
	}

	@Test
	public void testEmailDestinationAndFromOnSuccess() {
		// Given
		EventMessage eventMessage = givenEventMessage(ChangeSetEventType.CHANGE_SET_PUBLISHED);

		// When
		EmailDto emailDto = emailDtoConverter.fromEventMessage(eventMessage);

		// Then
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(emailDto.getFrom())
				.isEqualTo(FROM_EMAIL);
		softly.assertThat(emailDto.getTo())
				.containsExactlyInAnyOrder(PUBLISHER_EMAIL, CREATOR_EMAIL);
		softly.assertAll();
	}


	@Test
	public void testEmailDestinationAndFromOnFailure() {
		// Given
		EventMessage eventMessage = givenEventMessage(ChangeSetEventType.CHANGE_SET_PUBLISH_FAILED);

		// When
		EmailDto emailDto = emailDtoConverter.fromEventMessage(eventMessage);

		// Then
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(emailDto.getFrom())
				.isEqualTo(FROM_EMAIL);
		softly.assertThat(emailDto.getTo())
				.containsExactlyInAnyOrder(ADMIN_EMAIL, CREATOR_EMAIL, PUBLISHER_EMAIL);
		softly.assertAll();
	}

	@Test
	public void testConversionOfSummaryMessage() {
		// Given
		EventMessage eventMessage = givenEventMessage(ChangeSetEventType.CHANGE_SET_PUBLISHED);

		// When
		EmailDto emailDto = emailDtoConverter.fromEventMessage(eventMessage);

		// Then
		assertThat(emailDto.getTextBody()).containsSequence(PUBLISH_SUMMARY_SUCCESS_MESSAGE);
	}

	@Test
	public void testSuccessRecipientList() {
		// Given
		EventMessage eventMessage = givenEventMessage(ChangeSetEventType.CHANGE_SET_PUBLISHED);

		// When
		Collection<String> successEmailList = emailDtoConverter.createSuccessEmailList(eventMessage);

		// Then
		assertThat(successEmailList)
				.containsExactlyInAnyOrder(CREATOR_EMAIL, PUBLISHER_EMAIL);
	}

	@Test
	public void testFailureRecipientList() {
		// Given
		EventMessage eventMessage = givenEventMessage(ChangeSetEventType.CHANGE_SET_PUBLISH_FAILED);

		// When
		Collection<String> successEmailList = emailDtoConverter.createErrorEmailList(eventMessage);

		// Then
		assertThat(successEmailList)
				.containsExactlyInAnyOrder(CREATOR_EMAIL, PUBLISHER_EMAIL, ADMIN_EMAIL);
	}

	@Test
	public void testCreateBody() {
		// Given
		EventMessage eventMessage = givenEventMessage(ChangeSetEventType.CHANGE_SET_PUBLISHED);

		// When
		String emailBody = emailDtoConverter.createEmailBody(eventMessage);

		// Then
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(emailBody).contains(PUBLISH_SUMMARY_SUCCESS_MESSAGE);
		softly.assertAll();
	}

	@Test
	public void testNoEmailProvided() {
		// Given
		EventMessage eventMessage = givenEventMessage(ChangeSetEventType.CHANGE_SET_PUBLISHED);
		eventMessage.getData().remove(CHANGE_SET_CREATOR_KEY);
		eventMessage.getData().remove(CHANGE_SET_PUBLISH_INITIATOR_KEY);

		// When
		Throwable throwable = catchThrowable(() -> emailDtoConverter.fromEventMessage(eventMessage));

		// Then
		assertThat(throwable)
				.isInstanceOf(IllegalStateException.class)
				.hasMessage("No e-mails found to use as destination.");
	}

	@Test
	public void testErrorEmailAsSingleRecipient() {
		// Given
		EventMessage eventMessage = givenEventMessage(ChangeSetEventType.CHANGE_SET_PUBLISH_FAILED);
		eventMessage.getData().remove(CHANGE_SET_CREATOR_KEY);
		eventMessage.getData().remove(CHANGE_SET_PUBLISH_INITIATOR_KEY);

		// When
		EmailDto emailDto = emailDtoConverter.fromEventMessage(eventMessage);

		// Then
		assertThat(emailDto.getTo())
				.containsExactly(ADMIN_EMAIL);
	}

	private EventMessage givenEventMessage(final ChangeSetEventType changeSetEventType) {
		final List<String> syncSuccessDetailsList = ImmutableList.of("Success message 1", "Success message 2");
		final List<String> syncFailureDetailsList = ImmutableList.of("Failure message 1", "Failure message 2");

		Map<String, Object> changeSetCreatorMap = new HashMap<>();
		changeSetCreatorMap.put(EMAIL_ADDRESS_KEY, CREATOR_EMAIL);

		Map<String, Object> changeSetPublishInitiatorMap = new HashMap<>();
		changeSetPublishInitiatorMap.put(EMAIL_ADDRESS_KEY, PUBLISHER_EMAIL);

		Map<String, Object> messagesMap = new HashMap<>();
		messagesMap.put(SYNC_SUCCESS_DETAILS_KEY, syncSuccessDetailsList);
		messagesMap.put(SYNC_FAILURE_DETAILS_KEY, syncFailureDetailsList);

		Map<String, Object> dataMap = new HashMap<>();
		dataMap.put(CHANGE_SET_CREATOR_KEY, changeSetCreatorMap);
		dataMap.put(CHANGE_SET_PUBLISH_INITIATOR_KEY, changeSetPublishInitiatorMap);
		dataMap.put(SYNC_RESULTS_KEY, messagesMap);
		dataMap.put(PUBLISH_SUMMARY_KEY, PUBLISH_SUMMARY_SUCCESS_MESSAGE);

		return new EventMessageImpl(changeSetEventType, CHANGESET_GUID, dataMap);
	}

}
