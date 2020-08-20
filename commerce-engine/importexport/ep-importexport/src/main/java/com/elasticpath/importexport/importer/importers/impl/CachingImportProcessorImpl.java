/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.importexport.importer.importers.impl;

import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.lang3.mutable.MutableInt;

import com.elasticpath.caching.core.MutableCachingService;
import com.elasticpath.common.dto.Dto;
import com.elasticpath.importexport.common.caching.CachePopulator;
import com.elasticpath.importexport.common.summary.Summary;
import com.elasticpath.importexport.common.types.JobType;
import com.elasticpath.importexport.importer.context.ImportContext;
import com.elasticpath.importexport.importer.importers.Importer;
import com.elasticpath.persistence.api.Persistable;

/**
 * Caching import processor implementation.
 */
public class CachingImportProcessorImpl extends ImportProcessorImpl {

	private CachePopulator<Dto> cachePopulator;
	private List<MutableCachingService<Persistable>> cachingServices;

	/**
	 * Adds cache invalidation after the import is done.
	 *
	 * @param context      Import configuration settings
	 * @param streamReader XML stream reader setup with the file with data to import from
	 * @param summary      Contains summary details of the import of <code>jobType</code>
	 * @param jobType      Type of entity being imported
	 * @param importer     The importer to use to do the import
	 * @throws XMLStreamException
	 */
	@Override
	protected void runImport(final ImportContext context,
							 final XMLStreamReader streamReader,
							 final Summary summary,
							 final JobType jobType,
							 final Importer<? super Persistable, ? super Dto> importer) throws XMLStreamException {
		super.runImport(context, streamReader, summary, jobType, importer);
		invalidateAll();
	}

	/**
	 * Prepopulates caches for the object batch.
	 *
	 * @param unmarshalledObjects the unmarshalled objects
	 * @param jobType             the job type
	 * @param importer            the importer
	 * @param summary             the summary
	 * @param transactionVolume   the transaction volume
	 * @param processedVolume     the processed volume
	 */
	@Override
	protected void importObjects(final List<Dto> unmarshalledObjects, final JobType jobType,
								 final Importer<? super Persistable, ? super Dto> importer,
								 final Summary summary, final MutableInt transactionVolume, final MutableInt processedVolume) {
		getCachePopulator().populate(unmarshalledObjects);
		super.importObjects(unmarshalledObjects, jobType, importer, summary, transactionVolume, processedVolume);
	}

	/**
	 * Invalidate all caches.
	 */
	protected void invalidateAll() {
		getCachingServices().forEach(MutableCachingService::invalidateAll);
	}

	protected CachePopulator<Dto> getCachePopulator() {
		return cachePopulator;
	}

	public void setCachePopulator(final CachePopulator<Dto> cachePopulator) {
		this.cachePopulator = cachePopulator;
	}

	protected List<MutableCachingService<Persistable>> getCachingServices() {
		return cachingServices;
	}

	public void setCachingServices(final List<MutableCachingService<Persistable>> cachingServices) {
		this.cachingServices = cachingServices;
	}
}
