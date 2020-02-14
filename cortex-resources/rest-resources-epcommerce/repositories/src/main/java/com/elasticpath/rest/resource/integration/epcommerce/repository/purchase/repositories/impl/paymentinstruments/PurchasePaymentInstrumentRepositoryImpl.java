/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.purchase.repositories.impl.paymentinstruments;

import static com.elasticpath.rest.resource.integration.epcommerce.repository.payments.commons.PaymentResourceHelpers.buildPaymentInstrumentAttributesEntity;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import io.reactivex.Observable;
import io.reactivex.Single;
import org.osgi.service.component.annotations.Reference;

import com.elasticpath.domain.order.Order;
import com.elasticpath.domain.orderpaymentapi.CorePaymentInstrument;
import com.elasticpath.domain.orderpaymentapi.OrderPaymentInstrument;
import com.elasticpath.rest.cache.CacheResult;
import com.elasticpath.rest.definition.paymentinstruments.PurchasePaymentInstrumentEntity;
import com.elasticpath.rest.definition.paymentinstruments.PurchasePaymentInstrumentIdentifier;
import com.elasticpath.rest.definition.paymentinstruments.PurchasePaymentInstrumentsIdentifier;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.resource.integration.epcommerce.repository.order.OrderRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instruments.PaymentInstrumentManagementRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.purchase.repositories.PurchasePaymentInstrumentRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.transform.ReactiveAdapter;
import com.elasticpath.service.orderpaymentapi.OrderPaymentInstrumentService;

/**
 * Purchase Payment Instrument Repository implementation for operations with {@link PurchasePaymentInstrumentRepository}.
 */
@Singleton
@Named("purchasePaymentInstrumentRepository")
public class PurchasePaymentInstrumentRepositoryImpl implements PurchasePaymentInstrumentRepository {

	@Inject
	@Named("orderRepository")
	private OrderRepository orderRepository;

	@Inject
	@Named("orderPaymentInstrumentService")
	private OrderPaymentInstrumentService orderPaymentInstrumentService;

	@Inject
	@Named("paymentInstrumentManagementRepository")
	private PaymentInstrumentManagementRepository paymentInstrumentManagementRepository;

	@Inject
	@Named("reactiveAdapter")
	private ReactiveAdapter reactiveAdapter;

	@Override
	public Observable<PurchasePaymentInstrumentIdentifier> findPurchaseInstrumentsByPurchaseId(final String scope,
																							   final PurchasePaymentInstrumentsIdentifier
																									   identifier) {
		String purchaseId = identifier.getPurchase().getPurchaseId().getValue();

		return orderRepository.findByGuid(scope, purchaseId)
				.flatMapObservable(this::findByOrder)
				.map(orderPaymentInstrument -> PurchasePaymentInstrumentIdentifier.builder()
						.withPaymentInstrumentId(StringIdentifier.of(orderPaymentInstrument.getGuid()))
						.withPurchasePaymentInstruments(identifier)
						.build());
	}

	@Override
	public Single<PurchasePaymentInstrumentEntity> findOne(final PurchasePaymentInstrumentIdentifier purchasePaymentInstrumentIdentifier) {
		final String purchaseId = purchasePaymentInstrumentIdentifier.getPaymentInstrumentId().getValue();

		return findByGuid(purchaseId)
				.map(CorePaymentInstrument::getPaymentInstrumentGuid)
				.flatMap(instrumentGuid -> paymentInstrumentManagementRepository.getPaymentInstrumentByGuid(instrumentGuid))
				.map(paymentInstrumentDTO -> PurchasePaymentInstrumentEntity.builder()
						.withName(paymentInstrumentDTO.getName())
						.withPaymentInstrumentIdentificationAttributes(buildPaymentInstrumentAttributesEntity(paymentInstrumentDTO.getData()))
						.build());
	}

	@CacheResult
	private Observable<OrderPaymentInstrument> findByOrder(final Order order) {
		return reactiveAdapter.fromService(() -> orderPaymentInstrumentService.findByOrder(order))
				.flatMap(Observable::fromIterable);
	}

	@CacheResult
	private Single<OrderPaymentInstrument> findByGuid(final String purchaseId) {
		return reactiveAdapter.fromServiceAsSingle(() -> orderPaymentInstrumentService.findByGuid(purchaseId),
				"No purchase payment instruments found for GUID " + purchaseId + ".");
	}

	@Reference
	public void setReactiveAdapter(final ReactiveAdapter reactiveAdapter) {
		this.reactiveAdapter = reactiveAdapter;
	}
}
