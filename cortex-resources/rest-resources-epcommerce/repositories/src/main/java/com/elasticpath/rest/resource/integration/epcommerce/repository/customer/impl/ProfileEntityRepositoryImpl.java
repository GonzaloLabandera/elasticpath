/**
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */

package com.elasticpath.rest.resource.integration.epcommerce.repository.customer.impl;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.Maps;
import io.reactivex.Completable;
import io.reactivex.Single;
import org.apache.commons.lang3.StringUtils;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elasticpath.domain.attribute.Attribute;
import com.elasticpath.domain.attribute.AttributeType;
import com.elasticpath.domain.attribute.CustomerProfileValue;
import com.elasticpath.domain.attribute.impl.AttributeUsageImpl;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.repository.Repository;
import com.elasticpath.rest.cache.CacheResult;
import com.elasticpath.rest.definition.profiles.ProfileEntity;
import com.elasticpath.rest.definition.profiles.ProfileIdentifier;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.CustomerRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.ProfileAttributeFieldTransformer;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.validator.ProfileAttributeValidator;
import com.elasticpath.rest.resource.integration.epcommerce.transform.CustomerProfileValueTransformer;
import com.elasticpath.rest.resource.integration.epcommerce.transform.DateForEditTransformer;
import com.elasticpath.service.attribute.AttributeService;
import com.elasticpath.service.customer.CustomerProfileAttributeService;

/**
 * Profile Entity Repository.
 *
 * @param <E> the entity type
 * @param <I> the identifier type
 */
@Component
public class ProfileEntityRepositoryImpl<E extends ProfileEntity, I extends ProfileIdentifier>
		implements Repository<ProfileEntity, ProfileIdentifier> {

	@Reference
	private CustomerRepository customerRepository;

	@Reference
	private CustomerProfileAttributeService customerProfileAttributeService;

	@Reference
	private ProfileAttributeFieldTransformer profileAttributeFieldTransformer;

	@Reference
	private ProfileAttributeValidator profileAttributeValidator;

	@Reference
	private DateForEditTransformer dateForEditTransformer;

	@Reference
	private CustomerProfileValueTransformer customerProfileValueTransformer;

	@Reference
	private AttributeService attributeService;

	@Override
	public Completable update(final ProfileEntity entity, final ProfileIdentifier profileIdentifier) {
		return profileAttributeValidator.validate(entity, profileIdentifier)
				.andThen(customerRepository.getCustomer(profileIdentifier.getProfileId().getValue())
						.map(customer -> updateCustomerData(entity, profileIdentifier, customer))
						.flatMapCompletable(customerRepository::updateCustomer));
	}

	/**
	 * Updates the customer attributes with the profile entity.
	 *
	 * @param profileEntity     profile entity
	 * @param profileIdentifier the profile identifier
	 * @param customer          customer to update
	 * @return the updated customer
	 */
	protected Customer updateCustomerData(final ProfileEntity profileEntity, final ProfileIdentifier profileIdentifier, final Customer customer) {
		final Map<String, Attribute> attributeMap = attributeService.getCustomerProfileAttributesMap(AttributeUsageImpl.USER_PROFILE_USAGE);

		// replace map keys with internal keys
		Map<String, String> dynamicProperties = Maps.newHashMap();

		profileEntity.getDynamicProperties()
				.forEach((key, value) -> dynamicProperties.put(profileAttributeFieldTransformer.transformToAttributeKey(key),
						value));

		// add or update keys with non-empty values
		Set<String> updateKeys = dynamicProperties.entrySet()
				.stream()
				.filter(entry -> !StringUtils.isEmpty(entry.getValue()))
				.map(Map.Entry::getKey)
				.collect(Collectors.toSet());

		updateKeys.forEach(key ->
				this.updateCustomerAttribute(key, StringUtils.trimToNull(dynamicProperties.get(key)), customer, attributeMap));

		// get set of editable attribute keys that haven't been updated - semantically this implies they should be removed
		List<String> attributeKeysForRemoval =
				customerProfileAttributeService.getCustomerEditableAttributeKeys(profileIdentifier.getScope().getValue())
						.stream()
						.filter(key -> !updateKeys.contains(key))
						.collect(Collectors.toList());

		removeAttributes(attributeKeysForRemoval, customer);

		return customer;
	}

	private void removeAttributes(final List<String> attributeKeysForRemoval, final Customer customer) {
		Map<String, CustomerProfileValue> profileValueMap = Maps.newHashMap(customer.getProfileValueMap());
		attributeKeysForRemoval.forEach(profileValueMap::remove);
		customer.setProfileValueMap(profileValueMap);
	}

	@SuppressWarnings({"PMD.MissingBreakInSwitch", "fallthrough"}) // PMD false positive bug - https://sourceforge.net/p/pmd/bugs/1262
	private void updateCustomerAttribute(final String attributeKey, final String value, final Customer customer,
										 final Map<String, Attribute> attributeMap) {
		final AttributeType attributeType = attributeMap.get(attributeKey).getAttributeType();
		switch (attributeType.getTypeId()) {
			case AttributeType.DATE_TYPE_ID:
			case AttributeType.DATETIME_TYPE_ID:
				customer.getCustomerProfile().setProfileValue(attributeKey,
						dateForEditTransformer.transformToDomain(attributeType, value)
								.orElseThrow(() -> new IllegalArgumentException("Date value is missing.")));
				break;
			default:
				customer.getCustomerProfile().setStringProfileValue(attributeKey, value);
		}
	}

	@Override
	@CacheResult
	public Single<ProfileEntity> findOne(final ProfileIdentifier profileIdentifier) {
		String profileId = profileIdentifier.getProfileId().getValue();
		String scope = profileIdentifier.getScope().getValue();
		return customerRepository.getCustomer(profileId)
				.map(customer -> this.convertCustomerToProfileEntity(scope, customer));
	}

	/**
	 * Converts given Customer to ProfileEntity.
	 *
	 * @param scope    the scope to lookup attributes
	 * @param customer customer to convert
	 * @return the converted profile entity
	 */
	protected ProfileEntity convertCustomerToProfileEntity(final String scope, final Customer customer) {
		final Map<String, Optional<CustomerProfileValue>> attributeValueMap =
				customerProfileAttributeService.getCustomerEditableAttributes(scope, customer);

		final ProfileEntity.Builder builder = ProfileEntity.builder().withProfileId(customer.getGuid());
		attributeValueMap.keySet().forEach(key -> addProperty(builder, key, attributeValueMap));
		return builder.build();
	}

	private void addProperty(final ProfileEntity.Builder builder, final String key,
							 final Map<String, Optional<CustomerProfileValue>> attributeValueMap) {
		String value = null;
		Optional<CustomerProfileValue> optionalCustomerProfileValue = attributeValueMap.get(key);

		if (optionalCustomerProfileValue.isPresent()) {
			value = customerProfileValueTransformer.transformToString(optionalCustomerProfileValue.get());
		}

		builder.addingProperty(profileAttributeFieldTransformer.transformToFieldName(key), value);
	}

}
