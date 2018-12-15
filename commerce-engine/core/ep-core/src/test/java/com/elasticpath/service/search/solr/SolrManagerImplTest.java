/**
 * Copyright (c) Elastic Path Software Inc., 2007
 */
package com.elasticpath.service.search.solr;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.SolrParams;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

/**
 * Test <code>SolrManagerImpl</code>.
 */
public class SolrManagerImplTest {
	
	private SolrManager solrManager;

	@Rule
	public final JUnitRuleMockery context = new JUnitRuleMockery() {
		{
			setImposteriser(ClassImposteriser.INSTANCE);
		}
	};

	private SolrClient mockSolrServer;


	/**
	 * Prepare for test.
	 * 
	 * @throws Exception in case of error
	 */
	@Before
	public void setUp() throws Exception {
		mockSolrServer = context.mock(SolrClient.class);
		solrManager = new DefaultSolrManager();
	}

	/**
	 * Test method for
	 * 'com.elasticpath.persistence.impl.SolrManagerImpl.addUpdateDocument(SolrClient,
	 * SolrInputDocument)'.
	 */
	@Test
	public void testAddUpdateDocumentSolrServerSolrInputDocument() throws Exception {
		final SolrClient client = mockSolrServer;
		final SolrInputDocument document = new SolrInputDocument();
		// give some sort of meaningful data
		document.addField("some field", "some value");
		context.checking(new Expectations() {
			{
				oneOf(mockSolrServer).add(document);
			}
		});
		solrManager.addUpdateDocument(client, document);
	}

	/**
	 * Test method for
	 * 'com.elasticpath.persistence.impl.SolrManagerImpl.addUpdateDocument(SolrClient,
	 * Collection)'.
	 */
	@Test
	public void testAddUpdateDocumentSolrServerCollection() throws Exception {
		final SolrClient client = mockSolrServer;
		final Collection<SolrInputDocument> documents = new ArrayList<>();
		// give some sort of meaningful data
		documents.add(new SolrInputDocument());
		documents.iterator().next().addField("some field", "some value");
		context.checking(new Expectations() {
			{
				oneOf(mockSolrServer).add(documents);
			}
		});
		solrManager.addUpdateDocument(client, documents);
	}

	/**
	 * Test method for
	 * 'com.elasticpath.persistence.impl.SolrManagerImpl.deleteDocument(SolrClient, long)'.
	 */
	@Test
	public void testDeleteDocument() throws Exception {
		final SolrClient client = mockSolrServer;
		final long uid = 0;
		context.checking(new Expectations() {
			{
				oneOf(mockSolrServer).deleteById(String.valueOf(uid));
			}
		});
		solrManager.deleteDocument(client, uid);
	}

	/**
	 * Test method for
	 * 'com.elasticpath.persistence.impl.SolrManagerImpl.rebuildSpelling(SolrClient)'.
	 */
	@Test
	public void testRebuildSpelling() throws Exception {
		final SolrClient client = mockSolrServer;
		final SolrQuery spellingQuery = new SolrQuery();
		spellingQuery.set(CommonParams.QT, SolrIndexConstants.SPELL_CHECKER);
		final SolrQuery rebuildQuery = new SolrQuery();
		rebuildQuery.set("cmd", "rebuild");
		context.checking(new Expectations() {
			{
				oneOf(mockSolrServer).query(with(toStringContains(spellingQuery, rebuildQuery)));
			}
		});
		solrManager.rebuildSpelling(client);
	}

	private TypeSafeMatcher<SolrParams> toStringContains(final Object... values) {
		return new TypeSafeMatcher<SolrParams>() {
			@Override
			protected boolean matchesSafely(final SolrParams solrParams) {
				String stringValue = solrParams.toString();
				for (Object value : values) {
					if (!stringValue.contains(value.toString())) {
						return false;
					}
				}
				return true;
			}

			@Override
			public void describeTo(final Description description) {
				description.appendText("to string contains: ");
				for (Object value : values) {
					description.appendValue(value).appendText(" ");
				}
			}
		};
	}
}
