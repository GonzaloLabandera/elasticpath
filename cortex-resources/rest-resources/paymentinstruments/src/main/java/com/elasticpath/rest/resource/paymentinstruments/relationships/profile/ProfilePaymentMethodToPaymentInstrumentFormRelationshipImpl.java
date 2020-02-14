/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.paymentinstruments.relationships.profile;

import static com.elasticpath.rest.resource.integration.epcommerce.repository.payments.commons.PaymentResourceHelpers.buildProfilePaymentInstrumentFormIdentifier;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.definition.paymentinstruments.PaymentInstrumentFormForProfilePaymentMethodRelationship;
import com.elasticpath.rest.definition.paymentinstruments.ProfilePaymentInstrumentFormIdentifier;
import com.elasticpath.rest.definition.paymentmethods.ProfilePaymentMethodIdentifier;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;

/**
 * Profile Payment Method to Payment Instrument Form link.
 */
public class ProfilePaymentMethodToPaymentInstrumentFormRelationshipImpl implements PaymentInstrumentFormForProfilePaymentMethodRelationship.LinkTo {

	private final ProfilePaymentMethodIdentifier profilePaymentMethodIdentifier;

	/**
	 * Constructor.
	 *
	 * @param profilePaymentMethodIdentifier {@link ProfilePaymentMethodIdentifier}
	 */
	@Inject
	public ProfilePaymentMethodToPaymentInstrumentFormRelationshipImpl(
			@RequestIdentifier final ProfilePaymentMethodIdentifier profilePaymentMethodIdentifier) {
		this.profilePaymentMethodIdentifier = profilePaymentMethodIdentifier;
	}

	@Override
	public Observable<ProfilePaymentInstrumentFormIdentifier> onLinkTo() {
		return Observable.just(buildProfilePaymentInstrumentFormIdentifier(profilePaymentMethodIdentifier));
	}
}
