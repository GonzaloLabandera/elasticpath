/*
 * Copyright (c) Elastic Path Software Inc., 2019
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
import com.elasticpath.rest.definition.paymentmethods.PaymentMethodEntity;
import com.elasticpath.rest.definition.paymentmethods.ProfilePaymentMethodIdentifier;
import com.elasticpath.rest.definition.paymentmethods.ProfilePaymentMethodsIdentifier;
import com.elasticpath.rest.id.IdentifierPart;
import com.elasticpath.rest.id.ResourceIdentifier;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.identity.util.SubjectUtil;
import com.elasticpath.rest.resource.ResourceOperationContext;
import com.elasticpath.rest.resource.integration.epcommerce.repository.payments.method.PaymentMethodRepository;

/**
 * Payment Method Entity repository for Profile Payment Method Identifier.
 *
 * @param <E> the entity type
 * @param <I> the identifier type
 */
@Component
public class ProfilePaymentMethodEntityRepositoryImpl<E extends PaymentMethodEntity, I extends ProfilePaymentMethodIdentifier>
		implements Repository<PaymentMethodEntity, ProfilePaymentMethodIdentifier> {

	private ResourceOperationContext resourceOperationContext;
	private PaymentMethodRepository paymentMethodRepository;

	@Override
	public Observable<ProfilePaymentMethodIdentifier> findAll(final IdentifierPart<String> scope) {

		Optional<ResourceIdentifier> resourceIdentifier = resourceOperationContext.getResourceIdentifier();
		String userId = resourceOperationContext.getUserIdentifier();
		Locale locale = SubjectUtil.getLocale(resourceOperationContext.getSubject());
		Currency currency = SubjectUtil.getCurrency(resourceOperationContext.getSubject());

		if (resourceIdentifier.isPresent()) {
			ProfilePaymentMethodsIdentifier profilePaymentMethodsIdentifier = (ProfilePaymentMethodsIdentifier) resourceIdentifier.get();
			return paymentMethodRepository.getSaveableStorePaymentProviderConfigsForStoreCode(scope.getValue(), userId, locale, currency)
					.map(storePaymentProviderConfig -> ProfilePaymentMethodIdentifier.builder()
							.withProfilePaymentMethods(profilePaymentMethodsIdentifier)
							.withPaymentMethodId(StringIdentifier.of(storePaymentProviderConfig.getGuid()))
							.build());
		}
		return Observable.empty();
	}

	@Override
	public Single<PaymentMethodEntity> findOne(final ProfilePaymentMethodIdentifier identifier) {
		String paymentMethodId = identifier.getPaymentMethodId().getValue();
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
