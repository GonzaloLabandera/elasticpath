/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.offers.prototypes;

import java.util.Map;
import javax.inject.Inject;

import io.reactivex.Single;

import com.elasticpath.rest.definition.offers.CodeEntity;
import com.elasticpath.rest.definition.offers.CodeForOfferIdentifier;
import com.elasticpath.rest.definition.offers.CodeForOfferResource;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.resource.integration.epcommerce.repository.search.impl.SearchRepositoryImpl;

/**
 * Read prototype for code for offer resource.
 */
public final class ReadCodeForOfferPrototype implements CodeForOfferResource.Read {
	private final CodeForOfferIdentifier identifier;

	/**
	 * Constructor.
	 *
	 * @param identifier	identifier
	 */
	@Inject
	public ReadCodeForOfferPrototype(@RequestIdentifier final CodeForOfferIdentifier identifier) {
		this.identifier = identifier;
	}

	@Override
	public Single<CodeEntity> onRead() {
		final Map<String, String> offerIdMap = identifier.getOffer().getOfferId().getValue();
		final String productCode = offerIdMap.get(SearchRepositoryImpl.PRODUCT_GUID_KEY);
		return Single.just(CodeEntity.builder()
				.withCode(productCode)
				.build());
	}
}
