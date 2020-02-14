/*
 * Copyright © 2013 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.product.option.impl;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import io.reactivex.Single;

import com.elasticpath.domain.skuconfiguration.SkuOptionValue;
import com.elasticpath.rest.ResourceOperationFailure;
import com.elasticpath.rest.resource.integration.epcommerce.repository.product.option.SkuOptionRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.transform.ReactiveAdapter;
import com.elasticpath.service.catalog.SkuOptionService;

/**
 * Implements the {@link SkuOptionRepository}.
 */
@Singleton
@Named("skuOptionRepository")
public class SkuOptionRepositoryImpl implements SkuOptionRepository {

	private static final String CANNOT_FIND_OPTION_VALUE_MESSAGE = "Cannot find option value.";

	private final SkuOptionService skuOptionService;
	private final ReactiveAdapter reactiveAdapter;

	/**
	 * Default constructor.
	 *
	 * @param skuOptionService the sku option service
	 * @param reactiveAdapter  reactiveAdapter
	 */
	@Inject
	public SkuOptionRepositoryImpl(
			@Named("skuOptionService") final SkuOptionService skuOptionService,
			@Named("reactiveAdapter") final ReactiveAdapter reactiveAdapter) {

		this.skuOptionService = skuOptionService;
		this.reactiveAdapter = reactiveAdapter;
	}

	@Override
	public Single<SkuOptionValue> findSkuOptionValueByKey(final String skuOptionNameKey, final String skuOptionValueKey) {
		return reactiveAdapter.fromServiceAsSingle(() -> skuOptionService.findOptionValueByOptionAndValueKeys(skuOptionNameKey, skuOptionValueKey),
			CANNOT_FIND_OPTION_VALUE_MESSAGE)
				.toMaybe()
				.toSingle()
				.onErrorResumeNext(Single.error(ResourceOperationFailure.notFound(CANNOT_FIND_OPTION_VALUE_MESSAGE)));
	}
}
