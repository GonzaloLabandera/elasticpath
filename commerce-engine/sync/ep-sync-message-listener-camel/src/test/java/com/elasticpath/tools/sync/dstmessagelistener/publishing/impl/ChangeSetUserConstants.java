/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.tools.sync.dstmessagelistener.publishing.impl;

import java.util.UUID;

/**
 * Constants used in multiple tests.
 */
public class ChangeSetUserConstants {

	/**
	 * The GUID of a Change Set.
	 */
	public static final String CHANGE_SET_GUID = UUID.randomUUID().toString();

	/**
	 * The name of a Change Set.
	 */
	public static final String CHANGE_SET_NAME = "This is my Change Set.  There are many like it, but this one is mine.";

	/**
	 * The GUID of the user that created a Change Set.
	 */
	public static final String CHANGE_SET_CREATOR_GUID = UUID.randomUUID().toString();

	/**
	 * The username of the  user that created a Change Set.
	 */
	public static final String CHANGE_SET_CREATOR_USERNAME = "Fred.CSR";

	/**
	 * The first name of the  user that created thae Change Set.
	 */
	public static final String CHANGE_SET_CREATOR_FIRST_NAME = "Fred";

	/**
	 * The last name of the  user that created a Change Set.
	 */
	public static final String CHANGE_SET_CREATOR_LAST_NAME = "Csrman";

	/**
	 * The email address of the  user that created a Change Set.
	 */
	public static final String CHANGE_SET_CREATOR_EMAIL_ADDRESS = "fred.csr@elasticpath.com";

	/**
	 * The GUID of the  user that initiated the publish of a Change Set.
	 */
	public static final String CHANGE_SET_PUBLISH_INITIATOR_GUID = UUID.randomUUID().toString();

	/**
	 * The username of the  user that initiated the publish of a Change Set.
	 */
	public static final String CHANGE_SET_PUBLISH_INITIATOR_USERNAME = "Jane.Manager";

	/**
	 * The first name of the  user that initiated the publish of a Change Set.
	 */
	public static final String CHANGE_SET_PUBLISH_INITIATOR_FIRST_NAME = "Jane";

	/**
	 * The last name of the  user that initiated the publish of a Change Set.
	 */
	public static final String CHANGE_SET_PUBLISH_INITIATOR_LAST_NAME = "Manager";

	/**
	 * The email address of the  user that initiated the publish of a Change Set.
	 */
	public static final String CHANGE_SET_PUBLISH_INITIATOR_EMAIL_ADDRESS = "jane.manager@elasticpath.com";

}
