/**
 * Copyright (c) Elastic Path Software Inc., 2013
 */
package com.elasticpath.persistence.api;

import java.util.Collection;


/**
 * Load tuner that applies a particular fetch group to the fetch plan.
 */
public interface FetchGroupLoadTuner extends LoadTuner, Iterable<String> {

	/**
	 * Adds a list of fetch groups to added to the fetch plan.
	 * 
	 * @param fetchGroups the list of fetch groups to be added to the fetch plan
	 */
	void addFetchGroup(String... fetchGroups);

	/**
	 * Removes a list of fetch groups that were previously added. Ignores fetch groups that were
	 * not apart of this load tuner.
	 * 
	 * @param fetchGroups the list of fetch groups that were previously added
	 */
	void removeFetchGroup(String... fetchGroups);
	
	
	/**
	 * Gets the collection of fetch groups to use.
	 *
	 * @return fetchGroups the collection of fetch groups to use
	 */
	Collection<String> getFetchGroups();

	/**
	 * Set a flag indicating that existing fetch groups need to be removed.
	 * This method must be called on a <strong>new (Spring prototype)</strong> instance of {@link FetchGroupLoadTuner},
	 * not on a singleton.
	 *
	 * @param cleanExistingGroups if true, existing fetch groups will be removed.
	 * @return the current instance of {@link FetchGroupLoadTuner}
	 */
	FetchGroupLoadTuner setCleanExistingGroups(boolean cleanExistingGroups);
}
