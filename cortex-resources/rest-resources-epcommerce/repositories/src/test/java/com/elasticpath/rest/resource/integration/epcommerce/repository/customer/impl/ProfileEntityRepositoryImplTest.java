package com.elasticpath.rest.resource.integration.epcommerce.repository.customer.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.Map;
import java.util.Optional;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import io.reactivex.Completable;
import io.reactivex.Single;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.attribute.Attribute;
import com.elasticpath.domain.attribute.AttributeType;
import com.elasticpath.domain.attribute.CustomerProfileValue;
import com.elasticpath.domain.attribute.impl.AttributeImpl;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.CustomerProfile;
import com.elasticpath.domain.customer.impl.CustomerImpl;
import com.elasticpath.rest.ResourceOperationFailure;
import com.elasticpath.rest.definition.profiles.ProfileEntity;
import com.elasticpath.rest.definition.profiles.ProfileIdentifier;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.CustomerRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.ProfileAttributeFieldTransformer;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.validator.ProfileAttributeValidator;
import com.elasticpath.rest.resource.integration.epcommerce.transform.CustomerProfileValueTransformer;
import com.elasticpath.rest.resource.integration.epcommerce.transform.DateForEditTransformer;
import com.elasticpath.service.attribute.AttributeService;
import com.elasticpath.service.customer.CustomerProfileAttributeService;

@RunWith(MockitoJUnitRunner.class)
public class ProfileEntityRepositoryImplTest {

	private static final String ERROR = "ERROR";
	private static final String GIVEN_NAME = "given-name";
	private static final String FAMILY_NAME = "family-name";
	private static final String DATE_OF_BIRTH = "date-of-birth";
	private static final String DOB = "1999-09-09";
	private static final String GIVEN_NAME_VALUE = "Sage";
	private static final String FAMILY_NAME_VALUE = "Sau";
	private static final Date DOB_DATE = new Date();
	private static final String MY_STORE = "my-store";
	private static final String GUID = "GUID";

	private ProfileEntity profileEntity;

	private ProfileIdentifier profileIdentifier;

	@Mock
	private CustomerRepository customerRepository;

	@Mock
	private Customer customer;

	@Mock
	private CustomerProfile customerProfile;

	@Mock
	private CustomerProfileAttributeService customerProfileAttributeService;

	@Mock
	private ProfileAttributeFieldTransformer profileAttributeFieldTransformer;

	@Mock
	private ProfileAttributeValidator profileAttributeValidator;

	@Mock
	private DateForEditTransformer dateForEditTransformer;

	@Mock
	private CustomerProfileValueTransformer customerProfileValueTransformer;

	@Mock
	private AttributeService attributeService;

	@InjectMocks
	private ProfileEntityRepositoryImpl repository;

	@Mock
	private CustomerProfileValue firstNameCustomerProfileValue;

	@Captor
	private ArgumentCaptor<Map<String, CustomerProfileValue>> profileValueMap;

	private final Map<String, Attribute> attributeMap = Maps.newHashMap();

	private final Map<String, Optional<CustomerProfileValue>> attributeValueMap = Maps.newHashMap();

	@Before
	public void setup() {
		profileEntity = ProfileEntity.builder()
				.addingProperty(GIVEN_NAME, GIVEN_NAME_VALUE)
				.addingProperty(FAMILY_NAME, FAMILY_NAME_VALUE)
				.addingProperty(DATE_OF_BIRTH, DOB)
				.build();
		profileIdentifier = ProfileIdentifier
				.builder()
				.withProfileId(StringIdentifier.of("my-id"))
				.withScope(StringIdentifier.of(MY_STORE))
				.build();

		attributeMap.put(CustomerImpl.ATT_KEY_CP_FIRST_NAME, setupAttribute(CustomerImpl.ATT_KEY_CP_FIRST_NAME, AttributeType.SHORT_TEXT));
		attributeMap.put(CustomerImpl.ATT_KEY_CP_LAST_NAME, setupAttribute(CustomerImpl.ATT_KEY_CP_LAST_NAME, AttributeType.SHORT_TEXT));
		attributeMap.put(CustomerImpl.ATT_KEY_CP_DOB, setupAttribute(CustomerImpl.ATT_KEY_CP_DOB, AttributeType.DATE));

		when(profileAttributeFieldTransformer.transformToAttributeKey(GIVEN_NAME))
				.thenReturn(CustomerImpl.ATT_KEY_CP_FIRST_NAME);
		when(profileAttributeFieldTransformer.transformToAttributeKey(FAMILY_NAME))
				.thenReturn(CustomerImpl.ATT_KEY_CP_LAST_NAME);
		when(profileAttributeFieldTransformer.transformToAttributeKey(DATE_OF_BIRTH))
				.thenReturn(CustomerImpl.ATT_KEY_CP_DOB);
		when(dateForEditTransformer.transformToDomain(AttributeType.DATE, DOB))
				.thenReturn(Optional.of(DOB_DATE));
		when(customerRepository.getCustomer(profileIdentifier.getProfileId().getValue())).thenReturn(Single.just(customer));
		when(customerRepository.updateCustomerAsCompletable(customer))
				.thenReturn(Completable.complete());

		when(attributeService.getCustomerProfileAttributesMap())
				.thenReturn(attributeMap);

		when(customer.getCustomerProfile())
				.thenReturn(customerProfile);

	}

	@Test
	public void shouldFailUpdateProfileWhenValidationFails() {
		when(profileAttributeValidator.validate(profileEntity, profileIdentifier))
				.thenReturn(Completable.error(ResourceOperationFailure.badRequestBody(ERROR, Lists.emptyList())));
		when(customerRepository.getCustomer(profileIdentifier.getProfileId().getValue()))
				.thenReturn(Single.just(customer));
		repository.update(profileEntity, profileIdentifier)
				.test()
				.assertError(ResourceOperationFailure.class);
	}

	@Test
	public void shouldFailUpdateProfileWhenCustomerNotFound() {
		when(profileAttributeValidator.validate(profileEntity, profileIdentifier))
				.thenReturn(Completable.complete());
		when(customerRepository.getCustomer(profileIdentifier.getProfileId().getValue()))
				.thenReturn(Single.error(ResourceOperationFailure.notFound()));
		repository.update(profileEntity, profileIdentifier)
				.test()
				.assertError(ResourceOperationFailure.class);
	}

	@Test
	public void shouldUpdateProfile() {
		when(profileAttributeValidator.validate(profileEntity, profileIdentifier))
				.thenReturn(Completable.complete());
		when(customerProfileAttributeService.getCustomerEditableAttributeKeys(MY_STORE))
				.thenReturn(Sets.newHashSet(
						CustomerImpl.ATT_KEY_CP_DOB,
						CustomerImpl.ATT_KEY_CP_FIRST_NAME,
						CustomerImpl.ATT_KEY_CP_LAST_NAME));

		repository.update(profileEntity, profileIdentifier)
				.test()
				.assertNoErrors();

		verify(customerProfile).setProfileValue(CustomerImpl.ATT_KEY_CP_DOB, DOB_DATE);
		verify(customerProfile).setStringProfileValue(CustomerImpl.ATT_KEY_CP_FIRST_NAME, GIVEN_NAME_VALUE);
		verify(customerProfile).setStringProfileValue(CustomerImpl.ATT_KEY_CP_LAST_NAME, FAMILY_NAME_VALUE);
	}

	@Test
	public void shouldRemoveValues() {
		profileEntity = ProfileEntity.builder()
				.addingProperty(GIVEN_NAME, GIVEN_NAME_VALUE)
				.addingProperty(FAMILY_NAME, FAMILY_NAME_VALUE)
				.addingProperty(DATE_OF_BIRTH, "")
				.build();

		when(customerProfileAttributeService.getCustomerEditableAttributeKeys(MY_STORE))
				.thenReturn(Sets.newHashSet(
						CustomerImpl.ATT_KEY_CP_DOB,
						CustomerImpl.ATT_KEY_CP_FIRST_NAME,
						CustomerImpl.ATT_KEY_CP_LAST_NAME,
						CustomerImpl.ATT_KEY_CP_PHONE));

		when(profileAttributeValidator.validate(profileEntity, profileIdentifier))
				.thenReturn(Completable.complete());

		doNothing().when(customer).setProfileValueMap(profileValueMap.capture());

		repository.update(profileEntity, profileIdentifier)
				.test()
				.assertNoErrors();

		verify(customer).setProfileValueMap(anyMap());
		verify(customerProfile).setStringProfileValue(CustomerImpl.ATT_KEY_CP_FIRST_NAME, GIVEN_NAME_VALUE);
		verify(customerProfile).setStringProfileValue(CustomerImpl.ATT_KEY_CP_LAST_NAME, FAMILY_NAME_VALUE);

		assertThat(profileValueMap.getValue())
				.doesNotContainKeys(CustomerImpl.ATT_KEY_CP_DOB, CustomerImpl.ATT_KEY_CP_PHONE);
	}

	@Test
	public void shouldFailGetProfileWhenCustomerNotFoundError() {
		when(customerRepository.getCustomer(profileIdentifier.getProfileId().getValue()))
				.thenReturn(Single.error(ResourceOperationFailure.notFound()));
		repository.findOne(profileIdentifier)
				.test()
				.assertError(ResourceOperationFailure.class);
	}

	@Test
	public void shouldGetProfile() {
		attributeValueMap.put(CustomerImpl.ATT_KEY_CP_FIRST_NAME, Optional.of(firstNameCustomerProfileValue));

		when(customer.getGuid()).thenReturn(GUID);
		when(customerRepository.getCustomer(profileIdentifier.getProfileId().getValue())).thenReturn(Single.just(customer));
		when(customerProfileAttributeService.getCustomerEditableAttributes(MY_STORE, customer))
				.thenReturn(attributeValueMap);

		when(profileAttributeFieldTransformer.transformToFieldName(CustomerImpl.ATT_KEY_CP_FIRST_NAME))
				.thenReturn(GIVEN_NAME);
		when(customerProfileValueTransformer.transformToString(firstNameCustomerProfileValue))
				.thenReturn(GIVEN_NAME_VALUE);

		repository.findOne(profileIdentifier)
				.test()
				.assertNoErrors();

		verify(profileAttributeFieldTransformer).transformToFieldName(CustomerImpl.ATT_KEY_CP_FIRST_NAME);
		verify(customerProfileValueTransformer).transformToString(firstNameCustomerProfileValue);
	}

	private Attribute setupAttribute(final String key, final AttributeType type) {
		Attribute attribute = new AttributeImpl();
		attribute.setKey(key);
		attribute.setAttributeType(type);
		return attribute;
	}

}