/**
 * Copyright (c) Elastic Path Software Inc., 2016
 */
package com.elasticpath.domain.geoip.provider.impl;

import com.elasticpath.domain.geoip.provider.GeoIpProvider;


/**
 * Implementation of {@link GeoIpProvider}.
 */
public abstract class AbstractGeoIpProviderImpl implements GeoIpProvider {
	
	private String name;
	private String descriptioin;
	
	/**
	 * Returns provider description.
	 *
	 * @return provider description
	 */
	@Override
	public String getDescription() {
		return descriptioin;
	}

	/**
	 * Returns provider name.
	 *
	 * @return provider name
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * Sets provider description.
	 *
	 * @param description -  provider description
	 */
	@Override
	public void setDescription(final String description) {
		this.descriptioin = description;
	}

	/**
	 * Sets provider name.
	 *
	 * @param name -  provider name
	 */
	@Override
	public void setName(final String name) {
		this.name = name;
	}

}
