/**
 * Copyright (c) Elastic Path Software Inc., 2013
 */
package com.elasticpath.persistence.support.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.apache.openjpa.meta.FetchGroup;
import org.apache.openjpa.persistence.FetchPlan;

import com.elasticpath.domain.impl.AbstractEpDomainImpl;
import com.elasticpath.persistence.api.FetchGroupLoadTuner;
import com.elasticpath.persistence.api.LoadTuner;

/**
 * Default implementation of {@link FetchGroupLoadTuner}.
 */
public class FetchGroupLoadTunerImpl extends AbstractEpDomainImpl implements FetchGroupLoadTuner {

	/** Serial version id. */
	private static final long serialVersionUID = 5000000001L;
	private static final ThreadLocal<Boolean> CLEAN_EXISTING_GROUPS = ThreadLocal.withInitial(() -> Boolean.TRUE);

	private Collection<String> fetchGroups;

	@Override
	public void addFetchGroup(final String... fetchGroups) {
		if (fetchGroups == null) {
			return;
		}
		for (String fetchGroup : fetchGroups) {
			getFetchGroups().add(fetchGroup);
		}
	}

	@Override
	public void removeFetchGroup(final String... fetchGroups) {
		if (fetchGroups == null) {
			return;
		}
		for (String fetchGroup : fetchGroups) {
			getFetchGroups().remove(fetchGroup);
		}
	}

	@Override
	public Iterator<String> iterator() {
		return getFetchGroups().iterator();
	}

	/**
	 * Sets the collection of fetch groups to use. Here solely for spring injection.
	 *
	 * @param fetchGroups the collection of fetch groups to use
	 */
	public void setFetchGroups(final Collection<String> fetchGroups) {
		this.fetchGroups = fetchGroups;
	}

	@Override
	public Collection<String> getFetchGroups() {
		if (fetchGroups == null) {
			fetchGroups = new ArrayList<>();
		}
		return fetchGroups;
	}

	@Override
	public boolean contains(final LoadTuner loadTuner) {
		if (!(loadTuner instanceof FetchGroupLoadTuner)) {
			return false;
		}
		FetchGroupLoadTuner fetchGroupLoadTuner = (FetchGroupLoadTuner) loadTuner;
		return getFetchGroups().containsAll(fetchGroupLoadTuner.getFetchGroups());
	}

	@Override
	public LoadTuner merge(final LoadTuner loadTuner) {
		if (!(loadTuner instanceof FetchGroupLoadTuner)) {
			return this;
		}

		FetchGroupLoadTuner fetchGroupLoadTuner = (FetchGroupLoadTuner) loadTuner;
		FetchGroupLoadTuner mergedFetchGroupLoadTuner = new FetchGroupLoadTunerImpl();
		mergedFetchGroupLoadTuner.addFetchGroup(getFetchGroups().toArray(new String[]{}));
		for (String fetchGroup : fetchGroupLoadTuner.getFetchGroups()) {
			if (!mergedFetchGroupLoadTuner.getFetchGroups().contains(fetchGroup)) {
				mergedFetchGroupLoadTuner.addFetchGroup(fetchGroup);
			}
		}

		return mergedFetchGroupLoadTuner;
	}

	@Override
	public void configure(final FetchPlan fetchPlan) {
		if (CLEAN_EXISTING_GROUPS.get()) {
			fetchPlan.clearFetchGroups();
			fetchPlan.removeFetchGroup(FetchGroup.NAME_DEFAULT);
		}

		CLEAN_EXISTING_GROUPS.remove();

		for (String fetchGroup : this) {
			fetchPlan.addFetchGroup(fetchGroup);
		}
	}

	@Override
	public FetchGroupLoadTuner setCleanExistingGroups(final boolean cleanExistingGroups) {
		CLEAN_EXISTING_GROUPS.set(cleanExistingGroups);
		return this;
	}
}
