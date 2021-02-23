/**
 * Copyright (c) Elastic Path Software Inc., 2013
 */
package com.elasticpath.search.index.solr.builders.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Date;

import org.apache.solr.common.SolrInputDocument;
import org.assertj.core.api.SoftAssertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.CustomerType;
import com.elasticpath.domain.customer.impl.CustomerAddressImpl;
import com.elasticpath.search.index.pipeline.stats.impl.PipelinePerformanceImpl;
import com.elasticpath.search.index.solr.document.impl.CustomerSolrInputDocumentCreator;
import com.elasticpath.service.customer.CustomerService;
import com.elasticpath.service.search.solr.AnalyzerImpl;
import com.elasticpath.service.search.solr.SolrIndexConstants;

/**
 * Test <code>CustomerIndexBuilder</code>.
 */
@RunWith(MockitoJUnitRunner.class)
public class CustomerIndexBuilderTest {

	private CustomerSolrInputDocumentCreator customerDocumentBuilder;

	private CustomerIndexBuilder customerIndexBuilder;

	@Mock
	private CustomerService mockCustomerService;

	private AnalyzerImpl analyzer;

	/**
	 * Setup test.
	 *
	 * @throws Exception on error
	 */
	@Before
	public void setUp() {

		this.customerIndexBuilder = new CustomerIndexBuilder();
		this.analyzer = new AnalyzerImpl();

		this.customerIndexBuilder.setCustomerService(mockCustomerService);

		customerDocumentBuilder = new CustomerSolrInputDocumentCreator();
		customerDocumentBuilder.setAnalyzer(analyzer);
		customerDocumentBuilder.setPipelinePerformance(new PipelinePerformanceImpl());
	}

	/**
	 * Test method for 'com.elasticpath.service.index.impl.CustomerIndexBuildServiceImpl.getName()'.
	 */
	@Test
	public void testGetName() {
		assertThat(this.customerIndexBuilder.getName()).isNotNull();
	}

	/**
	 * Test that when the customer with the given UID has no sharedId, createDocument returns null.
	 */
	@Test
	public void testCreateDocumentWhenCustomerHasNoSharedIdReturnsNull() {
		final Customer mockCustomer = mock(Customer.class);
		when(mockCustomer.getSharedId()).thenReturn(null);
		customerDocumentBuilder.setEntity(mockCustomer);
		assertThat((Object) customerDocumentBuilder.createDocument()).isNull();
	}

	/**
	 * Test that createDocument asks the persistence layer for the customer with the given UID, and upon retrieving it will create a document based
	 * on that customer. Also checks that if the customer profile values are null the document is still created.
	 */
	@Test
	public void testCreateDocumentWithNulls() {
		final long uidPk = 2343L;
		final String storeCode = "dsfsdf";
		final Date date = new Date();
		final String sharedId = "some id";
		final CustomerType customerType = CustomerType.REGISTERED_USER;

		final Customer mockCustomer = mock(Customer.class);
		when(mockCustomer.getUidPk()).thenReturn(uidPk);

		when(mockCustomer.getStoreCode()).thenReturn(storeCode);
		when(mockCustomer.getAddresses()).thenReturn(new ArrayList<>());
		when(mockCustomer.getCreationDate()).thenReturn(date);
		when(mockCustomer.getSharedId()).thenReturn(sharedId);
		when(mockCustomer.getCustomerType()).thenReturn(customerType);

		when(mockCustomer.getUsername()).thenReturn(null);
		when(mockCustomer.getFirstName()).thenReturn(null);
		when(mockCustomer.getLastName()).thenReturn(null);
		when(mockCustomer.getEmail()).thenReturn(null);
		when(mockCustomer.getPhoneNumber()).thenReturn(null);
		when(mockCustomer.getPreferredBillingAddress()).thenReturn(null);

		customerDocumentBuilder.setEntity(mockCustomer);

		final SolrInputDocument document = customerDocumentBuilder.createDocument();
		assertThat((Object) document).isNotNull();

		SoftAssertions softly = new SoftAssertions();

		softly.assertThat(document.getFieldValue(SolrIndexConstants.OBJECT_UID)).isEqualTo(Long.toString(mockCustomer.getUidPk()));
		softly.assertThat(document.getFieldValue(SolrIndexConstants.SHARED_ID)).isEqualTo(sharedId);
		softly.assertThat(document.getFieldValue(SolrIndexConstants.USER_NAME)).isNull();
		softly.assertThat(document.getFieldValue(SolrIndexConstants.FIRST_NAME)).isNull();
		softly.assertThat(document.getFieldValue(SolrIndexConstants.LAST_NAME)).isNull();
		softly.assertThat(document.getFieldValue(SolrIndexConstants.EMAIL)).isNull();
		softly.assertThat(document.getFieldValue(SolrIndexConstants.PHONE_NUMBER)).isNull();
		softly.assertThat(document.getFieldValue(SolrIndexConstants.CREATE_TIME)).isEqualTo(analyzer.analyze(date));
		softly.assertThat(document.getFieldValue(SolrIndexConstants.STORE_CODE)).isEqualTo(analyzer.analyze(storeCode));
		softly.assertThat(document.getFieldValue(SolrIndexConstants.ZIP_POSTAL_CODE)).isNull();

		softly.assertAll();
	}

	/**
	 * Test method for 'com.elasticpath.service.index.impl.CustomerIndexBuildServiceImpl.createDocument(long, Locale)'.
	 */
	@Test
	public void testCreateDocument() {
		final long uidPk = 234234;
		final String firstName = "Jimijimi";
		final String lastName = "LaibaLaiba";
		final String email = "jimi.laiba@elasticpath.com";
		final String sharedId = "jimi.laiba@elasticpath.com";
		final String username = "someusername";
		final String phoneNumber = "012-345-6789";
		final Date createDate = new Date();
		final String storeCode = "sdfsdf";
		final CustomerType customerType = CustomerType.REGISTERED_USER;

		final Customer mockCustomer = mock(Customer.class);
		when(mockCustomer.getUidPk()).thenReturn(uidPk);
		when(mockCustomer.getFirstName()).thenReturn(firstName);
		when(mockCustomer.getLastName()).thenReturn(lastName);
		when(mockCustomer.getEmail()).thenReturn(email);
		when(mockCustomer.getSharedId()).thenReturn(sharedId);
		when(mockCustomer.getUsername()).thenReturn(username);
		when(mockCustomer.getPhoneNumber()).thenReturn(phoneNumber);
		when(mockCustomer.getCreationDate()).thenReturn(createDate);
		when(mockCustomer.getAddresses()).thenReturn(new ArrayList<>());
		when(mockCustomer.getPreferredBillingAddress()).thenReturn(new CustomerAddressImpl());

		when(mockCustomer.getStoreCode()).thenReturn(storeCode);
		when(mockCustomer.getCustomerType()).thenReturn(customerType);
		customerDocumentBuilder.setEntity(mockCustomer);

		final SolrInputDocument document = customerDocumentBuilder.createDocument();
		assertThat((Object) document).isNotNull();

		SoftAssertions softly = new SoftAssertions();

		softly.assertThat(document.getFieldValue(SolrIndexConstants.OBJECT_UID)).isEqualTo(Long.toString(uidPk));
		softly.assertThat(document.getFieldValue(SolrIndexConstants.SHARED_ID)).isEqualTo(analyzer.analyze(sharedId));
		softly.assertThat(document.getFieldValue(SolrIndexConstants.USER_NAME)).isEqualTo(analyzer.analyze(username));
		softly.assertThat(document.getFieldValue(SolrIndexConstants.FIRST_NAME)).isEqualTo(analyzer.analyze(firstName));
		softly.assertThat(document.getFieldValue(SolrIndexConstants.LAST_NAME)).isEqualTo(analyzer.analyze(lastName));
		softly.assertThat(document.getFieldValue(SolrIndexConstants.EMAIL)).isEqualTo(analyzer.analyze(email));
		softly.assertThat(document.getFieldValue(SolrIndexConstants.PHONE_NUMBER)).isEqualTo(analyzer.analyze(phoneNumber));
		softly.assertThat(document.getFieldValue(SolrIndexConstants.CREATE_TIME)).isEqualTo(analyzer.analyze(createDate));

		softly.assertAll();
	}
}
