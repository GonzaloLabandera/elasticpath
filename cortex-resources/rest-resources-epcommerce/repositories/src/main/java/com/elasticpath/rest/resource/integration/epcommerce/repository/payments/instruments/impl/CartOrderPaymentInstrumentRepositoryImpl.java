/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instruments.impl;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import org.osgi.service.component.annotations.Reference;

import com.elasticpath.domain.cartorder.CartOrder;
import com.elasticpath.domain.orderpaymentapi.CartOrderPaymentInstrument;
import com.elasticpath.rest.ResourceOperationFailure;
import com.elasticpath.rest.cache.CacheResult;
import com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instruments.CartOrderPaymentInstrumentRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.transform.ReactiveAdapter;
import com.elasticpath.service.orderpaymentapi.CartOrderPaymentInstrumentService;

/**
 * The facade for operations with cart order payment instruments.
 */
@Singleton
@Named("cartOrderPaymentInstrumentRepository")
public class CartOrderPaymentInstrumentRepositoryImpl implements CartOrderPaymentInstrumentRepository {

	private static final String UNABLE_TO_SAVE_MESSAGE = "Unable to save cart order payment instrument.";

	private final CartOrderPaymentInstrumentService cartOrderPaymentInstrumentService;
	private ReactiveAdapter reactiveAdapter;

	/**
	 * Constructor.
	 *
	 * @param cartOrderPaymentInstrumentService cartOrderPaymentInstrumentService
	 * @param reactiveAdapter                   reactiveAdapter
	 */
	@Inject
	public CartOrderPaymentInstrumentRepositoryImpl(
			@Named("cartOrderPaymentInstrumentService") final CartOrderPaymentInstrumentService cartOrderPaymentInstrumentService,
			@Named("reactiveAdapter") final ReactiveAdapter reactiveAdapter) {
		this.cartOrderPaymentInstrumentService = cartOrderPaymentInstrumentService;
		this.reactiveAdapter = reactiveAdapter;
	}

	@Override
	public Single<CartOrderPaymentInstrument> saveOrUpdate(final CartOrderPaymentInstrument cartOrderPaymentInstrument) {
		return reactiveAdapter.fromServiceAsSingle(() ->
				cartOrderPaymentInstrumentService.saveOrUpdate(cartOrderPaymentInstrument), UNABLE_TO_SAVE_MESSAGE)
				.onErrorResumeNext(Single.error(ResourceOperationFailure.serverError(UNABLE_TO_SAVE_MESSAGE)));
	}

	@Override
	public Completable remove(final CartOrderPaymentInstrument cartOrderPaymentInstrument) {
		return reactiveAdapter.fromServiceAsCompletable(() -> cartOrderPaymentInstrumentService.remove(cartOrderPaymentInstrument));
	}

	@CacheResult
	@Override
	public Observable<CartOrderPaymentInstrument> findByCartOrder(final CartOrder cartOrder) {
		return reactiveAdapter.fromService(() -> cartOrderPaymentInstrumentService.findByCartOrder(cartOrder))
				.flatMap(Observable::fromIterable);
	}

	@Override
	public Single<CartOrderPaymentInstrument> findByGuid(final String guid) {
		return reactiveAdapter.fromServiceAsSingle(() -> cartOrderPaymentInstrumentService.findByGuid(guid))
				.onErrorResumeNext(Single.error(ResourceOperationFailure.notFound("Cart order payment instrument not found for guid " + guid + ".")));
	}

	@Reference
	public void setReactiveAdapter(final ReactiveAdapter reactiveAdapter) {
		this.reactiveAdapter = reactiveAdapter;
	}

	protected CartOrderPaymentInstrumentService getCartOrderPaymentInstrumentService() {
		return cartOrderPaymentInstrumentService;
	}

	public ReactiveAdapter getReactiveAdapter() {
		return reactiveAdapter;
	}
}
