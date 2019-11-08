/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.persistence.openjpa.routing;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.openjpa.persistence.jdbc.FetchMode;

import com.elasticpath.persistence.api.LoadTuner;
import com.elasticpath.persistence.openjpa.util.FetchModeSettings;

/**
 * An interface towards request-scoped thread locals required for correct operation of the horizontal-db-scaling (HDS) feature.
 */
 public interface HDSSupportBean {

	/**
	 * Set a flag that indicates that query is safe to execute on replica.
	 *
	 * @param isSafe A flag.
	 */
	void setQueryIsSafeForReplica(boolean isSafe);

	/**
	 * Check if query is safe to execute on replica.
	 * @return true, if query is safe.
	 */
	boolean isQuerySafeForReplica();

	/**
	 * Return the list of modified entities (if any), found in the current thread.
	 *
	 * @return the list of modified entities.
	 */
	Set<String> getModifiedEntities();

	/**
	 * Add modified entity to the list.
	 *
	 * @param modifiedEntity the name of the modified entity (e.g. CustomerImpl)
	 */
	void addModifiedEntity(String modifiedEntity);

	/**
	 * Clear all thread-local variables.
	 */
	void clearAll();

	/**
	 * Check if HDS feature is enabled.
	 *
	 * @return true, if HDS feature is enabled.
	 */
	boolean isHdsSupportEnabled();

	/**
	 * One-time set on application startup - indicates whether HDS feature is enabled.
	 * @param hdsSupportEnabled the flag.
	 */
	void setHdsSupportEnabled(boolean hdsSupportEnabled);

	/**
	 * One-time set on application startup - RW and RO db connection URLs need to be checked to
	 * determine the availability of the HDS feature.
	 * @param needToCheck a flag to set if check is required
	 */
	void setNeedToCheckDbConnectionUrls(boolean needToCheck);

	/**
	 * Check if need to compare RW and RO db connection URLs.
	 *
	 * @return true, if it's required to check the urls
	 */
	boolean needToCheckDbConnectionUrls();

	/**
	 * Get load tuners.
	 *
	 * @return the load tuners.
	 */
	List<LoadTuner> getLoadTuners();

	/**
	 * Get load tuners and clear the list.
	 *
	 * @return the list of load tuners.
	 */
	List<LoadTuner> getLoadTunersAndRemove();

	/**
	 * Set load tuners.
	 *
	 * @param loadTuners the load tuners.
	 */
	void setLoadTuners(LoadTuner[] loadTuners);

	/**
	 * Get lazy fields.
	 *
	 * @return the lazy fields.
	 */
	Map<Class<?>, Collection<String>> getLazyFields();

	/**
	 * Return lazy fields and clear the map.
	 *
	 * @return the lazy fields.
	 */
	Map<Class<?>, Collection<String>> getLazyFieldsAndRemove();

	/**
	 * Add lazy fields.
	 *
	 * @param lazyFields the lazy fields.
	 */
	void addLazyFields(Map<Class<?>, Collection<String>> lazyFields);

	/**
	 * Add lazy fields.
	 *
	 * @param clazz the class to load lazy fields for.
	 * @param lazyFields the lazy fields.
	 */
	void addLazyFields(Class<?> clazz, Collection<String> lazyFields);

	/**
	 * Get the fetch mode.
	 *
	 * @return the fetch mode.
	 */
	FetchMode getFetchMode();

	/**
	 * Get the fetch mode and reset it.
	 *
	 * @return the fetch mode.
	 */
	FetchMode getFetchModeAndRemove();

	/**
	 * Set the fetch mode.
	 *
	 * @param fetchMode the fetch mode.
	 */
	void setFetchMode(FetchMode fetchMode);

	/**
	 * Get fetch mode settings.
	 *
	 * @return the fetch mode settings.
	 */
	FetchModeSettings getFetchModeSettings();

	/**
	 * Set fetch mode settings.
	 *
	 * @param fetchModeSettings the fetch mode settings.
	 */
	void setFetchModeSettings(FetchModeSettings fetchModeSettings);

	/**
	 * Clear fetch mode settings.
	 */
	void cleanFetchModeSettings();

	/**
	 * Return a flag if load tuners are set.
	 *
	 * @return true, if load tuners are set.
	 */
	boolean hasLoadTuners();

	/**
	 * Return a flag if lazy fields are set.
	 *
	 * @return true, if lazy fields are set.
	 */
	boolean hasLazyFields();

	/**
	 * Return a flag if fetch mode is set.
	 *
	 * @return true, if fetch mode is set
	 */
	boolean hasFetchMode();
}
