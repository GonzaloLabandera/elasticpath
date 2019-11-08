/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.persistence.openjpa.util;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elasticpath.persistence.api.EpPersistenceException;
import com.elasticpath.persistence.openjpa.routing.HDSSupportBean;

/**
 * This class is responsible for toggling support for horizontal db scaling (HDS).
 * It compares the connection URLs of read-only (RO) and read-write (RW) data sources and if they are the same,
 * then HDS feature is DISABLED.
 *
 * If ENABLED, then JPA data cache must be disabled since it will no longer make any sense given that data cache
 * may be populated from different databases (master and multiple replicas).
 *
 * It is expected to see a performance drop of ~7% with data cache disabled on the expense of increased db
 * scalability.
 *
 * It is mandatory for all web applications to have both data sources specified in the context.xml because handling
 * a missing RO DS is an interesting software challenge that could be solved only via heavy usage of reflection
 * (or comparing DS toString() values which is a silly solution) but it was simply not worth doing it.
 *
 * The main issue with missing data source is that Tomcat creates a default one (BasicDataSource), which doesn't
 * contain any connection-related property set and any attempt to obtain a connection results in throwing NPE
 * on missing URL string.
 *
 */
public class HDSSupportSwitch {

	private static final Logger LOG = LoggerFactory.getLogger(HDSSupportSwitch.class);
	private static final String OPENJPA_DATACACHE_MANAGER_PROPERTY = "openjpa.DataCacheManager";
	private static final String DATACACHE_MANAGER_DISABLED_STATE = "default";
	private static final String OPENJPA_DATACACHE_PROPERTY = "openjpa.DataCache";
	private static final String DATACACHE_DISABLED_STATE = "false";

	private HDSSupportBean hdsSupportBean;
	private DataSource readOnlyDataSource;

	/**
	 * Toggles the HDS feature based on master and replica db connection URLs.
	 * If the URLs are identical, the HDS feature will be disabled.
	 * If HDS is enabled, the <strong>JPA data cache</strong> will be <strong>disabled</strong> because the cache
	 * can be populated from different entity managers and thus different data and could lead to invalid reads.
	 *
	 * @param masterDataSource the data source.
	 * @param jpaPropertyMap the map with jpa properties used for runtime configuration of entity managers
	 * @return true, if HDS feature is enabled
	 */
	public boolean toggle(final DataSource masterDataSource, final Map<String, Object> jpaPropertyMap) {

		if (!hdsSupportBean.needToCheckDbConnectionUrls()) {
			return hdsSupportBean.isHdsSupportEnabled();
		}

		hdsSupportBean.setNeedToCheckDbConnectionUrls(false);

		if (!readOnlyDataSourceExists(masterDataSource, readOnlyDataSource)) {

			return false;
		}

		try (Connection masterDbConnection = masterDataSource.getConnection()) {

			boolean hdsEnabled = false;

			LOG.debug(" Comparing RW and RO db connection URLs");

			try (Connection replicaConnection = getConnection(readOnlyDataSource)) {
				String replicaDbUrl = getConnectionURL(replicaConnection);

				if (!StringUtils.equalsIgnoreCase(replicaDbUrl, getConnectionURL(masterDbConnection))) {
					//RO DS exists and points to RO cluster endpoint => HDS feature is ON -> must disable JPA data cache
					jpaPropertyMap.put(OPENJPA_DATACACHE_MANAGER_PROPERTY, DATACACHE_MANAGER_DISABLED_STATE);
					jpaPropertyMap.put(OPENJPA_DATACACHE_PROPERTY, DATACACHE_DISABLED_STATE);

					hdsEnabled = true;
					hdsSupportBean.setHdsSupportEnabled(true);
				}
			}

			LOG.info(":: Horizontal db scaling is {}", (hdsEnabled ? "enabled" : "disabled"));

			return hdsEnabled;

		} catch (Exception e) {
			throw new EpPersistenceException("Error occurred while handling read-only data source", e);
		}
	}

	public void setHdsSupportBean(final HDSSupportBean hdsSupportBean) {
		this.hdsSupportBean = hdsSupportBean;
	}

	private Connection getConnection(final DataSource dataSource) throws SQLException {
		return dataSource.getConnection();
	}

	private String getConnectionURL(final Connection connection) throws SQLException {
		String connectionURL = connection.getMetaData().getURL();

		LOG.debug(" Connection URL [{}]", connectionURL);

		return connectionURL;
	}

	private boolean readOnlyDataSourceExists(final DataSource masterDataSource, final DataSource readOnlyDataSource) {

		if (masterDataSource == null) {
			//may be null when running integration tests
			LOG.info(":: Horizontal db scaling is disabled");
			return false;
		}

		try {
			/* if DS is omitted in context.xml, Tomcat will create an instance of BasicDataSource which is a simple template and all methods
			 will throw an exception. getLoginTimeout() will throw UnsupportedOperationException if called from BasicDataSource */
			readOnlyDataSource.getLoginTimeout();

			return true;
		} catch (SQLException sqlExc) {
			throw new EpPersistenceException("Error occurred executing DataSource.getLoginTimeout method", sqlExc);
		} catch (UnsupportedOperationException uoExc) {
			LOG.info("Read-Only data source is missing in the context.xml. HDS feature is disabled");
		}

		return false;
	}

	public void setReadOnlyDataSource(final DataSource readOnlyDataSource) {
		this.readOnlyDataSource = readOnlyDataSource;
	}
}
