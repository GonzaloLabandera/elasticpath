/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.accounts.permissions;

import java.util.Collection;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.apache.shiro.subject.PrincipalCollection;

import com.elasticpath.repository.Repository;
import com.elasticpath.rest.authorization.parameter.AbstractCollectionValueStrategy;
import com.elasticpath.rest.definition.accounts.AssociateEntity;
import com.elasticpath.rest.definition.accounts.AssociateIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;
import com.elasticpath.rest.id.Identifier;
import com.elasticpath.rest.id.transform.IdentifierTransformer;
import com.elasticpath.rest.id.transform.IdentifierTransformerProvider;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.identity.util.PrincipalsUtil;

/**
 * Strategy to look up permission for account associate resource.
 */
@Singleton
@Named("associateIdPermissionParameterStrategy")
public class AssociateIdParameterStrategy extends AbstractCollectionValueStrategy {

	@Inject
	@ResourceRepository
	private Provider<Repository<AssociateEntity, AssociateIdentifier>> repository;

	@Inject
	private IdentifierTransformerProvider identifierTransformerProvider;

	@Override
	protected Collection<String> getParameterValues(final PrincipalCollection principals) {
		String scope = PrincipalsUtil.getScope(principals);
		final IdentifierTransformer<Identifier> identifierTransformer = identifierTransformerProvider.forUriPart(AssociateIdentifier.ASSOCIATE_ID);
		return repository.get()
				.findAll(StringIdentifier.of(scope))
				.map(associateIdentifier -> identifierTransformer.identifierToUri(associateIdentifier.getAssociateId()))
				.toList()
				.blockingGet();
	}
}
