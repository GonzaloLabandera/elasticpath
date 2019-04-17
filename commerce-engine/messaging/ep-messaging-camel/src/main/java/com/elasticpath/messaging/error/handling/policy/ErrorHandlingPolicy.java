/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.messaging.error.handling.policy;

import org.apache.camel.builder.RouteBuilder;

/**
 * A reusable representation of an error handling policy for use in {@link RouteBuilder} classes.
 */
public interface ErrorHandlingPolicy {

	/**
	 * Configures a given {@link RouteBuilder} with an error handling policy.
	 *
	 * @param routeBuilder the route builder to configure with an error handling policy
	 */
	void configureErrorHandlingPolicy(RouteBuilder routeBuilder);
}
