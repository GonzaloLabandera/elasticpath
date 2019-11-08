/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.service.modifier;

import java.util.List;

import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.domain.catalog.Catalog;
import com.elasticpath.domain.catalog.ProductType;
import com.elasticpath.domain.modifier.ModifierField;
import com.elasticpath.domain.modifier.ModifierGroup;
import com.elasticpath.domain.modifier.ModifierGroupFilter;
import com.elasticpath.domain.modifier.ModifierGroupLdf;
import com.elasticpath.service.EpPersistenceService;

/**
 * Manages modifiers.
 */
public interface ModifierService extends EpPersistenceService {

	/**
	 * Updates the given  modifier field.
	 *
	 * @param modifierGroup the  modifier group
	 * @return the persisted instance of  modifier group
	 * @throws EpServiceException - in case of any errors
	 */
	ModifierGroup saveOrUpdate(
			ModifierGroup modifierGroup) throws EpServiceException;

	/**
	 * Removes the given  modifier group.
	 *
	 * @param modifierGroup the  modifier group
	 * @throws EpServiceException if the  modifier group cannot be removed.
	 */
	void remove(ModifierGroup modifierGroup) throws EpServiceException;

	/**
	 * This method retrieves the  modifier group corresponding to the given guid.
	 *
	 * @param guid the guid for the  modifier field to retrieve
	 * @return the  modifier field or null if no matching  can be found
	 * @throws EpServiceException - in case of any errors
	 */
	ModifierGroup findModifierGroupByCode(String guid) throws EpServiceException;

	/**
	 * This method retrieves the  modifier group LDF corresponding to the given guid.
	 *
	 * @param guid the guid for the  modifier group LDF to retrieve
	 * @return the  modifier group LDF or null if no matching  can be found
	 * @throws EpServiceException - in case of any errors
	 */
	ModifierGroupLdf findModifierGroupLdfByGuid(String guid) throws EpServiceException;

	/**
	 * This method retrieves the  modifier field corresponding to the given guid.
	 *
	 * @param code the guid for the  modifier field to retrieve
	 * @return the  modifier field or null if no matching  can be found
	 * @throws EpServiceException - in case of any errors
	 */
	ModifierField findModifierFieldByCode(String code) throws EpServiceException;

	/**
	 * find modifier groups by catalog uid (eg catalog uid).
	 * @param catalogUid the catalog uid.
	 * @return the list of modifier groups.
	 */
	List<ModifierGroup> findModifierGroupsByCatalogUid(long catalogUid);

	/**
	 * Retrieve all the modifier fields corresponding to the given product type.
	 *
	 * @param productType the type of product.
	 * @return the list of modifier.
	 */
	List<ModifierField> findModifierFieldsByProductType(ProductType productType);


	/**
	 * Retrieves the associated Catalog for the modifier group, if it exists.
	 * @param modifierGroup the modifier group.
	 * @return a Catalog that contains the modifier group, null otherwise.
	 */
	Catalog findCatalogForModifierGroup(ModifierGroup modifierGroup);

	/**
	 * Updates the given  modifier field.
	 *
	 * @param modifierGroup the  modifier group
	 * @return the persisted instance of  modifier group
	 * @throws EpServiceException - in case of any errors
	 */
	ModifierGroup update(ModifierGroup modifierGroup);

	/**
	 * Adds the given  modifier field.
	 *
	 * @param modifierGroup the  modifier group
	 * @throws EpServiceException - in case of any errors
	 * @return added  modifier group
	 */
	ModifierGroup add(ModifierGroup modifierGroup);


	/**
	 * Checks whether the given UID is in use.
	 *
	 * @param uidToCheck the UID to check that is in use
	 * @return whether the UID is currently in use or not
	 * @throws EpServiceException in case of any errors
	 */
	boolean isInUse(long uidToCheck) throws EpServiceException;

	/**
	 * Gets all modifier groups.
	 * @return modifier groups.
	 */
	List<ModifierGroup> getAllModifierGroups();

	/**
	 * Gets the modifiers groups for the given list of codes.
	 * @param codes the modifier codes.
	 * @return List of modifier groups.
	 */
	List<ModifierGroup> findModifierGroupByCodes(List<String> codes);

	/**
	 * Gets modifier groups filtered by type and reference guid.
	 * @param type the type to filter.
	 * @param referenceGuid the reference guid.
	 * @return the list of modifiers for that particular reference guid and type.
	 */
	List<ModifierGroup> getFilteredModifierGroups(String type, String referenceGuid);

	/**
	 * Add Filter for modifier group.
	 * @param type the type.
	 * @param referenceGuid the reference guid.
	 * @param modifierCode the modifier code.
	 */
	void addGroupFilter(String type, String referenceGuid, String modifierCode);

	/**
	 * Find modifier group Filter.
	 * @param referenceGuid the reference guid.
	 * @param modifierCode the modifier code.
	 * @param type the type.
	 * @return the modifier group filter.
	 */
	ModifierGroupFilter findModifierGroupFilter(String referenceGuid, String modifierCode, String type);

	/**
	 * Get all modifier group filters.
	 * @return all the modifier group filters.
	 */
	List<ModifierGroupFilter> getAllModifierGroupFilters();

	/**
	 * Find a subset of modifier group filters.
	 * @param uids the uids to retrieve.
	 * @return the list of modifer group filters.
	 */
	List<ModifierGroupFilter> findModifierGroupFiltersByUids(List<Long> uids);
}
