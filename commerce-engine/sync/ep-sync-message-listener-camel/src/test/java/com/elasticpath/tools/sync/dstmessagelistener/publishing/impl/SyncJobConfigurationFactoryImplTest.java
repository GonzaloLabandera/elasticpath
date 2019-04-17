/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.tools.sync.dstmessagelistener.publishing.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import com.elasticpath.tools.sync.client.SyncJobConfiguration;

/**
 * Test class for {@link SyncJobConfigurationFactoryImpl}.
 */
public class SyncJobConfigurationFactoryImplTest {

	private static final String CHANGE_SET_GUID = UUID.randomUUID().toString();
	private static final String ROOT_PATH = "/";
	private static final String SUB_DIR = "test";

	private SyncJobConfigurationFactoryImpl factory;

	@Before
	public void setUp() {
		factory = new SyncJobConfigurationFactoryImpl();
	}

	@Test
	public void verifyFactoryCreatesSyncJobWithChangeSetGuid() {
		final SyncJobConfiguration syncJobConfiguration = factory.createSyncJobConfiguration(CHANGE_SET_GUID);

		assertThat(syncJobConfiguration.getAdapterParameter())
				.isEqualTo(CHANGE_SET_GUID);
	}

	@Test
	public void verifyFactoryCreatesSyncJobWithPreconfiguredValues() {
		factory.setRootPath(ROOT_PATH);
		factory.setSubDir(SUB_DIR);

		final SyncJobConfiguration syncJobConfiguration = factory.createSyncJobConfiguration(CHANGE_SET_GUID);

		assertThat(syncJobConfiguration.getRootPath())
				.isEqualTo(ROOT_PATH);

		assertThat(syncJobConfiguration.getSubDir())
				.isEqualTo(SUB_DIR);
	}

}