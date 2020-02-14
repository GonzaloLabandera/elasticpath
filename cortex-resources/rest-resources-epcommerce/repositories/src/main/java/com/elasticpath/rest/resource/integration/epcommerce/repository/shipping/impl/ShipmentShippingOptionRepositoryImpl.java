/*
 * Copyright © 2014 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.shipping.impl;

import java.util.Locale;
import java.util.Objects;
import javax.inject.Inject;
import javax.inject.Named;

import io.reactivex.Observable;
import io.reactivex.Single;

import com.elasticpath.rest.ResourceOperationFailure;
import com.elasticpath.rest.cache.CacheResult;
import com.elasticpath.rest.identity.util.SubjectUtil;
import com.elasticpath.rest.resource.ResourceOperationContext;
import com.elasticpath.rest.resource.integration.epcommerce.repository.shipping.ShipmentShippingOptionRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.transform.ReactiveAdapter;
import com.elasticpath.service.shipping.ShippingOptionResult;
import com.elasticpath.service.shipping.ShippingOptionService;
import com.elasticpath.shipping.connectivity.dto.ShippingOption;

/**
 * Default implementation of {@link ShipmentShippingOptionRepository}.
 */
@Named("shipmentShippingOptionRepository")
public class ShipmentShippingOptionRepositoryImpl implements ShipmentShippingOptionRepository {

	private static final String SHIPPING_OPTION_NOT_FOUND = "Shipping option not found.";

	private final ResourceOperationContext resourceOperationContext;
	private final ShippingOptionService shippingOptionService;
	private final ReactiveAdapter reactiveAdapter;

	/**
	 * Constructor.
	 *  @param resourceOperationContext a {@link ResourceOperationContext}
	 * @param shippingOptionService a {@link ShippingOptionService}
	 * @param reactiveAdapter a {@link ReactiveAdapter}
	 */
	@Inject
	public ShipmentShippingOptionRepositoryImpl(
			@Named("resourceOperationContext")
			final ResourceOperationContext resourceOperationContext,
			@Named("shippingOptionService")
			final ShippingOptionService shippingOptionService,
			@Named("reactiveAdapter")
			final ReactiveAdapter reactiveAdapter) {
		this.resourceOperationContext = resourceOperationContext;
		this.shippingOptionService = shippingOptionService;
		this.reactiveAdapter = reactiveAdapter;
	}

	@Override
	@CacheResult
	public Single<ShippingOption> findByCode(final String shippingOptionCode, final String storeCode) {

		final Locale locale = SubjectUtil.getLocale(resourceOperationContext.getSubject());

		return reactiveAdapter.fromServiceAsSingle(() -> shippingOptionService
				.getAllShippingOptions(storeCode, locale))
				.map(ShippingOptionResult::getAvailableShippingOptions)
				.flatMapObservable(Observable::fromIterable)
				.filter(shippingOption -> Objects.equals(shippingOptionCode, shippingOption.getCode()))
				.firstOrError()
				.onErrorResumeNext(Single.error(ResourceOperationFailure.notFound(SHIPPING_OPTION_NOT_FOUND)));
	}

}
