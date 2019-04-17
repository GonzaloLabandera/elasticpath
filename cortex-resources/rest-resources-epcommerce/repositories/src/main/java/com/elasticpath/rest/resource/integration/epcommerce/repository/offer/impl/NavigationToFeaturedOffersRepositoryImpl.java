/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.offer.impl;

import io.reactivex.Observable;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elasticpath.repository.LinksRepository;
import com.elasticpath.rest.definition.navigations.NavigationIdentifier;
import com.elasticpath.rest.definition.offersearches.FeaturedOffersIdentifier;
import com.elasticpath.rest.id.IdentifierPart;
import com.elasticpath.rest.resource.integration.epcommerce.repository.category.CategoryRepository;

/**
 * Repository logic for displaying featured offers.
 * @param <I> extends NavigationIdentifier
 * @param <IE> extends FeaturedOffersIdentifier
 */
@Component
public class NavigationToFeaturedOffersRepositoryImpl<I extends NavigationIdentifier, IE extends FeaturedOffersIdentifier>
		implements LinksRepository<NavigationIdentifier, FeaturedOffersIdentifier> {

	private CategoryRepository categoryRepository;

	@Override
	public Observable<FeaturedOffersIdentifier> getElements(final NavigationIdentifier identifier) {
		IdentifierPart<String> scope = identifier.getNavigations().getScope();
		IdentifierPart<String> categoryCode = identifier.getNodeId();
		return categoryRepository.findByStoreAndCategoryCode(scope.getValue(), categoryCode.getValue())
				.flatMapObservable(category -> categoryRepository.getFeaturedProducts(category.getUidPk()))
				.map(product -> buildFeaturedOffersIdentifier(scope, categoryCode));
	}

	private FeaturedOffersIdentifier buildFeaturedOffersIdentifier(final IdentifierPart<String> scope,
																   final IdentifierPart<String> categoryCode) {
		return FeaturedOffersIdentifier.builder()
				.withCategoryId(categoryCode)
				.withScope(scope)
				.build();
	}

	@Reference
	public void setCategoryRepository(final CategoryRepository categoryRepository) {
		this.categoryRepository = categoryRepository;
	}
}
