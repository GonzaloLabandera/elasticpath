/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.purchase.repositories.impl.components;

import io.reactivex.Observable;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elasticpath.domain.catalog.ProductBundle;
import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.repository.LinksRepository;
import com.elasticpath.rest.definition.purchases.PurchaseLineItemComponentsIdentifier;
import com.elasticpath.rest.definition.purchases.PurchaseLineItemIdentifier;
import com.elasticpath.rest.id.type.PathIdentifier;
import com.elasticpath.rest.resource.integration.epcommerce.repository.sku.ProductSkuRepository;

/**
 * Repository that returns components given line item.
 *
 * @param <LI> line item identifier
 * @param <LCI> line item components identifier
 */
@Component
public class LineItemToComponentsRepositoryImpl<LI extends PurchaseLineItemIdentifier, LCI extends PurchaseLineItemComponentsIdentifier>
		implements LinksRepository<PurchaseLineItemIdentifier, PurchaseLineItemComponentsIdentifier> {

	private ProductSkuRepository productSkuRepository;

	@Override
	public Observable<PurchaseLineItemComponentsIdentifier> getElements(final PurchaseLineItemIdentifier identifier) {
		String lineItemId = ((PathIdentifier) identifier.getLineItemId()).extractLeafId(); //which is an item guid

		return productSkuRepository.getProductSkuWithAttributesByGuidAsSingle(lineItemId)
				.map(ProductSku::getProduct)
				.map(product -> product instanceof ProductBundle)
				.flatMapObservable(isBundle -> {
					if (isBundle) {
						return Observable.just(PurchaseLineItemComponentsIdentifier.builder()
								.withPurchaseLineItem(identifier)
								.build());
					}
					return Observable.empty();
				});
	}

	@Reference
	public void setProductSkuRepository(final ProductSkuRepository productSkuRepository) {
		this.productSkuRepository = productSkuRepository;
	}

}
