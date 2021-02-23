/**
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.authorization;

import java.security.Principal;
import java.util.Collection;
import java.util.List;

import com.elasticpath.rest.authorization.PrincipalsOverride;
import com.elasticpath.rest.id.ResourceIdentifier;
import com.elasticpath.rest.resource.authorization.strategy.PrincipalOverrideStrategy;

/**
 * Implementation of PrincipalsOverride service.
 */
public class PrincipalsOverrideImpl implements PrincipalsOverride {

	private List<PrincipalOverrideStrategy> principalOverrideStrategyList;

	@Override
	public Collection<Principal> override(final String uri, final ResourceIdentifier resourceIdentifier,
									   final Collection<Principal> currentPrincipals) {
		return principalOverrideStrategyList.stream()
				.filter(strategy -> strategy.shouldOverride(resourceIdentifier.resourceName()))
				.findFirst()
				.map(strategy -> strategy.override(uri, resourceIdentifier, currentPrincipals))
				.orElse(currentPrincipals);
	}

	public List<PrincipalOverrideStrategy> getPrincipalOverrideStrategyList() {
		return principalOverrideStrategyList;
	}

	public void setPrincipalOverrideStrategyList(final List<PrincipalOverrideStrategy> principalOverrideStrategyList) {
		this.principalOverrideStrategyList = principalOverrideStrategyList;
	}

}
