/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.persistence.openjpa.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.persistence.openjpa.routing.HDSSupportBean;

/**
 * Unit test for the {@code QueryUtil} class.
 */
@RunWith(MockitoJUnitRunner.class)
public class HDSSupportSwitchTest {

	private static final String MASTER_JDBC_URL = "MASTER_JDBC_URL";
	private static final String REPLICA_JDBC_URL = "REPLICA_JDBC_URL";

	@InjectMocks private HDSSupportSwitch hdsSupportSwitch;

	@Mock private HDSSupportBean hdsSupportBean;
	@Mock private DataSource readWriteDataSource;
	@Mock (answer = Answers.RETURNS_DEEP_STUBS) private Connection readWriteConnection;
	@Mock private DataSource readOnlyDataSource;
	@Mock (answer = Answers.RETURNS_DEEP_STUBS) private Connection readOnlyConnection;

	@Before
	public void init() throws Exception {
		when(readWriteDataSource.getConnection()).thenReturn(readWriteConnection);
		when(readOnlyDataSource.getConnection()).thenReturn(readOnlyConnection);
	}

	/**
	 * Test enabling the HDS feature when RO DS exists and RO JDBC URL is different than the master DB's.
	 */
	@Test
	public final void shouldEnableHDSAndDisableJPAL2CacheWhenRODSExistsAndROJDBCUrlIsDifferentThanMasterDbUrl() throws Exception {

		Map<String, Object> jpaPropertyMap = new HashMap<>();

		when(hdsSupportBean.needToCheckDbConnectionUrls()).thenReturn(true);
		when(readOnlyDataSource.getLoginTimeout()).thenReturn(1);
		when(readWriteConnection.getMetaData().getURL()).thenReturn(MASTER_JDBC_URL);
		when(readOnlyConnection.getMetaData().getURL()).thenReturn(REPLICA_JDBC_URL);

		boolean isHDSEnabled = hdsSupportSwitch.toggle(readWriteDataSource, jpaPropertyMap);

		assertThat(isHDSEnabled)
			.isTrue();
		assertThat(jpaPropertyMap)
			.hasEntrySatisfying("openjpa.DataCacheManager", "default"::equals);
		assertThat(jpaPropertyMap)
			.hasEntrySatisfying("openjpa.DataCache", "false"::equals);

		verify(hdsSupportBean).needToCheckDbConnectionUrls();
		verify(hdsSupportBean).setNeedToCheckDbConnectionUrls(false);
		verify(hdsSupportBean).setHdsSupportEnabled(true);
		verify(readOnlyDataSource).getLoginTimeout();
		verify(readWriteDataSource).getConnection();
		verify(readOnlyDataSource).getConnection();
	}

	/**
	 * Test disabling the HDS feature when RO DS exists and RO JDBC URL is the same as the master DB's.
	 */
	@Test
	public final void shouldDisableHDSWhenRODSExistsAndROJDBCUrlIsSameAsMasterDbUrl() throws Exception {

		Map<String, Object> jpaPropertyMap = new HashMap<>();

		when(hdsSupportBean.needToCheckDbConnectionUrls()).thenReturn(true);
		when(readOnlyDataSource.getLoginTimeout()).thenReturn(1);
		when(readWriteConnection.getMetaData().getURL()).thenReturn(MASTER_JDBC_URL);
		when(readOnlyConnection.getMetaData().getURL()).thenReturn(MASTER_JDBC_URL);

		boolean isHDSEnabled = hdsSupportSwitch.toggle(readWriteDataSource, jpaPropertyMap);

		assertThat(isHDSEnabled)
			.isFalse();
		assertThat(jpaPropertyMap)
			.isEmpty();

		verify(hdsSupportBean).needToCheckDbConnectionUrls();
		verify(hdsSupportBean).setNeedToCheckDbConnectionUrls(false);
		verify(readOnlyDataSource).getLoginTimeout();
		verify(readWriteDataSource).getConnection();
		verify(readOnlyDataSource).getConnection();
	}

	/**
	 * Test disabling the HDS feature when RO DS exists and RO JDBC URL is the same as the master DB's.
	 */
	@Test
	public final void shouldNotDoSecondCheckIfAlreadyDone() {
		when(hdsSupportBean.needToCheckDbConnectionUrls()).thenReturn(false);

		hdsSupportSwitch.toggle(readWriteDataSource,  new HashMap<>());

		verify(hdsSupportBean).needToCheckDbConnectionUrls();
		verify(hdsSupportBean).isHdsSupportEnabled();
		verifyZeroInteractions(readOnlyDataSource, readWriteDataSource);
	}
}
