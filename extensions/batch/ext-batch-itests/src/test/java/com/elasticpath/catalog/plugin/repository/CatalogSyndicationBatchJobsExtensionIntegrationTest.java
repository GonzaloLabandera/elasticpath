/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.catalog.plugin.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.Test;

import com.elasticpath.test.db.DbTestCase;

/**
 * Test for extending catalog syndication batch jobs.
 */
public class CatalogSyndicationBatchJobsExtensionIntegrationTest extends DbTestCase {

	private static final String CATALOG_SYNDICATION_BATCH_JOBS = "catalogSyndicationBatchJobs";

	@Test
	public void testCatalogSyndicationBatchJobsExtension() {
		@SuppressWarnings("unchecked")
		final Map<String, ?> searchTerms = (Map<String, ?>) getBeanFactory().getSingletonBean(CATALOG_SYNDICATION_BATCH_JOBS, Map.class);
		assertThat(searchTerms.keySet()).containsExactlyInAnyOrder(
				"BUILD_TEST_EXTENSION_FEED",
				"BUILD_ALL_OPTIONS",
				"BUILD_ALL_ATTRIBUTES",
				"BUILD_ALL_BRANDS",
				"BUILD_ALL_FIELDMETADATA",
				"BUILD_ALL_OFFERS",
				"BUILD_ALL_CATEGORIES",
				"BUILD_ALL_PROJECTIONS");
	}
}
