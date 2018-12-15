/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.item.repositories;

import io.reactivex.Single;
import org.osgi.service.component.annotations.Component;

import com.elasticpath.repository.Repository;
import com.elasticpath.rest.definition.items.BatchItemsFormIdentifier;
import com.elasticpath.rest.definition.items.BatchItemsIdentifier;
import com.elasticpath.rest.definition.items.CodesEntity;
import com.elasticpath.rest.form.SubmitResult;
import com.elasticpath.rest.form.SubmitStatus;
import com.elasticpath.rest.id.IdentifierPart;
import com.elasticpath.rest.id.type.StringListIdentifier;

/**
 * Codes Entity from Batch Items Repository.
 *
 * @param <E> the entity type
 * @param <I> the identifier type
 */
@Component
public class CodesEntityBatchItemsIdentifierRepositoryImpl<E extends CodesEntity, I extends BatchItemsIdentifier>
		implements Repository<CodesEntity, BatchItemsIdentifier> {

	@Override
	public Single<SubmitResult<BatchItemsIdentifier>> submit(final CodesEntity entity, final IdentifierPart<String> scope) {

		BatchItemsFormIdentifier batchItemsFormIdentifier = BatchItemsFormIdentifier.builder()
				.withScope(scope)
				.build();

		BatchItemsIdentifier batchItemsIdentifier = BatchItemsIdentifier.builder()
				.withBatchItemsForm(batchItemsFormIdentifier)
				.withBatchId(StringListIdentifier.of(entity.getCodes()))
				.build();
		return Single.just(SubmitResult.<BatchItemsIdentifier>builder()
				.withIdentifier(batchItemsIdentifier)
				.withStatus(SubmitStatus.CREATED)
				.build());
	}

	@Override
	public Single<CodesEntity> findOne(final BatchItemsIdentifier identifier) {
		return Single.just(CodesEntity.builder()
				.withCodes(identifier.getBatchId().getValue())
				.build());
	}
}