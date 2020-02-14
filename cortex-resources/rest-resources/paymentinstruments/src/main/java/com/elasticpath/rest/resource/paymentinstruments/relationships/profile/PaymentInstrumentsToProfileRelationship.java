/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.paymentinstruments.relationships.profile;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.definition.paymentinstruments.PaymentInstrumentsForProfileRelationship;
import com.elasticpath.rest.definition.paymentinstruments.PaymentInstrumentsIdentifier;
import com.elasticpath.rest.definition.profiles.ProfileIdentifier;
import com.elasticpath.rest.helix.data.annotation.UriPart;
import com.elasticpath.rest.helix.data.annotation.UserId;
import com.elasticpath.rest.id.IdentifierPart;
import com.elasticpath.rest.id.type.StringIdentifier;

/**
 * Payment Instruments to profile link.
 */
public class PaymentInstrumentsToProfileRelationship implements PaymentInstrumentsForProfileRelationship.LinkFrom {

	private final String userId;

	private final IdentifierPart<String> scope;

	/**
	 * Constructor.
	 *
	 * @param userId the user id
	 * @param scope  the scope
	 */
	@Inject
	public PaymentInstrumentsToProfileRelationship(@UserId final String userId,
												   @UriPart(PaymentInstrumentsIdentifier.SCOPE) final IdentifierPart<String> scope) {
		this.scope = scope;
		this.userId = userId;
	}

	@Override
	public Observable<ProfileIdentifier> onLinkFrom() {
		return Observable.just(ProfileIdentifier.builder()
				.withProfileId(StringIdentifier.of(userId))
				.withScope(scope)
				.build());
	}
}
