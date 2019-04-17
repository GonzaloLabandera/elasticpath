/**
 * Copyright (c) Elastic Path Software Inc., 2013
 */
package com.elasticpath.search.index.solr.builders.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.solr.common.SolrInputDocument;
import org.assertj.core.api.SoftAssertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.catalog.Catalog;
import com.elasticpath.domain.catalog.impl.CatalogImpl;
import com.elasticpath.domain.cmuser.CmUser;
import com.elasticpath.domain.cmuser.UserRole;
import com.elasticpath.domain.cmuser.UserStatus;
import com.elasticpath.domain.cmuser.impl.CmUserImpl;
import com.elasticpath.domain.cmuser.impl.UserRoleImpl;
import com.elasticpath.domain.store.Store;
import com.elasticpath.domain.store.impl.StoreImpl;
import com.elasticpath.search.index.pipeline.stats.impl.PipelinePerformanceImpl;
import com.elasticpath.search.index.solr.document.impl.CmUserSolrInputDocumentCreator;
import com.elasticpath.service.cmuser.CmUserService;
import com.elasticpath.service.search.IndexType;
import com.elasticpath.service.search.solr.AnalyzerImpl;
import com.elasticpath.service.search.solr.IndexUtility;
import com.elasticpath.service.search.solr.IndexUtilityImpl;
import com.elasticpath.service.search.solr.SolrIndexConstants;

/**
 * Tests for CmUserIndexBuilder class.
 */
@RunWith(MockitoJUnitRunner.class)
public class CmUserIndexBuilderTest {

	private CmUserIndexBuilder cmUserIndexBuilder;
	
	private CmUserSolrInputDocumentCreator cmUserDocumentCreator;

	@Mock
	private CmUserService cmUserServiceMock;

	@Before
	public void setUp() {
		cmUserIndexBuilder = new CmUserIndexBuilder();
		IndexUtility indexUtility = new IndexUtilityImpl();
		AnalyzerImpl analyzer = new AnalyzerImpl();

		cmUserIndexBuilder.setCmUserService(cmUserServiceMock);

		cmUserDocumentCreator = new CmUserSolrInputDocumentCreator();
		cmUserDocumentCreator.setAnalyzer(analyzer);
		cmUserDocumentCreator.setIndexUtility(indexUtility);
		cmUserDocumentCreator.setPipelinePerformance(new PipelinePerformanceImpl());
		
	}

	/**
	 * Tests getName() method.
	 */
	@Test
	public void testGetName() {
		assertThat(cmUserIndexBuilder.getName()).isEqualTo(SolrIndexConstants.CMUSER_SOLR_CORE);
	}

	/**
	 * Tests getIndexType() method.
	 */
	@Test
	public void testGetIndexType() {
		assertThat(cmUserIndexBuilder.getIndexType()).isEqualTo(IndexType.CMUSER);
	}

	/**
	 * Tests findUidsByNotification() method.
	 */
	@Test(expected = UnsupportedOperationException.class)
	public void testFindUidsByNotification() {
		cmUserIndexBuilder.findUidsByNotification(null);
	}

	/**
	 * Tests findDeletedUids() method.
	 */
	@Test
	public void testFindDeletedUids() {
		assertThat(cmUserIndexBuilder.findDeletedUids(null)).isEqualTo(Collections.emptyList());
	}

	/**
	 * Tests findAllUids() method.
	 */
	@Test
	public void testFindAllUids() {
		final List<Long> result = new ArrayList<>();
		when(cmUserServiceMock.findAllUids()).thenReturn(result);
		assertThat(cmUserIndexBuilder.findAllUids()).isEqualTo(result);
		verify(cmUserServiceMock).findAllUids();
	}

	/**
	 * Tests findAddedOrModifiedUids() method.
	 */
	@Test
	public void testFindAddedOrModifiedUids() {
		final Date date = new Date();
		final List<Long> result = new ArrayList<>();
		when(cmUserServiceMock.findUidsByModifiedDate(date)).thenReturn(result);
		assertThat(cmUserIndexBuilder.findAddedOrModifiedUids(date)).isEqualTo(result);
		verify(cmUserServiceMock).findUidsByModifiedDate(date);
	}

	/**
	 * Tests createDocument() method.
	 */
	@Test
	public void testCreateDocument() {
		CmUser cmUser = createCmUser();

		cmUserDocumentCreator.setEntity(cmUser);
		final SolrInputDocument createDocument = cmUserDocumentCreator.createDocument();

		SoftAssertions softly = new SoftAssertions();

		softly.assertThat(createDocument.getFieldValue(SolrIndexConstants.OBJECT_UID)).isEqualTo(String.valueOf(cmUser.getUidPk()));
		softly.assertThat(createDocument.getFieldValue(SolrIndexConstants.USER_NAME)).isEqualTo(cmUser.getUserName());
		softly.assertThat(createDocument.getFieldValue(SolrIndexConstants.LAST_NAME)).isEqualTo(cmUser.getLastName());
		softly.assertThat(createDocument.getFieldValue(SolrIndexConstants.FIRST_NAME)).isEqualTo(cmUser.getFirstName());
		softly.assertThat(createDocument.getFieldValue(SolrIndexConstants.EMAIL)).isEqualTo(cmUser.getEmail());
		softly.assertThat(createDocument.getFieldValue(SolrIndexConstants.STATUS)).isEqualTo(UserStatus.ENABLED.getPropertyKey());
		softly.assertThat(createDocument.getFieldValue(SolrIndexConstants.ALL_CATALOGS_ACCESS)).isEqualTo(
				String.valueOf(cmUser.isAllCatalogsAccess()));
		softly.assertThat(createDocument.getFieldValue(SolrIndexConstants.ALL_STORES_ACCESS)).isEqualTo(String.valueOf(cmUser.isAllStoresAccess()));

		softly.assertThat(createDocument.getFieldValues(SolrIndexConstants.USER_ROLE)).containsExactlyInAnyOrder("role1", "role2");
		softly.assertThat(createDocument.getFieldValues(SolrIndexConstants.CATALOG_CODE)).containsExactlyInAnyOrder("catalog1", "catalog2");
		softly.assertThat(createDocument.getFieldValues(SolrIndexConstants.STORE_CODE)).containsExactlyInAnyOrder("store1", "store2");

		softly.assertAll();
	}

	private CmUser createCmUser() {
		CmUser cmUser = new CmUserImpl() {
			private static final long serialVersionUID = -5534887622471126321L;

			@Override
			public boolean isAccountNonLocked() {
				return true;
			}
		};
		cmUser.setGuid("guid");
		cmUser.initialize();
		cmUser.setUidPk(1L);
		cmUser.setUserName("userName");
		cmUser.setLastName("lastName");
		cmUser.setFirstName("firstName");
		cmUser.setEmail("email");
		cmUser.setEnabled(true);
		cmUser.setAllCatalogsAccess(false);
		cmUser.setAllStoresAccess(true);

		UserRole userRole1 = new UserRoleImpl();
		userRole1.setName("role1");
		UserRole userRole2 = new UserRoleImpl();
		userRole2.setName("role2");
		cmUser.addUserRole(userRole1);
		cmUser.addUserRole(userRole2);

		Catalog catalog1 = new CatalogImpl();
		catalog1.setCode("catalog1");
		Catalog catalog2 = new CatalogImpl();
		catalog2.setCode("catalog2");
		cmUser.addCatalog(catalog1);
		cmUser.addCatalog(catalog2);

		Store store1 = new StoreImpl();
		store1.setCode("store1");
		Store store2 = new StoreImpl();
		store2.setCode("store2");
		cmUser.addStore(store1);
		cmUser.addStore(store2);

		return cmUser;
	}

}
