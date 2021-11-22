/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.importexport.common.util;

import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;

import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.importexport.common.logging.IESummaryAppender;
import com.elasticpath.importexport.common.summary.Summary;
import com.elasticpath.importexport.common.summary.SummaryLogger;
import com.elasticpath.importexport.common.summary.impl.SummaryImpl;
import com.elasticpath.importexport.exporter.context.ExportContext;
import com.elasticpath.importexport.importer.context.ImportContext;

/**
 * Utility class for managing changes to the Import/Export log appender.
 */
public final class LogAppenderUtil {
	/**
	 * Private constructor to prevent instantiation.
	 */
	private LogAppenderUtil() {
		// No op
	}

	/**
	 * Create summary object for this import process, associate it with IESummaryAppender to get control over logging. All import-related application
	 * messages will be collected in summary.
	 * @param importContext the import context
	 */
	public static void initializeSummary(final ImportContext importContext) {
		initializeSummary(importContext::setSummary);

	}

	/**
	 * Create summary object for this export process, associate it with IESummaryAppender to get control over logging. All import-related application
	 * messages will be collected in summary.
	 * @param exportContext the export context
	 */
	public static void initializeSummary(final ExportContext exportContext) {
		initializeSummary(exportContext::setSummary);
	}

	/**
	 * Common method to create summary object for import/export process, associate it with IESummaryAppender to get control over logging.
	 * All import/export-related application messages will be collected in summary.
	 *
	 * @param context the context
	 */
	private static void initializeSummary(final Consumer<SummaryLogger> context) {
		IESummaryAppender summaryAppender = ((IESummaryAppender) ((org.apache.logging.log4j.core.Logger) LogManager.getRootLogger())
				.getAppenders()
				.get("IE_SUMMARY"));
		if (summaryAppender == null) {
			throw new EpServiceException("IE_SUMMARY appender is not configured in log4j2.xml");
		}
		SummaryLogger summary = new SummaryImpl();
		summaryAppender.setSummary(summary);
		context.accept(summary);
	}

	/**
	 * Detach the summary from the context to ensure that a new summary is initialized before the next Import/Export run.
	 * @param importContext the import context
	 * @return the summary
	 */
	public static Summary detachSummary(final ImportContext importContext) {
		Summary summary = importContext.getSummary();
		importContext.setSummary(null);
		return summary;
	}

	/**
	 * Detach the summary from the context to ensure that a new summary is initialized before the next Import/Export run.
	 * @param exportContext the export context
	 * @return the summary
	 */
	public static Summary detachSummary(final ExportContext exportContext) {
		Summary summary = exportContext.getSummary();
		exportContext.setSummary(null);
		return summary;
	}
}
