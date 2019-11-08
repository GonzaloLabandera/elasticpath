/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.caching.core.catalog;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.cache.Cache;
import com.elasticpath.commons.exception.DuplicateKeyException;
import com.elasticpath.domain.attribute.Attribute;
import com.elasticpath.domain.attribute.AttributeUsage;
import com.elasticpath.persistence.support.DistinctAttributeValueCriterion;
import com.elasticpath.service.attribute.AttributeService;
import com.elasticpath.service.attribute.impl.AttributeValueInfo;
import com.elasticpath.service.impl.AbstractEpPersistenceServiceImpl;

/**
 * Caching implementation of the Attribute Service.
 */
public class CachingAttributeServiceImpl extends AbstractEpPersistenceServiceImpl implements AttributeService {

	private Cache<Integer, List<Attribute>> attributesCache;
	private Cache<Long, List<AttributeValueInfo>> findProductAttributeValueByAttributeUidCache;
	private Cache<String, Attribute> findByKeyCache;
	private AttributeService fallbackAttributeService;


	@Override
	public Attribute add(final Attribute attribute) throws DuplicateKeyException {
		return fallbackAttributeService.add(attribute);
	}

	@Override
	public Attribute update(final Attribute attribute) throws DuplicateKeyException {
		return fallbackAttributeService.update(attribute);
	}

	@Override
	public void remove(final Attribute attribute) throws EpServiceException {
		fallbackAttributeService.remove(attribute);
	}

	@Override
	public Attribute load(final long attributeUid) throws EpServiceException {
		return fallbackAttributeService.load(attributeUid);
	}

	@Override
	public Attribute get(final long attributeUid) throws EpServiceException {
		return fallbackAttributeService.get(attributeUid);
	}

	@Override
	public List<Attribute> list() throws EpServiceException {
		return fallbackAttributeService.list();
	}

	@Override
	public Collection<Attribute> findAllCatalogOrGlobalAttributes(final long catalogUid) throws EpServiceException {
		return fallbackAttributeService.findAllCatalogOrGlobalAttributes(catalogUid);
	}

	@Override
	public Collection<Attribute> findAllCatalogAndGlobalAttributesByType(final long catalogUid,
																		 final Collection<Integer> globalAttributeUsage) throws EpServiceException {
		return fallbackAttributeService.findAllCatalogAndGlobalAttributesByType(catalogUid, globalAttributeUsage);
	}

	@Override
	public Collection<Attribute> findAllGlobalAttributes() throws EpServiceException {
		return fallbackAttributeService.findAllGlobalAttributes();
	}

	@Override
	public boolean keyExists(final String key) throws EpServiceException {
		return fallbackAttributeService.keyExists(key);
	}

	@Override
	public boolean keyExists(final Attribute attribute) throws EpServiceException {
		return fallbackAttributeService.keyExists(attribute);
	}

	@Override
	public Attribute findByKey(final String key) throws EpServiceException {
		if (findByKeyCache.get(key) != null) {
			return findByKeyCache.get(key);
		}
		Attribute attribute = fallbackAttributeService.findByKey(key);
		findByKeyCache.put(key, attribute);
		return attribute;

	}

	@Override
	public Map<String, String> getAttributeUsageMap() {
		return fallbackAttributeService.getAttributeUsageMap();
	}

	@Override
	public Map<String, String> getAttributeTypeMap() {
		return fallbackAttributeService.getAttributeTypeMap();
	}

	@Override
	public List<Long> getAttributeInUseUidList() {
		return fallbackAttributeService.getAttributeInUseUidList();
	}

	@Override
	public List<Attribute> getCategoryAttributes() {
		return fallbackAttributeService.getCategoryAttributes();
	}

	@Override
	public List<Attribute> getProductAttributes() {
		return fallbackAttributeService.getProductAttributes();
	}

	@Override
	public List<Attribute> getSkuAttributes() {
		return fallbackAttributeService.getSkuAttributes();
	}

	@Override
	public List<Attribute> getAttributes(final AttributeUsage usage) {
		return fallbackAttributeService.getAttributes(usage);
	}

	@Override
	public List<Attribute> getAttributes(final int usageId) {
		if (attributesCache.get(usageId) != null) {
			return attributesCache.get(usageId);
		}
		List<Attribute> result = fallbackAttributeService.getAttributes(usageId);
		attributesCache.put(usageId, result);
		return result;
	}

	@Override
	public List<String> getDistinctAttributeValueList(final Attribute attribute, final String languageCode) {
		return fallbackAttributeService.getDistinctAttributeValueList(attribute, languageCode);
	}

	@Override
	public void setDistinctAttributeValueCriterion(final DistinctAttributeValueCriterion distinctAttributeValueCriterion) {
		fallbackAttributeService.setDistinctAttributeValueCriterion(distinctAttributeValueCriterion);
	}

	@Override
	public Map<String, Attribute> getCustomerProfileAttributesMap() {
		return fallbackAttributeService.getCustomerProfileAttributesMap();
	}

	@Override
	public List<Attribute> getCustomerProfileAttributes() {
		return fallbackAttributeService.getCustomerProfileAttributes();
	}

	@Override
	public List<Attribute> getAttributesExcludeCustomerProfile() {
		return fallbackAttributeService.getAttributesExcludeCustomerProfile();
	}

	@Override
	public List<Long> getCustomerProfileAttributeInUseUidList() {
		return getCustomerProfileAttributeInUseUidList();
	}

	@Override
	public boolean nameExistsInAttributeUsage(final String name, final AttributeUsage attributeUsage) throws EpServiceException {
		return fallbackAttributeService.nameExistsInAttributeUsage(name, attributeUsage);
	}

	@Override
	public boolean nameExistsInAttributeUsage(final Attribute attribute) throws EpServiceException {
		return fallbackAttributeService.nameExistsInAttributeUsage(attribute);
	}

	@Override
	public Attribute findByNameAndUsage(final String name, final AttributeUsage attributeUsage) throws EpServiceException {
		return fallbackAttributeService.findByNameAndUsage(name, attributeUsage);
	}

	@Override
	public boolean isInUse(final long uidToCheck) throws EpServiceException {
		return fallbackAttributeService.isInUse(uidToCheck);
	}

	@Override
	public List<Attribute> findByCatalogAndUsage(final long catalogUid, final int attributeUsageId) {
		return fallbackAttributeService.findByCatalogAndUsage(catalogUid, attributeUsageId);
	}

	@Override
	public List<AttributeValueInfo> findProductAttributeValueByAttributeUid(final Attribute attribute) {
		long attributeUid = attribute.getUidPk();

		if (findProductAttributeValueByAttributeUidCache.get(attributeUid) != null) {
			return findProductAttributeValueByAttributeUidCache.get(attributeUid);
		}

		List<AttributeValueInfo> result = fallbackAttributeService.findProductAttributeValueByAttributeUid(attribute);
		findProductAttributeValueByAttributeUidCache.put(attributeUid, result);

		return result;
	}

	@Override
	public List<Attribute> getProductAttributes(final List<String> attributeKeys) {
		return fallbackAttributeService.getProductAttributes(attributeKeys);
	}

	@Override
	public List<AttributeValueInfo> findProductSkuValueAttributeByAttributeUid(final Attribute attribute) {
		return fallbackAttributeService.findProductSkuValueAttributeByAttributeUid(attribute);
	}

	@Override
	public Set<String> getCustomerProfileAttributeKeys() {
		return getAttributes(AttributeUsage.CUSTOMERPROFILE)
				.stream().map(Attribute::getKey)
				.collect(Collectors.toSet());
	}

	public void setAttributesCache(final Cache<Integer, List<Attribute>> attributesCache) {
		this.attributesCache = attributesCache;
	}

	public void setFindProductAttributeValueByAttributeUidCache(final Cache<Long, List<AttributeValueInfo>>
																			 findProductAttributeValueByAttributeUidCache) {
		this.findProductAttributeValueByAttributeUidCache = findProductAttributeValueByAttributeUidCache;
	}

	public void setFindByKeyCache(final Cache<String, Attribute> findByKeyCache) {
		this.findByKeyCache = findByKeyCache;
	}

	public void setFallbackAttributeService(final AttributeService fallbackAttributeService) {
		this.fallbackAttributeService = fallbackAttributeService;
	}

	@Override
	public Object getObject(final long uid) throws EpServiceException {
		return get(uid);
	}
}
