/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instruments;

import io.reactivex.Observable;
import io.reactivex.Single;

import com.elasticpath.provider.payment.service.instrument.PaymentInstrumentCreationFieldsDTO;
import com.elasticpath.rest.definition.paymentinstruments.AccountPaymentInstrumentIdentifier;
import com.elasticpath.rest.definition.paymentinstruments.OrderPaymentInstrumentForFormEntity;
import com.elasticpath.rest.definition.paymentinstruments.OrderPaymentInstrumentIdentifier;
import com.elasticpath.rest.definition.paymentinstruments.PaymentInstrumentForFormEntity;
import com.elasticpath.rest.definition.paymentinstruments.PaymentInstrumentIdentifier;
import com.elasticpath.rest.form.SubmitResult;
import com.elasticpath.rest.id.IdentifierPart;

/**
 * Payment instrument creation repository.
 */
public interface PaymentInstrumentRepository {

	/**
	 * Creates {@link com.elasticpath.domain.orderpaymentapi.CartOrderPaymentInstrument} and returns identifier pointing to it.
	 *
	 * @param scope      store code
	 * @param formEntity order payment instrument form
	 * @return order payment instrument identifier as submit result
	 */
	Single<SubmitResult<OrderPaymentInstrumentIdentifier>> submitOrderPaymentInstrument(IdentifierPart<String> scope,
																						OrderPaymentInstrumentForFormEntity formEntity);

	/**
	 * Creates {@link com.elasticpath.domain.orderpaymentapi.CustomerPaymentInstrument} and returns identifier pointing to it.
	 *
	 * @param scope      store code
	 * @param formEntity profile payment instrument form
	 * @return profile payment instrument identifier as submit result
	 */
	Single<SubmitResult<PaymentInstrumentIdentifier>> submitProfilePaymentInstrument(IdentifierPart<String> scope,
																					 PaymentInstrumentForFormEntity formEntity);

	/**
	 * Creates {@link com.elasticpath.domain.orderpaymentapi.CustomerPaymentInstrument} and returns identifier pointing to it.
	 *
	 * @param scope      store code
	 * @param formEntity profile payment instrument form
	 * @return account payment instrument identifier as submit result
	 */
	Single<SubmitResult<AccountPaymentInstrumentIdentifier>> submitAccountPaymentInstrument(IdentifierPart<String> scope,
																							PaymentInstrumentForFormEntity formEntity);


	/**
	 * Gets the payment provider {@link PaymentInstrumentCreationFieldsDTO} for the given StorePaymentProviderConfig guid.
	 *
	 * @param storeProviderConfigGuid StorePaymentProviderConfig guid
	 * @return Single {@link PaymentInstrumentCreationFieldsDTO} corresponding to the provided StorePaymentProviderConfig guid
	 */
	Single<PaymentInstrumentCreationFieldsDTO> getPaymentInstrumentCreationFieldsForProviderConfigGuid(String storeProviderConfigGuid);

	/**
	 * Gets the payment provider {@link PaymentInstrumentCreationFieldsDTO} for the given StorePaymentProviderConfig guid.
	 *
	 * @param storeProviderConfigGuid StorePaymentProviderConfig guid
	 * @param accountId the account ID
	 * @return Single {@link PaymentInstrumentCreationFieldsDTO} corresponding to the provided StorePaymentProviderConfig guid
	 */
	Single<PaymentInstrumentCreationFieldsDTO> getAccountPaymentInstrumentCreationFieldsForProviderConfigGuid(
			String storeProviderConfigGuid, String accountId);


	/**
	 * Finds PaymentInstrumentIdentifier that inside given scope.
	 *
	 * @param scope the scope in which PaymentInstrumentIdentifier finds.
	 * @return {@link PaymentInstrumentIdentifier}
	 */
	Observable<PaymentInstrumentIdentifier> findAll(IdentifierPart<String> scope);

	/**
	 * Find all account payment instruments give scope.
	 *
	 * @param scope the scope
	 * @return {@link AccountPaymentInstrumentIdentifier}
	 */
	Observable<AccountPaymentInstrumentIdentifier> findAllAccountPaymentInstruments(IdentifierPart<String> scope);

	/**
	 * Find all account payment instruments given account id.
	 *
	 * @param scope the scope
	 * @param accountId the account id
	 * @return {@link AccountPaymentInstrumentIdentifier}
	 */
	Observable<AccountPaymentInstrumentIdentifier> findAllAccountPaymentInstrumentsByAccountId(String scope, String accountId);

	/**
	 * Get account Id from the Resource Operation Context.
	 *
	 * @return the account id
	 */
	IdentifierPart<String> getAccountIdFromResourceOperationContext();
}
