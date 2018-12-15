/**
 * Copyright (c) Elastic Path Software Inc., 2014
 */
package com.elasticpath.service.search.solr;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.common.SolrInputDocument;

import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.domain.misc.SearchConfig;
import com.elasticpath.persistence.api.EpPersistenceException;
import com.elasticpath.service.search.IndexType;
import com.elasticpath.service.search.SearchConfigFactory;
import com.elasticpath.service.search.SolrIndexConstantFactory;

/**
 * Default implementation of <code>SolrManagerService</code>.
 */
public class DefaultSolrManager implements SolrManager {

	private SearchConfigFactory searchConfigFactory;

	private final Map<IndexType, SolrHolder> solrServers = new HashMap<>();

	private SolrIndexConstantFactory solrIndexConstantFactory;

	private SolrDocumentPublisherFactory solrDocumentPublisherFactory;

	@Override
	public SolrClient getServer(final IndexType indexType) throws EpPersistenceException {
		SolrHolder holder;
		synchronized (solrServers) {
			holder = solrServers.get(indexType);

			if (holder == null) {
				SolrClient httpServer = createServer(indexType);

				SolrDocumentPublisher publisher = getSolrDocumentPublisherFactory().createSolrDocumentPublisher();
				publisher.setSolrServer(httpServer);
				publisher.start();

				holder = new SolrHolder();
				holder.client = httpServer;
				holder.publisher = publisher;

				solrServers.put(indexType, holder);
			}
		}

		return holder.client;
	}

	@Override
	public SearchConfig getSearchConfig(final IndexType indexType) {
		final String solrIndex = solrIndexConstantFactory.getSolrIndexConstant(indexType);
		if (solrIndex == null) {
			throw new EpServiceException("SearchConfig not defined for : " + indexType);
		}
		return getSearchConfig(solrIndex);
	}

	/**
	 * Simple wrapper for searchConfig lookup.
	 */
	private SearchConfig getSearchConfig(final String name) {
		return getSearchConfigFactory().getSearchConfig(name);
	}

	@Override
	public void addUpdateDocument(final SolrClient client, final SolrInputDocument document) throws EpPersistenceException {
		try {
			client.add(document);
		} catch (SolrServerException e) {
			throw new EpPersistenceException("SOLR Error -- add document", e);
		} catch (IOException e) {
			throw new EpPersistenceException("IO Error -- add document", e);
		}
	}

	@Override
	public void addUpdateDocument(final SolrClient client, final Collection<SolrInputDocument> documents) throws EpPersistenceException {
		try {
			client.add(documents);
		} catch (SolrServerException e) {
			throw new EpPersistenceException("SOLR Error -- add document", e);
		} catch (IOException e) {
			throw new EpPersistenceException("IO Error -- add document", e);
		}
	}

	@Override
	public void deleteDocument(final SolrClient client, final long uid) throws EpPersistenceException {
		try {
			client.deleteById(String.valueOf(uid));
		} catch (SolrServerException e) {
			throw new EpPersistenceException("SOLR Error -- delete document", e);
		} catch (IOException e) {
			throw new EpPersistenceException("IO Error -- delete document", e);
		}
	}

	@Override
	public void flushChanges(final SolrClient client, final boolean optimize) throws EpPersistenceException {
		// the list of solr clients is generally low, iterating shouldn't degrade performance
		boolean known = false;
		synchronized (solrServers) {
			for (SolrHolder holder : solrServers.values()) {
				if (holder.client.equals(client)) {
					known = true;
					break;
				}
			}
		}
		if (!known) {
			throw new EpPersistenceException("Unable to flush changes of an unmanaged SOLR client.");
		}

		try {
			if (optimize) {
				client.optimize();
			} else {
				client.commit();
			}
		} catch (SolrServerException e) {
			throw new EpServiceException("SOLR Error -- flush", e);
		} catch (IOException e) {
			throw new EpServiceException("IO Error -- flush", e);
		}
	}

	@Override
	public void rebuildSpelling(final SolrClient client) throws EpPersistenceException {
		final SolrQuery query = new SolrQuery();
		query.setRequestHandler(SolrIndexConstants.SPELL_CHECKER);
		query.set("cmd", "rebuild");
		try {
			client.query(query);
		} catch (SolrServerException | IOException e) {
			throw new EpPersistenceException("SOLR Error -- spelling rebuild", e);
		}
	}

	@Override
	public SolrDocumentPublisher getDocumentPublisher(final IndexType indexType) {
		SolrDocumentPublisher publisher = null;
		synchronized (solrServers) {
			SolrHolder holder = solrServers.get(indexType);
			if (holder != null) {
				publisher = holder.publisher;
			}
		}
		return publisher;
	}

	/**
	 * Creates a HTTP SOLR client for a particular client name.
	 *
	 * @param indexType {@link IndexType} to create a client for
	 * @return a HTTP SOLR client
	 * @throws EpServiceException in case of any errors
	 */
	protected SolrClient createServer(final IndexType indexType) {
		String name = indexType.getIndexName();
		String searchUrl = getSearchConfig(name).getSearchHost();
		// check in case of user error
		if (!searchUrl.endsWith("/")) {
			searchUrl = searchUrl.concat("/");
		}
		searchUrl = searchUrl.concat(name);
		return new HttpSolrClient.Builder().withBaseSolrUrl(searchUrl).build();
	}

	protected SearchConfigFactory getSearchConfigFactory() {
		return searchConfigFactory;
	}

	public void setSearchConfigFactory(final SearchConfigFactory searchConfigFactory) {
		this.searchConfigFactory = searchConfigFactory;
	}

	public void setSolrIndexConstantFactory(final SolrIndexConstantFactory solrIndexConstantFactory) {
		this.solrIndexConstantFactory = solrIndexConstantFactory;
	}

	public SolrDocumentPublisherFactory getSolrDocumentPublisherFactory() {
		return solrDocumentPublisherFactory;
	}

	public void setSolrDocumentPublisherFactory(final SolrDocumentPublisherFactory solrDocumentPublisherFactory) {
		this.solrDocumentPublisherFactory = solrDocumentPublisherFactory;
	}

	/** Container class for solr index lookups. */
	private static class SolrHolder {
		private SolrClient client;

		private SolrDocumentPublisher publisher;
	}
}
