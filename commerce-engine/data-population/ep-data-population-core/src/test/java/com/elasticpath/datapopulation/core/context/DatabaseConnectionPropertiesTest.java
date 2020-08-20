/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.datapopulation.core.context;

import static com.elasticpath.datapopulation.core.context.DatabaseConnectionProperties.DATA_POPULATION_CREATEDB_URL;
import static com.elasticpath.datapopulation.core.context.DatabaseConnectionProperties.DATA_POPULATION_DATABASE_TYPE_KEY;
import static com.elasticpath.datapopulation.core.context.DatabaseConnectionProperties.DATA_POPULATION_URL;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.datapopulation.core.AbstractDataPopulationTestSetup;
import com.elasticpath.datapopulation.core.utils.ClasspathResourceResolverUtil;

/**
 * Tests the database connection properties helper methods.
 */
@RunWith(MockitoJUnitRunner.class)
public class DatabaseConnectionPropertiesTest extends AbstractDataPopulationTestSetup {

	public static final String DATA_POPULATION_JDBC_DRIVER = "data.population.jdbc.driver";

	private DatabaseConnectionProperties dbConnProperties;

	private final Properties databaseProperties = new Properties();

	@Before
	public void setUp() {
		databaseProperties.setProperty("test1", "test1value");
		databaseProperties.setProperty("test2", "value\\withbackslash");
		databaseProperties.setProperty(DATA_POPULATION_DATABASE_TYPE_KEY, "h2-testing");

		dbConnProperties = new DatabaseConnectionProperties();

		dbConnProperties.setDatabaseProperties(databaseProperties);
		dbConnProperties.setClasspathResolver(new ClasspathResourceResolverUtil());

		dbConnProperties.initialize();
	}

	@Test
	public void testReplaceBackSlash() {

		final Properties dbConnectionProperties = dbConnProperties.getProperties();

		assertThat(dbConnectionProperties.get("test1"))
				.as("Cannot find the value of test1.")
				.isNotNull()
				.isEqualTo("test1value");

		assertThat(dbConnectionProperties.get("test2"))
				.as("The value contains backslash should not be affected.")
				.isNotNull()
				.isEqualTo("value\\withbackslash");

		assertThat(dbConnectionProperties.get(DATA_POPULATION_CREATEDB_URL))
				.as("The url value contains backslash should be replaced to forward slash.")
				.isNotNull()
				.isEqualTo("jdbc:h2:file://url/to/createdb;");

		assertThat(dbConnectionProperties.get(DATA_POPULATION_URL))
				.as("The url value contains backslash should be replaced to forward slash.")
				.isNotNull()
				.isEqualTo("jdbc:h2:file://url/to/connectdb;");
	}

	@Test
	public void testFindProperty() {
		Properties properties = new Properties();

		properties.setProperty("keyOneFromSetOne", "valueOne");
		properties.setProperty("keyTwoFromSetTwo", "valueTwo");
		properties.setProperty("keyThreeFromSetTwo", "valueThree");

		String valueOne = dbConnProperties.findProperty("keyOneFromSetOne", properties);
		String valueTwo = dbConnProperties.findProperty("keyTwoFromSetTwo", properties);
		String valueThree = dbConnProperties.findProperty("keyThreeFromSetTwo", properties);

		assertThat(valueOne)
				.as("Find Property method did not find keyOneFromSetOne")
				.isNotNull();
		assertThat(valueTwo)
				.as("Find Property method did not find keyTwoFromSetTwo")
				.isNotNull();
		assertThat(valueThree)
				.as("Find Property method did not find keyThreeFromSetTwo")
				.isNotNull();
	}

	@Test
	public void testSpecifiedOverrideDatabaseType() {
		Properties properties = new Properties();
		properties.setProperty(DATA_POPULATION_DATABASE_TYPE_KEY, "sqlserver");
		properties.setProperty(DATA_POPULATION_JDBC_DRIVER, "oracle.jdbc.driver.OracleDriver");

		dbConnProperties.getDatabaseTypeProperties(properties);

		String dbType = properties.getProperty(DATA_POPULATION_DATABASE_TYPE_KEY);

		assertThat(dbType)
				.as("The database type is sqlserver has because the override")
				.isEqualTo("sqlserver");
	}
}
