/**
 * Copyright (c) Elastic Path Software Inc., 2013
 */
package com.elasticpath.ql.parser.query;

import org.apache.lucene.search.BooleanClause;


/**
 * This class represents lucene boolean clause. 
 */
public class LuceneBooleanClause  implements NativeBooleanClause {
	private static final long serialVersionUID = 5469401600179891639L;
	private BooleanClause booleanClause;

	/**
	 * Constructs lucene boolean clause.
	 * 
	 * @param lucenQuery the lucene query
	 * @param occur Specifies how clauses are to occur in matching documents
	 */
	public LuceneBooleanClause(final LuceneQuery lucenQuery, final BooleanClause.Occur occur) {
		setBooleanClause(new BooleanClause(lucenQuery.getQuery(), occur));
	}

	/**
	 * Gets the inner boolean clause.
	 * @return  the boolean clause.
	 */
	public BooleanClause getBooleanClause() {
		return booleanClause;
	}

	/**
	 * Sets the inner boolean clause.
	 * @param booleanClause the boolean clause.
	 */
	public final void setBooleanClause(final BooleanClause booleanClause) {
		this.booleanClause = booleanClause;
	}

	/**
	 * Sets the occur on the inner clause.
	 * @param occur the occur.
	 */
	public void setOccur(final BooleanClause.Occur occur) {
		if (getBooleanClause() != null) {
			setBooleanClause(new BooleanClause(getBooleanClause().getQuery(), occur));
		}
	}


}
