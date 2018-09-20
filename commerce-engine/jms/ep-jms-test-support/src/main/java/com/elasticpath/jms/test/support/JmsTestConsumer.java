/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */

package com.elasticpath.jms.test.support;

import java.util.List;

import org.json.simple.JSONObject;

/**
 * JMS consumer test interface.
 */
public interface JmsTestConsumer {

	/**
	 * Reads all messages from a channel and returns them as a list of JSON objects.
	 * Uses the default read timeout configured in the properties file.
	 *
	 * @return the messages as a list of JSON objects
	 */
	List<JSONObject> read();

	/**
	 * Reads all messages from a channel and returns them as a list of JSON objects.
	 *
	 * @param readTimeout the time to wait in ms for the read to complete.
	 * @return the messages as a list of JSON objects
	 */
	List<JSONObject> read(long readTimeout);

	/**
	 * Deletes all messages from a channel.
	 */
	void drain();

}
