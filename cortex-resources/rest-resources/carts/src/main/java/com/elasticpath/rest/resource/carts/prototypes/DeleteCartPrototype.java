/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.carts.prototypes;

import javax.inject.Inject;

import io.reactivex.Completable;

import com.elasticpath.repository.Repository;
import com.elasticpath.rest.definition.carts.CartEntity;
import com.elasticpath.rest.definition.carts.CartIdentifier;
import com.elasticpath.rest.definition.carts.CartResource;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;

/**
 * Carts prototype for delete operation.
 */
public class DeleteCartPrototype implements CartResource.Delete {

    private final CartIdentifier cartIdentifier;
    private final Repository<CartEntity, CartIdentifier> repository;

    /**
     * Constructor.
     *
     * @param cartIdentifier cart identifier
     * @param repository     repository
     */
    @Inject
    public DeleteCartPrototype(@RequestIdentifier final CartIdentifier cartIdentifier,
                               @ResourceRepository final Repository<CartEntity, CartIdentifier> repository) {
        this.cartIdentifier = cartIdentifier;
        this.repository = repository;
    }

    @Override
    public Completable onDelete() {
        return repository.delete(cartIdentifier);
    }
}
