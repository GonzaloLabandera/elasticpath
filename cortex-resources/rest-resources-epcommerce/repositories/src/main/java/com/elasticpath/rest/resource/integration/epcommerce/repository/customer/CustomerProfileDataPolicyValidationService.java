/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.customer;

import io.reactivex.Observable;

import com.elasticpath.rest.advise.LinkedMessage;
import com.elasticpath.rest.definition.datapolicies.DataPolicyConsentFormIdentifier;
import com.elasticpath.rest.id.IdentifierPart;

/**
 * Customer profile data policy validation service.
 */
public interface CustomerProfileDataPolicyValidationService {
	/**
	 * Checks if the user has granted consent for data policies relevant to the given scope
	 * and which contain data points with the given data point key.
	 * <p>
	 * This method can be used for advisors to validate against the desired advisor-triggering data point keys.
	 * For example, if an advisor should be triggered when a customer shopping in the "MOBEE" scope has not consented
	 * to a data policy containing a data point with the data point key value of "CP_PHONE", then those
	 * values should be passed to the "validate(...)" method.
	 *
	 * @param scope         scope
	 * @return linked messages for unaccepted data policies
	 */
	Observable<LinkedMessage<DataPolicyConsentFormIdentifier>> validate(IdentifierPart<String> scope);
}
