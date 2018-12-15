/**
 * Copyright (c) Elastic Path Software Inc., 2018
 */

package com.elasticpath.search.plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.search.BoostQuery;
import org.apache.lucene.search.DisjunctionMaxQuery;
import org.apache.lucene.search.Query;
import org.apache.solr.search.QParser;
import org.apache.solr.search.SolrQueryParser;
import org.apache.solr.search.SyntaxError;
import org.apache.solr.util.SolrPluginUtils;

/**
 * A subclass of SolrQueryParser that supports aliasing fields for
 * constructing DisjunctionMaxQueries.
 * <p>
 * Copied from SolrPlugUtils.DisjunctionMaxQueryParser from solr-core-1.3.0.
 * Parts of patch for SOLR 1.3 from SOLR-629 merged in. Did not merge in the parsing stuff.
 * Note that the vendor branch for queryParser has a prefixFields parameter. That code
 * allowed a PrefixQuery to be used for some fields. This code has not been included. @see
 * https://svn.elasticpath.com/perfect_ep/pd/vendor/lucene/custom/ep/org.apache.lucene/src/java/org/apache/lucene/queryParser/QueryParser.java
 */
public class FuzzyDisjunctionMaxQueryParser extends SolrQueryParser {

		private static final int DEFAULT_ALIAS_MAP_SIZE = 3;
		private boolean doFuzzyQuery;

		/**
		 * Where we store a map from field name we expect to see in our query
		 * string, to Alias object containing the fields to use in our
		 * DisjunctionMaxQuery and the tiebreaker to use.
		 */
		private final Map<String, Alias> aliases = new HashMap<>(DEFAULT_ALIAS_MAP_SIZE);

		/**
		 * If set to true then this parser will return {@code FuzzyQuery}s. The default is false.
		 *
		 * @param doFuzzyQuery True if fuzzy queries should be returned.
		 */
		public void setFuzzy(final boolean doFuzzyQuery) {
			this.doFuzzyQuery = doFuzzyQuery;
		}

		/**
		 * A simple container for storing alias info.
		 *
		 * @see #aliases
		 */
		protected static class Alias {
			private float tie;
			private Map<String, Float> fields;

			/**
			 * Gets the tie value.
			 *
			 * @return The tie value.
			 */
			public float getTie() {
				return tie;
			}

			/**
			 * The fields for this alias.
			 *
			 * @return The fields.
			 */
			public Map<String, Float> getFields() {
				return fields;
			}
		}

		/**
		 * Normal constructor.
		 *
		 * @param parser       an instance of QParser.
		 * @param defaultField The default field.
		 */
		@SuppressWarnings("checkstyle:redundantmodifier")
		public FuzzyDisjunctionMaxQueryParser(final QParser parser, final String defaultField) {
			super(parser, defaultField);
			// don't trust that our parent class won't ever change it's default
			setDefaultOperator(Operator.OR);
		}

		/**
		 * Constructor.
		 *
		 * @param parser an instance of QParser.
		 */
		@SuppressWarnings("checkstyle:redundantmodifier")
		public FuzzyDisjunctionMaxQueryParser(final QParser parser) {
			this(parser, null);
		}

		/**
		 * Add an alias to this query parser.
		 *
		 * @param field       the field name that should trigger alias mapping
		 * @param fieldBoosts the mapping from fieldname to boost value that
		 *                    should be used to build up the clauses of the
		 *                    DisjunctionMaxQuery.
		 * @param tiebreaker  to the tiebreaker to be used in the
		 *                    DisjunctionMaxQuery
		 * @see SolrPluginUtils#parseFieldBoosts
		 */
		public void addAlias(final String field, final float tiebreaker,
		                     final Map<String, Float> fieldBoosts) {

			Alias alias = new Alias();
			alias.tie = tiebreaker;
			alias.fields = fieldBoosts;
			aliases.put(field, alias);
		}

		/**
		 * Delegates to the super class unless the field has been specified
		 * as an alias.
		 *
		 * @param field     the field to query
		 * @param queryText the text to query with.
		 * @param quoted    whether or not the the query is quoted.
		 * @return the query
		 * @throws SyntaxError If a failure is detected.
		 */
		@Override
		protected Query getFieldQuery(final String field, final String queryText, final boolean quoted) throws SyntaxError {

			if (aliases.containsKey(field)) {

				Alias alias = aliases.get(field);
				List<Query> queries = new ArrayList<>();

				/* we might not get any valid queries from delegation,
				 * in which we should return null
				 */
				boolean isOk = false;

				for (String aliasField : alias.fields.keySet()) {

					isOk |= getSubQuery(queryText, quoted, alias, queries, aliasField);
				}
				DisjunctionMaxQuery query = new DisjunctionMaxQuery(queries, alias.tie);

				return isOk ? query : null;
			}
			return super.getFieldQuery(field, queryText, quoted);
		}

		private boolean getSubQuery(final String queryText, final boolean quoted, final Alias alias, final List<Query> queries,
									final String aliasField) throws SyntaxError {

			boolean isOk = false;
			Query sub;
			if (doFuzzyQuery) {
				// N.B.: SOLR-629 patch contained code that handled aliases and recursed through them to
				// create the correct fuzzy query. That code has not been included because it relies on the
				// parsing code which also has not been included.
				sub = getFuzzyQuery(aliasField, queryText, getFuzzyMinSim());
			} else {
				sub = getFieldQuery(aliasField, queryText, quoted);
			}

			if (null != sub) {
				if (null != alias.fields.get(aliasField)) {

					sub = new BoostQuery(sub, alias.fields.get(aliasField));
				}
				queries.add(sub);
				isOk = true;
			}
			return isOk;
		}
	}

