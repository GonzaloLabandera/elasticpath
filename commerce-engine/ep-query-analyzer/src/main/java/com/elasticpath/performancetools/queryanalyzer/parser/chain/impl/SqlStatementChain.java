/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.performancetools.queryanalyzer.parser.chain.impl;

import static com.elasticpath.performancetools.queryanalyzer.utils.Utils.processMultiLineQueries;

import java.io.BufferedReader;

import com.elasticpath.performancetools.queryanalyzer.beans.JPAQuery;
import com.elasticpath.performancetools.queryanalyzer.beans.Operation;
import com.elasticpath.performancetools.queryanalyzer.beans.QueryStatistics;
import com.elasticpath.performancetools.queryanalyzer.parser.ObjectWrapper;
import com.elasticpath.performancetools.queryanalyzer.parser.chain.AbstractParserChain;
import com.elasticpath.performancetools.queryanalyzer.utils.Markers;
import com.elasticpath.performancetools.queryanalyzer.utils.StatementProcessor;

/**
 * Parser chain that parses SQL query from the provided log line.
 */
public class SqlStatementChain extends AbstractParserChain {

	@Override
	public void parse(final BufferedReader reader,
					  final ObjectWrapper<String> line,
					  final ObjectWrapper<String> jpaLine,
					  final ObjectWrapper<Operation> operation,
					  final QueryStatistics queryStatistics,
					  final ObjectWrapper<JPAQuery> jpaQuery) throws Exception {

		if (isApplicableTo(line.getValue())) {

			final StringBuilder statementBuffer = new StringBuilder(line.getValue());
			final String processedLine = processMultiLineQueries(reader, statementBuffer);

			//there is no JPQL equivalent for INSERTs and an artificial one will be created
			// INSERTs will always appear as the SQL statements and must be handled here
			// there will be always 1 JPQL INSERT per SQL one because the inserta are batched and only those get executed against the db
			line.setValue(processedLine);

			if (isInsert(statementBuffer.toString())) {
				JPAQuery insertJPAQuery = new JPAQuery("BATCH INSERT");
				jpaQuery.setValue(insertJPAQuery);
				operation.getValue().addJPAQuery(jpaQuery.getValue());
			}

			StatementProcessor.processSQLStatementLine(line.getValue(), jpaQuery.getValue(), statementBuffer);
		}

		getNextChain().parse(reader, line, jpaLine, operation, queryStatistics, jpaQuery);
	}

	@Override
	protected boolean isApplicableTo(final String line) {
		return line != null && line.matches(Markers.MULTI_LINE_MATCH_SQL_STATEMENT_MARKER);
	}

	private boolean isInsert(final String statementLines) {
		return statementLines.contains(Markers.SQL_BATCH_STATEMENT_MARKER) || statementLines.contains("INSERT INTO");
	}
}
