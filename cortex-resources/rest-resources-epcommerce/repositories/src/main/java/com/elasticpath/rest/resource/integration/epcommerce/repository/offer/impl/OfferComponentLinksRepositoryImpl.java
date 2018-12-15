/*
 *  Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.offer.impl;

import java.util.Map;

import io.reactivex.Observable;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elasticpath.domain.catalog.BundleConstituent;
import com.elasticpath.repository.LinksRepository;
import com.elasticpath.rest.definition.items.ItemIdIdentifierPart;
import com.elasticpath.rest.definition.items.ItemIdentifier;
import com.elasticpath.rest.definition.offers.OfferComponentsIdentifier;
import com.elasticpath.rest.definition.offers.OfferIdentifier;
import com.elasticpath.rest.id.ResourceIdentifier;
import com.elasticpath.rest.id.type.CompositeIdentifier;
import com.elasticpath.rest.resource.integration.epcommerce.repository.item.ItemRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.product.StoreProductRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.search.impl.SearchRepositoryImpl;

/**
 * Repository that implements reading offer components for an offer.
 *
 * @param <I> extends OfferComponentsIdentifier
 * @param <LI> extends ResourceIdentifier
 */
@Component
public class OfferComponentLinksRepositoryImpl<I extends OfferComponentsIdentifier, LI extends ResourceIdentifier>
		implements LinksRepository<OfferComponentsIdentifier, ResourceIdentifier> {

	private StoreProductRepository storeProductRepository;
	private ItemRepository itemRepository;

	@Override
	public Observable<ResourceIdentifier> getElements(final OfferComponentsIdentifier identifier) {
		final Map<String, String> offerIdMap = identifier.getOffer().getOfferId().getValue();
		final String productCode = offerIdMap.get(SearchRepositoryImpl.PRODUCT_GUID_KEY);
		return storeProductRepository.findByGuid(productCode)
				.flatMap(itemRepository::asProductBundle)
				.flatMapObservable(productBundle -> Observable.fromIterable(productBundle.getConstituents()))
				.map(BundleConstituent::getConstituent)
				.map(constituent -> getOfferComponent(
						constituent.isProductSku() ? constituent.getProductSku().getSkuCode() : constituent.getProduct().getGuid(),
						identifier,
						constituent.isProductSku()));
	}

	private ResourceIdentifier getOfferComponent(final String componentId, final OfferComponentsIdentifier identifier, final boolean skuItem) {
		CompositeIdentifier itemId = ItemIdIdentifierPart.of(skuItem ? ItemRepository.SKU_CODE_KEY : SearchRepositoryImpl.PRODUCT_GUID_KEY,
				componentId);
		return skuItem
				? ItemIdentifier.builder().withItemId(itemId).withScope(identifier.getOffer().getScope()).build()
				: OfferIdentifier.builder().withOfferId(itemId).withScope(identifier.getOffer().getScope()).build();
	}

	@Reference
	public void setStoreProductRepository(final StoreProductRepository storeProductRepository) {
		this.storeProductRepository = storeProductRepository;
	}

	@Reference
	public void setItemRepository(final ItemRepository itemRepository) {
		this.itemRepository = itemRepository;
	}


}
