/**
 * Copyright (c) Elastic Path Software Inc., 2014
 */
package com.elasticpath.search.solr;

import org.apache.lucene.search.FieldComparator;
import org.apache.lucene.search.FieldComparatorSource;

/**
 * Factory for creating price comparators for dealing with price list price
 * sorting.
 */
public class PriceComparatorSource extends FieldComparatorSource {

	private static final long serialVersionUID = 1L;

	@Override
	public FieldComparator<String> newComparator(final String fieldname, final int numHits, final int sortPos, final boolean reversed) {
		return new PriceListPriceScoreDocComparator(fieldname, numHits);
	}

}
