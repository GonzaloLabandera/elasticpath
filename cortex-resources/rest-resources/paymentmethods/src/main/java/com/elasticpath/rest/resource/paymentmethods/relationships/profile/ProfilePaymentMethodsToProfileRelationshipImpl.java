/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.paymentmethods.relationships.profile;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.definition.paymentmethods.ProfilePaymentMethodsForProfileRelationship;
import com.elasticpath.rest.definition.paymentmethods.ProfilePaymentMethodsIdentifier;
import com.elasticpath.rest.definition.profiles.ProfileIdentifier;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.UserId;
import com.elasticpath.rest.id.type.StringIdentifier;

/**
 * Profile Payment Methods to profile link.
 */
public class ProfilePaymentMethodsToProfileRelationshipImpl implements ProfilePaymentMethodsForProfileRelationship.LinkFrom {

	private final String userId;

	private final  ProfilePaymentMethodsIdentifier profilePaymentMethodsIdentifier;

	/**
	 * Constructor.
	 *
	 * @param userId userId
	 * @param profilePaymentMethodsIdentifier  scope
	 */
	@Inject
	public ProfilePaymentMethodsToProfileRelationshipImpl(@UserId final String userId,
														  @RequestIdentifier final ProfilePaymentMethodsIdentifier profilePaymentMethodsIdentifier) {

		this.profilePaymentMethodsIdentifier = profilePaymentMethodsIdentifier;
		this.userId = userId;
	}

	@Override
	public Observable<ProfileIdentifier> onLinkFrom() {
		return Observable.just(ProfileIdentifier.builder()
				.withProfileId(StringIdentifier.of(userId))
				.withScope(profilePaymentMethodsIdentifier.getProfile().getScope())
				.build());
	}
}
