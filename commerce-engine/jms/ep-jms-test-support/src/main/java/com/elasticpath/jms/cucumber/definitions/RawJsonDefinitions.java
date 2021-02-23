/**
 * Copyright (c) Elastic Path Software Inc., 2017
 */
package com.elasticpath.jms.cucumber.definitions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.util.ArrayList;
import java.util.List;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

import org.json.simple.JSONObject;

import com.elasticpath.jms.cucumber.asserts.EmailTestFacade;
import com.elasticpath.jms.cucumber.asserts.RawJsonTestFacade;
import com.elasticpath.jms.test.support.JmsChannelType;
import com.elasticpath.jms.test.support.JmsTestFactory;
import com.elasticpath.jms.utilities.KeyValue;

/**
 * Common Definitions for validating channel messages' content.
 */
@SuppressWarnings("checkstyle.*")
public class RawJsonDefinitions {

	private static final int MAX_MESSAGE_READ_ATTEMPTS_IN_WAIT = 5;

	private List<JSONObject> jsonObjectList;

	private final JmsTestFactory jmsTestFactory = JmsTestFactory.getInstance("ep-test-plugin.properties");

	private int messageReadAttempts;

	/**
	 * Reads messages from topic channel.
	 *
	 * @param channelName name of the channel to read messages from
	 */
	@When("^I read (.+) message from channel$")
	public void getTopicMessage(final String channelName) {
		jsonObjectList = jmsTestFactory.getConsumer(channelName, JmsChannelType.TOPIC).read();

		assertThat(jsonObjectList)
				.as("Json Object is empty")
				.isNotEmpty();
	}

	/**
	 * Reads messages from topic channel.
	 *
	 * @param queueName name of the channel to read messages from
	 */
	@When("^I read (.+) message from queue$")
	public void getQueueMessage(final String queueName) {
		jsonObjectList = jmsTestFactory.getConsumer(queueName, JmsChannelType.QUEUE).read();

		assertThat(jsonObjectList)
				.as("Json Object is empty")
				.isNotEmpty();
	}

	/**
	 * Reads messages from topic channel.
	 *
	 * @param queueName name of the channel to read messages from
	 */
	@When("^I read byte message from queue (.+)$")
	public void getByteQueueMessage(final String queueName) {
		jsonObjectList = jmsTestFactory.getConsumer(queueName, JmsChannelType.QUEUE).readByte();

		assertThat(jsonObjectList)
				.as("Json Object is empty")
				.isNotEmpty();
	}

	/**
	 * Verifies email value.
	 *
	 * @param key   the key
	 * @param value the value
	 */
	@Then("^email value for (.+) should be (.+)$")
	public void verifyEmailValue(final String key, final String value) {
		EmailTestFacade.assertEmailValue(jsonObjectList, key, value);
	}

	/**
	 * Verifies multiple json values.
	 *
	 * @param parent       parent of the key, value pair
	 * @param keyValueList key value list
	 */
	@Then("^json (.+) should contain following values$")
	public void verifyJsonValues(final String parent, final List<KeyValue> keyValueList) {
		for (KeyValue keyValue : keyValueList) {
			RawJsonTestFacade.verifyJsonValues(jsonObjectList, parent, keyValue.getKey(), keyValue.getValue());
		}
	}

	/**
	 * Verifies single json value.
	 *
	 * @param key   the key
	 * @param value the value
	 */
	@Then("^json (.+) value should be (.+)$")
	public void verifyJsonValues(final String key, final String value) {
		RawJsonTestFacade.verifyJsonValues(jsonObjectList, "", key, value);
	}

	/**
	 * Attempts to read the specified events from the specified channel.  This method will check for message delivery every {jms.read.timeout}
	 * seconds for a max of (MAX_MESSAGE_READ_ATTEMPTS_IN_WAIT * jms.read.timeout) seconds.  If the message cannot be found after this time,
	 * a failed assertion is triggered.
	 * The jms.read.timeout is configured in ep-test-plugin.properties.
	 *
	 * @param eventType the event type to look for in the message
	 * @param guid the guid of the event
	 * @param channelName name of the channel to read messages from
	 */
	@When("^I wait for the (.+) event type for guid (.+) from the (.+) channel$")
	public void waitForEventMessage(final String eventType, final String guid, final String channelName) {
		initJsonObjectList();

		readNewMessages(channelName);

		if (isEventForGuidMissing(eventType, guid)) {
			retryWaitForEventMessageOrFail(channelName, guid, eventType);
		}
	}

	private void retryWaitForEventMessageOrFail(final String channelName, final String guid, final String eventType) {
		if (messageReadAttempts < MAX_MESSAGE_READ_ATTEMPTS_IN_WAIT) {
			messageReadAttempts++;
			waitForEventMessage(eventType, guid, channelName);
		} else {
			jsonObjectList = null;
			messageReadAttempts = 0;
			fail("Unable to find event type [ " + eventType
					+ "] for guid [" + guid
					+ "] on channel [" + channelName + "]");
		}
	}

	private boolean isEventForGuidMissing(final String eventType, final String guid) {
		List<JSONObject> eventJsonObjects =  RawJsonTestFacade.getJsonObjectsByPathAndValue(jsonObjectList, "eventType.name", eventType);

		if (!eventJsonObjects.isEmpty()) {
			return RawJsonTestFacade.getJsonObjectByPathAndValue(eventJsonObjects, "guid", guid).isEmpty();
		}
		return true;
	}

	private void readNewMessages(final String channelName) {
		List<JSONObject> newMessages = jmsTestFactory.getConsumer(channelName, JmsChannelType.TOPIC).read();
		if (!newMessages.isEmpty()) {
			jsonObjectList.addAll(newMessages);
		}
	}

	private void initJsonObjectList() {
		if (jsonObjectList == null) {
			jsonObjectList = new ArrayList<>();
		}
	}

	public List<JSONObject> getJsonObjectList() {
		return jsonObjectList;
	}

}
