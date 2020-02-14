/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.paymentmethods.prototypes.profile;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.repository.Repository;
import com.elasticpath.rest.definition.paymentmethods.PaymentMethodEntity;
import com.elasticpath.rest.definition.paymentmethods.ProfilePaymentMethodIdentifier;
import com.elasticpath.rest.definition.paymentmethods.ProfilePaymentMethodsResource;
import com.elasticpath.rest.definition.profiles.ProfileIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;
import com.elasticpath.rest.helix.data.annotation.UriPart;
import com.elasticpath.rest.id.IdentifierPart;

/**
 * Profile Payment Methods prototype for Read operation.
 */
public class ReadProfilePaymentMethodsPrototype implements ProfilePaymentMethodsResource.Read {

	private final IdentifierPart<String> scope;

	private final Repository<PaymentMethodEntity, ProfilePaymentMethodIdentifier> repository;

	/**
	 * Constructor.
	 *
	 * @param scope      profile
	 * @param repository repository
	 */
	@Inject
	public ReadProfilePaymentMethodsPrototype(@UriPart(ProfileIdentifier.SCOPE) final IdentifierPart<String> scope,
											  @ResourceRepository final Repository<PaymentMethodEntity, ProfilePaymentMethodIdentifier> repository) {
		this.scope = scope;
		this.repository = repository;
	}

	@Override
	public Observable<ProfilePaymentMethodIdentifier> onRead() {
		return repository.findAll(scope);
	}
}
