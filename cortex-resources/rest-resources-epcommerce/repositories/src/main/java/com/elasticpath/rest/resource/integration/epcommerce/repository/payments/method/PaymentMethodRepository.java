/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.rest.resource.integration.epcommerce.repository.payments.method;

import java.util.Currency;
import java.util.Locale;

import io.reactivex.Observable;
import io.reactivex.Single;

import com.elasticpath.domain.orderpaymentapi.StorePaymentProviderConfig;
import com.elasticpath.rest.definition.paymentmethods.PaymentMethodEntity;

/**
 * The facade for operations with payment method identifiers.
 */
public interface PaymentMethodRepository {

	/**
	 * Gets a {@link PaymentMethodEntity} corresponding to the provided payment method id.
	 *
	 * @param paymentMethodId GUID to search
	 * @return Single {@link PaymentMethodEntity}
	 */
	Single<PaymentMethodEntity> findOnePaymentMethodEntityForMethodId(String paymentMethodId);

	/**
	 * Gets all {@link StorePaymentProviderConfig}s which are active for the provided store.
	 *
	 * @param storeCode store code
	 * @return Observable of all {@link StorePaymentProviderConfig}s active for the store
	 */
	Observable<StorePaymentProviderConfig> getStorePaymentProviderConfigsForStoreCode(String storeCode);

	/**
	 * Gets all saveable {@link StorePaymentProviderConfig}s which are active for the provided store.
	 *
	 * @param storeCode store code
	 * @param userId    the user's unique identifier
	 * @param locale    locale for this request
	 * @param currency  currency for this request
	 * @return Observable of all saveable {@link StorePaymentProviderConfig}s active for the store
	 */
	Observable<StorePaymentProviderConfig> getSaveableStorePaymentProviderConfigsForStoreCode(String storeCode, String userId,
																							  Locale locale, Currency currency);
}
