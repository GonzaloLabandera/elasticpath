/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.carts.relationships;

import java.security.Principal;
import java.util.Collection;
import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.definition.carts.CartsFromRootRelationship;
import com.elasticpath.rest.definition.carts.CartsIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceService;
import com.elasticpath.rest.helix.data.annotation.UserScopes;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.resource.ResourceOperationContext;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.ShoppingCartRepository;

/**
 * Relationship to carts list from root.
 */
public class CartsFromRootRelationshipImpl implements CartsFromRootRelationship.LinkTo {

	private final Iterable<String> scopes;
	private static final String REGISTERED = "ROLE_REGISTERED";
	private final ResourceOperationContext resourceOperationContext;
	private final ShoppingCartRepository shoppingCartRepository;

	/**
	 * Constructor.
	 *  @param scopes               scopes
	 * @param resourceOperationContext resource operation context
	 * @param shoppingCartRepository the shopping cart repository.
	 */
	@Inject
	public CartsFromRootRelationshipImpl(@UserScopes final Iterable<String> scopes,
										 @ResourceService final ResourceOperationContext resourceOperationContext,
										 @ResourceService final ShoppingCartRepository shoppingCartRepository) {
		this.scopes = scopes;
		this.resourceOperationContext = resourceOperationContext;
		this.shoppingCartRepository = shoppingCartRepository;
	}

	@Override
	public Observable<CartsIdentifier> onLinkTo() {
		// Only registered users can see a link from ROOT to carts
		if (isRegisteredUser()) {
			return Observable.fromIterable(scopes)
					.filter(shoppingCartRepository::canCreateCart)
					.map(StringIdentifier::of)
					.map(scopeId -> CartsIdentifier.builder().withScope(scopeId).build())
					.firstElement()
					.toObservable();
		}
		return Observable.empty();
	}

	private boolean isRegisteredUser() {
		Collection<Principal> headers = resourceOperationContext.getSubject().getPrincipals();

		// Find if any principal matches registered role
		return headers.stream().anyMatch(principal -> principal.getName().equalsIgnoreCase(REGISTERED));
	}
}
