/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.paymentinstruments.advise;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.advise.Message;
import com.elasticpath.rest.definition.paymentinstruments.AccountPaymentInstrumentFormAdvisor;
import com.elasticpath.rest.definition.paymentinstruments.AccountPaymentInstrumentFormIdentifier;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;
import com.elasticpath.rest.helix.data.annotation.ResourceService;
import com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instruments.PaymentInstrumentRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.transform.StructuredErrorMessageTransformer;

/**
 * Advisor for account payment instrument form.
 */
public class AccountPaymentInstrumentFormAdvisorImpl implements AccountPaymentInstrumentFormAdvisor.FormAdvisor {

	private final AccountPaymentInstrumentFormIdentifier accountPaymentInstrumentFormIdentifier;
	private final PaymentInstrumentRepository paymentInstrumentRepository;
	private final StructuredErrorMessageTransformer messageTransformer;

	/**
	 * Constructor.
	 *
	 * @param accountPaymentInstrumentFormIdentifier account payment instrument form identifier.
	 * @param paymentInstrumentRepository            payment instrument repository.
	 * @param messageTransformer                     transforms {@link com.elasticpath.base.common.dto.StructuredErrorMessage} to  {@link Message}.
	 */
	@Inject
	public AccountPaymentInstrumentFormAdvisorImpl(
			@RequestIdentifier final AccountPaymentInstrumentFormIdentifier accountPaymentInstrumentFormIdentifier,
			@ResourceRepository final PaymentInstrumentRepository paymentInstrumentRepository,
			@ResourceService final StructuredErrorMessageTransformer messageTransformer
	) {
		this.accountPaymentInstrumentFormIdentifier = accountPaymentInstrumentFormIdentifier;
		this.paymentInstrumentRepository = paymentInstrumentRepository;
		this.messageTransformer = messageTransformer;
	}

	@Override
	public Observable<Message> onAdvise() {
		final String paymentProviderConfigId =
				accountPaymentInstrumentFormIdentifier.getAccountPaymentMethod().getAccountPaymentMethodId().getValue();
		final String accountId = accountPaymentInstrumentFormIdentifier.getAccountPaymentMethod()
				.getAccountPaymentMethods().getAccount().getAccountId().getValue();

		return paymentInstrumentRepository.getAccountPaymentInstrumentCreationFieldsForProviderConfigGuid(paymentProviderConfigId, accountId)
				.flatMapObservable(dto -> Observable.fromIterable(messageTransformer.transform(dto.getStructuredErrorMessages(), null)));
	}
}
