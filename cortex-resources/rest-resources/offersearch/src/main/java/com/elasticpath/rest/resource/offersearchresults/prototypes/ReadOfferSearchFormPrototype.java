/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */

package com.elasticpath.rest.resource.offersearchresults.prototypes;

import javax.inject.Inject;

import io.reactivex.Single;

import com.elasticpath.repository.Repository;
import com.elasticpath.rest.definition.offersearches.OfferSearchFormIdentifier;
import com.elasticpath.rest.definition.offersearches.OfferSearchFormResource;
import com.elasticpath.rest.definition.offersearches.SearchOfferEntity;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;

/**
 * Read prototype for offer search form resource.
 */
public class ReadOfferSearchFormPrototype implements OfferSearchFormResource.Read {

	private final OfferSearchFormIdentifier searchFormIdentifier;
	private final Repository<SearchOfferEntity, OfferSearchFormIdentifier> repository;

	/**
	 * Constructor.
	 *
	 * @param searchFormIdentifier SearchFormIdentifier
	 * @param repository           Repository
	 */
	@Inject
	public ReadOfferSearchFormPrototype(@RequestIdentifier final OfferSearchFormIdentifier searchFormIdentifier,
										@ResourceRepository final Repository<SearchOfferEntity, OfferSearchFormIdentifier> repository) {
		this.searchFormIdentifier = searchFormIdentifier;
		this.repository = repository;
	}

	@Override
	public Single<SearchOfferEntity> onRead() {
		return repository.findOne(searchFormIdentifier);
	}
}