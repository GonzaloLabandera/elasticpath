/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.tools.sync.dstmessagelistener.messages;

import java.util.Objects;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.elasticpath.tools.sync.job.Command;
import com.elasticpath.tools.sync.target.result.SyncResultItem;

/**
 * A error result from the publishing of a Change Set.
 */
public class DataSyncErrorResultItem extends SyncResultItem {
	
	private final String exceptionMessage;
	
	/**
	 * Constructor.
	 *
	 * @param jobEntryType the job entry type
	 * @param jobEntryGuid the job entry Guid
	 * @param transactionJobUnitName the transaction job unit name
	 * @param jobEntryCommand the job entry command
	 * @param exceptionMessage the exception message
	 */
	public DataSyncErrorResultItem(final Class<?> jobEntryType,
									   final String jobEntryGuid,
									   final String transactionJobUnitName,
									   final Command jobEntryCommand,
									   final String exceptionMessage) {
		
		super.setJobEntryType(jobEntryType);
		super.setJobEntryGuid(jobEntryGuid);
		super.setTransactionJobUnitName(transactionJobUnitName);
		super.setJobEntryCommand(jobEntryCommand);
		this.exceptionMessage = exceptionMessage;
	}

	/**
	 * The exception message.
	 * 
	 * @return exceptionMessage
	 */
	public String getExceptionMessage() {
		return exceptionMessage;
	}
	
	@Override
	public boolean equals(final Object other) {
		if (this == other) {
			return true;
		}
		if (other == null || getClass() != other.getClass()) {
			return false;
		}

		final DataSyncErrorResultItem that = (DataSyncErrorResultItem) other;

		return Objects.equals(getJobEntryType(), that.getJobEntryType())
				&& Objects.equals(getJobEntryGuid(), that.getJobEntryGuid())
				&& Objects.equals(getJobEntryCommand(), that.getJobEntryCommand())
				&& Objects.equals(exceptionMessage, that.exceptionMessage);
	}

	@Override
	public int hashCode() {
		return Objects.hash(
				getJobEntryType(),
				getJobEntryGuid(),
				getJobEntryCommand(),
				exceptionMessage
		);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

}
