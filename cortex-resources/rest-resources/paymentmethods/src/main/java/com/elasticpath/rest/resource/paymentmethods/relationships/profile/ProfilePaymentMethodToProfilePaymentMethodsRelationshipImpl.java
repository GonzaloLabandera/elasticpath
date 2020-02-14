/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.rest.resource.paymentmethods.relationships.profile;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.definition.paymentmethods.ProfilePaymentMethodIdentifier;
import com.elasticpath.rest.definition.paymentmethods.ProfilePaymentMethodsForProfilePaymentMethodRelationship;
import com.elasticpath.rest.definition.paymentmethods.ProfilePaymentMethodsIdentifier;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;

/**
 * Profile Payment Method to Profile Payment Methods link.
 */
public class ProfilePaymentMethodToProfilePaymentMethodsRelationshipImpl implements ProfilePaymentMethodsForProfilePaymentMethodRelationship.LinkTo {

	private final ProfilePaymentMethodIdentifier methodIdentifier;

	/**
	 * Constructor.
	 *
	 * @param methodIdentifier methodIdentifier
	 */
	@Inject
	public ProfilePaymentMethodToProfilePaymentMethodsRelationshipImpl(@RequestIdentifier final ProfilePaymentMethodIdentifier methodIdentifier) {
		this.methodIdentifier = methodIdentifier;
	}

	@Override
	public Observable<ProfilePaymentMethodsIdentifier> onLinkTo() {
		return Observable.just(methodIdentifier.getProfilePaymentMethods());
	}
}
