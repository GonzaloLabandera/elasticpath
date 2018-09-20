/*
 * Copyright © 2016 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.wishlist.repositories;

import static com.elasticpath.rest.resource.integration.epcommerce.repository.wishlist.WishlistIdentifierUtil.buildWishlistIdentifier;

import io.reactivex.Observable;
import io.reactivex.Single;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elasticpath.repository.Repository;
import com.elasticpath.rest.definition.wishlists.WishlistEntity;
import com.elasticpath.rest.definition.wishlists.WishlistIdentifier;
import com.elasticpath.rest.id.IdentifierPart;
import com.elasticpath.rest.resource.ResourceOperationContext;
import com.elasticpath.rest.resource.integration.epcommerce.repository.wishlist.WishlistRepository;

/**
 * Repository for wishlists.
 *
 * @param <E> the entity type
 * @param <I> the identifier type
 */
@Component
public class WishlistEntityRepositoryImpl<E extends WishlistEntity, I extends WishlistIdentifier>
		implements Repository<WishlistEntity, WishlistIdentifier> {

	private WishlistRepository wishlistRepository;

	private ResourceOperationContext resourceOperationContext;

	@Override
	public Single<WishlistEntity> findOne(final WishlistIdentifier identifier) {
		String wishlistId = identifier.getWishlistId().getValue();
		return wishlistRepository.getWishlist(wishlistId)
				.map(wishList -> WishlistEntity.builder()
						.withWishlistId(wishList.getGuid())
						.build());
	}

	@Override
	public Observable<WishlistIdentifier> findAll(final IdentifierPart<String> scope) {
		String customerGuid = resourceOperationContext.getUserIdentifier();
		return wishlistRepository.getWishlistIds(customerGuid, scope.getValue())
				.map(wishlistId -> buildWishlistIdentifier(scope.getValue(), wishlistId));
	}

	@Reference
	public void setWishlistRepository(final WishlistRepository wishlistRepository) {
		this.wishlistRepository = wishlistRepository;
	}

	@Reference
	public void setResourceOperationContext(final ResourceOperationContext resourceOperationContext) {
		this.resourceOperationContext = resourceOperationContext;
	}
}
