/**
 * Copyright (c) Elastic Path Software Inc., 2013
 */
package com.elasticpath.search.index.solr.document.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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
import com.elasticpath.search.index.pipeline.IndexingStage;
import com.elasticpath.search.index.pipeline.stats.PipelinePerformance;
import com.elasticpath.service.search.solr.AnalyzerImpl;
import com.elasticpath.service.search.solr.SolrIndexConstants;

/**
 * Test construction of {@link SolrInputDocument}s from {@link CmUser}s using {@link CmUserSolrInputDocumentCreator}.
 */
@RunWith(MockitoJUnitRunner.class)
public class CmUserSolrInputDocumentCreatorTest {

	private CmUserSolrInputDocumentCreator documentCreator;

	@Mock
	private PipelinePerformance pipelinePerformance;

	@Mock
	private IndexingStage<SolrInputDocument, ?> nextStage;

	/**
	 * Set up the test case.
	 */
	@Before
	public void setUp() {
		documentCreator = new CmUserSolrInputDocumentCreator();

		documentCreator.setAnalyzer(new AnalyzerImpl());
		documentCreator.setPipelinePerformance(pipelinePerformance);
		documentCreator.setNextStage(nextStage);
	}

	/**
	 * Tests {@link CmUserSolrInputDocumentCreator#run()} method.
	 */
	@Test
	public void testRun() {
		final CmUser cmUser = createCmUser();
		documentCreator.setEntity(cmUser);
		documentCreator.run();

		verify(pipelinePerformance, times(2)).addCount(any(String.class), any(Long.class));
		verify(nextStage).send(any(SolrInputDocument.class));
	}

	/**
	 * Tests {@link CmUserSolrInputDocumentCreator#run()} method with null user.
	 */
	@Test
	public void testRunNullCmUser() {
		documentCreator.setEntity(null);
		documentCreator.run();
	}

	/**
	 * Tests {@link CmUserSolrInputDocumentCreator#run()} method with null nextStage.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testRunNullNextStage() {
		documentCreator.setEntity(createCmUser());
		documentCreator.setNextStage(null);
		documentCreator.run();
	}

	/**
	 * Tests {@link CmUserSolrInputDocumentCreator#createDocument()} method.
	 */
	@Test
	public void testCreateDocument() {
		final CmUser cmUser = createCmUser();
		documentCreator.setEntity(cmUser);

		final SolrInputDocument createdDocument = documentCreator.createDocument();

		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(createdDocument.getFieldValue(SolrIndexConstants.OBJECT_UID)).isEqualTo(String.valueOf(cmUser.getUidPk()));
		softly.assertThat(createdDocument.getFieldValue(SolrIndexConstants.USER_NAME)).isEqualTo(cmUser.getUserName());
		softly.assertThat(createdDocument.getFieldValue(SolrIndexConstants.LAST_NAME)).isEqualTo(cmUser.getLastName());
		softly.assertThat(createdDocument.getFieldValue(SolrIndexConstants.FIRST_NAME)).isEqualTo(cmUser.getFirstName());
		softly.assertThat(createdDocument.getFieldValue(SolrIndexConstants.EMAIL)).isEqualTo(cmUser.getEmail());
		softly.assertThat(createdDocument.getFieldValue(SolrIndexConstants.STATUS)).isEqualTo(UserStatus.ENABLED.getPropertyKey());
		softly.assertThat(createdDocument.getFieldValue(SolrIndexConstants.ALL_CATALOGS_ACCESS))
			.isEqualTo(String.valueOf(cmUser.isAllCatalogsAccess()));
		softly.assertThat(createdDocument.getFieldValue(SolrIndexConstants.ALL_STORES_ACCESS)).isEqualTo(String.valueOf(cmUser.isAllStoresAccess()));

		softly.assertThat(createdDocument.getFieldValues(SolrIndexConstants.USER_ROLE)).containsExactlyInAnyOrder("role1", "role2");
		softly.assertThat(createdDocument.getFieldValues(SolrIndexConstants.CATALOG_CODE)).containsExactlyInAnyOrder("catalog1", "catalog2");
		softly.assertThat(createdDocument.getFieldValues(SolrIndexConstants.STORE_CODE)).containsExactlyInAnyOrder("store1", "store2");

		softly.assertAll();
	}

	/**
	 * Tests {@link CmUserSolrInputDocumentCreator#createDocument()} method with a null {@link CmUser}.
	 */
	@Test
	public void testCreateDocumentWithNullCmUser() {
		final SolrInputDocument createdDocument = documentCreator.createDocument();
		assertThat((Object) createdDocument).isNull();
	}

	/**
	 * Test {@link CmUserSolrInputDocumentCreator#setEntity(Object)}. Ensure cmUser gets set.
	 */
	@Test
	public void testSetEntity() {
		assertThat(documentCreator.getEntity()).isNull();

		final CmUserImpl entity = new CmUserImpl();
		documentCreator.setEntity(entity);

		assertThat(documentCreator.getEntity()).isEqualTo(entity);
	}

	private CmUser createCmUser() {
		final CmUser cmUser = new CmUserImpl() {
			private static final long serialVersionUID = -6507569527118530890L;

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

		final UserRole userRole1 = new UserRoleImpl();
		userRole1.setName("role1");
		final UserRole userRole2 = new UserRoleImpl();
		userRole2.setName("role2");
		cmUser.addUserRole(userRole1);
		cmUser.addUserRole(userRole2);

		final Catalog catalog1 = new CatalogImpl();
		catalog1.setCode("catalog1");
		final Catalog catalog2 = new CatalogImpl();
		catalog2.setCode("catalog2");
		cmUser.addCatalog(catalog1);
		cmUser.addCatalog(catalog2);

		final Store store1 = new StoreImpl();
		store1.setCode("store1");
		final Store store2 = new StoreImpl();
		store2.setCode("store2");
		cmUser.addStore(store1);
		cmUser.addStore(store2);

		return cmUser;
	}
}
