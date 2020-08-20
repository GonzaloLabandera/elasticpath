/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.tools.sync.dstmessagelistener.messages.impl;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableSet;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.elasticpath.domain.changeset.ChangeSet;
import com.elasticpath.tools.sync.dstmessagelistener.messages.ChangeSetSummaryMessage;
import com.elasticpath.tools.sync.target.result.Summary;
import com.elasticpath.tools.sync.target.result.SyncErrorResultItem;
import com.elasticpath.tools.sync.target.result.SyncResultItem;

/**
 * Represents a changeset publishing summary message.
 */
public class ChangeSetSummaryMessageImpl implements ChangeSetSummaryMessage {

	private static final int ONE_SECOND_IN_MILLISECONDS = 1000;
	private static final int ONE_DAY_IN_HOURS = 24;
	private static final int ONE_MINUTE_IN_SECONDS = 60;
	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

	private final String changeSetGuid;
	private final boolean wasSuccess;
	private final Collection<SyncResultItem> syncSuccessResults;
	private final Collection<SyncErrorResultItem> syncErrorResults;
	private final String publishSummary;

	private final String changeSetName;
	private final String changeSetDescription;
	private Long startTime;
	private Long endTime;

	/**
	 * Constructor.
	 *
	 * @param changeSet    change set that was published
	 * @param errorMessage an error message describing why the publish attempt could not take place
	 */
	public ChangeSetSummaryMessageImpl(final ChangeSet changeSet, final String errorMessage) {

		this.changeSetGuid = changeSet.getGuid();
		this.changeSetName = changeSet.getName();
		this.changeSetDescription = changeSet.getDescription();
		this.wasSuccess = false;
		this.publishSummary = errorMessage;
		this.syncSuccessResults = Collections.emptySet();
		this.syncErrorResults = Collections.emptySet();
	}

	/**
	 * Constructor.
	 *
	 * @param changeSet          the GUID of the change set that was published
	 * @param summary            the result summary
	 * @param syncSuccessResults a list of success results during synchronization
	 * @param syncErrorResults   a list of errors occurred during synchronization
	 */
	public ChangeSetSummaryMessageImpl(final ChangeSet changeSet,
									   final Summary summary,
									   final Collection<SyncResultItem> syncSuccessResults,
									   final Collection<SyncErrorResultItem> syncErrorResults) {

		this.changeSetGuid = changeSet.getGuid();
		this.startTime = summary.getStartTime();
		this.endTime = summary.getEndTime();
		this.changeSetDescription = changeSet.getDescription();
		this.changeSetName = changeSet.getName();

		this.syncSuccessResults = ImmutableSet.copyOf(syncSuccessResults);
		this.syncErrorResults = ImmutableSet.copyOf(syncErrorResults);

		wasSuccess = this.syncErrorResults.isEmpty();

		this.publishSummary = createPublishSummaryFormat();
	}

	private String createPublishSummaryFormat() {

		Map<String, Integer> successMap = getMapOfResultCounts(syncSuccessResults);

		Map<String, Integer> failMap = getMapOfResultCounts(syncErrorResults);

		return getFormattedResultText(successMap, failMap);
	}

	private Map<String, Integer> getMapOfResultCounts(final Collection<? extends SyncResultItem> resultsCollection) {
		Map<String, Integer> resultsMap = new HashMap<>();
		resultsCollection.stream()
				.filter(syncResultItem -> syncResultItem.getJobEntryType() != null)
				.forEach(syncResultItem -> {
					String key = syncResultItem.getJobEntryType().getSimpleName();
					Integer count = resultsMap.get(key) == null ? 1 : (resultsMap.get(key) + 1);
					resultsMap.put(key, count);
				});
		return resultsMap;
	}

	private String getFormattedResultText(final Map<String, Integer> successMap, final Map<String, Integer> failMap) {
		String time = getFormattedElapsedTime();

		int totalObjects = syncErrorResults.size() + syncSuccessResults.size();

		LocalDateTime stTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(startTime), ZoneId.systemDefault());
		LocalDateTime edTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(endTime), ZoneId.systemDefault());

		StringBuilder resultText = new StringBuilder(
				String.format("<br/><br/>Change Set Publishing for change set with GUID [%s]."
				+ "<br/><br/>Change Set Name: %s"
				+ "<br/><br/>Change Set Description: %s"
				+ "<br/><br/>Start Time: %s"
				+ "<br/><br/>End Time: %s"
				+ "<br/><br/>Elapsed Time: %s"
				+ "<br/><br/>Total Number of Objects: %s"
				+ "<br/>",
				changeSetGuid,
				changeSetName,
				changeSetDescription,
				stTime.format(DATE_TIME_FORMATTER),
				edTime.format(DATE_TIME_FORMATTER),
				time,
				totalObjects));

		addCountsToResultsMessage(successMap, resultText, "<br/>Successful Objects:<br/>");

		addCountsToResultsMessage(failMap, resultText, "<br/>Failed Objects:<br/>");

		return resultText.toString();
	}

	private void addCountsToResultsMessage(final Map<String, Integer> mapOfCounts, final StringBuilder resultText,
			final String sectionTitleMessage) {
		if (!mapOfCounts.isEmpty()) {
			resultText.append(sectionTitleMessage);

			for (Map.Entry<String, Integer> entry : mapOfCounts.entrySet()) {
				resultText.append(entry.getKey()).append(": ").append(entry.getValue()).append("<br/>");

			}
		}
	}

	private String getFormattedElapsedTime() {
		long elapsedTime = endTime - startTime;
		long millis = TimeUnit.NANOSECONDS.toMillis(elapsedTime) % ONE_SECOND_IN_MILLISECONDS;
		long hour = TimeUnit.NANOSECONDS.toHours(elapsedTime) % ONE_DAY_IN_HOURS;
		long minute = TimeUnit.NANOSECONDS.toMinutes(elapsedTime) % ONE_MINUTE_IN_SECONDS;
		long second = TimeUnit.NANOSECONDS.toSeconds(elapsedTime) % ONE_MINUTE_IN_SECONDS;

		return String.format("%02d:%02d:%02d.%d", hour, minute, second, millis);
	}

	@Override
	public String getChangeSetGuid() {
		return changeSetGuid;
	}

	@Override
	public String getPublishSummary() {
		return publishSummary;
	}

	@Override
	public boolean isSuccess() {
		return wasSuccess;
	}

	@Override
	public Collection<SyncResultItem> getSyncSuccessResults() {
		return syncSuccessResults;
	}

	@Override
	public Collection<SyncErrorResultItem> getSyncErrorResults() {
		return syncErrorResults;
	}

	@Override
	public Collection<SyncResultItem> getAllResults() {
		return Stream.concat(getSyncSuccessResults().stream(), getSyncErrorResults().stream())
				.collect(Collectors.toSet());
	}

	@Override
	public boolean equals(final Object other) {
		if (this == other) {
			return true;
		}
		if (other == null || getClass() != other.getClass()) {
			return false;
		}

		final ChangeSetSummaryMessageImpl that = (ChangeSetSummaryMessageImpl) other;

		return wasSuccess == that.wasSuccess
				&& Objects.equals(changeSetGuid, that.changeSetGuid)
				&& Objects.equals(syncSuccessResults, that.syncSuccessResults)
				&& Objects.equals(syncErrorResults, that.syncErrorResults);
	}

	@Override
	public int hashCode() {
		return Objects.hash(
				changeSetGuid,
				wasSuccess,
				syncSuccessResults,
				syncErrorResults
		);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

}