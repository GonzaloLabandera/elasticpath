/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.offer.impl;

import java.util.Map;

import io.reactivex.Observable;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elasticpath.repository.LinksRepository;
import com.elasticpath.rest.definition.items.ItemIdIdentifierPart;
import com.elasticpath.rest.definition.items.ItemIdentifier;
import com.elasticpath.rest.definition.offers.OfferItemsIdentifier;
import com.elasticpath.rest.resource.integration.epcommerce.repository.item.ItemRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.product.StoreProductRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.search.impl.SearchRepositoryImpl;

/**
 * Repository that implements reading offer items for an offer.
 *
 * @param <I>  extends OfferItemsIdentifier
 * @param <IE> extends ItemIdentifier
 */
@Component
public class OfferItemLinksRepositoryImpl<I extends OfferItemsIdentifier, IE extends ItemIdentifier>
		implements LinksRepository<OfferItemsIdentifier, ItemIdentifier> {

	private StoreProductRepository storeProductRepository;

	@Override
	public Observable<ItemIdentifier> getElements(final OfferItemsIdentifier identifier) {
		final Map<String, String> offerIdMap = identifier.getOffer().getOfferId().getValue();
		final String productCode = offerIdMap.get(SearchRepositoryImpl.PRODUCT_GUID_KEY);
		final String scope = identifier.getOffer().getScope().getValue();
		return storeProductRepository.findDisplayableStoreProductWithAttributesByProductGuid(scope, productCode)
				.flatMapObservable(product -> Observable.fromIterable(product.getProductSkus().keySet()))
				.map(offerId -> getOfferItem(offerId, identifier));
	}

	private ItemIdentifier getOfferItem(final String value, final OfferItemsIdentifier identifier) {
		return ItemIdentifier.builder()
				.withItemId(ItemIdIdentifierPart.of(ItemRepository.SKU_CODE_KEY, value))
				.withScope(identifier.getOffer().getScope())
				.build();
	}

	@Reference
	public void setStoreProductRepository(final StoreProductRepository storeProductRepository) {
		this.storeProductRepository = storeProductRepository;
	}
}
