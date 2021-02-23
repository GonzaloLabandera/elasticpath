/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.catalog.catalog.batch;


import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.elasticpath.catalog.plugin.entity.ProjectionEntity;
import com.elasticpath.catalog.plugin.entity.ProjectionId;
import com.elasticpath.catalog.plugin.repository.CatalogProjectionRepository;
import com.elasticpath.test.db.DbTestCase;
import com.elasticpath.test.integration.DirtiesDatabase;
import com.elasticpath.test.util.Utils;

@DirtiesDatabase
public class ExpiredProjectionJobImplTest extends DbTestCase {
	private static final String HASH_CODE = "hash";
	private static final String OPTION = "option";
	private static final int PAST_TIME = 60 * 1000;
	private static final int FUTURE_TIME = 140 * 1000;
	private static final int DELAY_FIRST_ITERATION = 125;
	private static final int DELAY_SECOND_ITERATION = 185;
	private static final int TEST_JOB_EXECUTION_TIMEOUT = 5;

	@Autowired
	private CatalogProjectionRepository projectionRepository;

	@Autowired
	@Qualifier("catalogJobLauncherTaskExecutor")
	private ThreadPoolTaskExecutor threadPoolTaskExecutor;

	@Before
	public void setUp() {
		threadPoolTaskExecutor.setAwaitTerminationSeconds(TEST_JOB_EXECUTION_TIMEOUT);
		threadPoolTaskExecutor.setWaitForTasksToCompleteOnShutdown(true);
		threadPoolTaskExecutor.afterPropertiesSet();
	}

	@Test
	public void testThatExpiredProjectionConvertsToTombstone() throws InterruptedException {
		persistProjectionEntity(new Date(System.currentTimeMillis() - PAST_TIME));

		final ProjectionId id = new ProjectionId();
		id.setStore("store");
		id.setType(OPTION);
		id.setCode("code");

		TimeUnit.SECONDS.sleep(DELAY_FIRST_ITERATION);
		assertThat(projectionRepository.findOne(id).isDeleted()).isTrue();
		threadPoolTaskExecutor.shutdown();
	}

	@Test
	public void testThatExpiredProjectionConvertsToTombstoneAtExpireTime() throws InterruptedException {
		persistProjectionEntity(new Date(System.currentTimeMillis() + FUTURE_TIME));

		final ProjectionId id = new ProjectionId();
		id.setStore("store");
		id.setType(OPTION);
		id.setCode("code");
		TimeUnit.SECONDS.sleep(DELAY_SECOND_ITERATION);

		assertThat(projectionRepository.findOne(id).isDeleted()).isTrue();
		threadPoolTaskExecutor.shutdown();
	}

	@Test
	public void testThatExpiredProjectionNotConvertsToTombstoneBeforeExpireTime() throws InterruptedException {
		persistProjectionEntity(new Date(System.currentTimeMillis() + FUTURE_TIME));

		final ProjectionId id = new ProjectionId();
		id.setStore("store");
		id.setType(OPTION);
		id.setCode("code");
		TimeUnit.SECONDS.sleep(DELAY_FIRST_ITERATION);
		assertThat(projectionRepository.findOne(id).isDeleted()).isFalse();
		threadPoolTaskExecutor.shutdown();
	}

	private ProjectionEntity persistProjectionEntity(final Date expiredDate) {
		final ProjectionId projectionId = new ProjectionId();
		projectionId.setType(OPTION);
		projectionId.setStore("store");
		projectionId.setCode("code");

		final ProjectionEntity projectionEntity = new ProjectionEntity();
		projectionEntity.setProjectionId(projectionId);
		projectionEntity.setProjectionDateTime(new Date());
		projectionEntity.setDisableDateTime(expiredDate);
		projectionEntity.setContentHash(Utils.uniqueCode(HASH_CODE));

		projectionRepository.save(projectionEntity);

		return projectionEntity;
	}
}
