/**
 * Copyright (c) Elastic Path Software Inc., 2013
 */
package com.elasticpath.service.search.solr.query;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import static com.elasticpath.service.search.solr.query.QueryComposerHelper.escape;
import static com.elasticpath.service.search.solr.query.QueryComposerHelper.escapeAndAddWildcards;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.lucene.search.Query;
import org.junit.Test;

import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.service.search.index.QueryComposer;
import com.elasticpath.service.search.query.CustomerSearchCriteria;
import com.elasticpath.service.search.query.SearchCriteria;
import com.elasticpath.service.search.solr.SolrIndexConstants;

/**
 * Test case for {@link CustomerQueryComposerImpl}.
 */
public class CustomerQueryComposerImplTest extends QueryComposerTestCase {

	private CustomerQueryComposerImpl customerQueryComposerImpl;

	private CustomerSearchCriteria searchCriteria;


	/**
	 * Prepare for tests.
	 * 
	 * @throws Exception in case of any errors.
	 */
	@Override
	public void setUp() throws Exception {
		super.setUp();

		customerQueryComposerImpl = new CustomerQueryComposerImpl();
		customerQueryComposerImpl.setAnalyzer(getAnalyzer());
		customerQueryComposerImpl.setIndexUtility(getIndexUtility());

		searchCriteria = new CustomerSearchCriteria();
	}

	/**
	 * Test method for {@link CustomerSearchCriteria#setFirstName(String)}.
	 */
	@Test
	public void testFirstName() {
		final String firstName = "first name";
		searchCriteria.setFirstName(firstName);

		Query query = customerQueryComposerImpl.composeQuery(searchCriteria, getSearchConfig());
		assertQueryContains(query, SolrIndexConstants.FIRST_NAME_EXACT, escapeAndAddWildcards(firstName));
		query = customerQueryComposerImpl.composeFuzzyQuery(searchCriteria, getSearchConfig());
		assertQueryContains(query, SolrIndexConstants.FIRST_NAME_EXACT, escapeAndAddWildcards(firstName));
	}

	/**
	 * Test method for {@link CustomerSearchCriteria#setLastName(String)}.
	 */
	@Test
	public void testLastName() {
		final String lastName = "last name";
		searchCriteria.setLastName(lastName);

		Query query = customerQueryComposerImpl.composeQuery(searchCriteria, getSearchConfig());
		assertQueryContains(query, SolrIndexConstants.LAST_NAME_EXACT, escapeAndAddWildcards(lastName));
		query = customerQueryComposerImpl.composeFuzzyQuery(searchCriteria, getSearchConfig());
		assertQueryContains(query, SolrIndexConstants.LAST_NAME_EXACT, escapeAndAddWildcards(lastName));
	}

	/**
	 * Test method for {@link CustomerSearchCriteria#setEmail(String)}.
	 */
	@Test
	public void testEmail() {
		final String email = "email";
		searchCriteria.setEmail(email);

		Query query = customerQueryComposerImpl.composeQuery(searchCriteria, getSearchConfig());
		assertQueryContains(query, SolrIndexConstants.EMAIL_EXACT, escapeAndAddWildcards(email));
		query = customerQueryComposerImpl.composeFuzzyQuery(searchCriteria, getSearchConfig());
		assertQueryContains(query, SolrIndexConstants.EMAIL_EXACT, escapeAndAddWildcards(email));
	}

	
	/**
	 * Test method for {@link CustomerSearchCriteria#isUserIdAndEmailMutualSearch()}.
	 */
	@Test
	public void testUserIdAndEmailMutualSearch() {
		final String email = "email";
		final String userId = "userId";
		searchCriteria.setUserIdAndEmailMutualSearch(true);
		searchCriteria.setUserId(userId);
		searchCriteria.setEmail(email);
		
		Query query = customerQueryComposerImpl.composeQuery(searchCriteria, getSearchConfig());
		assertQueryContains(query, SolrIndexConstants.EMAIL_EXACT, escapeAndAddWildcards(email));
		assertQueryContains(query, SolrIndexConstants.USER_ID_EXACT, escapeAndAddWildcards(userId));
		query = customerQueryComposerImpl.composeFuzzyQuery(searchCriteria, getSearchConfig());
		assertQueryContains(query, SolrIndexConstants.EMAIL_EXACT, escapeAndAddWildcards(email));
		assertQueryContains(query, SolrIndexConstants.USER_ID_EXACT, escapeAndAddWildcards(userId));

		searchCriteria.setUserIdAndEmailMutualSearch(false);
		
		query = customerQueryComposerImpl.composeQuery(searchCriteria, getSearchConfig());
		assertQueryContains(query, SolrIndexConstants.EMAIL_EXACT, escapeAndAddWildcards(email));
		assertQueryContains(query, SolrIndexConstants.USER_ID_EXACT, escapeAndAddWildcards(userId));
		query = customerQueryComposerImpl.composeFuzzyQuery(searchCriteria, getSearchConfig());
		assertQueryContains(query, SolrIndexConstants.EMAIL_EXACT, escapeAndAddWildcards(email));
		assertQueryContains(query, SolrIndexConstants.USER_ID_EXACT, escapeAndAddWildcards(userId));

	}

	/**
	 * Test method for {@link CustomerSearchCriteria#setCustomerNumber(String)}.
	 */
	@Test
	public void testCustomerNumber() {
		final String customerNumber = "customer number";
		searchCriteria.setCustomerNumber(customerNumber);

		Query query = customerQueryComposerImpl.composeQuery(searchCriteria, getSearchConfig());
		assertQueryContains(query, SolrIndexConstants.CUSTOMER_NUMBER, escape(customerNumber));
		query = customerQueryComposerImpl.composeFuzzyQuery(searchCriteria, getSearchConfig());
		assertQueryContainsNotFuzzy(query, SolrIndexConstants.CUSTOMER_NUMBER, escape(customerNumber));
	}

	/**
	 * Test method for {@link CustomerSearchCriteria#setPhoneNumber(String)}.
	 */
	@Test
	public void testPhoneNumber() {
		final String phoneNumber = "phone number";
		searchCriteria.setPhoneNumber(phoneNumber);

		Query query = customerQueryComposerImpl.composeQuery(searchCriteria, getSearchConfig());
		assertQueryContains(query, SolrIndexConstants.PHONE_NUMBER_EXACT, escapeAndAddWildcards(phoneNumber));
		query = customerQueryComposerImpl.composeFuzzyQuery(searchCriteria, getSearchConfig());
		assertQueryContainsNotFuzzy(query, SolrIndexConstants.PHONE_NUMBER_EXACT, escapeAndAddWildcards(phoneNumber));
	}

	/**
	 * Test method for {@link CustomerSearchCriteria#setFromDate(Date)}.
	 */
	@Test
	public void testToFromDate() {
		final Date createDate = new Date();
		searchCriteria.setFromDate(createDate);

		Query query = customerQueryComposerImpl.composeQuery(searchCriteria, getSearchConfig());
		assertQueryContainsDateRange(query, SolrIndexConstants.CREATE_TIME, createDate, null, true, true);
		query = customerQueryComposerImpl.composeFuzzyQuery(searchCriteria, getSearchConfig());
		assertQueryContainsDateRange(query, SolrIndexConstants.CREATE_TIME, createDate, null, true, true);
	}

	/**
	 * Test method for {@link CustomerSearchCriteria#setStoreCodes(Collection)}.
	 */
	@Test
	public void testStoreCodes() {
		final Collection<String> storeCodes = new ArrayList<>(Arrays.asList("code1", "code2", "blah"));
		searchCriteria.setStoreCodes(storeCodes);

		Query query = customerQueryComposerImpl.composeQuery(searchCriteria, getSearchConfig());
		assertQueryContainsSet(query, SolrIndexConstants.STORE_CODE, storeCodes);
		query = customerQueryComposerImpl.composeFuzzyQuery(searchCriteria, getSearchConfig());
		assertQueryContainsSet(query, SolrIndexConstants.STORE_CODE, storeCodes);
	}

	/**
	 * Test method for {@link CustomerSearchCriteria#setFilterUids(Set)}.
	 */
	@Test
	public void testFilterUids() {
		final Set<Long> emptySet = Collections.emptySet();
		final Set<Long> someSet = new HashSet<>(Arrays.asList(123L, 34325L, 123124124L));
		Query query;

		searchCriteria.setFilterUids(emptySet);
		try {
			query = customerQueryComposerImpl.composeQuery(searchCriteria, getSearchConfig());
			fail("EpServiceException expected for empty filter UID set in search criteria.");
		} catch (EpServiceException e) {
			assertNotNull(e);
		}
		try {
			query = customerQueryComposerImpl.composeFuzzyQuery(searchCriteria, getSearchConfig());
			fail("EpServiceException expected for empty filter UID set in search criteria.");
		} catch (EpServiceException e) {
			assertNotNull(e);
		}

		searchCriteria.setFilterUids(someSet);
		query = customerQueryComposerImpl.composeQuery(searchCriteria, getSearchConfig());
		assertQueryContainsSet(query, SolrIndexConstants.OBJECT_UID, someSet);
		query = customerQueryComposerImpl.composeFuzzyQuery(searchCriteria, getSearchConfig());
		assertQueryContainsSet(query, SolrIndexConstants.OBJECT_UID, someSet);
	}

	@Override
	protected QueryComposer getComposerUnderTest() {
		return customerQueryComposerImpl;
	}

	@Override
	protected SearchCriteria getCriteriaUnderTest() {
		return searchCriteria;
	}
}
