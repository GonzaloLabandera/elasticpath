/**
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.search.plugin;

import static java.lang.Float.compare;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.common.base.Strings;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BoostQuery;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.DisMaxParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.search.FunctionQParserPlugin;
import org.apache.solr.search.QParser;
import org.apache.solr.search.QueryParsing;
import org.apache.solr.search.SortSpecParsing;
import org.apache.solr.search.SyntaxError;
import org.apache.solr.util.SolrPluginUtils;

import com.elasticpath.service.search.solr.DisMaxConstants;

/**
 * Parser which creates {@code BooleanQuery}s or {@code FuzzyQuery}s as required.
 * <p>
 * Note: Copied from DismaxQParserPlugin. SuppressWarnings used to allow a compare to be less difficult.
 */
public class DismaxQParser extends QParser {

	/**
	 * A field we can't ever find in any schema, so we can safely tell
	 * DisjunctionMaxQueryParser to use it as our defaultField, and
	 * map aliases from it to any field in our schema.
	 */
	private static final String IMPOSSIBLE_FIELD_NAME = String.valueOf('\uFFFC' + '\uFFFC' + '\uFFFC');

	private Map<String, Float> queryFields;
	private Query parsedUserQuery;
	private String[] boostParams;
	private List<Query> boostQueries;
	private Query altUserQuery;

	/**
	 * Default constructor.
	 *
	 * @param qstr        The query string.
	 * @param localParams The parameters from the http request.
	 * @param params      The parameters from the http request.
	 * @param req         The query request
	 */
	@SuppressWarnings("checkstyle:redundantmodifier")
	public DismaxQParser(final String qstr, final SolrParams localParams, final SolrParams params, final SolrQueryRequest req) {
		super(qstr, localParams, params, req);
	}

	@Override
	public Query parse() throws SyntaxError {
		SolrParams solrParams;
		if (getLocalParams() == null) {
			solrParams = getParams();
		} else {
			solrParams = SolrParams.wrapDefaults(getLocalParams(), getParams());
		}

		queryFields = SolrPluginUtils.parseFieldBoosts(solrParams.getParams(DisMaxParams.QF));
		Map<String, Float> phraseFields = SolrPluginUtils.parseFieldBoosts(solrParams.getParams(DisMaxParams.PF));

		float tiebreaker = solrParams.getFloat(DisMaxParams.TIE, 0.0F);

		int pslop = solrParams.getInt(DisMaxParams.PS, 0);
		int qslop = solrParams.getInt(DisMaxParams.QS, 0);

		// Additional code for fuzzy queries starts here
		boolean fuzzy = solrParams.getBool(DisMaxConstants.FUZZY, false);
		float minimumSimilarity = solrParams.getFloat(DisMaxConstants.MINIMUM_SIMILARITY, FuzzyQuery.defaultMinSimilarity);
		int prefixLength = solrParams.getInt(DisMaxConstants.MINIMUM_LENGTH, FuzzyQuery.defaultPrefixLength);

		/* a parser for dealing with user input, which will convert
		 * things to DisjunctionMaxQueries
		 */
		FuzzyDisjunctionMaxQueryParser fuzzyDisjunctionMaxQueryParser =
				new FuzzyDisjunctionMaxQueryParser(this, IMPOSSIBLE_FIELD_NAME);
		fuzzyDisjunctionMaxQueryParser.addAlias(IMPOSSIBLE_FIELD_NAME,
				tiebreaker, queryFields);
		fuzzyDisjunctionMaxQueryParser.setPhraseSlop(qslop);

		fuzzyDisjunctionMaxQueryParser.setFuzzy(fuzzy);
		fuzzyDisjunctionMaxQueryParser.setFuzzyMinSim(minimumSimilarity);
		fuzzyDisjunctionMaxQueryParser.setFuzzyPrefixLength(prefixLength);
		// Additional code for fuzzy queries ends here

		/* for parsing sloppy phrases using DisjunctionMaxQueries */
		SolrPluginUtils.DisjunctionMaxQueryParser disjunctionMaxQueryParser =
				new SolrPluginUtils.DisjunctionMaxQueryParser(this, IMPOSSIBLE_FIELD_NAME);
		disjunctionMaxQueryParser.addAlias(IMPOSSIBLE_FIELD_NAME,
				tiebreaker, phraseFields);
		disjunctionMaxQueryParser.setPhraseSlop(pslop);

		/* the main query we will execute.  we disable the coord because
		 * this query is an artificial construct
		 */
		BooleanQuery.Builder queryBuilder = new BooleanQuery.Builder();


		/* * * Main User Query * * */
		parsedUserQuery = null;
		String userQuery = getString();
		altUserQuery = null;
		parseUserQuery(solrParams, fuzzyDisjunctionMaxQueryParser, disjunctionMaxQueryParser, queryBuilder, userQuery);

		/* * * Boosting Query * * */
		boostParams = solrParams.getParams(DisMaxParams.BQ);
		//List<Query> boostQueries = SolrPluginUtils.parseQueryStrings(req, boostParams);
		boostQueries = null;
		if (boostParams != null && boostParams.length > 0) {
			boostQueries = new ArrayList<>();
			for (String boostParam : boostParams) {
				String trimmedParam = boostParam.trim();
				if (trimmedParam.length() == 0) {
					continue;
				}
				Query innerQuery = subQuery(boostParam, null).parse();
				boostQueries.add(new BoostQuery(innerQuery, 0));
			}
		}
		if (null != boostQueries) {
			if (1 == boostQueries.size() && 1 == boostParams.length) {
				/* legacy logic */
				BoostQuery boostQuery = (BoostQuery) boostQueries.get(0);
				int result = compare(1.0F, boostQuery.getBoost());   // Returns 0 if numerically equal
				if (result == 0 && boostQuery.getQuery() instanceof BooleanQuery) {
					/* if the default boost was used, and we've got a BooleanQuery
					 * extract the subqueries out and use them directly
					 */
					for (Object boostClause : ((BooleanQuery) boostQuery.getQuery()).clauses()) {
						queryBuilder.add((BooleanClause) boostClause);
					}
				} else {
					queryBuilder.add(boostQuery, BooleanClause.Occur.SHOULD);
				}
			} else {
				for (Query boostQuery : boostQueries) {
					queryBuilder.add(boostQuery, BooleanClause.Occur.SHOULD);
				}
			}
		}

		/* * * Boosting Functions * * */
		String[] boostFuncs = solrParams.getParams(DisMaxParams.BF);
		if (null != boostFuncs && 0 != boostFuncs.length) {
			queryBuilder = getQueryWithBoostFunctions(queryBuilder, boostFuncs);
		}
		return queryBuilder.build();
	}

	private void parseUserQuery(final SolrParams solrParams, final FuzzyDisjunctionMaxQueryParser fuzzyDisjunctionMaxQueryParser,
								final SolrPluginUtils.DisjunctionMaxQueryParser disjunctionMaxQueryParser, final BooleanQuery.Builder queryBuilder,
								final String userQuery) throws SyntaxError {
		if (StringUtils.isEmpty(userQuery)) {
			// If no query is specified, we may have an alternate
			String altQ = solrParams.get(DisMaxParams.ALTQ);
			if (altQ == null) {
				throw new SolrException(SolrException.ErrorCode.BAD_REQUEST,
						"missing query string");
			} else {
				altUserQuery = subQuery(altQ, null).parse();
				queryBuilder.add(altUserQuery, BooleanClause.Occur.MUST);
			}
		} else {
			// There is a valid query string
			String sanitizedUserQuery = SolrPluginUtils.stripIllegalOperators(
					SolrPluginUtils.partialEscape(SolrPluginUtils.stripUnbalancedQuotes(userQuery)).toString()).toString();


			String minShouldMatch = solrParams.get(DisMaxParams.MM, "100%");
			Query disjunctionQuery = fuzzyDisjunctionMaxQueryParser.parse(sanitizedUserQuery);
			parsedUserQuery = disjunctionQuery;

			if (disjunctionQuery instanceof BooleanQuery) {
				BooleanQuery.Builder booleanQueryBuilder = new BooleanQuery.Builder();
				SolrPluginUtils.flattenBooleanQuery(booleanQueryBuilder, (BooleanQuery) disjunctionQuery);
				SolrPluginUtils.setMinShouldMatch(booleanQueryBuilder, minShouldMatch);
				parsedUserQuery = booleanQueryBuilder.build();
			}
			queryBuilder.add(parsedUserQuery, BooleanClause.Occur.MUST);

			/* * * Add on Phrases for the Query * * */
			/* build up phrase boosting queries */
			/* if the userQuery already has some quotes, stip them out.
			 * we've already done the phrases they asked for in the main
			 * part of the query, this is to boost docs that may not have
			 * matched those phrases but do match looser phrases.
			 */
			String userPhraseQuery = sanitizedUserQuery.replace("\"", "");
			Query phrase = disjunctionMaxQueryParser.parse("\"" + userPhraseQuery + "\"");
			if (null != phrase) {
				queryBuilder.add(phrase, BooleanClause.Occur.SHOULD);
			}
		}
	}

	private BooleanQuery.Builder getQueryWithBoostFunctions(final BooleanQuery.Builder queryBuilder, final String[] boostFuncs) throws SyntaxError {
		for (String boostFunc : boostFuncs) {
			if (null == boostFunc || "".equals(boostFunc)) {
				continue;
			}
			Map<String, Float> fieldBoosts = SolrPluginUtils.parseFieldBoosts(boostFunc);
			for (final Map.Entry<String, Float> entry : fieldBoosts.entrySet()) {
				Query subQuery = subQuery(entry.getKey(), FunctionQParserPlugin.NAME).parse();
				Float boost = entry.getValue();
				if (null != boost) {
					subQuery = new BoostQuery(subQuery, boost);
				}
				queryBuilder.add(subQuery, BooleanClause.Occur.SHOULD);
			}
		}
		return queryBuilder;
	}

	@Override
	public String[] getDefaultHighlightFields() {
		return queryFields.keySet().toArray(new String[0]);
	}

	@Override
	public Query getHighlightQuery() {
		return parsedUserQuery;
	}

	/**
	 * Adds debug info.
	 *
	 * @param debugInfo The debug info
	 */
	@Override
	public void addDebugInfo(final NamedList<Object> debugInfo) {
		super.addDebugInfo(debugInfo);
		debugInfo.add("altquerystring", altUserQuery);
		if (null != boostQueries) {
			debugInfo.add("boost_queries", boostParams);
			debugInfo.add("parsed_boost_queries",
					QueryParsing.toString(boostQueries, getReq().getSchema()));
		}
		debugInfo.add("boostfuncs", getReq().getParams().getParams(DisMaxParams.BF));
	}

	/**
	 * Determines the correct Sort based on the request parameter "sort".
	 *
	 * @param req The request.
	 * @return null if no sort is specified.
	 */
	public static Sort getSort(final SolrQueryRequest req) {

		String sort = req.getParams().get(CommonParams.SORT);
		if (Strings.isNullOrEmpty(sort)) {
			return null;
		}

		Sort sortResult = null;
		try {
			sortResult = SortSpecParsing.parseSortSpec(sort, req).getSort();
		} catch (SolrException e) {
			//do nothing
		}
		return sortResult;
	}

	/**
	 * Builds a list of Query objects that should be used to filter results.
	 *
	 * @param req The request
	 * @return null if no filter queries
	 * @throws SyntaxError if a failure is detected
	 * @see CommonParams#FQ
	 */
	public static List<Query> parseFilterQueries(final SolrQueryRequest req) throws SyntaxError {
		return parseQueryStrings(req, req.getParams().getParams(CommonParams.FQ));
	}

	/**
	 * Turns an array of query strings into a List of Query objects.
	 *
	 * @param req     The request
	 * @param queries The queries
	 * @return null if no queries are generated
	 * @throws SyntaxError if a failure is detected
	 */
	public static List<Query> parseQueryStrings(final SolrQueryRequest req,
	                                            final String[] queries) throws SyntaxError {
		if (null == queries || 0 == queries.length) {
			return null;
		}
		List<Query> out = new LinkedList<>();
		for (String q : queries) {
			if (null != q && 0 != q.trim().length()) {
				out.add(QParser.getParser(q, null, req).getQuery());
			}
		}
		return out;
	}
}
