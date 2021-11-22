/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package liquibase.sqlgenerator.ext;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;

import liquibase.database.core.MySQLDatabase;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.core.InsertSetGenerator;
import liquibase.statement.core.InsertSetStatement;
import liquibase.statement.core.InsertStatement;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests the {@link UpdatedInsertSetGenerator}.
 */
@SuppressWarnings({"unchecked","rawtypes"})
public class UpdatedInsertSetGeneratorTest {
	private static final int BATCH_SIZE = 2;
	private UpdatedInsertSetGenerator generator;

	/**
	 * Test case setup.
	 */
	@Before
	public void setUp() {
		generator = new UpdatedInsertSetGenerator();
	}

	@Test
	public void testGenerateValidSQLIfBatchSizeMultipleOfStatementsSize() {
		final Sql[] insertStatements = generator.generateSql(prepareStatement(BATCH_SIZE), new MySQLDatabase(),
				new SqlGeneratorChain(Collections.emptySortedSet()));

		assertThat(insertStatements[2].toSql()).isEmpty();
	}

	@Test
	public void testThatPriorityGreaterThanInsertSetStatement() {
		assertThat(generator.getPriority())
				.isGreaterThan(new InsertSetGenerator().getPriority());
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
