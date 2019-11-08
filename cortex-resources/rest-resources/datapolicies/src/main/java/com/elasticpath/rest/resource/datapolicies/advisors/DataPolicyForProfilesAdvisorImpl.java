/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.datapolicies.advisors;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.advise.LinkedMessage;
import com.elasticpath.rest.definition.datapolicies.DataPolicyConsentFormIdentifier;
import com.elasticpath.rest.definition.datapolicies.DataPolicyForProfilesAdvisor;
import com.elasticpath.rest.definition.profiles.ProfileIdentifier;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceService;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.CustomerProfileDataPolicyValidationService;

/**
 * Data policy advisor on profile details resource.
 */
public class DataPolicyForProfilesAdvisorImpl implements DataPolicyForProfilesAdvisor.UpdateLinkedAdvisor {

	private final ProfileIdentifier profileIdentifier;

	private final CustomerProfileDataPolicyValidationService validationService;

	/**
	 * Constructor.
	 *
	 * @param profileIdentifier profile identifier
	 * @param validationService validation service
	 */
	@Inject
	public DataPolicyForProfilesAdvisorImpl(@RequestIdentifier final ProfileIdentifier profileIdentifier,
												  @ResourceService final CustomerProfileDataPolicyValidationService validationService) {
		this.profileIdentifier = profileIdentifier;
		this.validationService = validationService;
	}

	@Override
	public Observable<LinkedMessage<DataPolicyConsentFormIdentifier>> onLinkedAdvise() {
		return validationService.validate(profileIdentifier.getScope());
	}
}