/**
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.customer.impl;

import java.util.Map;
import java.util.Optional;

import io.reactivex.Single;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elasticpath.domain.attribute.CustomerProfileValue;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.repository.Repository;
import com.elasticpath.rest.cache.CacheResult;
import com.elasticpath.rest.definition.profiles.AttributesIdentifier;
import com.elasticpath.rest.definition.profiles.ProfileAttributesEntity;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.CustomerRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.ProfileAttributeFieldTransformer;
import com.elasticpath.rest.resource.integration.epcommerce.transform.CustomerProfileValueTransformer;
import com.elasticpath.service.customer.CustomerProfileAttributeService;

/**
 * Profile Attributes Entity Repository.
 *
 * @param <E> the entity type
 * @param <I> the identifier type
 */
@Component
public class ProfileAttributesEntityRepositoryImpl<E extends ProfileAttributesEntity, I extends AttributesIdentifier>
		implements Repository<ProfileAttributesEntity, AttributesIdentifier> {

	@Reference
	private CustomerRepository customerRepository;

	@Reference
	private CustomerProfileAttributeService customerProfileAttributeService;

	@Reference
	private ProfileAttributeFieldTransformer profileAttributeFieldTransformer;

	@Reference
	private CustomerProfileValueTransformer customerProfileValueTransformer;

	@Override
	@CacheResult
	public Single<ProfileAttributesEntity> findOne(final AttributesIdentifier attributeIdentifier) {
		String profileId = attributeIdentifier.getProfile().getProfileId().getValue();
		String scope = attributeIdentifier.getProfile().getScope().getValue();
		return customerRepository.getCustomer(profileId)
				.map(customer -> this.convertCustomerToProfileAttributesEntity(scope, customer));
	}

	/**
	 * Converts given Customer to ProfileEntity.
	 *
	 * @param scope the scope to lookup attributes
	 * @param customer customer to convert
	 * @return the converted profile entity
	 */
	protected ProfileAttributesEntity convertCustomerToProfileAttributesEntity(final String scope, final Customer customer) {
		final Map<String, Optional<CustomerProfileValue>> attributeValueMap =
				customerProfileAttributeService.getCustomerReadOnlyAttributes(scope, customer);

		final ProfileAttributesEntity.Builder builder = ProfileAttributesEntity.builder();
		attributeValueMap.keySet().forEach(key -> addProperty(builder, key, attributeValueMap));
		return builder.build();
	}

	private void addProperty(final ProfileAttributesEntity.Builder builder, final String key,
							 final Map<String, Optional<CustomerProfileValue>> attributeValueMap) {
		String value = null;
		Optional<CustomerProfileValue> optionalCustomerProfileValue = attributeValueMap.get(key);

		if (optionalCustomerProfileValue.isPresent()) {
			value = customerProfileValueTransformer.transformToString(optionalCustomerProfileValue.get());
		}

		builder.addingProperty(profileAttributeFieldTransformer.transformToFieldName(key), value);
	}

	@Reference
	public void setCustomerRepository(final CustomerRepository customerRepository) {
		this.customerRepository = customerRepository;
	}
}
