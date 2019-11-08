/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.persistence.openjpa.routing;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.openjpa.persistence.jdbc.FetchMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elasticpath.persistence.api.LoadTuner;
import com.elasticpath.persistence.openjpa.util.FetchModeSettings;

/**
 * Holds vital info about modified domain entities, user id and whether a query can be safely executed on db replica.
 *
 * This is a request-scoped bean (managed by com.elasticpath.rest.relos.rs.authentication.web.listener.RequestListener) and it's thread-safe
 */
public class HDSSupportBeanImpl implements HDSSupportBean {

	private static final Logger LOG = LoggerFactory.getLogger(HDSSupportBeanImpl.class);

	private boolean hdsSupportEnabled;
	private boolean shouldCheckDbConnectionUrls = true;

	private final ThreadLocal<HDSSupportThreadLocalStates> hdsSupportThreadLocalStatesTL = ThreadLocal.withInitial(HDSSupportThreadLocalStates::new);


	/**
	 * Default constructor.
	 */
	public HDSSupportBeanImpl() {
		LOG.info(":: HDSSupportBean created");
	}

	@Override
	public void setQueryIsSafeForReplica(final boolean isSafe) {
		hdsSupportThreadLocalStatesTL.get().setQueryIsSafeForReplica(isSafe);
	}

	@Override
	public boolean isQuerySafeForReplica() {
		return hdsSupportThreadLocalStatesTL.get().isQueryIsSafeForReplica();
	}

	@Override
	public Set<String> getModifiedEntities() {
		return hdsSupportThreadLocalStatesTL.get().getModifiedEntities();
	}

	@Override
	public void addModifiedEntity(final String modifiedEntity) {
		getModifiedEntities().add(modifiedEntity);
	}

	@Override
	public void clearAll() {
		hdsSupportThreadLocalStatesTL.remove();
	}

	@Override
	public boolean isHdsSupportEnabled() {
		return hdsSupportEnabled;
	}

	@Override
	public void setHdsSupportEnabled(final boolean hdsSupportEnabled) {
		this.hdsSupportEnabled = hdsSupportEnabled;
	}

	@Override
	public void setNeedToCheckDbConnectionUrls(final boolean needToCheck) {
		this.shouldCheckDbConnectionUrls = needToCheck;
	}

	@Override
	public boolean needToCheckDbConnectionUrls() {
		return shouldCheckDbConnectionUrls;
	}

	@Override
	public List<LoadTuner> getLoadTuners() {
		return hdsSupportThreadLocalStatesTL.get().getLoadTuners();
	}

	@Override
	public List<LoadTuner> getLoadTunersAndRemove() {
		return hdsSupportThreadLocalStatesTL.get().getLoadTunersAndRemove();
	}

	@Override
	public void setLoadTuners(final LoadTuner[] loadTuners) {
		hdsSupportThreadLocalStatesTL.get().setLoadTuners(loadTuners);
	}

	@Override
	public Map<Class<?>, Collection<String>> getLazyFields() {
		return hdsSupportThreadLocalStatesTL.get().getLazyFields();
	}

	@Override
	public Map<Class<?>, Collection<String>> getLazyFieldsAndRemove() {
		return hdsSupportThreadLocalStatesTL.get().getLazyFieldsAndRemove();
	}

	@Override
	public void addLazyFields(final Map<Class<?>, Collection<String>> lazyFields) {
		getLazyFields().putAll(lazyFields);
	}

	@Override
	public void addLazyFields(final Class<?> clazz, final Collection<String> lazyFields) {
		getLazyFields().put(clazz, lazyFields);
	}

	@Override
	public FetchMode getFetchMode() {
		return hdsSupportThreadLocalStatesTL.get().getFetchMode();
	}

	@Override
	public FetchMode getFetchModeAndRemove() {
		return hdsSupportThreadLocalStatesTL.get().getFetchModeAndRemove();
	}

	@Override
	public void setFetchMode(final FetchMode fetchMode) {
		hdsSupportThreadLocalStatesTL.get().setFetchMode(fetchMode);
	}

	@Override
	public FetchModeSettings getFetchModeSettings() {
		return hdsSupportThreadLocalStatesTL.get().getFetchModeSettings();
	}

	@Override
	public void setFetchModeSettings(final FetchModeSettings fetchModeSettings) {
		hdsSupportThreadLocalStatesTL.get().setFetchModeSettings(fetchModeSettings);
	}

	@Override
	public void cleanFetchModeSettings() {
		hdsSupportThreadLocalStatesTL.get().setFetchModeSettings(null);
	}

	@Override
	public boolean hasLoadTuners() {
		return !hdsSupportThreadLocalStatesTL.get().getLoadTuners().isEmpty();
	}

	@Override
	public boolean hasLazyFields() {
		return !hdsSupportThreadLocalStatesTL.get().getLazyFields().isEmpty();
	}

	@Override
	public boolean hasFetchMode() {
		return hdsSupportThreadLocalStatesTL.get().getFetchMode() != null;
	}
}
