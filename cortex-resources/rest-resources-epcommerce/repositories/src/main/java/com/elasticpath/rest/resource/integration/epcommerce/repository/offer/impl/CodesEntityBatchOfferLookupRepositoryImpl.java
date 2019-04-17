/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.offer.impl;

import io.reactivex.Single;
import org.osgi.service.component.annotations.Component;

import com.elasticpath.repository.Repository;
import com.elasticpath.rest.definition.items.CodesEntity;
import com.elasticpath.rest.definition.offers.BatchOffersIdentifier;
import com.elasticpath.rest.definition.offers.BatchOffersLookupFormIdentifier;
import com.elasticpath.rest.form.SubmitResult;
import com.elasticpath.rest.form.SubmitStatus;
import com.elasticpath.rest.id.IdentifierPart;
import com.elasticpath.rest.id.type.StringListIdentifier;

/**
 * Repository that creates batch offers lookup form identifier.
 * @param <E> extends CodeEntity
 * @param <I> extends BatchOffersIdentifier
 */
@Component
public class CodesEntityBatchOfferLookupRepositoryImpl<E extends CodesEntity, I extends BatchOffersIdentifier>
		implements Repository<CodesEntity, BatchOffersIdentifier> {

	@Override
	public Single<SubmitResult<BatchOffersIdentifier>> submit(final CodesEntity entity, final IdentifierPart<String> scope) {
		BatchOffersLookupFormIdentifier batchOffersLookupFormIdentifier = BatchOffersLookupFormIdentifier.builder()
				.withScope(scope)
				.build();

		BatchOffersIdentifier batchOffersIdentifier = BatchOffersIdentifier.builder()
				.withBatchId(StringListIdentifier.of(entity.getCodes()))
				.withBatchOffersLookupForm(batchOffersLookupFormIdentifier)
				.build();

		return Single.just(SubmitResult.<BatchOffersIdentifier>builder()
				.withIdentifier(batchOffersIdentifier)
				.withStatus(SubmitStatus.CREATED)
				.build());
	}
}
