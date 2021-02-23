/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.payments.method.impl;

import java.util.Currency;
import java.util.Locale;
import java.util.Optional;

import io.reactivex.Observable;
import io.reactivex.Single;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elasticpath.repository.Repository;
import com.elasticpath.rest.definition.paymentmethods.AccountPaymentMethodIdentifier;
import com.elasticpath.rest.definition.paymentmethods.AccountPaymentMethodsIdentifier;
import com.elasticpath.rest.definition.paymentmethods.PaymentMethodEntity;
import com.elasticpath.rest.id.IdentifierPart;
import com.elasticpath.rest.id.ResourceIdentifier;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.identity.util.SubjectUtil;
import com.elasticpath.rest.resource.ResourceOperationContext;
import com.elasticpath.rest.resource.integration.epcommerce.repository.payments.method.PaymentMethodRepository;

/**
 * Payment Method Entity repository for Account Payment Method.
 *
 * @param <E> the entity type
 * @param <I> the identifier type
 */
@Component
public class AccountPaymentMethodEntityRepositoryImpl<E extends PaymentMethodEntity, I extends AccountPaymentMethodIdentifier> implements
		Repository<PaymentMethodEntity, AccountPaymentMethodIdentifier> {
	private ResourceOperationContext resourceOperationContext;
	private PaymentMethodRepository paymentMethodRepository;

	@Override
	public Observable<AccountPaymentMethodIdentifier> findAll(final IdentifierPart<String> scope) {

		Optional<ResourceIdentifier> resourceIdentifier = resourceOperationContext.getResourceIdentifier();
		Locale locale = SubjectUtil.getLocale(resourceOperationContext.getSubject());
		Currency currency = SubjectUtil.getCurrency(resourceOperationContext.getSubject());

		if (resourceIdentifier.isPresent()) {
			AccountPaymentMethodsIdentifier accountPaymentMethodsIdentifier = (AccountPaymentMethodsIdentifier) resourceIdentifier.get();
			String accountId = accountPaymentMethodsIdentifier.getAccount().getAccountId().getValue();
			return paymentMethodRepository.getSaveableStorePaymentProviderConfigsForStoreCode(scope.getValue(), accountId, locale, currency)
					.map(storePaymentProviderConfig -> AccountPaymentMethodIdentifier.builder()
							.withAccountPaymentMethods(accountPaymentMethodsIdentifier)
							.withAccountPaymentMethodId(StringIdentifier.of(storePaymentProviderConfig.getGuid()))
							.build());
		}
		return Observable.empty();
	}

	@Override
	public Single<PaymentMethodEntity> findOne(final AccountPaymentMethodIdentifier identifier) {
		String paymentMethodId = identifier.getAccountPaymentMethodId().getValue();
		return paymentMethodRepository.findOnePaymentMethodEntityForMethodId(paymentMethodId);
	}

	@Reference
	public void setResourceOperationContext(final ResourceOperationContext resourceOperationContext) {
		this.resourceOperationContext = resourceOperationContext;
	}

	@Reference
	public void setPaymentMethodRepository(final PaymentMethodRepository paymentMethodRepository) {
		this.paymentMethodRepository = paymentMethodRepository;
	}

}
