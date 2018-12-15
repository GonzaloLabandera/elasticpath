/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */

package com.elasticpath.caching.core.rules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;

import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.cache.Cache;
import com.elasticpath.caching.core.CodeUidDateCacheKey;
import com.elasticpath.commons.exception.DuplicateNameException;
import com.elasticpath.domain.catalog.Catalog;
import com.elasticpath.domain.rules.EpRuleBase;
import com.elasticpath.domain.rules.Rule;
import com.elasticpath.domain.store.Store;
import com.elasticpath.service.rules.DuplicatePromoCodeException;
import com.elasticpath.service.rules.RuleService;
import com.elasticpath.service.rules.impl.CacheableRuleService;

/**
 * A caching version of the {@link com.elasticpath.service.rules.RuleService} interface.
 */
public class CachingRuleServiceImpl implements CacheableRuleService {

	// Rule code -> Rule
	private Cache<String, Rule> ruleByRuleCodeCache;

	// Rule name -> Rule code
	private Cache<String, String> ruleCodeByRuleNameCache;

	// Rule uidPk -> Rule code
	private Cache<Long, String> ruleCodeByRuleUidCache;

	// Rule uidPks -> Rule codes
	private Cache<Collection<Long>, Collection<String>> ruleCodesByRuleUidsCache;

	// Rule base by scenario
	private Cache<Collection<Long>, EpRuleBase> ruleBaseByScenarioCache;

	// Rule base by store, scenario and date
	private Cache<CodeUidDateCacheKey, EpRuleBase> changedStoreRuleBaseCache;

	// Rule base by catalog, scenario and date
	private Cache<CodeUidDateCacheKey, EpRuleBase> changedCatalogRuleBaseCache;

	private RuleService decorated;
	
	@Override
	public String findRuleCodeById(final long ruleUid) {

		String ruleCode = ruleCodeByRuleUidCache.get(ruleUid);
		if (ruleCode != null) {
			return ruleCode;
		}

	 	ruleCode = getDecorated().findRuleCodeById(ruleUid);
		ruleCodeByRuleUidCache.put(ruleUid, ruleCode);

		return ruleCode;
	}

	@Override
	public Collection<String> findCodesByUids(final Collection<Long> ruleIds) throws EpServiceException {

		Collection<String> ruleCodes = ruleCodesByRuleUidsCache.get(ruleIds);
		if (ruleCodes != null) {
			return ruleCodes;
		}

		ruleCodes = getDecorated().findCodesByUids(ruleIds);
		ruleCodesByRuleUidsCache.put(ruleIds, ruleCodes);

		return ruleCodes;
	}


	@Override
	public List<Rule> findByUids(final Collection<Long> ruleUids) {

		final List<Rule> cachedRules = new ArrayList<>();

		/*
			find only those IDs that are not cached
		 */
		final List<Long> missingRuleIds = ruleUids.stream()
			.filter(ruleUid -> filterNonCachedRuleIds(cachedRules, ruleUid))
			.collect(Collectors.toList());

		if (!missingRuleIds.isEmpty()) {
			List<Rule> rules = getDecorated().findByUids(ruleUids);
			rules.forEach(this::addToCache);
			return rules;
		}

		return cachedRules;
	}

	private boolean filterNonCachedRuleIds(final List<Rule> cachedRules, final Long ruleUid) {
		final String ruleCode = ruleCodeByRuleUidCache.get(ruleUid);
		if (ruleCode == null) {
			return true;
		}

		final Rule cachedRule = ruleByRuleCodeCache.get(ruleCode);
		if (cachedRule == null) {
			return true;
		}

		cachedRules.add(cachedRule);
		return false;
	}


	@Override
	public Rule findByRuleCode(final String ruleCode) throws EpServiceException {

		final Rule cachedRule = ruleByRuleCodeCache.get(ruleCode);
		if (cachedRule != null) {
			return cachedRule;
		}

		final Rule rule = getDecorated().findByRuleCode(ruleCode);
		addToCache(rule);
		return rule;
	}

	@Override
	public Rule findByName(final String name) throws DuplicateNameException {

		final String ruleCode = ruleCodeByRuleNameCache.get(name);
		if (ruleCode != null) {
			return findByRuleCode(ruleCode);
		}

		final Rule rule = getDecorated().findByName(name);
		addToCache(rule);
		return rule;
	}

	@Override
	public Rule load(final long ruleUid) throws EpServiceException {
		return getObject(ruleUid);
	}

	@Override
	public Rule get(final long uid) throws EpServiceException {
		return getObject(uid);
	}

	@Override
	public Rule getObject(final long uid) throws EpServiceException {
		if (uid <= 0) {
			//returns a Spring bean - no need to cache
			return (Rule) getDecorated().getObject(uid);
		}

		final String ruleCode = ruleCodeByRuleUidCache.get(uid);
		if (ruleCode != null) {
			return findByRuleCode(ruleCode);
		}

		final Rule rule = (Rule) getDecorated().getObject(uid);
		addToCache(rule);
		return rule;
	}

	@Override
	public Rule add(final Rule rule) throws DuplicateNameException, DuplicatePromoCodeException {
		final Rule persistedRule = getDecorated().add(rule);
		addToCache(persistedRule);
		return persistedRule;
	}

	@Override
	public Rule update(final Rule rule) throws DuplicateNameException, DuplicatePromoCodeException {
		final Rule persistedRule = getDecorated().update(rule);
		addToCache(persistedRule);
		return persistedRule;
	}

	@Override
	public void remove(final Rule rule) throws EpServiceException {
		getDecorated().remove(rule);
		removeFromCache(rule);
	}

	@Override
	public EpRuleBase findRuleBaseByScenario(final Store store, final Catalog catalog, final int scenarioId) {
		long storeUid = Optional.ofNullable(store)
			.map(Store::getUidPk)
			.orElse(-1L);

		long catalogUid = Optional.ofNullable(catalog)
			.map(Catalog::getUidPk)
			.orElse(-1L);


		Collection<Long> cacheKeyIds = Lists.newLinkedList(Arrays.asList(storeUid, catalogUid, (long) scenarioId));

		if (ruleBaseByScenarioCache.containsKey(cacheKeyIds)) {
			return ruleBaseByScenarioCache.get(cacheKeyIds);
		}

		final EpRuleBase ruleBase = getDecorated().findRuleBaseByScenario(store, catalog, scenarioId);
		ruleBaseByScenarioCache.put(cacheKeyIds, ruleBase);

		return ruleBase;
	}

	@Override
	public EpRuleBase findChangedStoreRuleBases(final String storeCode, final int scenarioId, final Date date) {

		CodeUidDateCacheKey cacheKey = new CodeUidDateCacheKey(storeCode, scenarioId, date);

		if (changedStoreRuleBaseCache.containsKey(cacheKey)) {
			return changedStoreRuleBaseCache.get(cacheKey);
		}

		final EpRuleBase ruleBase = getDecorated().findChangedStoreRuleBases(storeCode, scenarioId, date);
		changedStoreRuleBaseCache.put(cacheKey, ruleBase);

		return ruleBase;
	}

	@Override
	public EpRuleBase findChangedCatalogRuleBases(final String catalogCode, final int scenarioId, final Date date) {

		CodeUidDateCacheKey cacheKey = new CodeUidDateCacheKey(catalogCode, scenarioId, date);

		if (changedCatalogRuleBaseCache.containsKey(cacheKey)) {
			return changedCatalogRuleBaseCache.get(cacheKey);
		}

		final EpRuleBase ruleBase = getDecorated().findChangedCatalogRuleBases(catalogCode, scenarioId, date);
		changedCatalogRuleBaseCache.put(cacheKey, ruleBase);

		return ruleBase;
	}

	@Override
	public void setDecorated(final RuleService decorated) {
		this.decorated = decorated;
	}

	@Override
	public RuleService getDecorated() {
		return decorated;
	}

	/**
	 * Add a rule to various caches.
	 * @param rule A rule {@link Rule}.
	 */
	protected void addToCache(final Rule rule) {

		final String code = rule.getCode();
		final String name = rule.getName();

		ruleByRuleCodeCache.put(code, rule);
		ruleCodeByRuleNameCache.put(name, code);
		ruleCodeByRuleUidCache.put(rule.getUidPk(), code);
	}

	/**
	 * Remove a rule from various caches.
	 * @param rule A rule {@link Rule}.
	 */
	protected void removeFromCache(final Rule rule) {
		final String code = rule.getCode();
		if (code != null) {
			ruleByRuleCodeCache.remove(code);
		}

		final String name = rule.getName();
		if (name != null) {
			ruleCodeByRuleNameCache.remove(name);
		}

		ruleCodeByRuleUidCache.remove(rule.getUidPk());
	}

	public void setRuleByRuleCodeCache(final Cache<String, Rule> ruleByRuleCodeCache) {
		this.ruleByRuleCodeCache = ruleByRuleCodeCache;
	}

	public void setRuleCodeByRuleNameCache(final Cache<String, String> ruleCodeByRuleNameCache) {
		this.ruleCodeByRuleNameCache = ruleCodeByRuleNameCache;
	}

	public void setRuleCodeByRuleUidCache(final Cache<Long, String> ruleCodeByRuleUidCache) {
		this.ruleCodeByRuleUidCache = ruleCodeByRuleUidCache;
	}

	public void setRuleCodesByRuleUidsCache(final Cache<Collection<Long>, Collection<String>> ruleCodesByRuleUidsCache) {
		this.ruleCodesByRuleUidsCache = ruleCodesByRuleUidsCache;
	}
	public void setRuleBaseByScenarioCache(final Cache<Collection<Long>, EpRuleBase> ruleBaseByScenarioCache) {
				this.ruleBaseByScenarioCache = ruleBaseByScenarioCache;
	}

	public void setChangedStoreRuleBaseCache(final Cache<CodeUidDateCacheKey, EpRuleBase> changedStoreRuleBaseCache) {
		this.changedStoreRuleBaseCache = changedStoreRuleBaseCache;
	}

	public void setChangedCatalogRuleBaseCache(final Cache<CodeUidDateCacheKey, EpRuleBase> changedCatalogRuleBaseCache) {
		this.changedCatalogRuleBaseCache = changedCatalogRuleBaseCache;
	}
}
