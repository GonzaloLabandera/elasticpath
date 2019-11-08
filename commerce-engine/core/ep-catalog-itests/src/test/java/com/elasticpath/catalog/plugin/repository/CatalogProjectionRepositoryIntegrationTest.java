/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.plugin.repository;

import static com.elasticpath.catalog.plugin.repository.CatalogProjectionRepositoryIntegrationTest.JMS_BROKER_URL;
import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.elasticpath.catalog.plugin.entity.ProjectionEntity;
import com.elasticpath.catalog.plugin.entity.ProjectionId;
import com.elasticpath.test.jta.JmsBrokerConfigurator;
import com.elasticpath.test.jta.XaTransactionTestSupport;

/**
 * Test for {@link CatalogProjectionRepository}.
 */
@JmsBrokerConfigurator(url = JMS_BROKER_URL)
public class CatalogProjectionRepositoryIntegrationTest extends XaTransactionTestSupport {

	public static final String JMS_BROKER_URL = "tcp://localhost:61617";

	@Autowired
	private CatalogProjectionRepository catalogProjectionRepository;

	/**
	 * Test that method "save" ProjectionEntity saved in database.
	 */
	@Test
	public void testThatMethodSaveProjectionEntityToDatabase() {
		final ProjectionEntity entity = getProjectionEntity("code", "guid");
		final ProjectionEntity result = catalogProjectionRepository.save(entity);

		assertThat(result).isEqualTo(entity);
		assertThat(entity.getType()).isEqualTo(entity.getType());
		assertThat(result.getStore()).isEqualTo(entity.getStore());
		assertThat(result.getVersion()).isEqualTo(entity.getVersion());
		assertThat(result.getCode()).isEqualTo(entity.getCode());
	}

	/**
	 * Test that method "extractProjectionEntity" find ProjectionEntity by query.
	 */
	@Test
	public void testThatExtractProjectionEntityFindProjectionEntity() {
		final ProjectionEntity entityOne = getProjectionEntity("store1", "code1");
		catalogProjectionRepository.save(entityOne);

		final Optional<ProjectionEntity> result = catalogProjectionRepository.extractProjectionEntity("option", "code1", "store1");

		assertThat(result).isNotEmpty();
	}

	private ProjectionEntity getProjectionEntity(final String store, final String code) {
		final ProjectionId id = new ProjectionId();
		id.setType("option");
		id.setCode(code);
		id.setStore(store);

		final ProjectionEntity entityOne = new ProjectionEntity();
		entityOne.setProjectionDateTime(Timestamp.valueOf(LocalDateTime.now()));
		entityOne.setContentHash("hash");
		entityOne.setContent("content");
		entityOne.setSchemaVersion("shemaVersion");
		entityOne.setProjectionId(id);
		return entityOne;
	}
}
