/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.purchase.repositories.impl.components;

import static com.elasticpath.rest.resource.integration.epcommerce.repository.item.ItemRepository.SKU_CODE_KEY;

import java.util.Map;

import com.google.common.collect.ImmutableSortedMap;
import io.reactivex.Observable;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.repository.LinksRepository;
import com.elasticpath.rest.definition.purchases.PurchaseLineItemComponentsIdentifier;
import com.elasticpath.rest.definition.purchases.PurchaseLineItemIdentifier;
import com.elasticpath.rest.id.type.PathIdentifier;
import com.elasticpath.rest.id.util.CompositeIdUtil;
import com.elasticpath.rest.resource.integration.epcommerce.repository.item.ItemRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.sku.ProductSkuRepository;

/**
 * This repository returns a list of components given components identifier.
 * 
 * @param <CS> Components identifier
 * @param <C> Component identifier
 */
@Component
public class ComponentsEntityRepositoryImpl<CS extends PurchaseLineItemComponentsIdentifier, C extends PurchaseLineItemIdentifier>
		implements LinksRepository<PurchaseLineItemComponentsIdentifier, PurchaseLineItemIdentifier> {

	private ItemRepository itemRepository;
	private ProductSkuRepository productSkuRepository;

	@Override
	public Observable<PurchaseLineItemIdentifier> getElements(final PurchaseLineItemComponentsIdentifier identifier) {
		String lineItemId = ((PathIdentifier) identifier.getPurchaseLineItem().getLineItemId()).extractLeafId();

		return productSkuRepository.getProductSkuWithAttributesByGuidAsSingle(lineItemId)
				.map(ProductSku::getSkuCode)
				.map(this::encodeSkuCode)
				.flatMapObservable(this::getComponentsIds)
				.map(componentId -> createComponentLineItemIdentifier(identifier, componentId));
	}

	private String encodeSkuCode(final String skuCode) {
		Map<String, String> itemIdMap = ImmutableSortedMap.of(SKU_CODE_KEY, skuCode);
		return CompositeIdUtil.encodeCompositeId(itemIdMap);
	}

	/**
	 * Get component Ids given skuCode.
	 *
	 * @param skuCode skuCode
	 * @return observable of ids
	 */
	protected Observable<String> getComponentsIds(final String skuCode) {
		return itemRepository.getSkuForItemIdAsSingle(skuCode)
				.map(ProductSku::getProduct)
				.map(itemRepository::asProductBundle)
				.flatMapObservable(productBundle -> Observable.fromIterable(productBundle.getConstituents()))
				.map(bundleConstituents -> bundleConstituents.getConstituent().getProductSku().getGuid());
	}

	private PurchaseLineItemIdentifier createComponentLineItemIdentifier(
			final PurchaseLineItemComponentsIdentifier identifier, final String componentId) {

		return PurchaseLineItemIdentifier.builder()
				.withPurchaseLineItems(identifier.getPurchaseLineItem().getPurchaseLineItems())
				.withLineItemId(PathIdentifier.of(identifier.getPurchaseLineItem().getLineItemId(), componentId))
				.build();
	}

	@Reference
	public void setItemRepository(final ItemRepository itemRepository) {
		this.itemRepository = itemRepository;
	}

	@Reference
	public void setProductSkuRepository(final ProductSkuRepository productSkuRepository) {
		this.productSkuRepository = productSkuRepository;
	}

}
