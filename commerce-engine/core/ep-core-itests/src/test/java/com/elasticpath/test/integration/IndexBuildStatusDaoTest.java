/**
 * Copyright (c) Elastic Path Software Inc., 2013
 */
package com.elasticpath.test.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.elasticpath.domain.search.IndexBuildStatus;
import com.elasticpath.domain.search.IndexStatus;
import com.elasticpath.persistence.dao.IndexBuildStatusDao;
import com.elasticpath.service.search.IndexType;

/**
 * Tests DAO operations on <code>IndexBuildStatusDao</code>.
 */
public class IndexBuildStatusDaoTest extends BasicSpringContextTest {

	private static final int NUM_OF_DEFAULT_INDEX_TYPES = 5;

	/** The main object under test. */
	@Autowired
	private IndexBuildStatusDao indexBuildStatusDao;
	
	/**
	 * Tests get() method.
	 */
	@DirtiesDatabase
	@Test
	public void testGet() {
		IndexBuildStatus indexBuildStatus = indexBuildStatusDao.get(IndexType.CATEGORY);

		assertThat(indexBuildStatus.getIndexType()).isEqualTo(IndexType.CATEGORY);
		assertThat(indexBuildStatus.getLastBuildDate()).isNull();
		assertThat(indexBuildStatus.getIndexStatus()).isEqualTo(IndexStatus.MISSING);
	}

	/**
	 * Tests list() method.
	 */
	@DirtiesDatabase
	@Test
	public void testList() {
		List<IndexBuildStatus> indexBuildStatusList = indexBuildStatusDao.list();
		assertThat(indexBuildStatusList).hasSize(NUM_OF_DEFAULT_INDEX_TYPES);

		assertThat(findIndexType(indexBuildStatusList, IndexType.CATEGORY)).isTrue();
		assertThat(findIndexType(indexBuildStatusList, IndexType.PRODUCT)).isTrue();
		assertThat(findIndexType(indexBuildStatusList, IndexType.PROMOTION)).isTrue();
		assertThat(findIndexType(indexBuildStatusList, IndexType.CMUSER)).isTrue();
		assertThat(findIndexType(indexBuildStatusList, IndexType.SKU)).isTrue();
	}
	
	/**
	 * Tests saveOrUpdate method.
	 */
	@DirtiesDatabase
	@Test
	public void testSave() {
		final Date buildDate = new Date();

		// save
		final IndexBuildStatus indexBuildStatusBeforeSave = indexBuildStatusDao.get(IndexType.PRODUCT);
		indexBuildStatusBeforeSave.setLastBuildDate(buildDate);
		indexBuildStatusBeforeSave.setIndexStatus(IndexStatus.COMPLETE);
		indexBuildStatusDao.saveOrUpdate(indexBuildStatusBeforeSave);

		// check
		final IndexBuildStatus indexBuildStatusAfterSave = indexBuildStatusDao.get(IndexType.PRODUCT);
		assertThat(indexBuildStatusAfterSave.getIndexType()).isEqualTo(IndexType.PRODUCT);

		assertThat(indexBuildStatusAfterSave.getLastBuildDate()).isInSameSecondAs(buildDate);
		assertThat(indexBuildStatusAfterSave.getIndexStatus()).isEqualTo(IndexStatus.COMPLETE);
	}
	
	/*
	 * Utility: tests if list contains IndexBuildStatus with type.  
	 */
	private static boolean findIndexType(final List<IndexBuildStatus> list, final IndexType type) {
		return list.stream()
			.anyMatch(indexBuildStatus -> indexBuildStatus.getIndexType().equals(type));
	}

}
