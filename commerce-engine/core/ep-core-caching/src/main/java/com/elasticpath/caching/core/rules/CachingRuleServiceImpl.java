/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */

package com.elasticpath.caching.core.rules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;

import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.cache.Cache;
import com.elasticpath.cache.CacheResult;
import com.elasticpath.caching.core.CodeUidDateCacheKey;
import com.elasticpath.commons.exception.DuplicateNameException;
import com.elasticpath.domain.catalog.Catalog;
import com.elasticpath.domain.rules.EpRuleBase;
import com.elasticpath.domain.rules.Rule;
import com.elasticpath.domain.store.Store;
import com.elasticpath.service.rules.DuplicatePromoCodeException;
import com.elasticpath.service.rules.RuleService;
import com.elasticpath.service.rules.SellingContextRuleSummary;
import com.elasticpath.service.rules.impl.CacheableRuleService;

/**
 * A caching version of the {@link com.elasticpath.service.rules.RuleService} interface.
 */
@SuppressWarnings({"PMD.GodClass"})
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

	private Cache<Long, Date> modifiedDateCache;
	// Selling context cache
	private Cache<SellingContextCacheKey, List<SellingContextRuleSummary>> sellingContextCache;

	private RuleService decorated;

	/**
	 * Initialize cache.
	 */
	public void init() {
		List<SellingContextRuleSummary> sellingContextDTOList = getDecorated().findAllActiveRuleIdSellingContext();
		for (SellingContextRuleSummary sellingContextDTO : sellingContextDTOList) {
			addToSellingContextCacheByCatalog(sellingContextDTO.getCatalog(), sellingContextDTO);
			addToSellingContextCacheByStore(sellingContextDTO.getStore(), sellingContextDTO);
		}
	}

	private void addToSellingContextCacheByCatalog(final Catalog catalog, final SellingContextRuleSummary sellingContextDTO) {
		if (Objects.nonNull(catalog)) {
			String catalogCode = catalog.getCode();
			SellingContextCacheKey key = SellingContextCacheKey.createCatalogCodeKey(sellingContextDTO.getScenario(), catalogCode);
			addToSellingContextCache(key, sellingContextDTO);
		}
	}

	private void addToSellingContextCacheByStore(final Store store, final SellingContextRuleSummary sellingContextDTO) {
		if (Objects.nonNull(store)) {
			String storeCode = store.getCode();
			SellingContextCacheKey key = SellingContextCacheKey.createStoreCodeKey(sellingContextDTO.getScenario(), storeCode);
			addToSellingContextCache(key, sellingContextDTO);
		}
	}

	private void addToSellingContextCache(final SellingContextCacheKey key, final SellingContextRuleSummary value) {
		CacheResult<List<SellingContextRuleSummary>> sellingContextDTOS = sellingContextCache.get(key);
		if (!sellingContextDTOS.isPresent() || Objects.isNull(sellingContextDTOS.get())) {
			List<SellingContextRuleSummary> dtoList = new ArrayList<>();
			dtoList.add(value);
			sellingContextCache.put(key, dtoList);
		} else {
			sellingContextDTOS.get().add(value);
		}
	}

	@Override
	public String findRuleCodeById(final long ruleUid) {
		return ruleCodeByRuleUidCache.get(ruleUid, key -> getDecorated().findRuleCodeById(ruleUid));
	}

	@Override
	public Collection<String> findCodesByUids(final Collection<Long> ruleIds) throws EpServiceException {
		return ruleCodesByRuleUidsCache.get(ruleIds, key -> getDecorated().findCodesByUids(ruleIds));
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
		final CacheResult<String> ruleCode = ruleCodeByRuleUidCache.get(ruleUid);
		if (!ruleCode.isPresent() || Objects.isNull(ruleCode.get())) {
			return true;
		}

		final CacheResult<Rule> cachedRule = ruleByRuleCodeCache.get(ruleCode.get());
		if (!cachedRule.isPresent() || Objects.isNull(cachedRule.get())) {
			return true;
		}

		cachedRules.add(cachedRule.get());

		return false;
	}

	@Override
	public Rule findByRuleCode(final String ruleCode) throws EpServiceException {

		final CacheResult<Rule> cachedRule = ruleByRuleCodeCache.get(ruleCode);
		if (cachedRule.isPresent() && Objects.nonNull(cachedRule.get())) {
			return cachedRule.get();
		}

		final Optional<Rule> rule = Optional.ofNullable(getDecorated().findByRuleCode(ruleCode));
		rule.ifPresent(this::addToCache);
		return rule.orElse(null);
	}

	@Override
	public Rule findByName(final String name) throws DuplicateNameException {

		final CacheResult<String> ruleCode = ruleCodeByRuleNameCache.get(name);
		if (ruleCode.isPresent() && Objects.nonNull(ruleCode.get())) {
			return findByRuleCode(ruleCode.get());
		}

		final Optional<Rule> rule = Optional.ofNullable(getDecorated().findByName(name));
		rule.ifPresent(this::addToCache);
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

		final CacheResult<String> ruleCode = ruleCodeByRuleUidCache.get(uid);
		if (ruleCode.isPresent() && Objects.nonNull(ruleCode.get())) {
			return findByRuleCode(ruleCode.get());
		}

		final Optional<Rule> rule = Optional.ofNullable((Rule) getDecorated().getObject(uid));

		rule.ifPresent(this::addToCache);
		return rule.orElse(null);
	}

	@Override
	public Rule add(final Rule rule) throws DuplicateNameException, DuplicatePromoCodeException {
		final Optional<Rule> persistedRule = Optional.ofNullable(getDecorated().add(rule));
		persistedRule.ifPresent(this::addToCache);
		return persistedRule.orElse(null);
	}

	@Override
	public Rule update(final Rule rule) throws DuplicateNameException, DuplicatePromoCodeException {
		final Optional<Rule> persistedRule = Optional.ofNullable(getDecorated().update(rule));
		persistedRule.ifPresent(this::addToCache);
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
		return ruleBaseByScenarioCache.get(cacheKeyIds, key -> getDecorated().findRuleBaseByScenario(store, catalog, scenarioId));
	}

	@Override
	public List<SellingContextRuleSummary> findActiveRuleIdSellingContextByScenarioAndCatalog(final int scenario, final String catalogCode) {
		final SellingContextCacheKey cacheKey = SellingContextCacheKey.createCatalogCodeKey(scenario, catalogCode);
		return sellingContextCache.get(cacheKey, key -> getDecorated().findActiveRuleIdSellingContextByScenarioAndCatalog(scenario, catalogCode));
	}

	@Override
	public List<SellingContextRuleSummary> findActiveRuleIdSellingContextByScenarioAndStore(final int scenario, final String storeCode) {
		final SellingContextCacheKey cacheKey = SellingContextCacheKey.createStoreCodeKey(scenario, storeCode);
		return sellingContextCache.get(cacheKey, key -> getDecorated().findActiveRuleIdSellingContextByScenarioAndStore(scenario, storeCode));
	}

	@Override
	public EpRuleBase findChangedStoreRuleBases(final String storeCode, final int scenarioId, final Date date) {
		final CodeUidDateCacheKey cacheKey = new CodeUidDateCacheKey(storeCode, scenarioId, date);
		return changedStoreRuleBaseCache.get(cacheKey, key -> getDecorated().findChangedStoreRuleBases(storeCode, scenarioId, date));
	}

	@Override
	public EpRuleBase findChangedCatalogRuleBases(final String catalogCode, final int scenarioId, final Date date) {
		final CodeUidDateCacheKey cacheKey = new CodeUidDateCacheKey(catalogCode, scenarioId, date);
		return changedCatalogRuleBaseCache.get(cacheKey, key -> getDecorated().findChangedCatalogRuleBases(catalogCode, scenarioId, date));
	}

	@Override
	public Date getModifiedDateForRuleBase(final long ruleUid) {
		return modifiedDateCache.get(ruleUid, key -> getDecorated().getModifiedDateForRuleBase(ruleUid));
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

		ruleByRuleCodeCache.put(code, rule);
		ruleCodeByRuleNameCache.put(name, code);
		ruleCodeByRuleUidCache.put(rule.getUidPk(), code);
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

	public void setModifiedDateCache(final Cache<Long, Date> modifiedDateCache) {
		this.modifiedDateCache = modifiedDateCache;
	}

	public void setSellingContextCache(final Cache<SellingContextCacheKey, List<SellingContextRuleSummary>> sellingContextCache) {
		this.sellingContextCache = sellingContextCache;
	}
}
