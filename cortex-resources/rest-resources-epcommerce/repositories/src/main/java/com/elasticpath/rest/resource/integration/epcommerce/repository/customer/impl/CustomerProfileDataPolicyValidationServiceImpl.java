/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.customer.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.Sets;
import io.reactivex.Observable;
import io.reactivex.Single;
import org.apache.commons.collections.CollectionUtils;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elasticpath.domain.datapolicy.ConsentAction;
import com.elasticpath.domain.datapolicy.DataPoint;
import com.elasticpath.domain.datapolicy.DataPolicy;
import com.elasticpath.rest.advise.LinkedMessage;
import com.elasticpath.rest.definition.datapolicies.DataPoliciesIdentifier;
import com.elasticpath.rest.definition.datapolicies.DataPolicyConsentFormIdentifier;
import com.elasticpath.rest.definition.datapolicies.DataPolicyIdentifier;
import com.elasticpath.rest.id.IdentifierPart;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.resource.ResourceOperationContext;
import com.elasticpath.rest.resource.StructuredErrorMessageIdConstants;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.CustomerProfileDataPolicyValidationService;
import com.elasticpath.rest.resource.integration.epcommerce.repository.datapolicies.DataPolicyRepository;
import com.elasticpath.rest.schema.StructuredMessageTypes;
import com.elasticpath.service.attribute.AttributeService;

/**
 * Implementation {@link CustomerProfileDataPolicyValidationService}.
 */
@Component(service = CustomerProfileDataPolicyValidationService.class)
public class CustomerProfileDataPolicyValidationServiceImpl implements CustomerProfileDataPolicyValidationService {
	/**
	 * Data policy name field for linked messages.
	 */
	static final String POLICY_NAME_FIELD = "policy-name";
	/**
	 * Data policy reference key field for linked messages.
	 */
	static final String POLICY_REFERENCE_KEY_FIELD = "policy-reference-key";

	private DataPolicyRepository dataPolicyRepository;
	private ResourceOperationContext resourceOperationContext;
	private AttributeService attributeService;

	@Override
	public Observable<LinkedMessage<DataPolicyConsentFormIdentifier>> validate(final IdentifierPart<String> scope) {
		String customerGuid = resourceOperationContext.getUserIdentifier();
		List<String> segmentHeaders = dataPolicyRepository.getSegmentHeadersLowerCase(resourceOperationContext.getSubject().getAttributes());

		return filterActiveDataPolicies(customerGuid, segmentHeaders, scope.getValue(),
				getProfileAttributeKeys().blockingGet())
				.map(dataPolicy -> buildLinkedMessage(dataPolicy, scope));
	}

	private Observable<DataPolicy> filterActiveDataPolicies(final String customerGuid, final List<String> segmentHeaders, final String scope,
															final Set<String> dataPointKeys) {
		Iterable<DataPolicy> dataPolicies = dataPolicyRepository.findActiveDataPoliciesForSegmentsAndStore(segmentHeaders, scope)
				.filter(dataPolicy -> CollectionUtils.containsAny(dataPolicy.getDataPoints().stream()
						.map(DataPoint::getDataKey)
						.collect(Collectors.toSet()), dataPointKeys)).blockingIterable();

		final Set<DataPolicy> filteredPolicies = Sets.newHashSet(dataPolicies);

		final boolean hasAtLeastOneGrantedConsent = filteredPolicies.stream()
				.filter(dataPolicy -> !dataPolicyRepository.findCustomerConsentByDataPolicyGuidForCustomer(dataPolicy.getGuid(), customerGuid)
						.isEmpty().blockingGet())
				.anyMatch(dataPolicy -> dataPolicyRepository.findCustomerConsentByDataPolicyGuidForCustomer(dataPolicy.getGuid(), customerGuid)
						.toSingle().blockingGet()
						.getAction().equals(ConsentAction.GRANTED));

		if (hasAtLeastOneGrantedConsent) {
			return Observable.empty();
		}

		return Observable.fromIterable(filteredPolicies);
	}

	@Reference
	public void setDataPolicyRepository(final DataPolicyRepository dataPolicyRepository) {
		this.dataPolicyRepository = dataPolicyRepository;
	}

	@Reference
	public void setAttributeService(final AttributeService attributeService) {
		this.attributeService = attributeService;
	}

	@Reference
	public void setResourceOperationContext(final ResourceOperationContext resourceOperationContext) {
		this.resourceOperationContext = resourceOperationContext;
	}

	private LinkedMessage<DataPolicyConsentFormIdentifier> buildLinkedMessage(final DataPolicy dataPolicy,
																			  final IdentifierPart<String> scope) {

		String debugMessage = String.format("Need user consent for data policy '%s'", dataPolicy.getPolicyName());

		Map<String, String> data = new HashMap<>();
		data.put(POLICY_NAME_FIELD, dataPolicy.getPolicyName());
		data.put(POLICY_REFERENCE_KEY_FIELD, dataPolicy.getReferenceKey());

		return LinkedMessage.<DataPolicyConsentFormIdentifier>builder()
				.withType(StructuredMessageTypes.NEEDINFO)
				.withId(StructuredErrorMessageIdConstants.NEED_DATA_POLICY_CONSENT)
				.withDebugMessage(debugMessage)
				.withData(data)
				.withLinkedIdentifier(buildDataPolicyConsentFormIdentifier(dataPolicy, scope))
				.build();
	}

	private DataPolicyIdentifier buildDataPolicyIdentifier(final DataPolicy dataPolicy, final IdentifierPart<String> scope) {
		return DataPolicyIdentifier.builder()
				.withPolicyId(StringIdentifier.of(dataPolicy.getGuid()))
				.withDataPolicies(buildDataPoliciesIdentifier(scope))
				.build();
	}

	private DataPoliciesIdentifier buildDataPoliciesIdentifier(final IdentifierPart<String> scope) {
		return DataPoliciesIdentifier.builder().withScope(scope).build();
	}

	private DataPolicyConsentFormIdentifier buildDataPolicyConsentFormIdentifier(final DataPolicy dataPolicy, final IdentifierPart<String> scope) {
		return DataPolicyConsentFormIdentifier.builder()
				.withDataPolicy(buildDataPolicyIdentifier(dataPolicy, scope))
				.build();
	}

	private Single<Set<String>> getProfileAttributeKeys() {
		return Single.just(attributeService.getCustomerProfileAttributeKeys());
	}

}
