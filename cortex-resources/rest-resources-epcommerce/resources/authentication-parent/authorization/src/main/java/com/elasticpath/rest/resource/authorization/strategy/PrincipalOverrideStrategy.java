/**
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.authorization.strategy;

import java.security.Principal;
import java.util.Collection;

import com.elasticpath.rest.id.ResourceIdentifier;

/**
 * Strategy for overriding principals when accessing specific resources.
 */
public interface PrincipalOverrideStrategy {

	/**
	 * Should this strategy handle overriding for the given resource?
	 * @param resourceName The name of the resource being accessed
	 * @return True if we should override.
	 */
	boolean shouldOverride(String resourceName);

	/**
	 * Override the principals for the resource.
	 * @param uri The URI of the resource being accessed.
	 * @param resourceIdentifier Identifier for the resource.
	 * @param principals Current principals to override.
	 * @return The overridden collection of principals.
	 */
	Collection<Principal> override(String uri, ResourceIdentifier resourceIdentifier, Collection<Principal> principals);
}
