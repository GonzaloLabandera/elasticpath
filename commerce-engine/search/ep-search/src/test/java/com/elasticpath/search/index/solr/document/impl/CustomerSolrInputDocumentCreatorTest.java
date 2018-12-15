/**
 * Copyright (c) Elastic Path Software Inc., 2013
 */
package com.elasticpath.search.index.solr.document.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Date;

import org.apache.solr.common.SolrInputDocument;
import org.assertj.core.api.SoftAssertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.impl.CustomerImpl;
import com.elasticpath.search.index.pipeline.IndexingStage;
import com.elasticpath.search.index.pipeline.stats.impl.PipelinePerformanceImpl;
import com.elasticpath.service.search.index.Analyzer;
import com.elasticpath.service.search.solr.AnalyzerImpl;
import com.elasticpath.service.search.solr.SolrIndexConstants;

/**
 * Test construction of {@link SolrInputDocument}s from {@link Customer}s using {@link CustomerSolrInputDocumentCreator}.
 */
@RunWith(MockitoJUnitRunner.class)
public class CustomerSolrInputDocumentCreatorTest {
	private Analyzer analyzer;

	private CustomerSolrInputDocumentCreator customerSolrInputDocumentCreator;

	@Captor
	private ArgumentCaptor<SolrInputDocument> documentCaptor;

	/**
	 * Set up {@link CustomerSolrInputDocumentCreator}.
	 */
	@Before
	public void setUp() {
		analyzer = new AnalyzerImpl();
		customerSolrInputDocumentCreator = new CustomerSolrInputDocumentCreator();
		customerSolrInputDocumentCreator.setAnalyzer(analyzer);
		customerSolrInputDocumentCreator.setPipelinePerformance(new PipelinePerformanceImpl());
	}

	/**
	 * Test that when the customer has no userId, createDocument returns null.
	 */
	@Test
	public void testCreateDocumentWhenCustomerHasNoUserIdReturnsNull() {
		final Customer customer = new CustomerImpl();
		customerSolrInputDocumentCreator.setEntity(customer);
		assertThat((Object) customerSolrInputDocumentCreator.createDocument()).isNull();
	}

	/**
	 * If you pass a null into the document creator, it should give a null back (and not throw a exception).
	 */
	@Test
	public void testNullYieldsNull() {
		customerSolrInputDocumentCreator.setEntity(null);
		assertThat((Object) customerSolrInputDocumentCreator.createDocument()).isNull();
	}

	/**
	 * Test that createDocument creates a document based on that customer. Also checks that if the customer profile values are null the document is
	 * still created.
	 */
	@Test
	public void testCreateDocumentWithNulls() {
		final long uidPk = 2343L;
		final String storeCode = "dsfsdf";
		final Date date = new Date();
		final String userId = "some id";

		final Customer customer = mock(Customer.class);

		final int expectedCallsToGetUserId = 3;

		@SuppressWarnings("unchecked") final IndexingStage<SolrInputDocument, Long> nextStage = mock(IndexingStage.class);

		customerSolrInputDocumentCreator.setNextStage(nextStage);

		when(customer.getStoreCode()).thenReturn(storeCode);
		when(customer.getUidPk()).thenReturn(uidPk);
		when(customer.getAddresses()).thenReturn(new ArrayList<>());
		when(customer.getCreationDate()).thenReturn(date);
		when(customer.getUserId()).thenReturn(userId);
		customerSolrInputDocumentCreator.setEntity(customer);
		customerSolrInputDocumentCreator.run();

		verify(nextStage).send(documentCaptor.capture());
		final SolrInputDocument document = documentCaptor.getValue();

		SoftAssertions softly = new SoftAssertions();

		softly.assertThat((Object) document).isNotNull();
		softly.assertThat(document.getFieldValue(SolrIndexConstants.OBJECT_UID)).isEqualTo(String.valueOf(customer.getUidPk()));
		softly.assertThat(document.getFieldValue(SolrIndexConstants.USER_ID)).isEqualTo(userId);
		softly.assertThat(document.getFieldValue(SolrIndexConstants.FIRST_NAME)).isNull();
		softly.assertThat(document.getFieldValue(SolrIndexConstants.LAST_NAME)).isNull();
		softly.assertThat(document.getFieldValue(SolrIndexConstants.EMAIL)).isNull();
		softly.assertThat(document.getFieldValue(SolrIndexConstants.PHONE_NUMBER)).isNull();
		softly.assertThat(document.getFieldValue(SolrIndexConstants.CREATE_TIME)).isEqualTo(analyzer.analyze(date));
		softly.assertThat(document.getFieldValue(SolrIndexConstants.STORE_CODE)).isEqualTo(analyzer.analyze(storeCode));
		softly.assertThat(document.getFieldValue(SolrIndexConstants.ZIP_POSTAL_CODE)).isNull();

		softly.assertAll();

		verify(customer, times(2)).getUidPk();
		verify(customer).getAddresses();
		verify(customer).getCreationDate();
		verify(customer, times(expectedCallsToGetUserId)).getUserId();
		verify(customer).getFirstName();
		verify(customer).getLastName();
		verify(customer).getEmail();
		verify(customer).getPhoneNumber();
		verify(customer).getPreferredBillingAddress();

	}

	/**
	 * Test document creation with all fields not null.
	 */
	@Test
	public void testCreateDocument() {
		final long uidPk = 234234;
		final String firstName = "Jimijimi";
		final String lastName = "LaibaLaiba";
		final String email = "jimi.laiba@elasticpath.com";
		final String userId = "jimi.laiba@elasticpath.com";
		final String phoneNumber = "012-345-6789";
		final Date createDate = new Date();
		final String storeCode = "sdfsdf";

		final Customer customer = mock(Customer.class);

		final int expectedCallsToGetUserId = 3;

		@SuppressWarnings("unchecked") final IndexingStage<SolrInputDocument, Long> nextStage = mock(IndexingStage.class);

		customerSolrInputDocumentCreator.setNextStage(nextStage);

		when(customer.getStoreCode()).thenReturn(storeCode);
		when(customer.getUidPk()).thenReturn(uidPk);
		when(customer.getAddresses()).thenReturn(new ArrayList<>());
		when(customer.getCreationDate()).thenReturn(createDate);
		when(customer.getUserId()).thenReturn(userId);
		when(customer.getFirstName()).thenReturn(firstName);
		when(customer.getLastName()).thenReturn(lastName);
		when(customer.getEmail()).thenReturn(email);
		when(customer.getPhoneNumber()).thenReturn(phoneNumber);

		customerSolrInputDocumentCreator.setEntity(customer);
		customerSolrInputDocumentCreator.run();

		verify(nextStage).send(documentCaptor.capture());
		final SolrInputDocument document = documentCaptor.getValue();

		SoftAssertions softly = new SoftAssertions();

		softly.assertThat((Object) document).isNotNull();
		softly.assertThat(document.getFieldValue(SolrIndexConstants.OBJECT_UID)).isEqualTo(String.valueOf(uidPk));
		softly.assertThat(document.getFieldValue(SolrIndexConstants.USER_ID)).isEqualTo(analyzer.analyze(userId));
		softly.assertThat(document.getFieldValue(SolrIndexConstants.FIRST_NAME)).isEqualTo(analyzer.analyze(firstName));
		softly.assertThat(document.getFieldValue(SolrIndexConstants.LAST_NAME)).isEqualTo(analyzer.analyze(lastName));
		softly.assertThat(document.getFieldValue(SolrIndexConstants.EMAIL)).isEqualTo(analyzer.analyze(email));
		softly.assertThat(document.getFieldValue(SolrIndexConstants.PHONE_NUMBER)).isEqualTo(analyzer.analyze(phoneNumber));
		softly.assertThat(document.getFieldValue(SolrIndexConstants.CREATE_TIME)).isEqualTo(analyzer.analyze(createDate));

		softly.assertAll();

		verify(customer).getUidPk();
		verify(customer).getAddresses();
		verify(customer).getCreationDate();
		verify(customer, times(expectedCallsToGetUserId)).getUserId();
		verify(customer).getFirstName();
		verify(customer).getLastName();
		verify(customer).getEmail();
		verify(customer).getPhoneNumber();
		verify(customer).getPreferredBillingAddress();

	}

}
