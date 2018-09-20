/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.purchase.repositories.impl.options;

import com.google.common.collect.ImmutableSortedMap;
import io.reactivex.Observable;
import io.reactivex.Single;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.repository.LinksRepository;
import com.elasticpath.rest.definition.purchases.PurchaseLineItemIdentifier;
import com.elasticpath.rest.definition.purchases.PurchaseLineItemOptionsIdentifier;
import com.elasticpath.rest.id.type.PathIdentifier;
import com.elasticpath.rest.id.util.CompositeIdUtil;
import com.elasticpath.rest.resource.integration.epcommerce.repository.item.ItemRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.sku.ProductSkuRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.transform.ReactiveAdapter;

/**
 * Repository that returns options for the given line item.
 *
 * @param <I> line item identifier
 * @param <O> line item options identifier
 */
@Component
public class LineItemToOptionsRepositoryImpl<I extends PurchaseLineItemIdentifier, O extends PurchaseLineItemOptionsIdentifier>
		implements LinksRepository<PurchaseLineItemIdentifier, PurchaseLineItemOptionsIdentifier> {

	private ItemRepository itemRepository;
	private ReactiveAdapter reactiveAdapter;

	private ProductSkuRepository productSkuRepository;

	@Override
	public Observable<PurchaseLineItemOptionsIdentifier> getElements(final PurchaseLineItemIdentifier identifier) {
		String lineItemId = ((PathIdentifier) identifier.getLineItemId()).extractLeafId(); //which is item guid

		return productSkuRepository.getProductSkuWithAttributesByGuidAsSingle(lineItemId)
				.map(ProductSku::getSkuCode)
				.map(this::encodeItemId)
				.flatMap(this::doesSkuContainOptions)
				.flatMapObservable(containsOptions -> {
					if (containsOptions) {
						return Observable.just(PurchaseLineItemOptionsIdentifier.builder()
								.withPurchaseLineItem(identifier)
								.build());
					}

					return Observable.empty();
				});
	}

	private String encodeItemId(final String skuCode) {
		return CompositeIdUtil.encodeCompositeId(
				ImmutableSortedMap.of(ItemRepository.SKU_CODE_KEY, skuCode)
		);
	}

	/**
	 * Checks if item has options.
	 *
	 * @param skuCode skuCode
	 * @return true any options are present for this item
	 */
	protected Single<Boolean> doesSkuContainOptions(final String skuCode) {
		return reactiveAdapter.fromRepository(() -> itemRepository.getSkuOptionsForItemId(skuCode))
				.isEmpty()
				.map(empty -> !empty);
	}

	@Reference
	public void setItemRepository(final ItemRepository itemRepository) {
		this.itemRepository = itemRepository;
	}

	@Reference
	public void setReactiveAdapter(final ReactiveAdapter reactiveAdapter) {
		this.reactiveAdapter = reactiveAdapter;
	}

	@Reference
	public void setProductSkuRepository(final ProductSkuRepository productSkuRepository) {
		this.productSkuRepository = productSkuRepository;
	}

}
