/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.purchase.repositories.impl.options;

import java.util.Locale;

import com.google.common.annotations.VisibleForTesting;
import io.reactivex.Single;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.domain.skuconfiguration.SkuOption;
import com.elasticpath.domain.skuconfiguration.SkuOptionValue;
import com.elasticpath.repository.Repository;
import com.elasticpath.rest.ResourceOperationFailure;
import com.elasticpath.rest.definition.purchases.PurchaseLineItemIdentifier;
import com.elasticpath.rest.definition.purchases.PurchaseLineItemOptionEntity;
import com.elasticpath.rest.definition.purchases.PurchaseLineItemOptionIdentifier;
import com.elasticpath.rest.id.type.PathIdentifier;
import com.elasticpath.rest.identity.util.SubjectUtil;
import com.elasticpath.rest.resource.ResourceOperationContext;
import com.elasticpath.rest.resource.integration.epcommerce.repository.sku.ProductSkuRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.transform.ReactiveAdapter;

/**
 * Repository which retrieves Option Entity.
 *
 * @param <E> purchase line item option entity
 * @param <I> purchase line item option identifier
 */
@Component
public class OptionEntityRepositoryImpl<E extends PurchaseLineItemOptionEntity, I extends PurchaseLineItemOptionIdentifier>
		implements Repository<PurchaseLineItemOptionEntity, PurchaseLineItemOptionIdentifier> {

	/**
	 * Option not found error.
	 */
	@VisibleForTesting
	static final String OPTION_NOT_FOUND_FOR_ITEM = "option not found for item";
	private static final String KEY_NOT_FOUND = "Key not found";
	private ResourceOperationContext resourceOperationContext;
	private ProductSkuRepository productSkuRepository;
	private ReactiveAdapter reactiveAdapter;

	@Override
	public Single<PurchaseLineItemOptionEntity> findOne(final PurchaseLineItemOptionIdentifier identifier) {
		PurchaseLineItemIdentifier lineItemIdentifier = identifier.getPurchaseLineItemOptions().getPurchaseLineItem();

		String lineItemId = ((PathIdentifier) lineItemIdentifier.getLineItemId()).extractLeafId();
		String optionId = identifier.getOptionId().getValue();

		return productSkuRepository.getProductSkuWithAttributesByGuidAsSingle(lineItemId)
				.map(ProductSku::getOptionValueMap)
				.flatMap(optionValueMap -> reactiveAdapter.fromNullableAsSingle(() -> optionValueMap.get(optionId), KEY_NOT_FOUND))
				.onErrorResumeNext(Single.error(ResourceOperationFailure.notFound(OPTION_NOT_FOUND_FOR_ITEM)))
				.map(this::buildOptionEntity);
	}

	/**
	 * Builds option entity from skuOptionValue.
	 *
	 * @param skuOptionValue sku option value
	 * @return option entity
	 */
	private PurchaseLineItemOptionEntity buildOptionEntity(final SkuOptionValue skuOptionValue) {
		SkuOption skuOption = skuOptionValue.getSkuOption();
		Locale locale = SubjectUtil.getLocale(resourceOperationContext.getSubject());

		return PurchaseLineItemOptionEntity.builder()
				.withName(skuOption.getOptionKey())
				.withDisplayName(skuOption.getDisplayName(locale, true))
				.withSelectedValueId(skuOptionValue.getOptionValueKey())
				.withOptionId(skuOption.getGuid())
				.build();
	}

	@Reference
	public void setProductSkuRepository(final ProductSkuRepository productSkuRepository) {
		this.productSkuRepository = productSkuRepository;
	}

	@Reference
	public void setResourceOperationContext(final ResourceOperationContext resourceOperationContext) {
		this.resourceOperationContext = resourceOperationContext;
	}

	@Reference
	public void setReactiveAdapter(final ReactiveAdapter reactiveAdapter) {
		this.reactiveAdapter = reactiveAdapter;
	}
}
