/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.rest.resource.paymentinstructions.relationships.profile;

import static com.elasticpath.rest.resource.integration.epcommerce.repository.payments.commons.PaymentResourceHelpers.buildRequestInstructionsForm;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.definition.paymentinstructions.RequestInstructionsFormForProfilePaymentMethodRelationship;
import com.elasticpath.rest.definition.paymentinstructions.RequestInstructionsFormIdentifier;
import com.elasticpath.rest.definition.paymentmethods.ProfilePaymentMethodIdentifier;
import com.elasticpath.rest.definition.profiles.ProfileIdentifier;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.id.IdentifierPart;

/**
 * Payment Methods to orders link.
 */
public class PaymentMethodToRequestInstructionsFormRelationshipImpl implements RequestInstructionsFormForProfilePaymentMethodRelationship.LinkTo {

	private final ProfilePaymentMethodIdentifier paymentMethod;

	/**
	 * Constructor.
	 *
	 * @param paymentMethod {@link ProfilePaymentMethodIdentifier}
	 */
	@Inject
	public PaymentMethodToRequestInstructionsFormRelationshipImpl(@RequestIdentifier final ProfilePaymentMethodIdentifier paymentMethod) {
		this.paymentMethod = paymentMethod;
	}

	@Override
	public Observable<RequestInstructionsFormIdentifier> onLinkTo() {
		ProfileIdentifier profileIdentifier = paymentMethod.getProfilePaymentMethods().getProfile();
		IdentifierPart<String> paymentMethodId = paymentMethod.getPaymentMethodId();

		return Observable.just(buildRequestInstructionsForm(profileIdentifier, paymentMethodId));
	}
}
