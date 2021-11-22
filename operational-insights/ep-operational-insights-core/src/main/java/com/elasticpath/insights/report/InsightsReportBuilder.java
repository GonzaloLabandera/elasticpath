/**
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.insights.report;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wrapper for a JSON object constructor.
 */
public class InsightsReportBuilder {

	private static final Logger LOG = LoggerFactory.getLogger(InsightsReportBuilder.class);
	private static final int JSON_INDENT_FACTOR = 2;
	private final JSONObject jsonObject = new JSONObject();
	private final long creationTime;
	private boolean timer;

	/**
	 * Constructor.
	 */
	public InsightsReportBuilder() {
		creationTime = System.currentTimeMillis();
	}

	/**
	 * Track the amount of time that passes between this class being instantiated
	 * and one of the build methods being invoked, and record in the object in a field
	 * named "report-section-time-ms".
	 * @return the current object
	 */
	public InsightsReportBuilder withTimer() {
		timer = true;
		return this;
	}

	/**
	 * Add a node to the JSON object that contains the passed report builder contents.
	 * @param key the node name
	 * @param section the report builder to populate within the node
	 * @return the current object
	 */
	public InsightsReportBuilder appendNode(final String key, final InsightsReportBuilder section) {
		jsonObject.put(key, section.buildJSON());
		return this;
	}

	/**
	 * Add a array to the JSON object that contains the passed report builder contents.
	 * @param key the node name
	 * @param sectionList list of report builders sections.
	 * @return the current object
	 */
	public InsightsReportBuilder appendNodeArray(final String key, final List<InsightsReportBuilder> sectionList) {
		JSONArray jsonArray = new JSONArray();
		sectionList.forEach(section ->  jsonArray.put(section.buildJSON()));
		jsonObject.put(key, jsonArray);
		return this;
	}

	/**
	 * Add a node to the JSON object that contains the value provided by the passed supplier.
	 * @param key the node name
	 * @param func the supplier function
	 * @param <T> the value type
	 * @return the current object
	 */
	public <T extends Object> InsightsReportBuilder appendNode(final String key, final Supplier<T> func) {
		try {
			T value = func.get();
			appendNode(key, value);
		} catch (Exception exception) {
			LOG.error("Unable to execute supplier.", exception);
		}
		return this;
	}

	private <T extends Object> InsightsReportBuilder appendNode(final String key, final T value) {
		jsonObject.put(key, value);
		return this;
	}

	/**
	 * Add a node to the JSON object that contains an array of values.
	 * @param key the node name
	 * @param values the array values
	 * @param <T> the type of the array values
	 * @return the current object
	 */
	public <T extends Object> InsightsReportBuilder appendArray(final String key, final Set<T> values) {
		if (values != null) {
			jsonObject.put(key, values);
		}
		return this;
	}

	/**
	 * Build the JSON object and return as a pretty-printed string.
	 * @return the JSON object as a string
	 */
	public String buildString() {
		return buildJSON().toString(JSON_INDENT_FACTOR);
	}

	/**
	 * Build the JSON object and return as a JSONObject.
	 * @return the JSON object
	 */
	public JSONObject buildJSON() {
		if (timer) {
			this.appendNode("report-section-time-ms", System.currentTimeMillis() - creationTime);
		}
		return jsonObject;
	}

}
