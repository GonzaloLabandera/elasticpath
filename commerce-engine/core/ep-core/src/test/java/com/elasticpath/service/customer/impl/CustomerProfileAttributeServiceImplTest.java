/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.service.customer.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.common.collect.Sets;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.assertj.core.api.Condition;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.base.common.dto.StructuredErrorMessage;
import com.elasticpath.domain.attribute.Attribute;
import com.elasticpath.domain.attribute.AttributeType;
import com.elasticpath.domain.attribute.CustomerProfileValue;
import com.elasticpath.domain.attribute.impl.AttributeImpl;
import com.elasticpath.domain.attribute.impl.AttributeUsageImpl;
import com.elasticpath.domain.attribute.impl.CustomerProfileValueImpl;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.AttributePolicy;
import com.elasticpath.domain.customer.PolicyKey;
import com.elasticpath.domain.customer.PolicyPermission;
import com.elasticpath.domain.customer.StoreCustomerAttribute;
import com.elasticpath.domain.customer.impl.AttributePolicyImpl;
import com.elasticpath.domain.customer.impl.StoreCustomerAttributeImpl;
import com.elasticpath.domain.store.Store;
import com.elasticpath.persistence.api.FetchGroupLoadTuner;
import com.elasticpath.service.attribute.AttributeService;
import com.elasticpath.service.customer.AttributePolicyService;
import com.elasticpath.service.customer.CustomerProfileAttributeValueRestrictor;
import com.elasticpath.service.customer.StoreCustomerAttributeService;
import com.elasticpath.service.store.StoreService;
import com.elasticpath.validation.service.AttributeValueValidationService;

/**
 * Test class for <code>CustomerProfileAttributeServiceImpl</code>.
 */
@RunWith(MockitoJUnitRunner.class)
public class CustomerProfileAttributeServiceImplTest {

	private static final String STORE = "STORE";
	private static final String ATTRIBUTE_KEY_EDITABLE = "ATTRIBUTE_KEY1";
	private static final String ATTRIBUTE_KEY_RO = "ATTRIBUTE_KEY2";
	private static final String ATTRIBUTE_KEY_DEFAULT = "ATTRIBUTE_KEY3";
	private static final String RESTRICTED_VALUE = "RESTRICTED_VALUE";
	private static final String TEST_VALUE = "TEST_VALUE";
	private static final String ERROR = "error";
	private static final String INCORRECT_LIST_OF_EDITABLE_KEYS = "Incorrect list of editable keys";

	@Mock
	private StoreCustomerAttributeService storeCustomerAttributeService;

	@Mock
	private AttributeService attributeService;

	@Mock
	private AttributePolicyService attributePolicyService;

	@Mock
	private Customer customer;

	@Mock
	private StoreService storeService;

	@Mock
	private FetchGroupLoadTuner fetchGroupLoadTuner;

	@Mock
	private Store store;

	@Mock
	private AttributeValueValidationService attributeValueValidationService;

	@InjectMocks
	private final CustomerProfileAttributeServiceImpl service = new CustomerProfileAttributeServiceImpl();

	private List<Attribute> customerProfileAttributes;

	private final List<String> editableKeys = Lists.newArrayList(ATTRIBUTE_KEY_EDITABLE, ATTRIBUTE_KEY_DEFAULT);

	private final List<String> nonEditableKeys = Lists.newArrayList(ATTRIBUTE_KEY_RO);

	private final Map<String, CustomerProfileValue> profileValueMap = Maps.newHashMap();

	private final CustomerProfileAttributeValueRestrictor testRestrictor = context -> Sets.newHashSet(RESTRICTED_VALUE);

	private final Map<String, CustomerProfileAttributeValueRestrictor> valueRestrictors = Maps.newHashMap();

	private final Map<String, PolicyKey> predefinedProfileAttributePolicies = Maps.newHashMap();

	@Captor
	ArgumentCaptor<Map<String, String>> attributeValueMapCaptor;

	@Captor
	ArgumentCaptor<Map<Attribute, Set<String>>> restrictedValuesCaptor;

	@Before
	public void setup() {
		when(storeCustomerAttributeService.findByStore(STORE))
				.thenReturn(setupStoreCustomerAttributes());

		customerProfileAttributes = setupCustomerProfileAttributes();

		when(attributeService.getCustomerProfileAttributeKeys(AttributeUsageImpl.USER_PROFILE_USAGE))
				.thenReturn(customerProfileAttributes.stream().map(attr -> attr.getKey()).collect(Collectors.toSet()));

		when(attributeService.getCustomerProfileAttributesMap(AttributeUsageImpl.USER_PROFILE_USAGE))
				.thenReturn(customerProfileAttributes.stream()
						.collect(Collectors.toMap(attribute -> attribute.getKey(), Function.identity())));

		when(attributePolicyService.findAll())
				.thenReturn(Lists.newArrayList(
						setupAttributePolicy(PolicyKey.DEFAULT, PolicyPermission.EDIT),
						setupAttributePolicy(PolicyKey.DEFAULT, PolicyPermission.EMIT),
						setupAttributePolicy(PolicyKey.READ_ONLY, PolicyPermission.EMIT)));

		profileValueMap.put(ATTRIBUTE_KEY_EDITABLE, new CustomerProfileValueImpl());

		when(customer.getProfileValueMap()).thenReturn(profileValueMap);

		service.setValueRestrictors(valueRestrictors);
		when(storeService.getTunedStore(STORE, fetchGroupLoadTuner))
				.thenReturn(store);

		Collection<Long> storeUids = Lists.newArrayList(1L);

		when(store.getAssociatedStoreUids())
				.thenReturn(storeUids);

		when(storeService.getTunedStores(storeUids, fetchGroupLoadTuner))
				.thenReturn(Lists.newArrayList(store));

		valueRestrictors.put(ATTRIBUTE_KEY_EDITABLE, testRestrictor);

		service.setPredefinedProfileAttributePolicies(predefinedProfileAttributePolicies);

	}

	@Test
	public void testGetCustomerEditableAttributeKeys() {

		assertThat(service.getCustomerEditableAttributeKeys(STORE))
				.as(INCORRECT_LIST_OF_EDITABLE_KEYS)
				.hasSize(2)
				.have(new Condition<>(editableKeys::contains, "editable keys"));
	}

	@Test
	public void testGetCustomerEditableAttribute() {

		Map<String, Optional<CustomerProfileValue>> editableAttributes = service.getCustomerEditableAttributes(STORE, customer);

		assertThat(editableAttributes.keySet())
				.as(INCORRECT_LIST_OF_EDITABLE_KEYS)
				.hasSize(2)
				.have(new Condition<>(editableKeys::contains, "editable keys"));

		assertThat(editableAttributes.get(ATTRIBUTE_KEY_EDITABLE).isPresent())
				.as("Missing value")
				.isTrue();

		assertThat(editableAttributes.get(ATTRIBUTE_KEY_DEFAULT).isPresent())
				.as("Expected null value is not null")
				.isFalse();

	}

	@Test
	public void testGetCustomerReadOnlyAttributes() {

		Map<String, Optional<CustomerProfileValue>> editableAttributes = service.getCustomerReadOnlyAttributes(STORE, customer);

		assertThat(editableAttributes.keySet())
				.as(INCORRECT_LIST_OF_EDITABLE_KEYS)
				.hasSize(1)
				.have(new Condition<>(nonEditableKeys::contains, "non-editable keys"));

		assertThat(editableAttributes.get(ATTRIBUTE_KEY_RO).isPresent())
				.as("Expected null value is not null")
				.isFalse();
	}

	@Test
	public void testGetCustomerReadOnlyAttributesWithPredefinedOverride() {

		predefinedProfileAttributePolicies.put(ATTRIBUTE_KEY_EDITABLE, PolicyKey.READ_ONLY);

		Map<String, Optional<CustomerProfileValue>> editableAttributes = service.getCustomerEditableAttributes(STORE, customer);

		assertThat(editableAttributes.keySet())
				.as(INCORRECT_LIST_OF_EDITABLE_KEYS)
				.hasSize(1)
				.have(new Condition<>(ATTRIBUTE_KEY_DEFAULT::equals, "editable keys"));

		}

	@Test
	public void testValidateAttributesWithNoErrors() {

		Map<String, String> attributeValueMap = Maps.newHashMap();

		attributeValueMap.put(ATTRIBUTE_KEY_EDITABLE, RESTRICTED_VALUE);
		attributeValueMap.put(ATTRIBUTE_KEY_DEFAULT, TEST_VALUE);

		when(attributeValueValidationService.validate(anyMap(), anyMap()))
				.thenReturn(Collections.emptyList());

		assertThat(service.validateAttributes(attributeValueMap, STORE, AttributeUsageImpl.USER_PROFILE_USAGE))
				.as("unexpected errors were encountered")
				.isEmpty();

		verify(attributeValueValidationService).validate(attributeValueMapCaptor.capture(), restrictedValuesCaptor.capture());
		assertThat(attributeValueMapCaptor.getValue())
				.as("attribute value map modified")
				.isEqualTo(attributeValueMap);

		assertThat(restrictedValuesCaptor.getValue().get(customerProfileAttributes.get(0)))
				.as("restricted value map entry incorrect")
				.isNotNull()
				.isEqualTo(Sets.newHashSet(RESTRICTED_VALUE));

		assertThat(restrictedValuesCaptor.getValue().get(customerProfileAttributes.get(1)))
				.as("restricted value map entry incorrect")
				.isNotNull()
				.isEqualTo(Collections.emptySet());

	}

	@Test
	public void testValidateAttributesWithErrors() {

		Map<String, String> attributeValueMap = Maps.newHashMap();

		attributeValueMap.put(ATTRIBUTE_KEY_EDITABLE, RESTRICTED_VALUE);
		attributeValueMap.put(ATTRIBUTE_KEY_DEFAULT, TEST_VALUE);

		when(attributeValueValidationService.validate(anyMap(), anyMap()))
				.thenReturn(Lists.newArrayList(new StructuredErrorMessage(ERROR, ERROR, Collections.emptyMap())));

		assertThat(service.validateAttributes(attributeValueMap, STORE, AttributeUsageImpl.USER_PROFILE_USAGE))
				.as("expected errors were not encountered")
				.isNotEmpty();
	}

	private List<StoreCustomerAttribute> setupStoreCustomerAttributes() {
		return Lists.newArrayList(
				setupStoreCustomerAttribute(ATTRIBUTE_KEY_EDITABLE, PolicyKey.DEFAULT),
				setupStoreCustomerAttribute(ATTRIBUTE_KEY_RO, PolicyKey.READ_ONLY));
	}

	private StoreCustomerAttribute setupStoreCustomerAttribute(final String attributeKey, final PolicyKey policyKey) {
		StoreCustomerAttribute attribute = new StoreCustomerAttributeImpl();
		attribute.setAttributeKey(attributeKey);
		attribute.setPolicyKey(policyKey);
		return attribute;
	}

	private List<Attribute> setupCustomerProfileAttributes() {
		return Lists.newArrayList(
				setupAttribute(ATTRIBUTE_KEY_EDITABLE, AttributeType.SHORT_TEXT),
				setupAttribute(ATTRIBUTE_KEY_DEFAULT, AttributeType.SHORT_TEXT),
				setupAttribute(ATTRIBUTE_KEY_RO, AttributeType.SHORT_TEXT));

	}

	private Attribute setupAttribute(final String attributeKey, final AttributeType type) {
		Attribute attribute = new AttributeImpl();
		attribute.setKey(attributeKey);
		attribute.setAttributeType(type);
		return attribute;
	}

	private AttributePolicy setupAttributePolicy(final PolicyKey key, final PolicyPermission permission) {
		AttributePolicy policy = new AttributePolicyImpl();
		policy.setPolicyKey(key);
		policy.setPolicyPermission(permission);
		return policy;
	}

}
