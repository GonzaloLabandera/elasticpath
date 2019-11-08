/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.service.modifier.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.catalog.Catalog;
import com.elasticpath.domain.catalog.ProductType;
import com.elasticpath.domain.modifier.ModifierField;
import com.elasticpath.domain.modifier.ModifierGroup;
import com.elasticpath.domain.modifier.ModifierGroupFilter;
import com.elasticpath.domain.modifier.ModifierGroupLdf;
import com.elasticpath.service.impl.AbstractEpPersistenceServiceImpl;
import com.elasticpath.service.modifier.ModifierService;

/**
 * Manages modifiers.
 */
public class ModifierServiceImpl extends AbstractEpPersistenceServiceImpl implements ModifierService {

	@Override
	public ModifierGroup saveOrUpdate(final ModifierGroup modifierGroup) throws EpServiceException {
		sanityCheck();
		return getPersistenceEngine().saveOrUpdate(modifierGroup);
	}

	@Override
	public void remove(final ModifierGroup modifierGroup) throws EpServiceException {
		sanityCheck();
		getPersistenceEngine().delete(modifierGroup);
	}

	@Override
	public Object getObject(final long uid) throws EpServiceException {
		sanityCheck();
		return get(uid);
	}

	private ModifierGroup get(final long modifierFieldUid) throws EpServiceException {
		sanityCheck();
		ModifierGroup modifierGroup;
		if (modifierFieldUid <= 0) {
			modifierGroup = getPrototypeBean(ContextIdNames.MODIFIER_GROUP, ModifierGroup.class);
		} else {
			modifierGroup = getPersistentBeanFinder().get(ContextIdNames.MODIFIER_GROUP, modifierFieldUid);
		}
		return modifierGroup;
	}

	@Override
	public ModifierGroup findModifierGroupByCode(final String code) throws EpServiceException {
		sanityCheck();
		List<ModifierGroup> modifierGroups = getPersistenceEngine().retrieveByNamedQuery("MODIFIER_GROUP_BY_CODE", code);

		if (modifierGroups != null && modifierGroups.size() > 1) {
			throw new IllegalStateException("Cannot have two ModifierGroup with the same GUID");
		}

		if (modifierGroups != null && !modifierGroups.isEmpty()) {
			return modifierGroups.get(0);
		}

		return null;
	}

	@Override
	public ModifierGroupLdf findModifierGroupLdfByGuid(final String guid) throws EpServiceException {
		sanityCheck();
		List<ModifierGroupLdf> modifierGroupLdfs = getPersistenceEngine()
				.retrieveByNamedQuery("MODIFIER_GROUP_LDF_BY_GUID", guid);

		if (modifierGroupLdfs != null && modifierGroupLdfs.size() > 1) {
			throw new IllegalStateException("Cannot have two ModifierGroupLdf with the same GUID");
		}

		if (modifierGroupLdfs != null && !modifierGroupLdfs.isEmpty()) {
			return modifierGroupLdfs.get(0);
		}

		return null;
	}

	@Override
	public ModifierField findModifierFieldByCode(final String code) throws EpServiceException {
		sanityCheck();
		List<ModifierField> modifierFields = getPersistenceEngine().retrieveByNamedQuery("MODIFIER_FIELD_BY_CODE", code);

		if (modifierFields != null && modifierFields.size() > 1) {
			throw new IllegalStateException("Cannot have two ModifierField with the same GUID");
		}

		if (modifierFields != null && !modifierFields.isEmpty()) {
			return modifierFields.get(0);
		}

		return null;
	}


	@Override
	public List<ModifierField> findModifierFieldsByProductType(final ProductType productType) {

		List<ModifierField> fields = new ArrayList<>();

		Set<ModifierGroup> modifierGroups = productType.getModifierGroups();

		for (ModifierGroup modifierGroup : modifierGroups) {
			fields.addAll(modifierGroup.getModifierFields());
		}
		return fields;
	}


	@Override
	public	List<ModifierGroup> getAllModifierGroups() {

		return getPersistenceEngine().retrieveByNamedQuery("MODIFIER_GROUPS");
	}

	@Override
	public List<ModifierGroup> findModifierGroupByCodes(final List<String> codes) {
		return getPersistenceEngine().retrieveByNamedQueryWithList("MODIFIER_GROUPS_BY_CODES", "list", codes);

	}

	@Override
	public List<ModifierGroup> getFilteredModifierGroups(final String type, final String referenceGuid) {

		return getPersistenceEngine().retrieveByNamedQuery("MODIFIER_GROUPS_BY_FILTER", type, referenceGuid);

	}

	@Override
	public void addGroupFilter(final String type, final String referenceGuid, final String modifierCode) {
		ModifierGroupFilter filter = getPrototypeBean(ContextIdNames.MODIFIER_GROUP_FILTER, ModifierGroupFilter.class);
		filter.setModifierCode(modifierCode);
		filter.setReferenceGuid(referenceGuid);
		filter.setType(type);
		getPersistenceEngine().save(filter);
	}

	@Override
	public ModifierGroupFilter findModifierGroupFilter(final String referenceGuid, final String modifierCode, final String type) {
		sanityCheck();
		List<ModifierGroupFilter> modifierGroupFilters = getPersistenceEngine().retrieveByNamedQuery("MODIFIER_FILTERS_BY_CODE_AND_REFERENCE",
				modifierCode, referenceGuid, type);

		if (modifierGroupFilters != null && modifierGroupFilters.size() > 1) {
			throw new IllegalStateException("Cannot have two ModifierGroupFilters with the same unique fields");
		}

		if (modifierGroupFilters != null && !modifierGroupFilters.isEmpty()) {
			return modifierGroupFilters.get(0);
		}

		return null;
	}

	@Override
	public List<ModifierGroupFilter> getAllModifierGroupFilters() {
		return getPersistenceEngine().retrieveByNamedQuery("MODIFIER_GROUP_FILTERS");
	}

	@Override
	public List<ModifierGroupFilter> findModifierGroupFiltersByUids(final List<Long> uids) {
		return getPersistenceEngine().retrieveByNamedQueryWithList("MODIFIER_GROUP_FILTERS_BY_UIDS",
				"list", uids);
	}

	@Override
	public List<ModifierGroup> findModifierGroupsByCatalogUid(final long catalogUid) {
		sanityCheck();
		List<ModifierGroup> modifierGroups = getPersistenceEngine().retrieveByNamedQuery("MODIFIER_GROUP_BY_CATALOG_UID", catalogUid);

		if (modifierGroups == null) {
			return new ArrayList<>();
		}

		return modifierGroups;
	}

	@Override
	public Catalog findCatalogForModifierGroup(final ModifierGroup modifierGroup) {
		sanityCheck();
		List<Catalog> catalogs = getPersistenceEngine().retrieveByNamedQuery("CATALOG_FOR_MODIFIER_UID", modifierGroup.getUidPk());

		if (catalogs != null && !catalogs.isEmpty()) {
			return catalogs.get(0);
		}

		return null;
	}

	@Override
	public ModifierGroup update(final ModifierGroup modifierGroup) {
		return getPersistenceEngine().merge(modifierGroup);
	}

	@Override
	public ModifierGroup add(final ModifierGroup modifierGroup) {
		return getPersistenceEngine().saveOrUpdate(modifierGroup);
	}

	@Override
	public boolean isInUse(final long uidToCheck) throws EpServiceException {
		sanityCheck();
		return !getPersistenceEngine().retrieveByNamedQuery("MODIFIER_GROUP_IN_USE", uidToCheck).isEmpty();
	}
}
