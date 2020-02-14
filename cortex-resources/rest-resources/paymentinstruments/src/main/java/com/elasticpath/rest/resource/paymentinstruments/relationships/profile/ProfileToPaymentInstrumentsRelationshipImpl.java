/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.paymentinstruments.relationships.profile;

import static com.elasticpath.rest.resource.integration.epcommerce.repository.payments.commons.PaymentResourceHelpers.buildPaymentInstrumentsIdentifier;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.definition.paymentinstruments.PaymentInstrumentsForProfileRelationship;
import com.elasticpath.rest.definition.paymentinstruments.PaymentInstrumentsIdentifier;
import com.elasticpath.rest.definition.profiles.ProfileIdentifier;
import com.elasticpath.rest.helix.data.annotation.UriPart;
import com.elasticpath.rest.id.IdentifierPart;

/**
 * Profile to Payment Instruments Link.
 */
public class ProfileToPaymentInstrumentsRelationshipImpl implements PaymentInstrumentsForProfileRelationship.LinkTo {

	private final IdentifierPart<String> scope;

	/**
	 * Constructor.
	 *
	 * @param scope scope
	 */
	@Inject
	public ProfileToPaymentInstrumentsRelationshipImpl(@UriPart(ProfileIdentifier.SCOPE) final IdentifierPart<String> scope) {
		this.scope = scope;
	}

	@Override
	public Observable<PaymentInstrumentsIdentifier> onLinkTo() {
		return Observable.just(buildPaymentInstrumentsIdentifier(scope));
	}
}
