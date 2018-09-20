/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.purchase.repositories.impl.options;

import com.google.common.annotations.VisibleForTesting;
import io.reactivex.Observable;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.domain.skuconfiguration.SkuOptionValue;
import com.elasticpath.repository.LinksRepository;
import com.elasticpath.rest.definition.purchases.PurchaseLineItemIdentifier;
import com.elasticpath.rest.definition.purchases.PurchaseLineItemOptionIdentifier;
import com.elasticpath.rest.definition.purchases.PurchaseLineItemOptionValueIdentifier;
import com.elasticpath.rest.id.type.PathIdentifier;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.resource.integration.epcommerce.repository.sku.ProductSkuRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.transform.ReactiveAdapter;

/**
 * Links repository which returns option values given option identifier.
 *
 * @param <OI> option identifier
 * @param <VI> option value identifier
 */
@Component
public class OptionsToOptionValuesLinksRepositoryImpl<OI extends PurchaseLineItemOptionIdentifier, VI extends PurchaseLineItemOptionValueIdentifier>
		implements LinksRepository<PurchaseLineItemOptionIdentifier, PurchaseLineItemOptionValueIdentifier> {

	/**
	 * Value for option not found error.
	 */
	@VisibleForTesting
	static final String VALUE_FOR_OPTION_NOT_FOUND = "Option value for option was not found.";
	private ProductSkuRepository productSkuRepository;
	private ReactiveAdapter reactiveAdapter;

	@Override
	public Observable<PurchaseLineItemOptionValueIdentifier> getElements(final PurchaseLineItemOptionIdentifier identifier) {

		String optionId = identifier.getOptionId().getValue();
		PurchaseLineItemIdentifier purchaseLineItem = identifier.getPurchaseLineItemOptions().getPurchaseLineItem();
		String lineItemId = ((PathIdentifier) purchaseLineItem.getLineItemId()).extractLeafId();

		return productSkuRepository.getProductSkuWithAttributesByGuidAsSingle(lineItemId)
				.map(ProductSku::getOptionValueMap)
				.flatMap(stringSkuOptionValueMap ->
						reactiveAdapter.fromNullableAsSingle(() -> stringSkuOptionValueMap.get(optionId), VALUE_FOR_OPTION_NOT_FOUND))
				.map(SkuOptionValue::getOptionValueKey)
				.map(valueKey -> buildOptionValueIdentifier(identifier, valueKey))
				.toObservable();
	}

	/**
	 * Builds option value identifier.
	 *
	 * @param identifier option identifier
	 * @param valueKey key used to retrieve the value of the line item option
	 * @return option value identifier
	 */
	protected PurchaseLineItemOptionValueIdentifier buildOptionValueIdentifier(
			final PurchaseLineItemOptionIdentifier identifier, final String valueKey) {

		return PurchaseLineItemOptionValueIdentifier.builder()
				.withOptionValueId(StringIdentifier.of(valueKey))
				.withPurchaseLineItemOption(identifier)
				.build();
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
