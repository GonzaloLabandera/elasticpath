/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.purchase.repositories.impl.options;

import com.google.common.annotations.VisibleForTesting;
import io.reactivex.Observable;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elasticpath.repository.LinksRepository;
import com.elasticpath.rest.ResourceOperationFailure;
import com.elasticpath.rest.definition.purchases.PurchaseLineItemOptionIdentifier;
import com.elasticpath.rest.definition.purchases.PurchaseLineItemOptionsIdentifier;
import com.elasticpath.rest.id.type.PathIdentifier;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.resource.integration.epcommerce.repository.sku.ProductSkuRepository;

/**
 * This repository returns a lit of options given `options` identifier.
 *
 * @param <OSI> options identifier
 * @param <OI> option identifier
 */
@Component
public class OptionsEntityRepositoryImpl<OSI extends PurchaseLineItemOptionsIdentifier, OI extends PurchaseLineItemOptionIdentifier>
		implements LinksRepository<PurchaseLineItemOptionsIdentifier, PurchaseLineItemOptionIdentifier> {

	/**
	 * Error for not found options.
	 */
	@VisibleForTesting
	static final String OPTIONS_NOT_FOUND = "No options found for line item.";

	private ProductSkuRepository productSkuRepository;

	@Override
	public Observable<PurchaseLineItemOptionIdentifier> getElements(final PurchaseLineItemOptionsIdentifier identifier) {
		String lineItemId = ((PathIdentifier) identifier.getPurchaseLineItem().getLineItemId()).extractLeafId();

		return productSkuRepository.getProductSkuWithAttributesByGuidAsSingle(lineItemId)
				.flatMapObservable(productSku -> Observable.fromIterable(productSku.getOptionValueCodes()))
				.map(optionId -> buildPurchaseLineItemOptionIdentifier(identifier, optionId))
				.switchIfEmpty(Observable.error(ResourceOperationFailure.notFound(OPTIONS_NOT_FOUND)));
	}

	/**
	 * Build identifier.
	 *
	 * @param identifier options identifier
	 * @param optionId option id
	 * @return option identifier
	 */
	protected PurchaseLineItemOptionIdentifier buildPurchaseLineItemOptionIdentifier(
			final PurchaseLineItemOptionsIdentifier identifier, final String optionId) {

		return PurchaseLineItemOptionIdentifier.builder()
				.withOptionId(StringIdentifier.of(optionId))
				.withPurchaseLineItemOptions(identifier)
				.build();
	}

	@Reference
	public void setProductSkuRepository(final ProductSkuRepository productSkuRepository) {
		this.productSkuRepository = productSkuRepository;
	}

}
