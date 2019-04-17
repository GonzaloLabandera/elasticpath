/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.tools.sync.dstmessagelistener.messages.impl;

import static com.elasticpath.commons.constants.ContextIdNames.CHANGESET_LOAD_TUNER;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.assertj.core.api.SoftAssertions;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.core.messaging.changeset.ChangeSetEventType;
import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.changeset.ChangeSet;
import com.elasticpath.domain.cmuser.CmUser;
import com.elasticpath.messaging.EventMessage;
import com.elasticpath.messaging.EventType;
import com.elasticpath.messaging.factory.EventMessageFactory;
import com.elasticpath.messaging.impl.EventMessageImpl;
import com.elasticpath.service.changeset.ChangeSetLoadTuner;
import com.elasticpath.service.changeset.ChangeSetManagementService;
import com.elasticpath.service.cmuser.CmUserService;
import com.elasticpath.tools.sync.dstmessagelistener.commons.constants.DataTransferExchangeConstants;
import com.elasticpath.tools.sync.dstmessagelistener.messages.DataSyncErrorResultItem;
import com.elasticpath.tools.sync.dstmessagelistener.messages.DataSyncEventMessageBuilder;
import com.elasticpath.tools.sync.job.Command;
import com.elasticpath.tools.sync.target.result.SyncErrorResultItem;
import com.elasticpath.tools.sync.target.result.SyncResultItem;

/**
 * Unit test for {@link DataSyncEventMessageServiceImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class DataSyncEventMessageServiceImplTest {

	private static final String CHANGE_SET_GUID = "GUID_1234";
	private static final String JOB_ENTRY_GUID = "JOBGUID_1234";
	private static final String TRANSACTION_JOB_UNIT_NAME = "Transaction name";
	private static final String CHANGE_SET_NAME = "The Change Set name";
	private static final String PUBLISH_SUMMARY = "Some publish summary.";
	private static final String CREATOR_GUID = "CREATOR_GUID1234";
	private static final String CREATOR_USER_NAME = "creatorUserName";
	private static final String CREATOR_FIRST_NAME = "creatorFirstName";
	private static final String CREATOR_LAST_NAME = "creatorLastName";
	private static final String CREATOR_EMAIL_ADDRESS = "creatorEmailAddress";

	@Mock
	private EventMessageFactory eventMessageFactory;

	@Mock
	private BeanFactory beanFactory;

	@Mock
	private ChangeSetLoadTuner changeSetLoadTuner;

	@Mock
	private ChangeSetManagementService changeSetManagementService;

	@Mock
	private CmUserService cmUserService;

	private ChangeSet changeSet;

	@Mock
	private CmUser cmUser;

	@Spy
	@InjectMocks
	private DataSyncEventMessageBuilderImpl<? extends DataSyncEventMessageBuilder<?>> dataSyncEventMessageBuilder;


	@Spy
	@InjectMocks
	private DataSyncEventMessageServiceImpl datasyncEventMessageService;

	@Captor
	private ArgumentCaptor<Map<String, Object>> dataArgumentCaptor;

	@SuppressWarnings("unchecked")
	@Before
	public void setUp() {
		// Do not inline. @InjectMocks will wrongly inject this even though it is a private field without a setter.
		changeSet = mock(ChangeSet.class);

		given(eventMessageFactory.createEventMessage(any(EventType.class), any(String.class), any(Map.class))).willAnswer(
				invocation -> new EventMessageImpl(
						(EventType) invocation.getArguments()[0],
						(String) invocation.getArguments()[1],
						(Map<String, Object>) invocation.getArguments()[2])
		);

		given(changeSet.getCreatedByUserGuid()).willReturn(CREATOR_GUID);
		given(changeSet.getName()).willReturn(CHANGE_SET_NAME);

		given(beanFactory.getBean(CHANGESET_LOAD_TUNER)).willReturn(changeSetLoadTuner);
		given(changeSetManagementService.get(eq(CHANGE_SET_GUID), same(changeSetLoadTuner))).willReturn(changeSet);

		doReturn(dataSyncEventMessageBuilder).when(datasyncEventMessageService).prepareMessage();
	}

	@Test
	public void testSimpleSend() {
		// When
		datasyncEventMessageService.prepareMessage()
				.withChangeSetGuid(CHANGE_SET_GUID)
				.withSuccess(true)
				.build();

		// Then
		verify(dataSyncEventMessageBuilder).build();
		verify(dataSyncEventMessageBuilder).buildData();
	}

	@Test
	public void testValidation() {
		// Given

		// When
		final Throwable throwable = catchThrowable(() -> datasyncEventMessageService.prepareMessage().build());

		// Then
		final SoftAssertions softly = new SoftAssertions();
		softly.assertThat(throwable)
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("Change Set Name is required.")
				.hasMessageContaining("Success flag is required.");
		softly.assertAll();
	}

	@Test
	public void testCompleteConstruction() {
		// Given
		final SyncResultItem syncResultItem = givenSyncResultItem();
		final SyncErrorResultItem syncErrorResultItem = givenSyncErrorResultItem();

		final Map<String, String> changeSetPublishInitiator = new HashMap<>();
		changeSetPublishInitiator.put("aPublisherKey", "aPublisherValue");

		final Map<String, String> changeSetCreator = new HashMap<>();
		changeSetCreator.put("aCreatorKey", "aCreatorValue");

		// When
		final EventMessage actualEventMessage = datasyncEventMessageService.prepareMessage()
				.withChangeSetGuid(CHANGE_SET_GUID)
				.withChangeSetName(CHANGE_SET_NAME)
				.withPublishSummary(PUBLISH_SUMMARY)
				.withSuccess(true)
				.withSyncSuccessResults(Lists.newArrayList(syncResultItem))
				.withSyncErrorResults(Lists.newArrayList(syncErrorResultItem))
				.withChangeSetCreatorData(changeSetCreator)
				.withChangeSetPublishInitiator(changeSetPublishInitiator)
				.build();

		// Then
		verify(eventMessageFactory).createEventMessage(
				eq(ChangeSetEventType.CHANGE_SET_PUBLISHED),
				eq(CHANGE_SET_GUID),
				dataArgumentCaptor.capture());
		verifyZeroInteractions(beanFactory);
		verifyZeroInteractions(changeSetManagementService);
		verifyZeroInteractions(cmUserService);

		final Map<String, Object> actualData = dataArgumentCaptor.getValue();

		final SoftAssertions softly = new SoftAssertions();

		assertData(syncErrorResultItem, changeSetPublishInitiator, changeSetCreator, actualData, softly);

		softly.assertThat(actualEventMessage).isNotNull();
		softly.assertThat(actualEventMessage.getGuid()).isEqualTo(CHANGE_SET_GUID);

		softly.assertAll();
	}

	@Test
	public void testFalseSuccessFlag() {
		// When
		datasyncEventMessageService.prepareMessage()
				.withChangeSetGuid(CHANGE_SET_GUID)
				.withSuccess(false)
				.build();

		// Then
		verify(dataSyncEventMessageBuilder).build();
		verify(dataSyncEventMessageBuilder).buildData();
	}

	@Test
	public void testCreatorSupplementedData() {
		// Given
		given(cmUserService.findByGuid(CREATOR_GUID)).willReturn(cmUser);

		given(cmUser.getUserName()).willReturn(CREATOR_USER_NAME);
		given(cmUser.getFirstName()).willReturn(CREATOR_FIRST_NAME);
		given(cmUser.getLastName()).willReturn(CREATOR_LAST_NAME);
		given(cmUser.getEmail()).willReturn(CREATOR_EMAIL_ADDRESS);
		given(cmUser.getGuid()).willReturn(CREATOR_GUID);

		// When
		final EventMessage eventMessage = datasyncEventMessageService.prepareMessage()
				.withChangeSetGuid(CHANGE_SET_GUID)
				.withChangeSetName(CHANGE_SET_NAME)
				.withPublishSummary(PUBLISH_SUMMARY)
				.withSuccess(true)
				.build();

		// Then
		verify(beanFactory).getBean(eq(CHANGESET_LOAD_TUNER));
		verify(changeSetManagementService).get(eq(CHANGE_SET_GUID), same(changeSetLoadTuner));
		verify(cmUserService).findByGuid(eq(CREATOR_GUID));

		final SoftAssertions softly = new SoftAssertions();

		softly.assertThat(eventMessage)
				.isNotNull()
				.extracting("data")
				.isNotNull();

		final Map<String, Object> data = eventMessage.getData();
		softly.assertThat(data)
				.containsKey(DataTransferExchangeConstants.CHANGE_SET_CREATOR_KEY);

		@SuppressWarnings("unchecked") final Map<String, String> creatorDataMap = (Map<String, String>) data.get(DataTransferExchangeConstants
				.CHANGE_SET_CREATOR_KEY);

		softly.assertThat(creatorDataMap)
				.containsEntry("guid", CREATOR_GUID)
				.containsEntry("username", CREATOR_USER_NAME)
				.containsEntry("firstName", CREATOR_FIRST_NAME)
				.containsEntry("lastName", CREATOR_LAST_NAME)
				.containsEntry("emailAddress", CREATOR_EMAIL_ADDRESS);

		softly.assertAll();
	}

	@Test
	public void testChangeSetNameSupplementedData() {
		// Given

		// When
		final EventMessage eventMessage = datasyncEventMessageService.prepareMessage()
				.withChangeSetGuid(CHANGE_SET_GUID)
				.withPublishSummary(PUBLISH_SUMMARY)
				.withSuccess(true)
				.build();

		// Then
		verify(beanFactory).getBean(eq(CHANGESET_LOAD_TUNER));
		verify(changeSetManagementService).get(eq(CHANGE_SET_GUID), same(changeSetLoadTuner));
		verify(cmUserService).findByGuid(eq(CREATOR_GUID));

		final SoftAssertions softly = new SoftAssertions();

		softly.assertThat(eventMessage)
				.isNotNull()
				.extracting("data")
				.isNotNull();

		final Map<String, Object> data = eventMessage.getData();
		softly.assertThat(data)
				.containsEntry(DataTransferExchangeConstants.CHANGE_SET_NAME_KEY, CHANGE_SET_NAME);

		softly.assertAll();
	}

	private void assertData(final SyncErrorResultItem syncErrorResultItem,
							final Map<String, String> changeSetPublishInitiator,
							final Map<String, String> changeSetCreator,
							final Map<String, Object> actualData,
							final SoftAssertions softly) {
		softly.assertThat(actualData)
				.containsEntry("changeSetName", CHANGE_SET_NAME)
				.containsEntry("publishSummary", PUBLISH_SUMMARY)
				.containsEntry("changeSetCreator", changeSetCreator)
				.containsEntry("changeSetPublishInitiator", changeSetPublishInitiator)
				.containsKey("syncResults");

		@SuppressWarnings("unchecked") final Map<String, Object> syncResults = (Map<String, Object>) actualData.get("syncResults");
		softly.assertThat(syncResults)
				.containsKey("syncSuccessDetails")
				.containsKey("syncFailureDetails");
		@SuppressWarnings("unchecked") final List<SyncResultItem> syncSuccessDetails = (List<SyncResultItem>) syncResults.get("syncSuccessDetails");
		softly.assertThat(syncSuccessDetails)
				.extracting("jobEntryCommand", "jobEntryGuid", "jobEntryType", "transactionJobUnitName")
				.contains(tuple(Command.UPDATE, JOB_ENTRY_GUID, Product.class, TRANSACTION_JOB_UNIT_NAME));
		@SuppressWarnings("unchecked") final List<DataSyncErrorResultItem> syncFailureDetails
				= (List<DataSyncErrorResultItem>) syncResults.get("syncFailureDetails");
		softly.assertThat(syncFailureDetails)
				.extracting("jobEntryCommand", "jobEntryGuid", "jobEntryType", "transactionJobUnitName", "exceptionMessage")
				.contains(tuple(Command.UPDATE, JOB_ENTRY_GUID, Product.class, TRANSACTION_JOB_UNIT_NAME,
						syncErrorResultItem.getCause().getMessage()));

		softly.assertAll();
	}

	private SyncResultItem givenSyncResultItem() {
		final SyncResultItem syncResultItem = new SyncResultItem();
		syncResultItem.setJobEntryCommand(Command.UPDATE);
		syncResultItem.setJobEntryGuid(JOB_ENTRY_GUID);
		syncResultItem.setJobEntryType(Product.class);
		syncResultItem.setTransactionJobUnitName(TRANSACTION_JOB_UNIT_NAME);
		return syncResultItem;
	}

	private SyncErrorResultItem givenSyncErrorResultItem() {
		final SyncErrorResultItem syncResultItem = new SyncErrorResultItem();
		syncResultItem.setJobEntryCommand(Command.UPDATE);
		syncResultItem.setJobEntryGuid(JOB_ENTRY_GUID);
		syncResultItem.setJobEntryType(Product.class);
		syncResultItem.setTransactionJobUnitName(TRANSACTION_JOB_UNIT_NAME);
		syncResultItem.setCause(new Exception());
		return syncResultItem;
	}


}
