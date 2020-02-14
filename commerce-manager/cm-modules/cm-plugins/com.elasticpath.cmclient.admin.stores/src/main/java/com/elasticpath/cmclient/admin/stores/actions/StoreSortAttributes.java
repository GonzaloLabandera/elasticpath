/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.cmclient.admin.stores.actions;

import static com.elasticpath.service.search.solr.FacetConstants.ATTRIBUTE_LABEL;
import static com.elasticpath.service.search.solr.FacetConstants.FIELD_LABEL;
import static com.elasticpath.service.search.solr.FacetConstants.SORT_FIELD_GROUP;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.elasticpath.cmclient.core.BeanLocator;
import com.elasticpath.cmclient.core.helpers.store.StoreEditorModel;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.attribute.Attribute;
import com.elasticpath.domain.catalog.Catalog;
import com.elasticpath.domain.search.SortAttribute;
import com.elasticpath.domain.search.SortAttributeGroup;
import com.elasticpath.domain.search.SortAttributeType;
import com.elasticpath.service.catalog.CatalogService;
import com.elasticpath.service.search.SortAttributeService;

/**
 * Saving and retrieving Sort Attributes.
 */
public class StoreSortAttributes {

	private final String storeCode;

	private final SortAttributeService sortAttributeService;

	private final CatalogService catalogService;

	private final StoreEditorModel storeEditorModel;

	/**
	 * Constructor.
	 * @param storeEditorModel store model
	 */
	public StoreSortAttributes(final StoreEditorModel storeEditorModel) {
		this.storeEditorModel = storeEditorModel;
		storeCode = storeEditorModel.getStoreCode();
		sortAttributeService = BeanLocator.getSingletonBean(ContextIdNames.SORT_ATTRIBUTE_SERVICE, SortAttributeService.class);
		catalogService = BeanLocator.getSingletonBean(ContextIdNames.CATALOG_SERVICE, CatalogService.class);
	}

	/**
	 * Retrieves all sort attributes.
	 * @return map keyed on guid with a value being the sort attribute
	 */
	public Map<String, SortAttribute> getAllAvailableSortAttributes() {
		Catalog catalog = storeEditorModel.getCatalog();

		List<Long> catalogUids = new ArrayList<>();
		if (catalog.isMaster()) {
			catalogUids.add(catalog.getUidPk());
		} else {
			catalogService.findMastersUsedByVirtualCatalog(catalog.getCode())
					.forEach(masterCatalog -> catalogUids.add(masterCatalog.getUidPk()));
		}

		List<Attribute> attributes = sortAttributeService.findSortableProductAttributesByCatalogIds(catalogUids);
		Map<String, SortAttributeType> availableSortGroup = new HashMap<>(SORT_FIELD_GROUP);

		Map<String, SortAttribute> map = new HashMap<>();

		availableSortGroup.forEach((key, value) -> map.put(FIELD_LABEL + key,  initializeSortAttribute(key, value, SortAttributeGroup.FIELD_TYPE)));

		attributes.forEach(attribute ->
				map.put(ATTRIBUTE_LABEL + attribute.getKey(), getSortAttribute(attribute)));

		return map;
	}

	private SortAttribute getSortAttribute(final Attribute attribute) {
		return initializeSortAttribute(attribute.getKey(), SortAttributeType.getTypeOfAttribute(attribute), SortAttributeGroup.ATTRIBUTE_TYPE);
	}

	/**
	 * Retrieves all sort attributes in the database.
	 * @return map keyed on guid with a value being the sort attribute
	 */
	public Map<String, SortAttribute> getConfiguredSortAttributes() {
		return sortAttributeService.findSortAttributesByStoreCode(storeCode).stream()
				.collect(Collectors.toMap(SortAttribute::getGuid, Function.identity()));
	}

	/**
	 * Save all modified sort attributes.
	 * @param modifiedSortAttributes sort attributes
	 */
	public void saveSortAttributes(final Collection<SortAttribute> modifiedSortAttributes) {
		modifiedSortAttributes.forEach(sortAttributeService::saveOrUpdate);
	}

	/**
	 * Remove persisted sort attributes.
	 * @param sortAttributesToBeRemoved sort attributes
	 */
	public void deleteSortAttibutes(final List<SortAttribute> sortAttributesToBeRemoved) {
		sortAttributesToBeRemoved.forEach(sortAttributeService::remove);
	}

	private SortAttribute initializeSortAttribute(final String businessObjectId, final SortAttributeType sortAttributeType,
												  final SortAttributeGroup sortAttributeGroup) {
		SortAttribute sortAttribute = BeanLocator.getPrototypeBean(ContextIdNames.SORT_ATTRIBUTE, SortAttribute.class);
		sortAttribute.setSortAttributeType(sortAttributeType);
		sortAttribute.setStoreCode(storeCode);
		sortAttribute.setBusinessObjectId(businessObjectId);
		sortAttribute.setGuid(UUID.randomUUID().toString());
		sortAttribute.setSortAttributeGroup(sortAttributeGroup);
		sortAttribute.setLocalizedNames(new HashMap<>());
		return sortAttribute;
	}

	/**
	 * Get the default sort attribute.
	 * @return default sort
	 */
	public SortAttribute getDefaultSortAttribute() {
		return sortAttributeService.getDefaultSortAttributeForStore(storeCode);
	}

}
