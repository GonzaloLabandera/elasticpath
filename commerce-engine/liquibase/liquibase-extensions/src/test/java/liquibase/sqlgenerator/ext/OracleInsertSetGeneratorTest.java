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
@RunWith(MockitoJUnitRunner.class)
public class OracleInsertSetGeneratorTest {
	private OracleInsertSetGenerator generator;

	/**
	 * Test case setup.
	 */
	@Before
	public void setUp() {
		generator = new OracleInsertSetGenerator();
	}

	/**
	 * Test calculate Import Export configuration path.
	 */
	@Test
	public void testGenerateInsertForOracleDB() {
		final Sql[] insertSatements = generator.generateSql(prepareStatement(), new OracleDatabase(), new SqlGeneratorChain(Collections.emptySortedSet()));
		assertThat(insertSatements[0].toSql())
				.isEqualTo("INSERT ALL  "
						+ "INTO testCatalog.testTable (columnOne, columnTwo) VALUES ('value11', 'value12') "
						+ "INTO testCatalog.testTable (columnOne, columnTwo) VALUES ('value21', 'value22') "
						+ "SELECT * FROM DUAL");
	}

	@Test
	public void testThatPriorityGreaterThanInsertSetStatement() {
		assertThat(generator.getPriority())
				.isGreaterThan(new InsertSetGenerator().getPriority());
	}

	private InsertSetStatement prepareStatement() {
		final String catalog = "testCatalog";
		final String schema = "testSchema";
		final String table = "testTable";

		final InsertStatement statementOne = new InsertStatement(catalog, schema, table);
		statementOne.addColumnValue("columnOne", "value11");
		statementOne.addColumnValue("columnTwo", "value12");

		final InsertStatement statementTwo = new InsertStatement(catalog, schema, table);
		statementTwo.addColumnValue("columnOne", "value21");
		statementTwo.addColumnValue("columnTwo", "value22");

		InsertSetStatement setStatement = new InsertSetStatement(catalog, schema, table);
		setStatement.addInsertStatement(statementOne);
		setStatement.addInsertStatement(statementTwo);

		return setStatement;
	}
}
