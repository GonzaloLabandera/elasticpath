/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.importexport.common.util;

import org.apache.commons.lang3.StringUtils;

import com.elasticpath.commons.ThreadLocalMap;
import com.elasticpath.commons.enums.OperationEnum;
import com.elasticpath.domain.changeset.ChangeSet;
import com.elasticpath.importexport.common.exception.ConfigurationException;
import com.elasticpath.service.changeset.ChangeSetManagementService;
import com.elasticpath.service.changeset.ChangeSetService;

/**
 * Service for populating the metadata map depending on Import/Export configuration.
 */
public class MetaDataMapPopulatorImpl implements MetaDataMapPopulator {
	private static final String STAGE_1 = "stage1";
	private static final String STAGE_2 = "stage2";
	private static final String KEY_CHANGE_SET_GUID = "changeSetGuid";
	private static final String KEY_CHANGE_SET_OPERATION = "changeSetOperation";
	private static final String KEY_IMPORT_OPERATION = "importOperation";

	private ChangeSetService changeSetService;
	private ChangeSetManagementService changeSetManagementService;
	private ThreadLocalMap<String, Object> metadataMap;

	@Override
	public void configureMetadataMapForImport(final String changeSetGuid, final String stage) throws ConfigurationException {
		if (changeSetGuid == null) {
			importOnly();
			return;
		}
		if (!changeSetService.isChangeSetEnabled()) {
			throw new ConfigurationException("Changesets are not enabled in system configuration.");
		}
		ChangeSet changeSet = changeSetManagementService.get(changeSetGuid, null);
		if (changeSet == null) {
			throw new ConfigurationException(String.format("Change set %s does not exist.", changeSetGuid));
		} else if (!changeSetManagementService.isChangeAllowed(changeSetGuid)) {
			throw new ConfigurationException(String.format(
					"Change set %s does not allow changes. It is probably locked, publishing or finalized.", changeSetGuid));
		}

		metadataMap.put(KEY_CHANGE_SET_GUID, changeSetGuid);
		if (StringUtils.isBlank(stage)) {
			addToChangeSetAndImport();
		} else if (STAGE_1.equals(stage)) {
			addToChangeSetOnly();
		} else if (STAGE_2.equals(stage)) {
			importOnly();
		} else {
			throw new ConfigurationException(stage + " is not a valid argument. Accepted stages: stage1, stage2");
		}
	}

	private void addToChangeSetAndImport() {
		metadataMap.put(KEY_CHANGE_SET_OPERATION, OperationEnum.OPERATIONAL);
		metadataMap.put(KEY_IMPORT_OPERATION, OperationEnum.OPERATIONAL);
	}

	private void addToChangeSetOnly() {
		metadataMap.put(KEY_CHANGE_SET_OPERATION, OperationEnum.OPERATIONAL);
		metadataMap.put(KEY_IMPORT_OPERATION, OperationEnum.NONOPERATIONAL);
	}

	private void importOnly() {
		metadataMap.put(KEY_CHANGE_SET_OPERATION, OperationEnum.NONOPERATIONAL);
		metadataMap.put(KEY_IMPORT_OPERATION, OperationEnum.OPERATIONAL);
	}

	protected ChangeSetService getChangeSetService() {
		return changeSetService;
	}

	public void setChangeSetService(final ChangeSetService changeSetService) {
		this.changeSetService = changeSetService;
	}

	protected ChangeSetManagementService getChangeSetManagementService() {
		return changeSetManagementService;
	}

	public void setChangeSetManagementService(final ChangeSetManagementService changeSetManagementService) {
		this.changeSetManagementService = changeSetManagementService;
	}

	protected ThreadLocalMap<String, Object> getMetadataMap() {
		return metadataMap;
	}

	public void setMetadataMap(final ThreadLocalMap<String, Object> metadataMap) {
		this.metadataMap = metadataMap;
	}
}
