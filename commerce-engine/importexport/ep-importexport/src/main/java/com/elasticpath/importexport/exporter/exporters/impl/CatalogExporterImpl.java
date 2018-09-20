/**
 * Copyright (c) Elastic Path Software Inc., 2013
 */
package com.elasticpath.importexport.exporter.exporters.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import com.elasticpath.domain.attribute.Attribute;
import com.elasticpath.domain.catalog.Brand;
import com.elasticpath.domain.catalog.Catalog;
import com.elasticpath.domain.catalog.CategoryType;
import com.elasticpath.domain.catalog.ProductType;
import com.elasticpath.domain.search.SynonymGroup;
import com.elasticpath.domain.skuconfiguration.SkuOption;
import com.elasticpath.importexport.common.adapters.DomainAdapter;
import com.elasticpath.importexport.common.dto.catalogs.CatalogDTO;
import com.elasticpath.importexport.common.types.JobType;
import com.elasticpath.importexport.common.util.Message;
import com.elasticpath.importexport.exporter.context.ExportContext;
import com.elasticpath.importexport.exporter.search.ImportExportSearcher;
import com.elasticpath.ql.parser.EPQueryType;
import com.elasticpath.service.catalog.CatalogService;

/**
 * Exporter prepares the list of CatalogAdapters based on query and executes export job.
 */
public class CatalogExporterImpl extends AbstractExporterImpl<Catalog, CatalogDTO, String> {

	private CatalogService catalogService;

	private DomainAdapter<Catalog, CatalogDTO> catalogAdapter;

	private Set<String> catalogGuidSet;
	
	private ImportExportSearcher importExportSearcher;

	private static final Logger LOG = Logger.getLogger(CatalogExporterImpl.class);

	@Override
	protected void initializeExporter(final ExportContext context) {
		catalogGuidSet = new TreeSet<>(importExportSearcher.searchGuids(getContext().getSearchConfiguration(), EPQueryType.CATALOG));
		LOG.info("The list for " + catalogGuidSet.size() + " catalogs is retrieved from database.");
	}

	@Override
	protected List<Catalog> findByIDs(final List<String> subList) {
		List<Catalog> catalogList = new ArrayList<>();
		for (String catalogCode : subList) {
			Catalog catalog = catalogService.findByCode(catalogCode);
			if (catalog == null) {
				LOG.error(new Message("IE-20601", catalogCode));
				continue;
			}

			catalogList.add(catalog);
		}
		return catalogList;
	}

	/**
	 * Determines whether a catalog with the given uid should be filtered or not. A catalog should be filtered if it is
	 * returned as part of the search query in your configuration.
	 *
	 * @param primaryObjectUid {@inheritDoc}
	 * @returns {@inheritDoc}
	 */
	@Override
	public boolean isFiltered(final long primaryObjectUid) {
		Catalog catalog = getCatalogService().getCatalog(primaryObjectUid);
		return catalogGuidSet.contains(catalog.getGuid());
	}

	@Override
	protected Class<? extends CatalogDTO> getDtoClass() {
		return CatalogDTO.class;
	}

	@Override
	public JobType getJobType() {
		return JobType.CATALOG;
	}

	@Override
	protected List<String> getListExportableIDs() {
		final Set<String> mergeGuids = new TreeSet<>(catalogGuidSet);
		mergeGuids.addAll(getContext().getDependencyRegistry().getDependentGuids(Catalog.class));
		return new ArrayList<>(mergeGuids);
	}

	@Override
	protected void exportFailureHandler(final Catalog catalog) {
		LOG.error(new Message("IE-20600", catalog.getCode()));
	}

	@Override
	public Class<?>[] getDependentClasses() {
		return new Class<?>[] { SynonymGroup.class, SkuOption.class, CategoryType.class, ProductType.class, Attribute.class, Brand.class,
				Catalog.class };
	}

	/**
	 * Gets the catalogService.
	 * 
	 * @return the catalogService
	 */
	public CatalogService getCatalogService() {
		return catalogService;
	}

	/**
	 * Sets the catalogService.
	 * 
	 * @param catalogService the catalogService to set
	 */
	public void setCatalogService(final CatalogService catalogService) {
		this.catalogService = catalogService;
	}

	/**
	 * Gets the catalogAdapter.
	 * 
	 * @return the catalogAdapter
	 */
	@Override
	public DomainAdapter<Catalog, CatalogDTO> getDomainAdapter() {
		return catalogAdapter;
	}

	/**
	 * Sets the catalogAdapter.
	 * 
	 * @param catalogAdapter the catalogAdapter to set
	 */
	public void setCatalogAdapter(final DomainAdapter<Catalog, CatalogDTO> catalogAdapter) {
		this.catalogAdapter = catalogAdapter;
	}

	/**
	 * Gets importExportSearcher.
	 * 
	 * @return importExportSearcher
	 */
	public ImportExportSearcher getImportExportSearcher() {
		return importExportSearcher;
	}

	/**
	 * Sets importExportSearcher.
	 * @param importExportSearcher the ImportExportSearcher
	 */
	public void setImportExportSearcher(final ImportExportSearcher importExportSearcher) {
		this.importExportSearcher = importExportSearcher;
	}
}
