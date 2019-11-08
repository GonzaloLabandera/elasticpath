/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.persistence.openjpa.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.FlushModeType;
import javax.persistence.Query;

import org.apache.commons.lang3.StringUtils;
import org.apache.openjpa.persistence.OpenJPAQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elasticpath.persistence.api.EpPersistenceException;
import com.elasticpath.persistence.api.FlushMode;
import com.elasticpath.persistence.openjpa.QueryParameterEscaper;
import com.elasticpath.persistence.openjpa.impl.QueryParameterEscaperImpl;

/**
 * A util class for query-related operations.
 */
public class QueryUtil {

	private static final Logger LOG = LoggerFactory.getLogger(QueryUtil.class);
	
	private static final int EXPRESSION_ESTIMATE_LENGTH = 8;
	private static final int MAX_ALLOW_EXPRESSIONS_IN_QUERY = 900;
	private static final String NAMED_PARAMETER_PREFIX = ":";
	private static final QueryParameterEscaper PARAMETER_ESCAPER = new QueryParameterEscaperImpl();

	/**
	 * Get query results.
	 *
	 * @param query the query.
	 * @param <T> the entity type.
	 * @return the results.
	 */
	@SuppressWarnings("unchecked")
	public <T> List<T> getResults(final Query query) {
		return new ArrayList<>((List<T>) query.getResultList());
	}

	/**
	 * Set an array of parameters to a given query.
	 *
	 * @param query the query.
	 * @param parameters the array of parameters.
	 */
	public void setQueryParameters(final Query query, final Object[] parameters) {
		if (parameters == null) {
			return;
		}

		for (int i = 0; i < parameters.length; i++) {
			query.setParameter(i + 1, parameters[i]);
		}
	}

	/**
	 * Set a map with named parameters to a given query.
	 *
	 * @param query the query.
	 * @param parameters the map with named parameters.
	 */
	public void setQueryParameters(final Query query, final Map<String, ?> parameters) {
		if (parameters == null) {
			return;
		}

		for (Map.Entry<String, ?> entry : parameters.entrySet()) {
			query.setParameter(entry.getKey(), entry.getValue());
		}
	}

	/**
	 * Convert {@link FlushMode} to {@link FlushModeType}.
	 *
	 * @param flushMode the flush mode.
	 * @return the flush mode type.
	 */
	public FlushModeType toFlushModeType(final FlushMode flushMode) {
		if (flushMode == FlushMode.AUTO) {
			return FlushModeType.AUTO;
		} else if (flushMode == FlushMode.COMMIT) {
			return FlushModeType.COMMIT;
		}

		throw new EpPersistenceException("Unknown flush mode: " + flushMode);
	}

	/**
	 * Create a named {@link Query} instance using given {@link EntityManager} and the query name.
	 *
	 * @param entityManager the entity manager.
	 * @param queryName the query name.
	 * @return an instance of named query.
	 */
	public Query createNamedQuery(final EntityManager entityManager, final String queryName) {
		LOG.debug("Creating named query for query name {} ", queryName);
		return entityManager.createNamedQuery(queryName);
	}

	/**
	 * Split a collection of values into batches since some databases impose a restriction on number of elements that can be set in the IN clause.
	 * Also, it is advisable to split huge collections to avoid large result sets.
	 *
	 * @param values the collection of values to split.
	 * @param parameterCount the number of parameters.
	 * @param <V> value type.
	 * @return the list of strings containing a group of values
	 */
	public  <V> List<String> splitCollection(final Collection<V> values, final int parameterCount) {
		// Try to get the most appropriate buffer size to avoid buffer expanding.
		int bufferSize;
		if (values.size() > MAX_ALLOW_EXPRESSIONS_IN_QUERY - parameterCount) {
			bufferSize = MAX_ALLOW_EXPRESSIONS_IN_QUERY * EXPRESSION_ESTIMATE_LENGTH;
		} else {
			bufferSize = values.size() * EXPRESSION_ESTIMATE_LENGTH;
		}
		final StringBuilder sbf = new StringBuilder(bufferSize);

		final List<String> result = new ArrayList<>();
		int cursor = 0;
		for (V value : values) {
			cursor++;
			if (value instanceof String) {
				value = escapeParameter(value);
				sbf.append('\'').append(value).append("\',");
			} else {
				sbf.append(value).append(',');
			}
			if (cursor >= MAX_ALLOW_EXPRESSIONS_IN_QUERY) {
				sbf.deleteCharAt(sbf.length() - 1);
				result.add(sbf.toString());
				sbf.delete(0, sbf.length());
				cursor = 0;
			}
		}

		if (sbf.length() > 0) {
			sbf.deleteCharAt(sbf.length() - 1);
			result.add(sbf.toString());
		}

		return result;
	}

	/**
	 * Creates a string of single-quoted and comma-separated values, used in IN clause.
	 *
	 * E.g. IN ('a' , 'b', 'c')
	 * @param collection the collection of values to be processed.
	 * @return a string of single-quoted and comma-separated values
	 */
	public String getInParameterValues(final Collection<?> collection) {
		StringBuilder result = new StringBuilder();
		for (Object item : collection) {
			boolean isString = item instanceof String;
			if (isString) {
				result.append('\'');
			}
			result.append(escapeParameter(item));
			if (isString) {
				result.append('\'');
			}
			result.append(',');
		}
		return StringUtils.chop(result.toString());
	}

	/**
	 * Replace list parameter with a string, with comma-separated values, in a given query.
	 *
	 * @param namedQuery the named query.
	 * @param listParameterName the list parameter name to find and replace with  values.
	 * @param listValues the string with comma-separated values.
	 * @return a new instance of {@link Query} with inserted parameters.
	 */
	@SuppressWarnings("rawtypes")
	public Query insertListIntoQuery(final OpenJPAQuery namedQuery, final String listParameterName, final String listValues) {

		String queryString = namedQuery.getQueryString();

		StringBuilder queryStringBuilder = new StringBuilder(queryString);
		String stringToReplace = NAMED_PARAMETER_PREFIX + listParameterName;

		int idxForReplacement = queryStringBuilder.indexOf(stringToReplace);

		if (idxForReplacement == -1) {
			throw new IllegalArgumentException("Parameter " + listParameterName + " does not exist as a named parameter in ["
				+ queryString + "]");
		}
		queryStringBuilder.replace(idxForReplacement, idxForReplacement + stringToReplace.length(), listValues);
		return namedQuery.getEntityManager().createQuery(queryStringBuilder.toString());
	}

	/**
	 * If parameter is a String, it will escape single quotes, if any.
	 * Otherwise, the parameter will be returned as -is.
	 *
	 * @param parameter the parameter to escape.
	 * @param <P> the parameter type.
	 * @return possibly escaped parameter.
	 */
	@SuppressWarnings("unchecked")
	public <P> P escapeParameter(final P parameter) {
		if (parameter instanceof String) {
			return (P) PARAMETER_ESCAPER.escapeStringParameter((String) parameter);
		}
		return parameter;
	}

	/**
	 * Return entity class name either from Entity#name() or Class#getSimpleName().
	 *
	 * @param entityClass the class to obtain the name from
	 * @return String representing a class name
	 */
	public String getEntityClassName(final Class<?> entityClass) {
		String entityClassName = "";

		Annotation entityAnnotation = entityClass.getDeclaredAnnotation(Entity.class);

		if (entityAnnotation != null) { //sanity check - entity classes must be annotated with @Entity
			try {
				Method nameMethod = entityAnnotation.getClass().getDeclaredMethod("name");
				entityClassName = nameMethod.invoke(entityAnnotation).toString();

			} catch (Exception e) {
				//do nothing
			}
		}

		if (StringUtils.isBlank(entityClassName)) {
			entityClassName = entityClass.getSimpleName();
		}

		return entityClassName;
	}
}
