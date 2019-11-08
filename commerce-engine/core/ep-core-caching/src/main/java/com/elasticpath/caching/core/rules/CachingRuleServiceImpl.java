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
@SuppressWarnings({ "PMD.GodClass" })
public class CachingRuleServiceImpl implements CacheableRuleService {

	// Rule code -> Rule
	private Cache<String, Optional<Rule>> ruleByRuleCodeCache;

	// Rule name -> Rule code
	private Cache<String, Optional<String>> ruleCodeByRuleNameCache;

	// Rule uidPk -> Rule code
	private Cache<Long, Optional<String>> ruleCodeByRuleUidCache;

	// Rule uidPks -> Rule codes
	private Cache<Collection<Long>, Optional<Collection<String>>> ruleCodesByRuleUidsCache;

	// Rule base by scenario
	private Cache<Collection<Long>, Optional<EpRuleBase>> ruleBaseByScenarioCache;

	// Rule base by store, scenario and date
	private Cache<CodeUidDateCacheKey, Optional<EpRuleBase>> changedStoreRuleBaseCache;

	// Rule base by catalog, scenario and date
	private Cache<CodeUidDateCacheKey, Optional<EpRuleBase>> changedCatalogRuleBaseCache;

	private Cache<Long, Optional<Date>> modifiedDateCache;

	private RuleService decorated;

	@Override
	public String findRuleCodeById(final long ruleUid) {

		Optional<String> ruleCode = ruleCodeByRuleUidCache.get(ruleUid);
		if (ruleCode != null) {
			return ruleCode.orElse(null);
		}

		ruleCode = Optional.ofNullable(getDecorated().findRuleCodeById(ruleUid));
		ruleCodeByRuleUidCache.put(ruleUid, ruleCode);

		return ruleCode.orElse(null);
	}

	@Override
	public Collection<String> findCodesByUids(final Collection<Long> ruleIds) throws EpServiceException {

		Optional<Collection<String>> ruleCodes = ruleCodesByRuleUidsCache.get(ruleIds);
		if (ruleCodes != null) {
			return ruleCodes.orElse(null);
		}

		ruleCodes = Optional.ofNullable(getDecorated().findCodesByUids(ruleIds));
		ruleCodesByRuleUidsCache.put(ruleIds, ruleCodes);

		return ruleCodes.orElse(null);
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
		final Optional<String> ruleCode = ruleCodeByRuleUidCache.get(ruleUid);
		if (ruleCode == null || !ruleCode.isPresent()) {
			return true;
		}

		final Optional<Rule> cachedRule = ruleByRuleCodeCache.get(ruleCode.get());
		if (cachedRule == null || !cachedRule.isPresent()) {
			return true;
		}

		cachedRules.add(cachedRule.get());

		return false;
	}

	@Override
	public Rule findByRuleCode(final String ruleCode) throws EpServiceException {

		final Optional<Rule> cachedRule = ruleByRuleCodeCache.get(ruleCode);
		if (cachedRule != null && cachedRule.isPresent()) {
			return cachedRule.get();
		}

		final Optional<Rule> rule = Optional.ofNullable(getDecorated().findByRuleCode(ruleCode));
		if (rule.isPresent()) {
			addToCache(rule.get());
		}
		return rule.orElse(null);
	}

	@Override
	public Rule findByName(final String name) throws DuplicateNameException {

		final Optional<String> ruleCode = ruleCodeByRuleNameCache.get(name);
		if (ruleCode != null && ruleCode.isPresent()) {
			return findByRuleCode(ruleCode.get());
		}

		final Optional<Rule> rule = Optional.ofNullable(getDecorated().findByName(name));
		if (rule.isPresent()) {
			addToCache(rule.get());
		}
		return rule.orElse(null);
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

		final Optional<String> ruleCode = ruleCodeByRuleUidCache.get(uid);
		if (ruleCode != null && ruleCode.isPresent()) {
			return findByRuleCode(ruleCode.get());
		}

		final Optional<Rule> rule = Optional.ofNullable((Rule) getDecorated().getObject(uid));

		if (rule.isPresent()) {
			addToCache(rule.get());
		}
		return rule.orElse(null);
	}

	@Override
	public Rule add(final Rule rule) throws DuplicateNameException, DuplicatePromoCodeException {
		final Optional<Rule> persistedRule = Optional.ofNullable(getDecorated().add(rule));
		if (persistedRule.isPresent()) {
			addToCache(persistedRule.get());
		}
		return persistedRule.orElse(null);
	}

	@Override
	public Rule update(final Rule rule) throws DuplicateNameException, DuplicatePromoCodeException {
		final Optional<Rule> persistedRule = Optional.ofNullable(getDecorated().update(rule));
		if (persistedRule.isPresent()) {
			addToCache(persistedRule.get());
		}
		return persistedRule.orElse(null);
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

		Optional<EpRuleBase> epRuleBaseCached = ruleBaseByScenarioCache.get(cacheKeyIds);

		if (epRuleBaseCached != null) {
			return epRuleBaseCached.orElse(null);
		}

		final Optional<EpRuleBase> ruleBase = Optional.ofNullable(getDecorated().findRuleBaseByScenario(store, catalog, scenarioId));
		ruleBaseByScenarioCache.put(cacheKeyIds, ruleBase);

		return ruleBase.orElse(null);
	}

	@Override
	public EpRuleBase findChangedStoreRuleBases(final String storeCode, final int scenarioId, final Date date) {

		CodeUidDateCacheKey cacheKey = new CodeUidDateCacheKey(storeCode, scenarioId, date);
		Optional<EpRuleBase> epRuleBaseCached = changedStoreRuleBaseCache.get(cacheKey);

		if (epRuleBaseCached != null) {
			return epRuleBaseCached.orElse(null);
		}

		final Optional<EpRuleBase> ruleBase = Optional.ofNullable(getDecorated().findChangedStoreRuleBases(storeCode, scenarioId, date));

		changedStoreRuleBaseCache.put(cacheKey, ruleBase);

		return ruleBase.orElse(null);
	}

	@Override
	public EpRuleBase findChangedCatalogRuleBases(final String catalogCode, final int scenarioId, final Date date) {

		CodeUidDateCacheKey cacheKey = new CodeUidDateCacheKey(catalogCode, scenarioId, date);
		Optional<EpRuleBase> epRuleBaseCached = changedCatalogRuleBaseCache.get(cacheKey);

		if (epRuleBaseCached != null) {
			return epRuleBaseCached.orElse(null);
		}

		final Optional<EpRuleBase> ruleBase = Optional.ofNullable(getDecorated().findChangedCatalogRuleBases(catalogCode, scenarioId, date));

		changedCatalogRuleBaseCache.put(cacheKey, ruleBase);

		return ruleBase.orElse(null);
	}

	@Override
	public Date getModifiedDateForRuleBase(final long ruleUid) {
		Optional<Date> date = modifiedDateCache.get(ruleUid);
		if (date != null) {
			return date.orElse(null);
		}
		Date modifiedDate = getDecorated().getModifiedDateForRuleBase(ruleUid);

		modifiedDateCache.put(ruleUid, Optional.ofNullable(modifiedDate));

		return modifiedDate;
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
	 *
	 * @param rule A rule {@link Rule}.
	 */
	protected void addToCache(final Rule rule) {

		final String code = rule.getCode();
		final String name = rule.getName();

		ruleByRuleCodeCache.put(code, Optional.of(rule));
		ruleCodeByRuleNameCache.put(name, Optional.of(code));
		ruleCodeByRuleUidCache.put(rule.getUidPk(), Optional.of(code));
	}

	/**
	 * Remove a rule from various caches.
	 *
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

	public void setRuleByRuleCodeCache(final Cache<String, Optional<Rule>> ruleByRuleCodeCache) {
		this.ruleByRuleCodeCache = ruleByRuleCodeCache;
	}

	public void setRuleCodeByRuleNameCache(final Cache<String, Optional<String>> ruleCodeByRuleNameCache) {
		this.ruleCodeByRuleNameCache = ruleCodeByRuleNameCache;
	}

	public void setRuleCodeByRuleUidCache(final Cache<Long, Optional<String>> ruleCodeByRuleUidCache) {
		this.ruleCodeByRuleUidCache = ruleCodeByRuleUidCache;
	}

	public void setRuleCodesByRuleUidsCache(final Cache<Collection<Long>, Optional<Collection<String>>> ruleCodesByRuleUidsCache) {
		this.ruleCodesByRuleUidsCache = ruleCodesByRuleUidsCache;
	}

	public void setRuleBaseByScenarioCache(final Cache<Collection<Long>, Optional<EpRuleBase>> ruleBaseByScenarioCache) {
		this.ruleBaseByScenarioCache = ruleBaseByScenarioCache;
	}

	public void setChangedStoreRuleBaseCache(final Cache<CodeUidDateCacheKey, Optional<EpRuleBase>> changedStoreRuleBaseCache) {
		this.changedStoreRuleBaseCache = changedStoreRuleBaseCache;
	}

	public void setChangedCatalogRuleBaseCache(final Cache<CodeUidDateCacheKey, Optional<EpRuleBase>> changedCatalogRuleBaseCache) {
		this.changedCatalogRuleBaseCache = changedCatalogRuleBaseCache;
	}

	public void setModifiedDateCache(final Cache<Long, Optional<Date>> modifiedDateCache) {
		this.modifiedDateCache = modifiedDateCache;
	}
}
