/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.offer.impl;

import static com.elasticpath.rest.resource.integration.epcommerce.repository.search.OfferSearchUtil.buildOfferIdentifier;

import io.reactivex.Observable;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elasticpath.repository.LinksRepository;
import com.elasticpath.rest.definition.offers.OfferIdentifier;
import com.elasticpath.rest.definition.offersearches.FeaturedOffersIdentifier;
import com.elasticpath.rest.id.IdentifierPart;
import com.elasticpath.rest.resource.integration.epcommerce.repository.category.CategoryRepository;

/**
 * Retrieves the list of featured offers for a given category.
 * @param <I> The FeaturedOffersIdentifier.
 * @param <LI> THe OfferIdentifier.
 */
@Component
public class FeaturedOffersRepositoryImpl<I extends FeaturedOffersIdentifier, LI extends OfferIdentifier>
		implements LinksRepository<FeaturedOffersIdentifier, OfferIdentifier> {

	private CategoryRepository categoryRepository;

	@Override
	public Observable<OfferIdentifier> getElements(final FeaturedOffersIdentifier identifier) {
		IdentifierPart<String> scope = identifier.getScope();
		String categoryCode = identifier.getCategoryId().getValue();
		return categoryRepository.findByStoreAndCategoryCode(scope.getValue(), categoryCode)
				.flatMapObservable(category -> categoryRepository.getFeaturedProducts(category.getUidPk()))
				.map(product -> buildOfferIdentifier(product, scope));
	}

	@Reference
	public void setCategoryRepository(final CategoryRepository categoryRepository) {
		this.categoryRepository = categoryRepository;
	}
}
