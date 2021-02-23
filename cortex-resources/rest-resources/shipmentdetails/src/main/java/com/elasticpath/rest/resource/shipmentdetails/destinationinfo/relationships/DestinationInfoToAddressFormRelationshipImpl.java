/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.shipmentdetails.destinationinfo.relationships;

import javax.inject.Inject;

import io.reactivex.Observable;
import org.apache.commons.lang3.StringUtils;

import com.elasticpath.rest.definition.addresses.AddressFormIdentifier;
import com.elasticpath.rest.definition.addresses.AddressesIdentifier;
import com.elasticpath.rest.definition.shipmentdetails.ShippingAddressFormForOrderRelationship;
import com.elasticpath.rest.helix.data.annotation.UriPart;
import com.elasticpath.rest.helix.data.annotation.UserSubject;
import com.elasticpath.rest.id.IdentifierPart;
import com.elasticpath.rest.identity.Subject;
import com.elasticpath.rest.identity.util.SubjectUtil;

/**
 * Adds an address form link in destination info.
 */
public class DestinationInfoToAddressFormRelationshipImpl implements ShippingAddressFormForOrderRelationship.LinkTo {

	private final IdentifierPart<String> scope;
	private final Subject subject;

	/**
	 * Constructor.
	 *
	 * @param subject represents the authenticated Subject
	 * @param scope   scope
	 */
	@Inject
	public DestinationInfoToAddressFormRelationshipImpl(@UserSubject final Subject subject,
														@UriPart(AddressesIdentifier.SCOPE) final IdentifierPart<String> scope) {
		this.subject = subject;
		this.scope = scope;
	}

	@Override
	public Observable<AddressFormIdentifier> onLinkTo() {
		final String sharedId = SubjectUtil.getAccountSharedId(subject);
		AddressesIdentifier addressesIdentifier = AddressesIdentifier.builder()
				.withScope(scope)
				.build();
		return StringUtils.isEmpty(sharedId)
				? Observable.just(AddressFormIdentifier.builder()
				.withAddresses(addressesIdentifier)
				.build())
				: Observable.empty();
	}
}
