/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.purchase.repositories.impl;

import io.reactivex.Single;
import io.reactivex.functions.Function;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.domain.customer.CustomerSession;
import com.elasticpath.domain.misc.CheckoutResults;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.shoppingcart.ShoppingCartTaxSnapshot;
import com.elasticpath.rest.ResourceOperationFailure;
import com.elasticpath.rest.cache.CacheResult;
import com.elasticpath.rest.resource.integration.epcommerce.repository.purchase.repositories.PurchaseRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.transform.ReactiveAdapter;
import com.elasticpath.service.shoppingcart.CheckoutService;

/**
 * Default implementation.
 */
@Component
public class PurchaseRepositoryImpl implements PurchaseRepository {
	private static final Logger LOG = LoggerFactory.getLogger(PurchaseRepositoryImpl.class);

	private CheckoutService checkoutService;
	private ReactiveAdapter reactiveAdapter;

	@Override
	@CacheResult
	public Single<CheckoutResults> checkout(final ShoppingCart shoppingCart,
											final ShoppingCartTaxSnapshot taxSnapshot,
											final CustomerSession customerSession) {
		return reactiveAdapter.fromServiceAsSingle(() -> checkoutService.checkout(
				shoppingCart,
				taxSnapshot,
				customerSession,
				true
		)).onErrorResumeNext(handleCheckoutException());
	}

	/**
	 * Checkout handler.
	 *
	 * @param <T> error type
	 * @return function that returns corresponding error
	 */
	protected <T> Function<Throwable, Single<? extends T>> handleCheckoutException() {
		return throwable -> {
			if (throwable instanceof EpServiceException) {
				String message = ExceptionUtils.getRootCauseMessage(throwable);
				return Single.error(ResourceOperationFailure.stateFailure(message));
			} else if (throwable instanceof ResourceOperationFailure) {
				return Single.error(throwable);
			} else {
				LOG.error("Unexpected checkout error", throwable);
				return Single.error(ResourceOperationFailure.stateFailure("The purchase failed: " + throwable.getMessage()));
			}
		};
	}

	@Reference
	public void setCheckoutService(final CheckoutService checkoutService) {
		this.checkoutService = checkoutService;
	}

	@Reference
	public void setReactiveAdapter(final ReactiveAdapter reactiveAdapter) {
		this.reactiveAdapter = reactiveAdapter;
	}
}
