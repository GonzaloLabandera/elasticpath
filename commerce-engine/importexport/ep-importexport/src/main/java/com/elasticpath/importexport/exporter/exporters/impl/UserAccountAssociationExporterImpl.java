/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.importexport.exporter.exporters.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.elasticpath.common.dto.customer.UserAccountAssociationDTO;
import com.elasticpath.domain.customer.UserAccountAssociation;
import com.elasticpath.importexport.common.adapters.DomainAdapter;
import com.elasticpath.importexport.common.types.JobType;
import com.elasticpath.importexport.common.util.Message;
import com.elasticpath.importexport.exporter.context.ExportContext;
import com.elasticpath.service.customer.UserAccountAssociationService;

/**
 * Exporter retrieves the list of UserAccountAssociation UIDs and executes export job.
 */
public class UserAccountAssociationExporterImpl extends AbstractExporterImpl<UserAccountAssociation, UserAccountAssociationDTO, String> {

	private UserAccountAssociationService userAccountAssociationService;

	private static final Logger LOG = Logger.getLogger(UserAccountAssociationExporterImpl.class);

	private DomainAdapter<UserAccountAssociation, UserAccountAssociationDTO> userAccountAssociationAdapter;

	@Override
	protected void initializeExporter(final ExportContext context) {
		// do nothing
	}

	@Override
	protected List<UserAccountAssociation> findByIDs(final List<String> subList) {
		return new ArrayList<>(userAccountAssociationService.findByIDs(subList.stream().map(Long::parseLong).collect(Collectors.toList())));
	}

	@Override
	protected DomainAdapter<UserAccountAssociation, UserAccountAssociationDTO> getDomainAdapter() {
		return userAccountAssociationAdapter;
	}

	@Override
	protected Class<? extends UserAccountAssociationDTO> getDtoClass() {
		return UserAccountAssociationDTO.class;
	}

	@Override
	public JobType getJobType() {
		return JobType.USERACCOUNTASSOCIATION;
	}

	@Override
	protected List<String> getListExportableIDs() {
		return userAccountAssociationService.findAllUids().stream()
				.map(String::valueOf)
				.collect(Collectors.toList());
	}

	@Override
	protected void exportFailureHandler(final UserAccountAssociation object) {
		LOG.error(new Message("IE-20900", String.valueOf(object.getUidPk())));
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Class<?>[] getDependentClasses() {
		return new Class[]{UserAccountAssociation.class};
	}

	/**
	 * Gets user account association service.
	 *
	 * @return user account association service
	 */
	public UserAccountAssociationService getUserAccountAssociationService() {
		return userAccountAssociationService;
	}

	/**
	 * Sets user account association service.
	 *
	 * @param userAccountAssociationService user account association service
	 */
	public void setUserAccountAssociationService(final UserAccountAssociationService userAccountAssociationService) {
		this.userAccountAssociationService = userAccountAssociationService;
	}

	public DomainAdapter<UserAccountAssociation, UserAccountAssociationDTO> getUserAccountAssociationAdapter() {
		return userAccountAssociationAdapter;
	}

	public void setUserAccountAssociationAdapter(
			final DomainAdapter<UserAccountAssociation, UserAccountAssociationDTO> userAccountAssociationAdapter) {
		this.userAccountAssociationAdapter = userAccountAssociationAdapter;
	}
}
