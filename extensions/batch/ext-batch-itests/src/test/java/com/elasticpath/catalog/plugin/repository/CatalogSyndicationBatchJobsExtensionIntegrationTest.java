/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.catalog.plugin.repository;

import static com.elasticpath.catalog.plugin.repository.CatalogSyndicationBatchJobsExtensionIntegrationTest.JMS_BROKER_URL;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.Test;

import com.elasticpath.test.jta.JmsBrokerConfigurator;
import com.elasticpath.test.jta.XaTransactionTestSupport;

/**
 * Test for extending catalog syndication batch jobs.
 */
@JmsBrokerConfigurator(url = JMS_BROKER_URL)
public class CatalogSyndicationBatchJobsExtensionIntegrationTest extends XaTransactionTestSupport {

	static final String JMS_BROKER_URL = "tcp://localhost:61617";
	private static final String CATALOG_SYNDICATION_BATCH_JOBS = "catalogSyndicationBatchJobs";

	@Test
	public void testCatalogSyndicationBatchJobsExtension() {
		final Map<String, ?> searchTerms = getBeanFactory().getBean(CATALOG_SYNDICATION_BATCH_JOBS);
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
