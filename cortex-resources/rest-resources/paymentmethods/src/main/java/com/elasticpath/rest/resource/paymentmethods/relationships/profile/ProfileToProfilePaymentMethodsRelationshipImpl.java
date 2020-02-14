/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.paymentmethods.relationships.profile;

import static com.elasticpath.rest.resource.integration.epcommerce.repository.payments.commons.PaymentResourceHelpers.buildProfilePaymentMethodsIdentifier;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.definition.paymentmethods.ProfilePaymentMethodsForProfileRelationship;
import com.elasticpath.rest.definition.paymentmethods.ProfilePaymentMethodsIdentifier;
import com.elasticpath.rest.definition.profiles.ProfileIdentifier;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;
import com.elasticpath.rest.helix.data.annotation.UserId;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.CustomerRepository;

/**
 * Profile to Profile Payment Methods link.
 */
public class ProfileToProfilePaymentMethodsRelationshipImpl implements ProfilePaymentMethodsForProfileRelationship.LinkTo {

	private final String userId;

	private final ProfileIdentifier profileIdentifier;

	private final CustomerRepository customerRepository;

	/**
	 * Constructor.
	 *
	 * @param userId             user ID
	 * @param profileIdentifier  profile identifier
	 * @param customerRepository customer repository
	 */
	@Inject
	public ProfileToProfilePaymentMethodsRelationshipImpl(@UserId final String userId,
														  @RequestIdentifier final ProfileIdentifier profileIdentifier,
														  @ResourceRepository final CustomerRepository customerRepository) {
		this.userId = userId;
		this.profileIdentifier = profileIdentifier;
		this.customerRepository = customerRepository;
	}

	@Override
	public Observable<ProfilePaymentMethodsIdentifier> onLinkTo() {
		return customerRepository.getCustomer(userId)
				.map(customer -> buildProfilePaymentMethodsIdentifier(profileIdentifier))
				.toObservable();
	}
}