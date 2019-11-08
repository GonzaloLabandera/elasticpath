/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.update.processor;

import java.util.Optional;

/**
 * Processes catalog update notifications.
 */
public interface CatalogUpdateProcessingService {

	/**
	 * <p>Retrieve the requested Capability from the service.</p>
	 *
	 * @param capabilityClass the class of the Capability being requested
	 * @param <T>             the particular Capability type being requested
	 * @return an instance of the requested Capability
	 */
	<T extends CatalogUpdateProcessorCapability> Optional<T> getCapability(Class<T> capabilityClass);

}
