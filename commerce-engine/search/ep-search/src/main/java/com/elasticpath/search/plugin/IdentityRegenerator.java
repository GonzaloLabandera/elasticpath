/**
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.search.plugin;

import java.io.IOException;

import org.apache.solr.search.CacheRegenerator;
import org.apache.solr.search.SolrCache;
import org.apache.solr.search.SolrIndexSearcher;

/**
 * A CacheRegenerator that can be used whenever the items in the cache
 * are not dependant on the current searcher.
 *
 * <p>
 * Flat out copies the oldKey=&gt;oldVal pair into the newCache
 * </p>
 */
public class IdentityRegenerator implements CacheRegenerator {

	/**
	 * Regenerates the item.
	 *
	 * @param newSearcher The new searcher.
	 * @param newCache    The new cache.
	 * @param oldCache    The old cache.
	 * @param oldKey      The old key
	 * @param oldVal      The old value
	 * @return true
	 * @throws IOException if a failure is detected.
	 */
	@Override
	@SuppressWarnings({"unchecked", "rawtypes"})
	public boolean regenerateItem(final SolrIndexSearcher newSearcher,
								  final SolrCache newCache,
								  final SolrCache oldCache,
								  final Object oldKey,
								  final Object oldVal)
			throws IOException {

		newCache.put(oldKey, oldVal);
		return true;
	}
}
