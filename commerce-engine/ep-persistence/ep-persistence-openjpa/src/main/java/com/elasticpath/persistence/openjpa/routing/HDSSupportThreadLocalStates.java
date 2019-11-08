/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.persistence.openjpa.routing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.apache.openjpa.persistence.jdbc.FetchMode;

import com.elasticpath.persistence.api.LoadTuner;
import com.elasticpath.persistence.openjpa.util.FetchModeSettings;

/**
 * A composite structure used for storing various state and as replacement for
 * multiple thread-locals.
 */
public class HDSSupportThreadLocalStates {
	private boolean queryIsSafeForReplica;
	private final Set<String> modifiedEntities = new HashSet<>();

	private FetchMode fetchMode;
	private FetchModeSettings fetchModeSettings;

	private final List<LoadTuner> loadTuners = new ArrayList<>();
	private Map<Class<?>, Collection<String>> lazyFields = new HashMap<>();

	public boolean isQueryIsSafeForReplica() {
		return queryIsSafeForReplica;
	}

	public void setQueryIsSafeForReplica(final boolean queryIsSafeForReplica) {
		this.queryIsSafeForReplica = queryIsSafeForReplica;
	}

	public Set<String> getModifiedEntities() {
		return modifiedEntities;
	}

	public FetchMode getFetchMode() {
		return fetchMode;
	}

	/**
	 * Get the fetch mode and reset it.
	 *
	 * @return the fetch mode.
	 */
	public FetchMode getFetchModeAndRemove() {
		FetchMode copy = this.fetchMode;
		this.fetchMode = null;

		return copy;
	}


	public void setFetchMode(final FetchMode fetchMode) {
		this.fetchMode = fetchMode;
	}

	public FetchModeSettings getFetchModeSettings() {
		return fetchModeSettings;
	}

	public void setFetchModeSettings(final FetchModeSettings fetchModeSettings) {
		this.fetchModeSettings = fetchModeSettings;
	}

	public List<LoadTuner> getLoadTuners() {
		return loadTuners;
	}

	/**
	 * Get load tuners and clear the list.
	 *
	 * @return the list of load tuners.
	 */
	public List<LoadTuner> getLoadTunersAndRemove() {
		List<LoadTuner> immutableLoadTuners = ImmutableList.copyOf(loadTuners);
		this.loadTuners.clear();

		return immutableLoadTuners;
	}

	/**
	 * Set load tuners.
	 *
	 * @param loadTuners the load tuners.
	 */
	public void setLoadTuners(final LoadTuner[] loadTuners) {
		this.loadTuners.addAll(Arrays.asList(loadTuners));
	}

	public Map<Class<?>, Collection<String>> getLazyFields() {
		return lazyFields;
	}

	public void setLazyFields(final Map<Class<?>, Collection<String>> lazyFields) {
		this.lazyFields = lazyFields;
	}

	/**
	 * Return lazy fields and clear the map.
	 *
	 * @return the lazy fields.
	 */
	public Map<Class<?>, Collection<String>> getLazyFieldsAndRemove() {
		Map<Class<?>, Collection<String>> immutableLazyFields = ImmutableMap.copyOf(this.lazyFields);
		this.lazyFields.clear();

		return immutableLazyFields;
	}

}
