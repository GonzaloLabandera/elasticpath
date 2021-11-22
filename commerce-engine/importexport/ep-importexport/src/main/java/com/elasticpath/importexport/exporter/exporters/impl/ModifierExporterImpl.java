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

import com.elasticpath.domain.modifier.ModifierGroup;
import com.elasticpath.importexport.common.adapters.DomainAdapter;
import com.elasticpath.importexport.common.dto.modifier.ModifierGroupDTO;
import com.elasticpath.importexport.common.exception.ConfigurationException;
import com.elasticpath.importexport.common.types.JobType;
import com.elasticpath.importexport.exporter.context.ExportContext;
import com.elasticpath.service.modifier.ModifierService;

/**
 * This class is responsible for exporting {@link com.elasticpath.domain.modifier.ModifierGroup}.
 */
public class ModifierExporterImpl extends AbstractExporterImpl<ModifierGroup, ModifierGroupDTO, String> {

	private ModifierService modifierService;

	private DomainAdapter<ModifierGroup, ModifierGroupDTO> domainAdapter;

	private Set<String> modifierCodeSet;


	private static final Logger LOG = LogManager.getLogger(ModifierExporterImpl.class);


	@Override
	protected void initializeExporter(final ExportContext context) throws ConfigurationException {
		modifierCodeSet = modifierService.getAllModifierGroups().stream()
				.map(ModifierGroup::getCode)
				.collect(Collectors.toCollection(TreeSet::new));
		LOG.info("The list for " + modifierCodeSet.size() + " modifier groups is retrieved from database.");

	}

	@Override
	protected Class<? extends ModifierGroupDTO> getDtoClass() {
		return ModifierGroupDTO.class;
	}

	@Override
	protected List<String> getListExportableIDs() {
		final Set<String> mergedCodes = new TreeSet<>(modifierCodeSet);
		mergedCodes.addAll(getContext().getDependencyRegistry().getDependentGuids(ModifierGroup.class));

		return new ArrayList<>(mergedCodes);
	}

	@Override
	protected List<ModifierGroup> findByIDs(final List<String> subList) {
		return modifierService.findModifierGroupByCodes(subList);
	}

	@Override
	protected DomainAdapter<ModifierGroup, ModifierGroupDTO> getDomainAdapter() {
		return domainAdapter;
	}

	@Override
	public JobType getJobType() {
		return JobType.MODIFIERGROUP;
	}

	@Override
	public Class<?>[] getDependentClasses() {
		return new Class<?>[] { ModifierGroup.class };
	}

	/**
	 * Set the modifierService.
	 *
	 * @param cartItemModifierService the modifierService
	 */
	public void setModifierService(final ModifierService cartItemModifierService) {
		this.modifierService = cartItemModifierService;
	}

	public void setDomainAdapter(final DomainAdapter<ModifierGroup, ModifierGroupDTO> domainAdapter) {
		this.domainAdapter = domainAdapter;
	}
}
