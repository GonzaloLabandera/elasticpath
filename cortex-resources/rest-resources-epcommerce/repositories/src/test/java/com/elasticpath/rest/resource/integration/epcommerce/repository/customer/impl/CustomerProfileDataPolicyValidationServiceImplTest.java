/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */

package com.elasticpath.rest.resource.integration.epcommerce.repository.customer.impl;

import static com.elasticpath.rest.resource.integration.epcommerce.repository.customer.impl.CustomerProfileDataPolicyValidationServiceImpl
		.POLICY_NAME_FIELD;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.customer.impl.CustomerProfileDataPolicyValidationServiceImpl
		.POLICY_REFERENCE_KEY_FIELD;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.reactivex.Maybe;
import io.reactivex.Observable;

import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.datapolicy.ConsentAction;
import com.elasticpath.domain.datapolicy.CustomerConsent;
import com.elasticpath.domain.datapolicy.DataPoint;
import com.elasticpath.domain.datapolicy.DataPolicy;
import com.elasticpath.domain.datapolicy.DataPolicyState;
import com.elasticpath.rest.advise.LinkedMessage;
import com.elasticpath.rest.definition.datapolicies.DataPoliciesIdentifier;
import com.elasticpath.rest.definition.datapolicies.DataPolicyConsentFormIdentifier;
import com.elasticpath.rest.definition.datapolicies.DataPolicyIdentifier;
import com.elasticpath.rest.id.IdentifierPart;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.identity.attribute.SubjectAttribute;
import com.elasticpath.rest.resource.ResourceOperationContext;
import com.elasticpath.rest.resource.StructuredErrorMessageIdConstants;
import com.elasticpath.rest.resource.integration.epcommerce.repository.datapolicies.DataPolicyRepository;
import com.elasticpath.rest.schema.StructuredMessageTypes;
import com.elasticpath.service.attribute.AttributeService;

@RunWith(MockitoJUnitRunner.class)
public class CustomerProfileDataPolicyValidationServiceImplTest {

	private static final String USER_ID = "userId";
	private static final String DATA_POLICY_NAME = "EU_Data_Policy";
	private static final StringIdentifier SCOPE = StringIdentifier.of("SCOPE");

	private static final String REQUIRED_REGISTRATION_PERSONAL_DETAILS = "Required Registration Personal Details";
	private static final String POLICY_GUID_1 = "policy_guid_1";
	static final String KEY_1 = "Reference_key_1";

	private static final String FIRST_NAME = "FIRST_NAME";
	private static final String CP_FIRST_NAME = "CP_FIRST_NAME";
	private static final String CP_LAST_NAME = "CP_LAST_NAME";
	private static final String CP_EMAIL = "CP_EMAIL";

	private static final Set<String> DATA_POINT_KEYS = new HashSet<>(Arrays.asList(CP_EMAIL, CP_FIRST_NAME, CP_LAST_NAME));

	private DataPolicy dataPolicy1;
	private DataPolicy dataPolicy2;

	@Mock
	private CustomerConsent customerConsent;

	@Mock
	private SubjectAttribute subjectAttribute;

	@Mock(answer = Answers.RETURNS_DEEP_STUBS)
	private ResourceOperationContext resourceOperationContext;

	@Mock
	private DataPolicyRepository dataPolicyRepository;

	@Mock
	private AttributeService attributeService;

	@InjectMocks
	private CustomerProfileDataPolicyValidationServiceImpl validationService;

	@Before
	public void setUp() {
		doReturn(USER_ID).when(resourceOperationContext).getUserIdentifier();

		when(resourceOperationContext.getSubject().getAttributes()).thenReturn(Collections.singletonList(subjectAttribute));

		DataPoint dataPoint1 = createMockDataPoint(FIRST_NAME);
		DataPoint dataPoint2 = createMockDataPoint(CP_FIRST_NAME);
		DataPoint dataPoint3 = createMockDataPoint(CP_LAST_NAME);

		dataPolicy1 = createMockDataPolicy(REQUIRED_REGISTRATION_PERSONAL_DETAILS, POLICY_GUID_1, KEY_1,
				Arrays.asList(dataPoint1, dataPoint2, dataPoint3));
		dataPolicy2 = createMockDataPolicy(REQUIRED_REGISTRATION_PERSONAL_DETAILS, POLICY_GUID_1, KEY_1,
				Arrays.asList(dataPoint1, dataPoint2, dataPoint3));

		when(attributeService.getCustomerProfileAttributeKeys()).thenReturn(DATA_POINT_KEYS);

	}

	@Test
	public void validateTestNoMessage() {

		List<String> headers = Collections.singletonList(DATA_POLICY_NAME);
		doReturn(headers).when(dataPolicyRepository).getSegmentHeadersLowerCase(Collections.singletonList(subjectAttribute));
		doReturn(Observable.just(dataPolicy1)).when(dataPolicyRepository).findActiveDataPoliciesForSegmentsAndStore(headers, SCOPE.getValue());

		when(customerConsent.getAction()).thenReturn(ConsentAction.GRANTED);
		when(dataPolicyRepository.findCustomerConsentByDataPolicyGuidForCustomer(dataPolicy1.getGuid(), USER_ID)).
				thenReturn(Maybe.just(customerConsent));

		validationService.validate(SCOPE)
				.test()
				.assertNoErrors()
				.assertNoValues();
	}

	@Test
	public void validateTestHasConsent() {

		List<String> headers = Collections.singletonList(DATA_POLICY_NAME);
		when(dataPolicyRepository.getSegmentHeadersLowerCase(Collections.singletonList(subjectAttribute)))
				.thenReturn(headers);
		when(dataPolicyRepository.findActiveDataPoliciesForSegmentsAndStore(headers, SCOPE.getValue()))
				.thenReturn(Observable.just(dataPolicy1, dataPolicy2));

		doReturn(Maybe.empty()).when(dataPolicyRepository).findCustomerConsentByDataPolicyGuidForCustomer(anyString(), anyString());

		validationService.validate(SCOPE)
				.test()
				.assertNoErrors()
				.assertValueCount(2)
				.assertValueAt(0, this::validateLinkedMessage)
				.assertValueAt(1, this::validateLinkedMessage);
	}

	@Test
	public void validateTestHasRevokedConsent() {

		List<String> headers = Collections.singletonList(DATA_POLICY_NAME);
		when(dataPolicyRepository.getSegmentHeadersLowerCase(Collections.singletonList(subjectAttribute)))
				.thenReturn(headers);
		when(dataPolicyRepository.findActiveDataPoliciesForSegmentsAndStore(headers, SCOPE.getValue()))
				.thenReturn(Observable.just(dataPolicy1, dataPolicy2));

		when(customerConsent.getAction()).thenReturn(ConsentAction.REVOKED);
		doReturn(Maybe.just(customerConsent)).when(dataPolicyRepository).findCustomerConsentByDataPolicyGuidForCustomer(anyString(), anyString());

		validationService.validate(SCOPE)
				.test()
				.assertNoErrors()
				.assertValueCount(2)
				.assertValueAt(0, this::validateLinkedMessage)
				.assertValueAt(1, this::validateLinkedMessage);
	}

	private boolean validateLinkedMessage(final LinkedMessage<DataPolicyConsentFormIdentifier> linkedMessage) {
		return linkedMessage.getLinkedIdentifier().get().equals(buildDataPolicyConsentFormIdentifier(dataPolicy1, SCOPE))
				&& linkedMessage.getType().equals(StructuredMessageTypes.NEEDINFO)
				&& linkedMessage.getId().equals(StructuredErrorMessageIdConstants.NEED_DATA_POLICY_CONSENT)
				&& linkedMessage.getDebugMessage().contains("Need user consent for data policy")
				&& linkedMessage.getData().containsKey(POLICY_NAME_FIELD)
				&& linkedMessage.getData().containsKey(POLICY_REFERENCE_KEY_FIELD);
	}

	private DataPoliciesIdentifier buildDataPoliciesIdentifier(final IdentifierPart<String> scope) {
		return DataPoliciesIdentifier.builder().withScope(scope).build();
	}

	private DataPolicyIdentifier buildDataPolicyIdentifier(final DataPolicy dataPolicy, final IdentifierPart<String> scope) {
		return DataPolicyIdentifier.builder()
				.withPolicyId(StringIdentifier.of(dataPolicy.getGuid()))
				.withDataPolicies(buildDataPoliciesIdentifier(scope))
				.build();
	}

	private DataPolicyConsentFormIdentifier buildDataPolicyConsentFormIdentifier(final DataPolicy dataPolicy, final IdentifierPart<String> scope) {
		return DataPolicyConsentFormIdentifier.builder()
				.withDataPolicy(buildDataPolicyIdentifier(dataPolicy, scope))
				.build();
	}

	private DataPolicy createMockDataPolicy(final String policyName, final String guid, final String referenceKey,
			final List<DataPoint> dataPoints) {
		DataPolicy dataPolicy = mock(DataPolicy.class, RETURNS_DEEP_STUBS);
		when(dataPolicy.getState().getOrdinal()).thenReturn(DataPolicyState.ACTIVE_ORDINAL);
		when(dataPolicy.getPolicyName()).thenReturn(policyName);
		when(dataPolicy.getStartDate().before(any(Date.class))).thenReturn(true);
		when(dataPolicy.getGuid()).thenReturn(guid);
		when(dataPolicy.getReferenceKey()).thenReturn(referenceKey);
		when(dataPolicy.getDataPoints()).thenReturn(dataPoints);
		return dataPolicy;
	}

	private DataPoint createMockDataPoint(final String dataPointKey) {
		DataPoint dataPoint = mock(DataPoint.class);
		when(dataPoint.getDataKey()).thenReturn(dataPointKey);
		return dataPoint;
	}
}
