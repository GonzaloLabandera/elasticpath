/**
 * Copyright (c) Elastic Path Software Inc., 2014
 */
package com.elasticpath.search.index.solr.service.impl;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedResource;

import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.search.IndexNotification;
import com.elasticpath.domain.search.IndexNotification.AffectedEntityType;
import com.elasticpath.domain.search.UpdateType;
import com.elasticpath.persistence.api.EpPersistenceException;
import com.elasticpath.search.IndexNotificationProcessor;
import com.elasticpath.search.index.pipeline.stats.PipelineStatus;
import com.elasticpath.search.index.solr.IndexBuildEventListener;
import com.elasticpath.search.index.solr.builders.IndexBuilder;
import com.elasticpath.search.index.solr.service.IndexBuildPolicy;
import com.elasticpath.search.index.solr.service.IndexBuildPolicyContext;
import com.elasticpath.search.index.solr.service.IndexBuildPolicyContextFactory;
import com.elasticpath.service.search.IndexType;
import com.elasticpath.service.search.index.IndexSearchService;
import com.elasticpath.service.search.query.LuceneRawSearchCriteria;
import com.elasticpath.service.search.solr.SolrManager;

/**
 * Methods for building/rebuilding search indexes.
 */
@ManagedResource(objectName = "com.elasticpath.search:name=IndexBuildService",
		description = "Operate on the index building service", currencyTimeLimit = 1)
@SuppressWarnings("PMD.GodClass")
public class IndexBuildServiceImpl extends AbstractIndexServiceImpl {

	private static final Logger LOG = Logger.getLogger(IndexBuildServiceImpl.class);

	private SolrManager solrManager;

	private IndexSearchService indexSearchService;

	private IndexBuildPolicy indexBuildPolicy;

	private Set<String> optimizedIndexesListExclusions;

	private IndexBuildPolicyContextFactory indexBuildPolicyContextFactory;

	private Predicate<IndexType> searchIndexExistencePredicate;

	/**
	 * Finds all deleted uids.
	 *
	 * @param indexBuilder the index builder
	 * @param lastBuildDate the last build date
	 * @return a set of uids
	 */
	protected Set<Long> findDeletedUidsInternal(final IndexBuilder indexBuilder, final Date lastBuildDate) {
		final Collection<Long> deletedUids = indexBuilder.findDeletedUids(lastBuildDate);
		final IndexNotificationProcessor indexNotificationProcessor = indexBuilder.getIndexNotificationProcessor();

		final Set<Long> deletedUidsSet = new HashSet<>();
		deletedUidsSet.addAll(deletedUids);

		for (final IndexNotification notification : indexNotificationProcessor.getNotifications()) {
			if (notification.getUpdateType() != UpdateType.DELETE) {
				continue;
			}

			if (notification.getAffectedEntityType() == null) {
				deletedUidsSet.addAll(findAffectedUidsByQuery(indexBuilder.getIndexType(), notification.getQueryString()));
			} else if (AffectedEntityType.SINGLE_UNIT.equals(notification.getAffectedEntityType())) {
				deletedUidsSet.add(notification.getAffectedUid());
			} else {
				deletedUidsSet.addAll(indexBuilder.findUidsByNotification(notification));
			}
		}

		if (LOG.isDebugEnabled()) {
			LOG.debug("Building Index -- total objects of " + indexBuilder.getName() + " removed:" + deletedUidsSet.size());
		}

		return deletedUidsSet;
	}

	/**
	 * Finds all the modified or added uids.
	 *
	 * @param indexBuilder the index builder
	 * @param lastBuildDate the last build date
	 * @return a set of uids
	 */
	protected Set<Long> findAddedOrModifiedUidsInternal(final IndexBuilder indexBuilder, final Date lastBuildDate) {
		final Collection<Long> addedOrModifiedUids = indexBuilder.findAddedOrModifiedUids(lastBuildDate);
		final IndexNotificationProcessor indexNotificationProcessor = indexBuilder.getIndexNotificationProcessor();

		final Set<Long> allModifiedUids = new HashSet<>(addedOrModifiedUids.size());
		allModifiedUids.addAll(addedOrModifiedUids);

		for (final IndexNotification notification : indexNotificationProcessor.getNotifications()) {
			if (notification.getUpdateType() != UpdateType.UPDATE) {
				continue;
			}

			if (notification.getAffectedEntityType() == null) {
				allModifiedUids.addAll(findAffectedUidsByQuery(indexBuilder.getIndexType(), notification.getQueryString()));
			} else if (AffectedEntityType.SINGLE_UNIT.equals(notification.getAffectedEntityType())) {
				allModifiedUids.add(notification.getAffectedUid());
			} else {
				allModifiedUids.addAll(indexBuilder.findUidsByNotification(notification));
			}
		}
		if (LOG.isDebugEnabled()) {
			LOG.debug("Building Index -- total objects of " + indexBuilder.getName() + " modified:" + allModifiedUids.size());
		}

		return allModifiedUids;
	}

	/**
	 * Concrete implementation of the build method. <br>
	 * Checks whether there is a notification for rebuild and triggers one if required.
	 *
	 * @param indexType the index type
	 * @param rebuild signifies whether a rebuild should be triggered
	 */
	@Override
	protected void build(final IndexType indexType, final boolean rebuild) {
		final IndexBuilder indexBuilder = getIndexBuilder(indexType);
		final SolrClient solrClient = getSolrManager().getServer(indexBuilder.getIndexType());

		int operations = 0;

		if (rebuild) {
			// just find notifications for indexType and then clear this notifications then buildFinished(..) called
			indexBuilder.getIndexNotificationProcessor().findAllNewNotifications(indexType);

			operations = rebuildInternal(indexBuilder, solrClient);
		} else {
			operations = buildInternal(indexBuilder, solrClient);
		}
		buildFinished(indexBuilder, operations, solrClient);
	}

	/**
	 * Checks whether there is an update type that requires this index to be rebuilt.
	 *
	 * @param indexType the index type
	 * @return true if a rebuild is required
	 */
	@Override
	public boolean isRebuildRequired(final IndexType indexType) {
		return isRebuildRequested()
				.or(getSearchIndexExistencePredicate().negate())
				.test(indexType);
	}

	/**
	 * Checks whether there is a REBUILD status in all the available notifications for this {@link IndexBuilder}.
	 *
	 * @return true if {@link UpdateType#REBUILD} is contained in the notifications of this indexBuilder
	 */
	private Predicate<IndexType> isRebuildRequested() {
		return indexType -> {
			final IndexBuilder indexBuilder = getIndexBuilder(indexType);
			final IndexNotificationProcessor indexNotificationProcessor = indexBuilder.getIndexNotificationProcessor();
			final List<IndexNotification> notifications = indexNotificationProcessor.findAllNewNotifications(indexBuilder.getIndexType());
			for (final IndexNotification notification : notifications) {
				if (notification.getUpdateType() == UpdateType.REBUILD) {
					return true;
				}
			}
			return false;
		};
	}

	/**
	 * Finds all the uids to be updated and/or deleted and adds/deletes them to/from the index.
	 *
	 * @param indexBuilder the index builder to use
	 * @param solrClient the solr client instance to use
	 * @return the number of changes done to the indexes in terms of updating them
	 */
	protected int buildInternal(final IndexBuilder indexBuilder, final SolrClient solrClient) {
		final Date lastBuildDate = getLastBuildDate(indexBuilder.getIndexType());

		// add or update the corresponding documents in the index for products that were added or updated since the last build
		final Collection<Long> addedOrModifiedUids = findAddedOrModifiedUidsInternal(indexBuilder, lastBuildDate);

		// delete the corresponding documents in the index for products that were deleted since the last build
		final Collection<Long> deletedUids = findDeletedUidsInternal(indexBuilder, lastBuildDate);
		final boolean isIndexToBeUpdated = !addedOrModifiedUids.isEmpty() || !deletedUids.isEmpty();
		int operations = 0;

		if (isIndexToBeUpdated) {

			onIndexUpdatingInternal(indexBuilder, solrClient);

			operations = addedOrModifiedUids.size();

			if (operations > 0) {

				onAddUpdateDocuments(indexBuilder, addedOrModifiedUids, solrClient, operations);

				indexBuilder.submit(addedOrModifiedUids);

				PipelineStatus status = getIndexingStatistics().getPipelineStatus(indexBuilder.getIndexType());

				if (status == null) {
					LOG.error("Pipeline status could not be found. "
							+ "Index build is started asynchronously, and there is no guarantee it finishes before this method returns.");
				} else {
					try {
						status.waitUntilCompleted();
					} catch (InterruptedException e) {
						LOG.error("While waiting for index to build, received exception:", e);
						Thread.currentThread().interrupt();
					}
				}
			}

			operations += deleteDocumentInIndex(indexBuilder, deletedUids, operations);

		}
		return operations;
	}

	/**
	 * Rebuild an index by removing all the elements and building it from scratch.
	 *
	 * @param indexBuilder the index builder to use
	 * @param solrClient the solr client to use
	 * @return the number of changes done to the index
	 */
	protected int rebuildInternal(final IndexBuilder indexBuilder, final SolrClient solrClient) {
		// if rebuild, create new index
		if (LOG.isDebugEnabled()) {
			LOG.debug("Building Index -- recreate start: " + indexBuilder.getName());
		}

		final List<Long> allUids = indexBuilder.findAllUids();
		if (LOG.isDebugEnabled()) {
			LOG.debug("Building Index -- total objects of " + indexBuilder.getName() + " to index:" + allUids.size());
		}

		// This is a rebuild, so we delete all the existing things and then add all the found uids.
		// If there were no found uids, then we don't actually have any work to do, so we return right away.
		// Historically we would return the number of Solr operations done, however with the introduction of {@code IndexingPipeline},
		// this need has gone away and we attempt to appease code that expects these numbers.
		int operations = 0;

		onIndexUpdatingInternal(indexBuilder, solrClient);

		deleteIndex(solrClient);

		operations = allUids.size();

		if (operations > 0) {

			onAddUpdateDocuments(indexBuilder, allUids, solrClient, operations);

			indexBuilder.submit(allUids);

			PipelineStatus status = getIndexingStatistics().getPipelineStatus(indexBuilder.getIndexType());

			if (status == null) {
				LOG.error("Pipeline status could not be found. "
						+ "Index build is started asynchronously, and there is no guarantee it finishes before this method returns.");
			} else {
				try {
					status.waitUntilCompleted();
				} catch (InterruptedException e) {
					LOG.error("While waiting for index to build, received exception:", e);
					Thread.currentThread().interrupt();
				}
			}
		} else {
			commitWithEmptyIndex(solrClient);
		}

		return operations;
	}

	/**
	 * Run finalizing build operations (i.e. final commit, build date)
	 *
	 * @param indexBuilder the index builder
	 * @param operations the number of solr operations performed during the build
	 * @param client the solr client instance
	 */
	protected void buildFinished(final IndexBuilder indexBuilder, final int operations, final SolrClient client) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Building Index -- finished: " + indexBuilder.getName());
		}
		if (operations > 0) {
			onIndexUpdatedInternal(indexBuilder, client);
		}
		final IndexNotificationProcessor indexNotificationProcessor = indexBuilder.getIndexNotificationProcessor();
		indexNotificationProcessor.removeStoredNotifications();
		IndexType indexType = indexBuilder.getIndexType();
		synchronized (indexType) {
			setLastBuildDate(indexType);
		}
	}

	/**
	 * A callback method for when documents are to be added to the index.
	 *
	 * @param indexBuilder the index builder
	 * @param uidList the list of UID
	 * @param solrClient the Solr client
	 * @param operations the number of operations done so far
	 */
	protected void onAddUpdateDocuments(final IndexBuilder indexBuilder,
			final Collection<Long> uidList,
			final SolrClient solrClient,
			final int operations) {
		// by default does nothing
	}

	/**
	 * Deletes documents from the index for objects with the given UID list.
	 *
	 * @param indexBuilder the index builder
	 * @param deletedUidList the list of UIDs to delete
	 * @param operations the number of SOLR operations thus far
	 * @return the number of SOLR operations performed
	 * @throws EpServiceException in case of any errors
	 */
	private int deleteDocumentInIndex(final IndexBuilder indexBuilder, final Collection<Long> deletedUidList, final int operations)
			throws EpServiceException {

		int solrOperations = operations; // reassigned to silence PMD
		LOG.debug("Building Index -- delete start for " + indexBuilder.getName());

		for (final long uid : deletedUidList) {

			try {
				getSolrManager().getDocumentPublisher(indexBuilder.getIndexType()).deleteDocument(indexBuilder.getIndexType(), uid);
				solrOperations++;
			} catch (InterruptedException e) {
				LOG.warn("Interrupted while waiting for the publisher queue to drain", e);
				Thread.currentThread().interrupt();
			}
		}

		LOG.debug("Building Index -- delete end for " + indexBuilder.getName());
		return solrOperations;
	}

	public void setSolrManager(final SolrManager solrManager) {
		this.solrManager = solrManager;
	}

	/**
	 * Removes all indexes from a SOLR client.
	 *
	 * @param client the client to operate on
	 */
	private void deleteIndex(final SolrClient client) {
		LOG.debug("Removing all indexes from client " + client);

		try {
			client.deleteByQuery("*:*");
		} catch (final SolrServerException e) {
			if (client instanceof HttpSolrClient) {
				LOG.error("Error executing search. Solr Manager url : " + ((HttpSolrClient) client).getBaseURL(), e);
			}
			throw new EpPersistenceException("remove all indexes", e);
		} catch (final IOException e) {
			throw new EpPersistenceException("remove all indexes", e);
		}
	}

	private void commitWithEmptyIndex(final SolrClient solrClient) {
		try {
			solrClient.commit();
		} catch (final SolrServerException e) {
			if (solrClient instanceof HttpSolrClient) {
				LOG.error("Error executing rebuild on empty index. Solr Manager url : " + ((HttpSolrClient) solrClient).getBaseURL(), e);
			}
			throw new EpPersistenceException("Rebuild on empty index", e);
		} catch (final IOException e) {
			throw new EpPersistenceException("Rebuild on empty index", e);
		}
	}

	protected SolrManager getSolrManager() {
		return solrManager;
	}

	private void onIndexUpdatedInternal(final IndexBuilder indexBuilder, final SolrClient client) {
		final IndexBuildEventListener buildEventListener = indexBuilder.getIndexBuildEventListener();
		if (buildEventListener != null) {
			buildEventListener.indexBuildComplete();
		}

		// flush here just in case we didn't catch anything
		// do this before spelling so that our request handler can pick up new
		// fields
		getSolrManager().getDocumentPublisher(indexBuilder.getIndexType()).flush();
		getSolrManager().getDocumentPublisher(indexBuilder.getIndexType()).commit();

		indexBuilder.onIndexUpdated(client);
	}

	protected IndexBuildPolicy getIndexBuildPolicy() {
		return indexBuildPolicy;
	}

	public void setIndexBuildPolicy(final IndexBuildPolicy indexBuildPolicy) {
		this.indexBuildPolicy = indexBuildPolicy;
	}

	private void onIndexUpdatingInternal(final IndexBuilder indexBuilder, final SolrClient client) {
		indexBuilder.onIndexUpdating(client);
	}

	private Collection<Long> findAffectedUidsByQuery(final IndexType indexType, final String query) {
		final LuceneRawSearchCriteria searchCriteria = getPrototypeBean(ContextIdNames.LUCENE_RAW_SEARCH_CRITERIA, LuceneRawSearchCriteria.class);
		searchCriteria.setIndexType(indexType);
		searchCriteria.setQuery(query);
		return indexSearchService.search(searchCriteria).getAllResults();
	}

	public void setIndexSearchService(final IndexSearchService indexSearchService) {
		this.indexSearchService = indexSearchService;
	}

	protected IndexSearchService getIndexSearchService() {
		return indexSearchService;
	}

	/**
	 * A method meant to be used by a quartz job to trigger the optimization of indices.
	 */
	public void optimizeIndicesJobRunner() {
		final long startTime = System.currentTimeMillis();
		LOG.info("Start optimize indices quartz job at: " + new Date(startTime));

		for (final IndexType indexType : IndexType.values()) {
			if (getOptimizedIndexesListExclusions().contains(indexType.getIndexName())) {
				return;
			}
			final IndexBuildPolicyContext context = getIndexBuildPolicyContextFactory().createIndexBuildPolicyContext();
			context.setIndexType(indexType);
			context.setDocumentsAdded(-1);
			context.setOperationsCount(-1);

			// if a rebuild has been scheduled or is running at the moment do not optimize as this will commit changes partially
			// and will break the search on store front and CM Client
			if (getIndexBuildPolicy().isOptimizationRequired(context) && isRebuildRequested().negate().test(indexType)) {
				optimizeIndex(indexType);
			}
		}
		LOG.info("Optimize indices quartz job completed in (ms): " + (System.currentTimeMillis() - startTime));
	}

	/**
	 * Expose index rebuilding via JMX.
	 *
	 * @param indexTypeName the string name of the {@code IndexType}
	 */
	@ManagedOperation(description = "Request that the named IndexType be rebuilt.")
	@ManagedOperationParameter(name = "indexTypeName", description = "The string value of the IndexType.")
	public void rebuildIndex(final String indexTypeName) {
		final IndexType indexType = IndexType.findFromName(indexTypeName);
		build(indexType, true);
	}

	/**
	 * Optimizes an index using the {@link SolrClient} instance of that index.
	 *
	 * @param indexType the index type
	 */
	protected void optimizeIndex(final IndexType indexType) {
		try {
			final SolrClient solrClient = getSolrManager().getServer(indexType);
			solrClient.optimize();
		} catch (final Exception exc) {
			LOG.warn("Could not optimize the index: " + indexType, exc);
		}
	}

	public void setOptimizedIndexesListExclusions(final Set<String> optimizedIndexesListExclusions) {
		this.optimizedIndexesListExclusions = optimizedIndexesListExclusions;
	}

	protected Set<String> getOptimizedIndexesListExclusions() {
		return optimizedIndexesListExclusions;
	}

	protected IndexBuildPolicyContextFactory getIndexBuildPolicyContextFactory() {
		return indexBuildPolicyContextFactory;
	}

	public void setIndexBuildPolicyContextFactory(final IndexBuildPolicyContextFactory indexBuildPolicyContextFactory) {
		this.indexBuildPolicyContextFactory = indexBuildPolicyContextFactory;
	}

	protected Predicate<IndexType> getSearchIndexExistencePredicate() {
		return searchIndexExistencePredicate;
	}

	public void setSearchIndexExistencePredicate(final Predicate<IndexType> searchIndexExistencePredicate) {
		this.searchIndexExistencePredicate = searchIndexExistencePredicate;
	}

}
