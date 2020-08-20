/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.batch.jobs.impl.tokens;

import static com.elasticpath.persistence.api.PersistenceConstants.LIST_PARAMETER_NAME;

import java.util.List;

import com.elasticpath.batch.jobs.AbstractBatchProcessor;

/**
 * The batch job processor for expired tokens.
 */
public class PurgeExpiredOAuth2TokensBatchProcessor extends AbstractBatchProcessor<Long> {
	@Override
	protected void executeBulkOperations(final List<Long> batch) {
		getPersistenceEngine().executeNamedQueryWithList("REMOVE_OAUTHACCESSTOKEN_BY_TOKENID_LIST",
				LIST_PARAMETER_NAME, batch);
	}
}
