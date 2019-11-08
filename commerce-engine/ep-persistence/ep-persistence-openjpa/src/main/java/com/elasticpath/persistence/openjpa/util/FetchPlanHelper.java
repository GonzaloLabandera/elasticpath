/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.persistence.openjpa.util;

import java.util.Collection;
import java.util.Map;

import javax.persistence.EntityManager;

import org.apache.openjpa.persistence.FetchPlan;
import org.apache.openjpa.persistence.jdbc.FetchMode;

import com.elasticpath.persistence.api.LoadTuner;

/**
 * The interface for {@link org.apache.openjpa.persistence.FetchPlan} configuration related operations.
 * Unlike the previous version, the majority of methods in this version are  setters for load tuners and lazy fields, used in service and DAO
 * classes.
 *
 * The {@link #configureFetchPlan(EntityManager)} and {@link #clearFetchPlan(FetchPlan)} methods are used at the very end, right before the
 * query execution, ensuring that a {@link FetchPlan} is consistently configured and cleared throughout the calls.
 */
public interface FetchPlanHelper {

	/**
	 * Set one or more load tuners.
	 *
	 * @param loadTuners an array of load tuners.
	 */
	void setLoadTuners(LoadTuner... loadTuners);

	/**
	 * Set a map with pairs of Class and a collection of lazy fields to be loaded.
	 * E.g CustomerImpl.class, {"preferredShippingAddress", "customerProfile"}
	 *
	 * @param lazyFields a map with lazy fields to be loaded.
	 */
	void setCollectionOfLazyFields(Map<Class<?>, Collection<String>> lazyFields);

	/**
	 * Set a map with pairs of Class and a lazy field to be loaded.
	 * E.g CustomerImpl.class, "preferredShippingAddress"
	 *
	 * @param lazyFields a map with lazy fields to be loaded.
	 */
	void setLazyFields(Map<Class<?>, String> lazyFields);

	/**
	 * Set a class and a collection of lazy fields to be loaded.
	 *
	 * @param clazz the target class to load lazy fields for.
	 * @param lazyFields a collection of lazy fields to be loaded.
	 */
	void setLazyFields(Class<?> clazz, Collection<String> lazyFields);

	/**
	 * Set fetch mode.
	 *
	 * @param fetchMode the fetch mode.
	 */
	void setFetchMode(FetchMode fetchMode);

	/**
	 * Obtain a {@link FetchPlan} from a given entity manager and configure it by adding
	 * lazy fields, groups and the fetch mode.
	 *
	 * @param entityManager the entity manager.
	 * @return configured fetch plan.
	 */
	FetchPlan configureFetchPlan(EntityManager entityManager);

	/**
	 * Clear provided fetch plan.
	 *
	 * @param fetchPlan the {@link FetchPlan}
	 */
	void clearFetchPlan(FetchPlan fetchPlan);
}
