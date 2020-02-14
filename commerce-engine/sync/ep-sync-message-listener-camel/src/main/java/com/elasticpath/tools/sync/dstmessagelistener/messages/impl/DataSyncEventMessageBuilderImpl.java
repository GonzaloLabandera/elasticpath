/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.tools.sync.dstmessagelistener.messages.impl;

import static com.elasticpath.commons.constants.ContextIdNames.CHANGESET_LOAD_TUNER;
import static com.elasticpath.core.messaging.changeset.ChangeSetEventType.CHANGE_SET_PUBLISHED;
import static com.elasticpath.core.messaging.changeset.ChangeSetEventType.CHANGE_SET_PUBLISH_FAILED;
import static com.elasticpath.tools.sync.dstmessagelistener.commons.constants.DataTransferExchangeConstants.CHANGE_SET_CREATOR_KEY;
import static com.elasticpath.tools.sync.dstmessagelistener.commons.constants.DataTransferExchangeConstants.CHANGE_SET_NAME_KEY;
import static com.elasticpath.tools.sync.dstmessagelistener.commons.constants.DataTransferExchangeConstants.CHANGE_SET_PUBLISH_INITIATOR_KEY;
import static com.elasticpath.tools.sync.dstmessagelistener.commons.constants.DataTransferExchangeConstants.PUBLISH_SUMMARY_KEY;
import static com.elasticpath.tools.sync.dstmessagelistener.commons.constants.DataTransferExchangeConstants.SYNC_FAILURE_DETAILS_KEY;
import static com.elasticpath.tools.sync.dstmessagelistener.commons.constants.DataTransferExchangeConstants.SYNC_RESULTS_KEY;
import static com.elasticpath.tools.sync.dstmessagelistener.commons.constants.DataTransferExchangeConstants.SYNC_SUCCESS_DETAILS_KEY;
import static com.google.common.collect.Maps.newHashMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.domain.changeset.ChangeSet;
import com.elasticpath.domain.cmuser.CmUser;
import com.elasticpath.messaging.EventMessage;
import com.elasticpath.messaging.EventType;
import com.elasticpath.messaging.factory.EventMessageFactory;
import com.elasticpath.service.changeset.ChangeSetLoadTuner;
import com.elasticpath.service.changeset.ChangeSetManagementService;
import com.elasticpath.service.cmuser.CmUserService;
import com.elasticpath.tools.sync.dstmessagelistener.messages.DataSyncErrorResultItem;
import com.elasticpath.tools.sync.dstmessagelistener.messages.DataSyncEventMessageBuilder;
import com.elasticpath.tools.sync.target.result.SyncErrorResultItem;
import com.elasticpath.tools.sync.target.result.SyncResultItem;

/**
 * Implementation of {@link DataSyncEventMessageBuilder}.
 *
 * @param <T> the builder type
 */
public class DataSyncEventMessageBuilderImpl<T extends DataSyncEventMessageBuilder<T>>
		implements DataSyncEventMessageBuilder<T> {

	private static final String CREATOR_DATA_KEY_GUID = "guid";
	private static final String CREATOR_DATA_KEY_USER_NAME = "username";
	private static final String CREATOR_DATA_KEY_FIRST_NAME = "firstName";
	private static final String CREATOR_DATA_KEY_LAST_NAME = "lastName";
	private static final String CREATOR_DATA_KEY_EMAIL_ADDRESS = "emailAddress";
	private EventMessageFactory eventMessageFactory;
	private ChangeSetManagementService changeSetManagementService;
	private CmUserService cmUserService;
	private BeanFactory beanFactory;

	private ChangeSet changeSet;

	private String changeSetName;
	private Map<String, String> changeSetCreatorData;
	private String changeSetGuid;
	private Map<String, String> changeSetPublishInitiatorData;
	private Boolean success;
	private String publishSummary;
	private Collection<SyncResultItem> syncSuccessResults;
	private Collection<SyncErrorResultItem> syncErrorResults;

	/**
	 * Returns a reference to this Builder.
	 *
	 * @return this Builder
	 */
	@SuppressWarnings("unchecked")
	protected T self() {
		return (T) this;
	}

	/**
	 * Resets the builder state.
	 */
	protected void reset() {
		this.changeSetName = null;
		this.changeSetCreatorData = null;
		this.changeSetGuid = null;
		this.changeSetPublishInitiatorData = null;
		this.success = null;
		this.publishSummary = null;
		this.syncSuccessResults = null;
		this.syncErrorResults = null;
	}

	/**
	 * Builds the Event Message.
	 *
	 * @return the populated event message
	 */
	public EventMessage build() {
		validate();

		final Map<String, Object> data = buildData();

		final EventType eventType;
		if (success) {
			eventType = CHANGE_SET_PUBLISHED;
		} else {
			eventType = CHANGE_SET_PUBLISH_FAILED;
		}

		EventMessage result = getEventMessageFactory().createEventMessage(eventType, changeSetGuid, ImmutableMap.copyOf(data));
		reset();
		return result;
	}

	/**
	 * Builds the Event Message data map.
	 *
	 * @return the event message data map
	 */
	protected Map<String, Object> buildData() {
		final Map<String, Object> data = newHashMap();
		data.computeIfAbsent(CHANGE_SET_NAME_KEY, key -> supplementChangeSetName());
		data.computeIfAbsent(CHANGE_SET_CREATOR_KEY, key -> supplementCreatorData());
		data.computeIfAbsent(CHANGE_SET_PUBLISH_INITIATOR_KEY, key -> changeSetPublishInitiatorData);
		data.computeIfAbsent(SYNC_RESULTS_KEY, key -> createSyncResultsData(syncSuccessResults, syncErrorResults));
		data.computeIfAbsent(PUBLISH_SUMMARY_KEY, key -> publishSummary);
		return data;
	}

	/**
	 * Sets the Change Set name from the persisted value, if not already set.
	 *
	 * @return the change set name
	 */
	protected String supplementChangeSetName() {
		if (StringUtils.isEmpty(changeSetName)) {
			changeSetName = findChangeSet().getName();
		}
		return changeSetName;
	}

	/**
	 * Sets the Change Set creator data from the persisted values, if not already set.
	 *
	 * @return the change set creator data
	 */
	protected Map<String, String> supplementCreatorData() {
		if (MapUtils.isEmpty(changeSetCreatorData)) {
			final ChangeSet foundChangeSet = findChangeSet();
			final CmUser cmUser = getCmUserService().findByGuid(foundChangeSet.getCreatedByUserGuid());

			if (cmUser != null) {
				changeSetCreatorData = new HashMap<>();
				changeSetCreatorData.computeIfAbsent(CREATOR_DATA_KEY_GUID, key -> cmUser.getGuid());
				changeSetCreatorData.computeIfAbsent(CREATOR_DATA_KEY_USER_NAME, key -> cmUser.getUserName());
				changeSetCreatorData.computeIfAbsent(CREATOR_DATA_KEY_FIRST_NAME, key -> cmUser.getFirstName());
				changeSetCreatorData.computeIfAbsent(CREATOR_DATA_KEY_LAST_NAME, key -> cmUser.getLastName());
				changeSetCreatorData.computeIfAbsent(CREATOR_DATA_KEY_EMAIL_ADDRESS, key -> cmUser.getEmail());
			}
		}

		return changeSetCreatorData;
	}

	/**
	 * Loads the corresponding Change Set from the persistent data store.
	 *
	 * @return the change set
	 */
	protected ChangeSet findChangeSet() {
		if (changeSet == null) {
			final ChangeSetLoadTuner changeSetLoadTuner = getBeanFactory().getPrototypeBean(CHANGESET_LOAD_TUNER, ChangeSetLoadTuner.class);
			changeSetLoadTuner.setLoadingMemberObjects(false);
			changeSetLoadTuner.setLoadingMemberObjectsMetadata(false);
			changeSet = getChangeSetManagementService().get(changeSetGuid, changeSetLoadTuner);
		}

		return changeSet;
	}

	/**
	 * Asserts that required fields have been populated.
	 *
	 * @throws IllegalStateException if required fields are non populated
	 */
	protected void validate() {
		final StringBuilder errorMessageBuilder = new StringBuilder();

		if (StringUtils.isEmpty(changeSetGuid)) {
			errorMessageBuilder.append("Change Set Name is required. \n");
		}

		if (success == null) {
			errorMessageBuilder.append("Success flag is required. \n");
		}

		if (StringUtils.isNotEmpty(errorMessageBuilder)) {
			throw new IllegalStateException(errorMessageBuilder.toString());
		}
	}


	@Override
	public T withChangeSetGuid(final String changeSetGuid) {
		this.changeSetGuid = changeSetGuid;
		return self();
	}

	@Override
	public T withChangeSetName(final String changeSetName) {
		this.changeSetName = changeSetName;
		return self();
	}

	@Override
	public T withChangeSetCreatorData(final Map<String, String> changeSetCreatorData) {
		this.changeSetCreatorData = changeSetCreatorData;
		return self();
	}

	@Override
	public T withChangeSetPublishInitiator(final Map<String, String> changeSetPublishInitiatorData) {
		this.changeSetPublishInitiatorData = changeSetPublishInitiatorData;
		return self();
	}

	@Override
	public T withSuccess(final Boolean success) {
		this.success = success;
		return self();
	}

	@Override
	public T withPublishSummary(final String publishSummary) {
		this.publishSummary = publishSummary;
		return self();
	}

	@Override
	public T withSyncSuccessResults(final Collection<SyncResultItem> syncSuccessResults) {
		this.syncSuccessResults = syncSuccessResults;
		return self();
	}

	@Override
	public T withSyncErrorResults(final Collection<SyncErrorResultItem> syncErrorResults) {
		this.syncErrorResults = syncErrorResults;
		return self();
	}

	/**
	 * Creates a populated map containing sync results, for use within Event Message data.
	 *
	 * @param syncSuccessResults details of successfully synchronised entities
	 * @param syncErrorResults   details of entities that failed to synchronise
	 * @return a populated map
	 */
	protected Map<String, Object> createSyncResultsData(final Collection<SyncResultItem> syncSuccessResults,
														final Collection<SyncErrorResultItem> syncErrorResults) {
		final ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();

		if (CollectionUtils.isNotEmpty(syncSuccessResults)) {
			builder.put(SYNC_SUCCESS_DETAILS_KEY, syncSuccessResults);

		}

		if (CollectionUtils.isNotEmpty(syncErrorResults)) {
			builder.put(SYNC_FAILURE_DETAILS_KEY, createDataSyncErrorResultsData(syncErrorResults));
		}

		return builder.build();
	}

	/**
	 * Creates a collection of DataSyncErrorResultItemImpl from a collection of SyncErrorResultItem.
	 *
	 * @param syncErrorResults - the syncErrorResults to transform to DataSyncErrorResultItemImpl
	 * @return collection of DataSyncErrorResultItemImpl
	 */
	protected Collection<DataSyncErrorResultItem> createDataSyncErrorResultsData(final Collection<SyncErrorResultItem> syncErrorResults) {
		Collection<DataSyncErrorResultItem> dataSyncErrorResults = new ArrayList<>();

		syncErrorResults.forEach(syncErrorResultItem ->
				dataSyncErrorResults.add(
						new DataSyncErrorResultItem(
								syncErrorResultItem.getJobEntryType(),
								syncErrorResultItem.getJobEntryGuid(),
								syncErrorResultItem.getTransactionJobUnitName(),
								syncErrorResultItem.getJobEntryCommand(),
								(syncErrorResultItem.getCause() == null ? null : syncErrorResultItem.getCause().getMessage())
						)
				)
		);

		return dataSyncErrorResults;
	}

	public EventMessageFactory getEventMessageFactory() {
		return eventMessageFactory;
	}

	public void setEventMessageFactory(final EventMessageFactory eventMessageFactory) {
		this.eventMessageFactory = eventMessageFactory;
	}

	public ChangeSetManagementService getChangeSetManagementService() {
		return changeSetManagementService;
	}

	public void setChangeSetManagementService(final ChangeSetManagementService changeSetManagementService) {
		this.changeSetManagementService = changeSetManagementService;
	}

	public CmUserService getCmUserService() {
		return cmUserService;
	}

	public void setCmUserService(final CmUserService cmUserService) {
		this.cmUserService = cmUserService;
	}

	public BeanFactory getBeanFactory() {
		return beanFactory;
	}

	public void setBeanFactory(final BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}
}