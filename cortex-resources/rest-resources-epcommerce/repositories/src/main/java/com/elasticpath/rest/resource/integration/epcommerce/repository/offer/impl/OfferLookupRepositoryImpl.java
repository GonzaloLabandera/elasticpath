/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.offer.impl;

import static com.elasticpath.rest.resource.integration.epcommerce.repository.search.OfferSearchUtil.buildOfferIdentifier;

import io.reactivex.Single;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elasticpath.repository.Repository;
import com.elasticpath.rest.definition.offers.CodeEntity;
import com.elasticpath.rest.definition.offers.OfferIdentifier;
import com.elasticpath.rest.form.SubmitResult;
import com.elasticpath.rest.form.SubmitStatus;
import com.elasticpath.rest.id.IdentifierPart;
import com.elasticpath.rest.resource.integration.epcommerce.repository.product.StoreProductRepository;

/**
 * Repository for offer lookup.
 * @param <E> extends CodeEntity
 * @param <IE> extends OfferIdentifier
 */
@Component
public class OfferLookupRepositoryImpl<E extends CodeEntity, IE extends OfferIdentifier> implements Repository<CodeEntity, OfferIdentifier> {

	private StoreProductRepository storeProductRepository;

	@Override
	public Single<SubmitResult<OfferIdentifier>> submit(final CodeEntity entity, final IdentifierPart<String> scope) {
		return storeProductRepository.findDisplayableStoreProductWithAttributesByProductGuid(scope.getValue(), entity.getCode())
				.map(product -> SubmitResult.<OfferIdentifier>builder()
						.withStatus(SubmitStatus.CREATED)
						.withIdentifier(buildOfferIdentifier(product, scope))
						.build());
	}

	@Reference
	public void setStoreProductRepository(final StoreProductRepository storeProductRepository) {
		this.storeProductRepository = storeProductRepository;
	}
}
