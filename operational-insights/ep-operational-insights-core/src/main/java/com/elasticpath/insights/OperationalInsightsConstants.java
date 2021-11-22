/**
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.insights;

/**
 * Operational Insights constants.
 */
public final class OperationalInsightsConstants {
	/**
	 * The query parameter and JMS header for the report ID parameter.
	 */
	public static final String HEADER_REPORT_ID = "reportId";

	/**
	 * The query parameter and JMS header for the zoom parameter.
	 */
	public static final String HEADER_ZOOM = "zoom";

	/**
	 * The configuration value for zooms.
	 */
	public static final String ZOOM_CONFIGURATION = "configuration";

	/**
	 * The runtime value for zooms.
	 */
	public static final String ZOOM_RUNTIME = "runtime";

	/**
	 * The data shape value for zooms.
	 */
	public static final String ZOOM_DATA_SHAPE = "data-shape";

	/**
	 * The revenue value for zooms.
	 */
	public static final String ZOOM_REVENUE = "revenue";

	/**
	 * Constructor.
	 */
	private OperationalInsightsConstants() {
		// Prevent instantiation.
	}

}
