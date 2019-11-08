/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.persistence.openjpa.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import javax.persistence.EntityManager;

import org.apache.openjpa.persistence.FetchPlan;
import org.apache.openjpa.persistence.OpenJPAPersistence;
import org.apache.openjpa.persistence.jdbc.FetchMode;
import org.apache.openjpa.persistence.jdbc.JDBCFetchPlan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elasticpath.persistence.api.LoadTuner;
import com.elasticpath.persistence.openjpa.routing.HDSSupportBean;

/**
 * A util class for performing various operations on a given {@link FetchPlan}.
 */
public class OpenJPAFetchPlanHelperImpl implements FetchPlanHelper {
	private static final Logger LOG = LoggerFactory.getLogger(OpenJPAFetchPlanHelperImpl.class);

	private HDSSupportBean hdsSupportBean;

	@Override
	public void setLoadTuners(final LoadTuner... loadTuners) {
		int numOfTuners = loadTuners.length;

		if (numOfTuners > 1 || loadTuners[0] != null) {
			hdsSupportBean.setLoadTuners(loadTuners);
		}
	}

	@Override
	public void setCollectionOfLazyFields(final Map<Class<?>, Collection<String>> lazyFields) {
		hdsSupportBean.addLazyFields(lazyFields);
	}

	@Override
	public void setLazyFields(final Map<Class<?>, String> lazyFields) {
		Map<Class<?>, Collection<String>> tlLazyFieldsMap = hdsSupportBean.getLazyFields();

		lazyFields.forEach((clazz, field) -> {
			Collection<String> tlLazyFields = tlLazyFieldsMap.computeIfAbsent(clazz, fields -> new ArrayList<>());
			tlLazyFields.add(field);
		});
	}

	@Override
	public void setLazyFields(final Class<?> clazz, final Collection<String> lazyFields) {
		hdsSupportBean.addLazyFields(clazz, lazyFields);
	}

	@Override
	public void setFetchMode(final FetchMode fetchMode) {
		hdsSupportBean.setFetchMode(fetchMode);
	}

	@Override
	public FetchPlan configureFetchPlan(final EntityManager entityManager) {
		FetchPlan fetchPlan = getFetchPlanIfRequired(entityManager);

		if (fetchPlan != null) {
			configureLoadTuners(fetchPlan);
			configureFetchMode(fetchPlan);
			configureLazyFields(fetchPlan);
		}

		return fetchPlan;
	}

	@Override
	@SuppressWarnings("PMD.AvoidDeeplyNestedIfStmts")
	public void clearFetchPlan(final FetchPlan fetchPlan) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Clearing fetch plan fields");
		}

		if (fetchPlan != null) {
			FetchModeSettings settings = hdsSupportBean.getFetchModeSettings();

			JDBCFetchPlan jdbcFetchPlan = (JDBCFetchPlan) fetchPlan;
			jdbcFetchPlan.clearFields();
			jdbcFetchPlan.resetFetchGroups();

			if (settings != null) {
				if (settings.getEagerFetchMode() != null) {
					jdbcFetchPlan.setEagerFetchMode(settings.getEagerFetchMode());
				}
				if (settings.getSubclassFetchMode() != null) {
					jdbcFetchPlan.setSubclassFetchMode(settings.getSubclassFetchMode());
				}

				hdsSupportBean.cleanFetchModeSettings();
			}
		}
	}

	private FetchPlan getFetchPlanIfRequired(final EntityManager entityManager) {
		if (hdsSupportBean.hasLoadTuners()
			|| hdsSupportBean.hasLazyFields()
			|| hdsSupportBean.hasFetchMode()) {

			return OpenJPAPersistence.cast(entityManager).getFetchPlan();
		}

		return null;
	}
	private void configureFetchMode(final FetchPlan fetchPlan) {

		FetchMode fetchMode = hdsSupportBean.getFetchModeAndRemove();
		if (fetchMode == null) {
			return;
		}

		JDBCFetchPlan jdbcFetchPlan = (JDBCFetchPlan) fetchPlan;

		saveFetchModeSettings(jdbcFetchPlan);

		switch (fetchMode) {
			case NONE:
				jdbcFetchPlan.setEagerFetchMode(FetchMode.NONE);
				jdbcFetchPlan.setSubclassFetchMode(FetchMode.NONE);
				break;
			case JOIN:
				jdbcFetchPlan.setEagerFetchMode(FetchMode.JOIN);
				jdbcFetchPlan.setSubclassFetchMode(FetchMode.JOIN);
				break;
			case PARALLEL:
				jdbcFetchPlan.setEagerFetchMode(FetchMode.PARALLEL);
				jdbcFetchPlan.setSubclassFetchMode(FetchMode.PARALLEL);
				break;
			default:
				break;
		}
	}

	private void configureLoadTuners(final FetchPlan fetchPlan) {
		for (LoadTuner tuner : hdsSupportBean.getLoadTunersAndRemove()) {
			tuner.configure(fetchPlan);
		}
	}

	private  void configureLazyFields(final FetchPlan fetchPlan) {
		hdsSupportBean.getLazyFieldsAndRemove()
			.forEach(fetchPlan::addFields);
	}

	private void saveFetchModeSettings(final JDBCFetchPlan jdbcFetchPlan) {
		FetchModeSettings settings = new FetchModeSettings();
		settings.setEagerFetchMode(jdbcFetchPlan.getEagerFetchMode());
		settings.setSubclassFetchMode(jdbcFetchPlan.getSubclassFetchMode());

		hdsSupportBean.setFetchModeSettings(settings);
	}

	public void setHdsSupportBean(final HDSSupportBean hdsSupportBean) {
		this.hdsSupportBean = hdsSupportBean;
	}
}
