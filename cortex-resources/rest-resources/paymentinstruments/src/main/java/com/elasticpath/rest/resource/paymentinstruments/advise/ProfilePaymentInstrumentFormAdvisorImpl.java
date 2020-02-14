/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.rest.resource.paymentinstruments.advise;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.advise.Message;
import com.elasticpath.rest.definition.paymentinstruments.ProfilePaymentInstrumentFormAdvisor;
import com.elasticpath.rest.definition.paymentinstruments.ProfilePaymentInstrumentFormIdentifier;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;
import com.elasticpath.rest.helix.data.annotation.ResourceService;
import com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instruments.PaymentInstrumentRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.transform.StructuredErrorMessageTransformer;

/**
 * Advisor for profile payment instrument form.
 */
public class ProfilePaymentInstrumentFormAdvisorImpl implements ProfilePaymentInstrumentFormAdvisor.FormAdvisor {

	private final ProfilePaymentInstrumentFormIdentifier profilePaymentInstrumentFormIdentifier;
	private final PaymentInstrumentRepository paymentInstrumentRepository;
	private final StructuredErrorMessageTransformer messageTransformer;

	/**
	 * Constructor.
	 *
	 * @param profilePaymentInstrumentFormIdentifier profile payment instrument form identifier.
	 * @param paymentInstrumentRepository            payment instrument repository.
	 * @param messageTransformer                     transforms {@link com.elasticpath.base.common.dto.StructuredErrorMessage} to  {@link Message}.
	 */
	@Inject
	public ProfilePaymentInstrumentFormAdvisorImpl(
			@RequestIdentifier final ProfilePaymentInstrumentFormIdentifier profilePaymentInstrumentFormIdentifier,
			@ResourceRepository final PaymentInstrumentRepository paymentInstrumentRepository,
			@ResourceService final StructuredErrorMessageTransformer messageTransformer
	) {
		this.profilePaymentInstrumentFormIdentifier = profilePaymentInstrumentFormIdentifier;
		this.paymentInstrumentRepository = paymentInstrumentRepository;
		this.messageTransformer = messageTransformer;
	}

	@Override
	public Observable<Message> onAdvise() {
		final String paymentProviderConfigId = profilePaymentInstrumentFormIdentifier.getProfilePaymentMethod().getPaymentMethodId().getValue();

		return paymentInstrumentRepository.getPaymentInstrumentCreationFieldsForProviderConfigGuid(paymentProviderConfigId)
				.flatMapObservable(dto -> Observable.fromIterable(messageTransformer.transform(dto.getStructuredErrorMessages(), null)));
	}
}
