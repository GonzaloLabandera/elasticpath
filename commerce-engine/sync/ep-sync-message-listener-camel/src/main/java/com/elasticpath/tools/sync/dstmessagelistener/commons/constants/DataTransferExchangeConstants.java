/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.tools.sync.dstmessagelistener.commons.constants;

/**
 * Constants which are set on exchange maps such as properties and headers.
 */
public final class DataTransferExchangeConstants {

	/**
	 * The constant used by properties and headers to get the requested changeset name to use from an exchange.
	 */
	public static final String CHANGE_SET_GUID_KEY = "changeSetGuid";

	/**
	 * The constant used by properties and headers to get the changeset guid from an exchange.
	 */
	public static final String CHANGE_SET_NAME_KEY = "changeSetName";

	/**
	 * The constant used by properties to get the changeset creator profile data.
	 */
	public static final String CHANGE_SET_CREATOR_KEY = "changeSetCreator";

	/**
	 * The constant used by properties to get the changeset publish initiator profile data.
	 */
	public static final String CHANGE_SET_PUBLISH_INITIATOR_KEY = "changeSetPublishInitiator";

	/**
	 * The constant used by event messages to store the changeset publishing result objects.
	 */
	public static final String SYNC_RESULTS_KEY = "syncResults";

	/**
	 * The constant used by event messages to store the changeset publishing summary.
	 */
	public static final String PUBLISH_SUMMARY_KEY = "publishSummary";

	/**
	 * The constant used by event messages to store the changeset success details inside the Sync Results collection.
	 */
	public static final String SYNC_SUCCESS_DETAILS_KEY = "syncSuccessDetails";

	/**
	 * The constant used by event messages to store the changeset failure details inside the Sync Results collection.
	 */
	public static final String SYNC_FAILURE_DETAILS_KEY = "syncFailureDetails";


	/**
	 * A private constructor to hide the public one.
	 */
	private DataTransferExchangeConstants() {
	}

}
