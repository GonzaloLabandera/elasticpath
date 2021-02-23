/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.orders.billingaddress.relationship;

import javax.inject.Inject;

import io.reactivex.Observable;
import org.apache.commons.lang3.StringUtils;

import com.elasticpath.rest.definition.addresses.AddressFormIdentifier;
import com.elasticpath.rest.definition.addresses.AddressesIdentifier;
import com.elasticpath.rest.definition.orders.BillingaddressFormRelationship;
import com.elasticpath.rest.definition.orders.BillingaddressInfoIdentifier;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.UserSubject;
import com.elasticpath.rest.identity.Subject;
import com.elasticpath.rest.identity.util.SubjectUtil;

/**
 * Billing address info to billing address form link.
 */
public class BillingAddressInfoToFormRelationshipImpl implements BillingaddressFormRelationship.LinkTo {

	private final BillingaddressInfoIdentifier billingaddressInfoIdentifier;
	private final Subject subject;

	/**
	 * Constructor.
	 *
	 * @param subject represents the authenticated Subject
	 * @param billingaddressInfoIdentifier billingaddressInfoIdentifier
	 */
	@Inject
	public BillingAddressInfoToFormRelationshipImpl(@UserSubject final Subject subject,
													@RequestIdentifier final BillingaddressInfoIdentifier billingaddressInfoIdentifier) {
		this.subject = subject;
		this.billingaddressInfoIdentifier = billingaddressInfoIdentifier;
	}

	@Override
	public Observable<AddressFormIdentifier> onLinkTo() {
		final String sharedId = SubjectUtil.getAccountSharedId(subject);
		return StringUtils.isEmpty(sharedId)
				? Observable.just(AddressFormIdentifier.builder()
				.withAddresses(AddressesIdentifier.builder()
						.withScope(billingaddressInfoIdentifier.getOrder().getScope())
						.build())
				.build())
				: Observable.empty();
	}
}
