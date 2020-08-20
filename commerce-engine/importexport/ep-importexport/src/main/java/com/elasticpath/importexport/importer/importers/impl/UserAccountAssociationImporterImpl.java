/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */

package com.elasticpath.importexport.importer.importers.impl;

import org.apache.log4j.Logger;

import com.elasticpath.common.dto.customer.UserAccountAssociationDTO;
import com.elasticpath.domain.customer.UserAccountAssociation;
import com.elasticpath.importexport.common.adapters.DomainAdapter;
import com.elasticpath.service.customer.UserAccountAssociationService;

/**
 * User Account Association Importer.
 */
public class UserAccountAssociationImporterImpl extends AbstractImporterImpl<UserAccountAssociation, UserAccountAssociationDTO> {

	private DomainAdapter<UserAccountAssociation, UserAccountAssociationDTO> userAccountAssociationAdapter;

	private UserAccountAssociationService userAccountAssociationService;

	private static final Logger LOG = Logger.getLogger(UserAccountAssociationImporterImpl.class);

	@Override
	protected String getDtoGuid(final UserAccountAssociationDTO dto) {
		return null;
	}

	@Override
	protected DomainAdapter<UserAccountAssociation, UserAccountAssociationDTO> getDomainAdapter() {
		return userAccountAssociationAdapter;
	}

	@Override
	protected UserAccountAssociation findPersistentObject(final UserAccountAssociationDTO dto) {
		return userAccountAssociationService.findByGuid(dto.getGuid());
	}

	@Override
	protected void setImportStatus(final UserAccountAssociationDTO object) {
		getStatusHolder().setImportStatus("(" + object.getAccountGuid() + ")");
	}

	@Override
	public Class<? extends UserAccountAssociationDTO> getDtoClass() {
		return UserAccountAssociationDTO.class;
	}

	@Override
	public String getImportedObjectName() {
		return UserAccountAssociationDTO.ROOT_ELEMENT;
	}

	@Override
	public boolean executeImport(final UserAccountAssociationDTO object) {
		sanityCheck();
		LOG.debug("Executing import for object: " + object);
		setImportStatus(object);
		return userAccountAssociationService.findOrCreateUserAccountAssociation(object.getGuid(),
				object.getUserGuid(),
				object.getAccountGuid(),
				object.getRole()) != null;
	}

	public DomainAdapter<UserAccountAssociation, UserAccountAssociationDTO> getUserAccountAssociationAdapter() {
		return userAccountAssociationAdapter;
	}

	public void setUserAccountAssociationAdapter(
			final DomainAdapter<UserAccountAssociation, UserAccountAssociationDTO> userAccountAssociationAdapter) {
		this.userAccountAssociationAdapter = userAccountAssociationAdapter;
	}

	public UserAccountAssociationService getUserAccountAssociationService() {
		return userAccountAssociationService;
	}

	public void setUserAccountAssociationService(final UserAccountAssociationService userAccountAssociationService) {
		this.userAccountAssociationService = userAccountAssociationService;
	}
}
