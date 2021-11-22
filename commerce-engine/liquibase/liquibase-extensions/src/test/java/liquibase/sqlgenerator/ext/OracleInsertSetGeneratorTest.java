/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package liquibase.sqlgenerator.ext;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;

import liquibase.database.core.OracleDatabase;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.core.InsertSetGenerator;
import liquibase.statement.core.InsertSetStatement;
import liquibase.statement.core.InsertStatement;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Tests the {@link OracleInsertSetGenerator}.
 */
@SuppressWarnings({"unchecked","rawtypes"})
@RunWith(MockitoJUnitRunner.class)
public class OracleInsertSetGeneratorTest {
	private static final int BATCH_SIZE = 2;
	private OracleInsertSetGenerator generator;

	/**
	 * Test case setup.
	 */
	@Before
	public void setUp() {
		generator = new OracleInsertSetGenerator();
	}

	@Test
	public void testGenerateInsertsForOracleDBWithGivenBatchSize() {
		final Sql[] insertStatements = generator.generateSql(prepareStatement(BATCH_SIZE), new OracleDatabase(),
				new SqlGeneratorChain(Collections.emptySortedSet()));
		assertThat(insertStatements.length)
				.isEqualTo(3);

		assertThat(insertStatements[0].toSql())
				.isEqualTo("INSERT ALL  "
						+ "INTO testCatalog.testTable (columnOne, columnTwo) VALUES ('value11', 'value12') "
						+ "INTO testCatalog.testTable (columnOne, columnTwo) VALUES ('value21', 'value22') "
						+ "INTO testCatalog.testTable (columnOne, columnTwo) VALUES ('value21', 'value22') SELECT * FROM DUAL");
		assertThat(insertStatements[1].toSql())
				.isEqualTo("INSERT ALL  "
						+ "INTO testCatalog.testTable (columnOne, columnTwo) VALUES ('value21', 'value22') "
						+ "INTO testCatalog.testTable (columnOne, columnTwo) VALUES ('value21', 'value22') "
						+ "INTO testCatalog.testTable (columnOne, columnTwo) VALUES ('value21', 'value22') SELECT * FROM DUAL");
	}

	@Test
	public void testGenerateValidSQLIfBatchSizeMultipleOfStatementsSize() {
		final Sql[] insertStatements = generator.generateSql(prepareStatement(BATCH_SIZE), new OracleDatabase(),
				new SqlGeneratorChain(Collections.emptySortedSet()));

		assertThat(insertStatements[2].toSql())
				.isEqualTo("SELECT * FROM DUAL");

	}

	@Test
	public void testThatPriorityGreaterThanInsertSetStatement() {
		assertThat(generator.getPriority())
				.isGreaterThan(new InsertSetGenerator().getPriority());
	}

	@Test
	public void testThatPriorityGreaterThanUpdatedInsertSetStatement() {
		assertThat(generator.getPriority())
				.isGreaterThan(new UpdatedInsertSetGenerator().getPriority());
	}

	private InsertSetStatement prepareStatement(final int batchSize) {
		final String catalog = "testCatalog";
		final String schema = "testSchema";
		final String table = "testTable";

		final InsertStatement statementOne = new InsertStatement(catalog, schema, table);
		statementOne.addColumnValue("columnOne", "value11");
		statementOne.addColumnValue("columnTwo", "value12");

		final InsertStatement statementTwo = new InsertStatement(catalog, schema, table);
		statementTwo.addColumnValue("columnOne", "value21");
		statementTwo.addColumnValue("columnTwo", "value22");

		InsertSetStatement setStatement = new InsertSetStatement(catalog, schema, table, batchSize);
		setStatement.addInsertStatement(statementOne);
		setStatement.addInsertStatement(statementTwo);
		setStatement.addInsertStatement(statementTwo);
		setStatement.addInsertStatement(statementTwo);
		setStatement.addInsertStatement(statementTwo);
		setStatement.addInsertStatement(statementTwo);

		return setStatement;
	}
}
