/*
 * Copyright (c) Elastic Path Software Inc., 2016
 */

package com.elasticpath.importexport.exporter.exporters.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.elasticpath.domain.modifier.ModifierGroupFilter;
import com.elasticpath.importexport.common.adapters.DomainAdapter;
import com.elasticpath.importexport.common.dto.modifier.ModifierGroupFilterDTO;
import com.elasticpath.importexport.common.exception.ConfigurationException;
import com.elasticpath.importexport.common.types.JobType;
import com.elasticpath.importexport.exporter.context.ExportContext;
import com.elasticpath.persistence.api.Persistable;
import com.elasticpath.service.modifier.ModifierService;

/**
 * This class is responsible for exporting {@link ModifierGroupFilter}.
 */
public class ModifierGroupFilterExporterImpl extends AbstractExporterImpl<ModifierGroupFilter, ModifierGroupFilterDTO, Long> {

	private ModifierService modifierService;

	private DomainAdapter<ModifierGroupFilter, ModifierGroupFilterDTO> domainAdapter;

	private Set<Long> modifierGroupFilterGroupCodes;


	private static final Logger LOG = LogManager.getLogger(ModifierGroupFilterExporterImpl.class);


	@Override
	protected void initializeExporter(final ExportContext context) throws ConfigurationException {
		modifierGroupFilterGroupCodes = modifierService.getAllModifierGroupFilters().stream()
				.map(Persistable::getUidPk)
				.collect(Collectors.toCollection(TreeSet::new));
		LOG.info("The list for " + modifierGroupFilterGroupCodes.size() + " modifier group filters is retrieved from database.");

	}

	@Override
	protected Class<? extends ModifierGroupFilterDTO> getDtoClass() {
		return ModifierGroupFilterDTO.class;
	}

	@Override
	protected List<Long> getListExportableIDs() {

		return new ArrayList<>(modifierGroupFilterGroupCodes);
	}

	@Override
	protected List<ModifierGroupFilter> findByIDs(final List<Long> subList) {
		return modifierService.findModifierGroupFiltersByUids(subList);
	}

	@Override
	protected DomainAdapter<ModifierGroupFilter, ModifierGroupFilterDTO> getDomainAdapter() {
		return domainAdapter;
	}

	@Override
	public JobType getJobType() {
		return JobType.MODIFIERGROUPFILTER;
	}

	@Override
	public Class<?>[] getDependentClasses() {
		return new Class<?>[] { ModifierGroupFilter.class };
	}

	/**
	 * Set the modifierService.
	 *
	 * @param modifierService the modifierService
	 */
	public void setModifierService(final ModifierService modifierService) {
		this.modifierService = modifierService;
	}

	public void setDomainAdapter(final DomainAdapter<ModifierGroupFilter, ModifierGroupFilterDTO> domainAdapter) {
		this.domainAdapter = domainAdapter;
	}
}
