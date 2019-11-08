/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */

package com.elasticpath.rest.resource.integration.epcommerce.repository.offer.impl;

import io.reactivex.Observable;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.repository.LinksRepository;
import com.elasticpath.rest.definition.items.ItemIdentifier;
import com.elasticpath.rest.definition.offers.OfferIdIdentifierPart;
import com.elasticpath.rest.definition.offers.OfferIdentifier;
import com.elasticpath.rest.resource.integration.epcommerce.repository.item.ItemRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.search.impl.SearchRepositoryImpl;

/**
 * Repository that implements reading offer for an item.
 *
 * @param <I>  extends ItemIdentifier
 * @param <LI> extends OfferIdentifier
 */
@Component
public class OfferForItemLinksRepositoryImpl<I extends ItemIdentifier, LI extends OfferIdentifier>
		implements LinksRepository<ItemIdentifier, OfferIdentifier> {

	private ItemRepository itemRepository;

	@Override
	public Observable<OfferIdentifier> getElements(final ItemIdentifier itemIdentifier) {

		return itemRepository.getSkuForItemId(itemIdentifier.getItemId().getValue())
				.map(ProductSku::getProduct)
				.map(Product::getGuid)
				.map(productGuid -> buildOfferIdentifier(itemIdentifier, productGuid))
				.toObservable();

	}

	private OfferIdentifier buildOfferIdentifier(final ItemIdentifier identifier, final String productGuid) {
		return OfferIdentifier.builder()
				.withScope(identifier.getScope())
				.withOfferId(OfferIdIdentifierPart.of(SearchRepositoryImpl.PRODUCT_GUID_KEY, productGuid))
				.build();
	}

	@Reference
	public void setItemRepository(final ItemRepository itemRepository) {
		this.itemRepository = itemRepository;
	}
}
