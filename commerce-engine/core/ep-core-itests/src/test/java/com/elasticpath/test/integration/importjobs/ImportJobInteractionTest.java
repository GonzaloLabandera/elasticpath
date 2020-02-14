/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.test.integration.importjobs;

import static org.awaitility.Awaitility.await;
import static org.awaitility.Duration.ONE_MINUTE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Locale;

import org.apache.log4j.Logger;
import org.joda.time.Interval;
import org.junit.Test;

import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.cmuser.CmUser;
import com.elasticpath.domain.dataimport.ImportJob;
import com.elasticpath.domain.dataimport.ImportJobRequest;
import com.elasticpath.domain.dataimport.ImportJobStatus;
import com.elasticpath.domain.dataimport.impl.ImportJobRequestImpl;
import com.elasticpath.service.catalog.CategoryLookup;
import com.elasticpath.test.integration.DirtiesDatabase;

/**
 * Test that the import job running correctly during concurrent modifications.
 */
public class ImportJobInteractionTest extends ImportJobTestCase {
	private static final Logger log = Logger.getLogger(ImportJobInteractionTest.class);

	/**
	 * Tests that in case of import cancellation some data gets skipped.
	 */
	@DirtiesDatabase
	@Test
	public void testCancelImportCategories() {
		CmUser initiator = scenario.getCmUser();
		ImportJob importJob = createInsertCategoriesImportJobManyRows();
		ImportJobRequest importJobProcessRequest = new ImportJobRequestImpl();
		importJobProcessRequest.setImportJob(importJob);
		importJobProcessRequest.setImportSource(importJob.getCsvFileName());
		importJobProcessRequest.setInitiator(initiator);
		importJobProcessRequest.setReportingLocale(Locale.getDefault());

		final ImportJobStatus status = importService.scheduleImport(importJobProcessRequest);

		new ImportJobProcessorLauncher(getBeanFactory()).launch();

		/** wait until several row gets imported. */
		await().atMost(ONE_MINUTE).until(() -> importService.getImportJobStatus(status.getProcessId()).getCurrentRow() != 0);
		assertEquals(0, status.getFailedRows());

		/** force import cancellation. */
		importService.cancelImportJob(status.getProcessId(), initiator);

		/** wait until working thread will be actually cancelled and all the statuses are to be set. */
		await().atMost(ONE_MINUTE).until(() -> importService.getImportJobStatus(status.getProcessId()).isCanceled());

		final ImportJobStatus updatedStatus = importService.getImportJobStatus(status.getProcessId());

		assertTrue(updatedStatus.isFinished());
		// at least the last category should not be imported, since import job was canceled.
		CategoryLookup categoryLookup = getBeanFactory().getSingletonBean(ContextIdNames.CATEGORY_LOOKUP, CategoryLookup.class);
		assertNull(categoryLookup.findByCategoryCodeAndCatalog("10100", scenario.getCatalog()));

		Interval interval = new Interval(updatedStatus.getStartTime().getTime(), updatedStatus.getEndTime().getTime());
		log.info("ImportJobInteractionTest#testCancelImportCategories took " + interval.toDurationMillis() + "ms");
	}
}
