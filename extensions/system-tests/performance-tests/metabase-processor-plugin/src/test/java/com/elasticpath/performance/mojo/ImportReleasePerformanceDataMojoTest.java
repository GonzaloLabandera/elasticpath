/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */

package com.elasticpath.performance.mojo;

import static com.elasticpath.performance.mojo.utils.FileUtils.readFile;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.maven.plugin.MojoExecutionException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/** Test class for {@link ImportReleasePerformanceDataMojo}. */
@RunWith(MockitoJUnitRunner.class)
public class ImportReleasePerformanceDataMojoTest {
	private static final long MAGIC_3L = 3L;

	private ImportReleasePerformanceDataMojo fixture;

	@Mock
	private Connection connection;
	@Mock
	private Statement statement;
	@Mock
	private ResultSet resultSet;
	private String samplesFolderPath;

	@Before
	public void init() throws SQLException {
		String baseOutputFolderPath = getClass().getClassLoader().getResource(".").getPath();
		this.samplesFolderPath = baseOutputFolderPath + "samples";

		when(connection.createStatement()).thenReturn(statement);

		fixture = new ImportReleasePerformanceDataMojo() {
			protected Connection getConnection() {
				return connection;
			}

			protected String getJdbcUrl() {
				return "jdbcURL";
			}
		};
		fixture.setPerformanceMetabaseDumpFilePath(this.samplesFolderPath + "/sampleMetabaseDump.txt");
	}

	@Test
	public void shouldImportRecordsFromExportDump() throws MojoExecutionException, SQLException, IOException {
		String exportDump = readFile(samplesFolderPath, "sampleMetabaseDump.txt");
		String verifyQuery = "SELECT COUNT(*) FROM \"PUBLIC\".\"cucumber_performance_results\"";

		when(statement.executeQuery(verifyQuery)).thenReturn(resultSet);
		when(resultSet.next()).thenReturn(true, false);
		when(resultSet.getLong(1)).thenReturn(MAGIC_3L);

		fixture.executeMojo();

		verify(statement).execute(exportDump);
	}

	@Test
	public void shouldThrowExceptionWhenDumpCanNotBeImported() throws MojoExecutionException, SQLException, IOException {
		String verifyQuery = "SELECT COUNT(*) FROM \"PUBLIC\".\"cucumber_performance_results\"";

		when(statement.executeQuery(verifyQuery)).thenThrow(new SQLException("Invalid dump file"));

		assertThatThrownBy(() ->fixture.executeMojo())
				.hasMessage("Invalid dump file")
				.isInstanceOf(MojoExecutionException.class);

		verifyZeroInteractions(resultSet);
	}

	@Test
	public void shouldThrowExceptionWhenDumpIsImportedButNoRecordsAreFound() throws MojoExecutionException, SQLException, IOException {
		String exportDump = readFile(samplesFolderPath, "sampleMetabaseDump.txt");
		String verifyQuery = "SELECT COUNT(*) FROM \"PUBLIC\".\"cucumber_performance_results\"";

		when(statement.executeQuery(verifyQuery)).thenReturn(resultSet);
		when(resultSet.next()).thenReturn(false);

		assertThatThrownBy(() ->fixture.executeMojo())
				.hasMessage("The import of the OOTB release performance results has failed. The database [jdbcURL] is empty")
				.isInstanceOf(MojoExecutionException.class);

		verify(statement).execute(exportDump);
	}
}
