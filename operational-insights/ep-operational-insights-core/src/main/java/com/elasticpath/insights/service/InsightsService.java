/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.insights.service;

import java.util.Set;

import org.json.JSONObject;

/**
 * Service for generating a JSON report of operational insights into an Elastic Path Commerce environment.
 */
public interface InsightsService {
	/**
	 * Generate a JSON report of operational insights.
	 *
	 * @param zooms a set containing the top-level nodes that should be included in the report. If empty, all nodes will be included.
	 * @param configurationEpServiceReports a set of JSONObjects containing configuration details from all running EP services
	 * @param runtimeEpServiceReports a set of JSONObjects containing runtime details from all running EP services
	 * @return a string representation of the JSON report of operational insights
	 */
	String getReport(Set<String> zooms, Set<JSONObject> configurationEpServiceReports, Set<JSONObject> runtimeEpServiceReports);

	/**
	 * Generate a JSON report of service configuration details for this running service.
	 *
	 * @return a string representation of the JSON report of service configuration details
	 */
	String getThisEpServiceConfiguration();

	/**
	 * Generate a JSON report of service runtime details for this running service.
	 *
	 * @return a string representation of the JSON report of service runtime details
	 */
	String getThisEpServiceRuntime();
}
