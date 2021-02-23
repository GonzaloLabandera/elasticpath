/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.paymentinstructions.prototypes.account;

import java.util.Currency;
import java.util.Locale;
import javax.inject.Inject;

import io.reactivex.Single;

import com.elasticpath.rest.definition.paymentinstructions.AccountPaymentInstructionsIdentifier;
import com.elasticpath.rest.definition.paymentinstructions.AccountRequestInstructionsFormIdentifier;
import com.elasticpath.rest.definition.paymentinstructions.AccountRequestInstructionsFormResource;
import com.elasticpath.rest.definition.paymentinstructions.PaymentMethodConfigurationEntity;
import com.elasticpath.rest.form.SubmitResult;
import com.elasticpath.rest.form.SubmitStatus;
import com.elasticpath.rest.helix.data.annotation.RequestForm;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;
import com.elasticpath.rest.helix.data.annotation.UserCurrency;
import com.elasticpath.rest.helix.data.annotation.UserLocale;
import com.elasticpath.rest.id.IdentifierPart;
import com.elasticpath.rest.resource.integration.epcommerce.repository.payments.commons.PaymentResourceHelpers;
import com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instructions.InstructionsEntityRepository;

/**
 * Payment Instructions Form prototype for Submit operation.
 */
public class SubmitAccountRequestInstructionsFormPrototype implements AccountRequestInstructionsFormResource.SubmitWithResult {

	private final PaymentMethodConfigurationEntity paymentMethodConfigurationEntity;
	private final AccountRequestInstructionsFormIdentifier requestIdentifier;
	private final InstructionsEntityRepository repository;
	private final Locale locale;
	private final Currency currency;

	/**
	 * Constructor.
	 *
	 * @param paymentMethodConfigurationEntity {@link PaymentMethodConfigurationEntity} encapsulating the submitted form data
	 * @param requestIdentifier                {@link AccountRequestInstructionsFormIdentifier}
	 * @param repository                       {@link InstructionsEntityRepository}
	 * @param locale                           {@link Locale} for this request
	 * @param currency                         {@link Currency} for this request
	 */
	@Inject
	public SubmitAccountRequestInstructionsFormPrototype(@RequestForm final PaymentMethodConfigurationEntity paymentMethodConfigurationEntity,
														 @RequestIdentifier final AccountRequestInstructionsFormIdentifier requestIdentifier,
														 @ResourceRepository final InstructionsEntityRepository repository,
														 @UserLocale final Locale locale,
														 @UserCurrency final Currency currency) {
		this.paymentMethodConfigurationEntity = paymentMethodConfigurationEntity;
		this.requestIdentifier = requestIdentifier;
		this.repository = repository;
		this.locale = locale;
		this.currency = currency;
	}

	@Override
	public Single<SubmitResult<AccountPaymentInstructionsIdentifier>> onSubmitWithResult() {
		IdentifierPart<String> methodId = requestIdentifier.getAccountPaymentMethod().getAccountPaymentMethodId();
		String accountId = requestIdentifier.getAccountPaymentMethod().getAccountPaymentMethods().getAccount().getAccountId().getValue();

		return repository.submitRequestInstructionsForm(methodId.getValue(), locale, currency, paymentMethodConfigurationEntity, accountId)
				.map(picInstructionsDTO -> PaymentResourceHelpers.buildAccountPaymentInstructionsIdentifier(
						requestIdentifier.getAccountPaymentMethod().getAccountPaymentMethods().getAccount(),
						methodId, picInstructionsDTO.getCommunicationInstructions(),
						picInstructionsDTO.getPayload()))
				.map(this::buildSubmitResult);
	}

	private SubmitResult<AccountPaymentInstructionsIdentifier> buildSubmitResult(final AccountPaymentInstructionsIdentifier identifier) {
		return SubmitResult.<AccountPaymentInstructionsIdentifier>builder()
				.withIdentifier(identifier)
				.withStatus(SubmitStatus.CREATED)
				.build();
	}

}
