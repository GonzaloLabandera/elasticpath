/*
 * Copyright © 2021 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.wishlists.permissions;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.apache.shiro.subject.PrincipalCollection;

import com.elasticpath.repository.LinksRepository;
import com.elasticpath.repository.Repository;
import com.elasticpath.rest.authorization.parameter.AbstractCollectionValueStrategy;
import com.elasticpath.rest.definition.wishlists.WishlistEntity;
import com.elasticpath.rest.definition.wishlists.WishlistIdentifier;
import com.elasticpath.rest.definition.wishlists.WishlistLineItemIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;
import com.elasticpath.rest.id.Identifier;
import com.elasticpath.rest.id.transform.IdentifierTransformer;
import com.elasticpath.rest.id.transform.IdentifierTransformerProvider;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.identity.util.PrincipalsUtil;

/**
 * Strategy for resolving the wishlist line item ID parameter.
 */
@Singleton
@Named
public final class WishlistLineItemIdParameterStrategy extends AbstractCollectionValueStrategy {

	@Inject
	@ResourceRepository
	private Provider<LinksRepository<WishlistIdentifier, WishlistLineItemIdentifier>> lineItemRepository;

	@Inject
	@ResourceRepository
	private Provider<Repository<WishlistEntity, WishlistIdentifier>> wishlistRepository;

	@Inject
	private IdentifierTransformerProvider identifierTransformerProvider;

	@Override
	protected Collection<String> getParameterValues(final PrincipalCollection principals) {
		String scope = PrincipalsUtil.getScope(principals);
		final IdentifierTransformer<Identifier> identifierTransformer = identifierTransformerProvider.forUriPart(WishlistIdentifier.WISHLIST_ID);
		List<WishlistIdentifier> wishlistIdentifiers = wishlistRepository.get()
				.findAll(StringIdentifier.of(scope)).toList().blockingGet();

		return wishlistIdentifiers.stream()
				.map(wishlistIdentifier -> lineItemRepository.get().getElements(wishlistIdentifier)
						.toList().blockingGet())
				.flatMap(Collection::stream)
				.map(lineItemIdentifier ->
						identifierTransformer.identifierToUri(lineItemIdentifier.getLineItemId()))
				.collect(Collectors.toList());
	}
}
