/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.addresses.prototypes;

import io.reactivex.Single;
import org.apache.commons.lang3.StringUtils;

import com.elasticpath.rest.definition.addresses.AccountAddressFormResource;
import com.elasticpath.rest.definition.addresses.AddressEntity;
import com.elasticpath.rest.definition.base.NameEntity;

/**
 * Account Address Form prototype for Read operation.
 */
public class ReadAccountAddressFormPrototype implements AccountAddressFormResource.Read {

	@Override
	public Single<AddressEntity> onRead() {
		com.elasticpath.rest.definition.base.AddressEntity addressEntity = com.elasticpath.rest.definition.base.AddressEntity.builder()
				.withCountryName(StringUtils.EMPTY)
				.withExtendedAddress(StringUtils.EMPTY)
				.withLocality(StringUtils.EMPTY)
				.withPostalCode(StringUtils.EMPTY)
				.withRegion(StringUtils.EMPTY)
				.withStreetAddress(StringUtils.EMPTY)
				.build();

		NameEntity nameEntity = NameEntity.builder()
				.withFamilyName(StringUtils.EMPTY)
				.withGivenName(StringUtils.EMPTY)
				.build();

		return Single.just(AddressEntity.builder()
				.withAddress(addressEntity)
				.withName(nameEntity)
				.withPhoneNumber(StringUtils.EMPTY)
				.withOrganization(StringUtils.EMPTY)
				.build());
	}
}
