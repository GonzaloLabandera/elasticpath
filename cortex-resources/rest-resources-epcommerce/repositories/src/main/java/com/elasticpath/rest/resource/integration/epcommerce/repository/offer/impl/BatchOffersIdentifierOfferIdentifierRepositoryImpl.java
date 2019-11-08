/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.offer.impl;

import static com.elasticpath.rest.resource.integration.epcommerce.repository.search.impl.SearchRepositoryImpl.PRODUCT_GUID_KEY;

import com.google.common.collect.ImmutableMap;
import io.reactivex.Observable;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elasticpath.domain.catalog.Product;
import com.elasticpath.repository.LinksRepository;
import com.elasticpath.rest.definition.offers.BatchOffersIdentifier;
import com.elasticpath.rest.definition.offers.OfferIdentifier;
import com.elasticpath.rest.id.IdentifierPart;
import com.elasticpath.rest.id.type.CompositeIdentifier;
import com.elasticpath.rest.resource.integration.epcommerce.repository.product.StoreProductRepository;

/**
 * Repository that retrieves all offers given product codes.
 * @param <LI> extends BatchOffersIdentifier
 * @param <I> extends OfferIdentifier
 */
@Component
public class BatchOffersIdentifierOfferIdentifierRepositoryImpl<LI extends BatchOffersIdentifier, I extends OfferIdentifier>
		implements LinksRepository<BatchOffersIdentifier, OfferIdentifier> {

	private StoreProductRepository storeProductRepository;

	private static final Logger LOG = LoggerFactory.getLogger(BatchOffersIdentifierOfferIdentifierRepositoryImpl.class);

	@Override
	public Observable<OfferIdentifier> getElements(final BatchOffersIdentifier identifier) {
		IdentifierPart<String> storeCode = identifier.getBatchOffersLookupForm().getScope();
		return Observable.fromIterable(identifier.getBatchId().getValue())
				.flatMapSingle(productCode ->
						storeProductRepository.findDisplayableStoreProductWithAttributesByProductGuid(storeCode.getValue(), productCode)
								.doOnError(throwable -> LOG.info("There was a problem finding product code '{}'.", productCode)), true)
				.map(storeProduct -> buildOfferIdentifier(storeProduct, storeCode))
				.onErrorResumeNext(Observable.empty());
	}

	private OfferIdentifier buildOfferIdentifier(final Product product, final IdentifierPart<String> scope) {
		return OfferIdentifier.builder()
				.withOfferId(CompositeIdentifier.of(ImmutableMap.of(PRODUCT_GUID_KEY, product.getGuid())))
				.withScope(scope)
				.build();
	}

	@Reference
	public void setStoreProductRepository(final StoreProductRepository storeProductRepository) {
		this.storeProductRepository = storeProductRepository;
	}
}
