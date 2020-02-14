/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.rest.resource.paymentinstructions.relationships.profile;

import static com.elasticpath.rest.resource.integration.epcommerce.repository.payments.commons.PaymentResourceHelpers.buildProfilePaymentMethodIdentifier;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.definition.paymentinstructions.PaymentInstructionsIdentifier;
import com.elasticpath.rest.definition.paymentinstructions.PaymentInstructionsToProfilePaymentMethodRelationship;
import com.elasticpath.rest.definition.paymentmethods.ProfilePaymentMethodIdentifier;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;

/**
 * Payment Request Instructions Form to Profile Payment Method link.
 */
public class PaymentInstructionsToProfilePaymentMethodRelationshipImpl
		implements PaymentInstructionsToProfilePaymentMethodRelationship.LinkTo {

	private final PaymentInstructionsIdentifier instructionsIdentifier;

	/**
	 * Constructor.
	 *
	 * @param instructionsIdentifier instructionsIdentifier
	 */
	@Inject
	public PaymentInstructionsToProfilePaymentMethodRelationshipImpl(@RequestIdentifier final PaymentInstructionsIdentifier instructionsIdentifier) {
		this.instructionsIdentifier = instructionsIdentifier;

	}

	@Override
	public Observable<ProfilePaymentMethodIdentifier> onLinkTo() {
		return Observable.just(buildProfilePaymentMethodIdentifier(
				instructionsIdentifier.getProfilePaymentMethod().getProfilePaymentMethods().getProfile(),
				instructionsIdentifier.getProfilePaymentMethod().getPaymentMethodId()));
	}
}
