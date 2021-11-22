/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */

package com.elasticpath.performance.mojo;

import static com.elasticpath.performance.mojo.utils.Constants.APPLICATION_COL;
import static com.elasticpath.performance.mojo.utils.Constants.COMMIT_HASH_COL;
import static com.elasticpath.performance.mojo.utils.Constants.DATE_EXECUTED_COL;
import static com.elasticpath.performance.mojo.utils.Constants.EPC_VERSION_COL;
import static com.elasticpath.performance.mojo.utils.Constants.TEST_NAME_COL;
import static com.elasticpath.performance.mojo.utils.Constants.TOTAL_DB_DELETES_COL;
import static com.elasticpath.performance.mojo.utils.Constants.TOTAL_DB_INSERTS_COL;
import static com.elasticpath.performance.mojo.utils.Constants.TOTAL_DB_SELECTS_COL;
import static com.elasticpath.performance.mojo.utils.Constants.TOTAL_DB_TIME_COL;
import static com.elasticpath.performance.mojo.utils.Constants.TOTAL_DB_UPDATES_COL;
import static com.elasticpath.performance.mojo.utils.DbQueryUtils.UNIQUE_EPC_VERSIONS_QUERY;
import static com.elasticpath.performance.mojo.utils.FileUtils.readFile;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.TimeZone;

import org.apache.maven.plugin.MojoExecutionException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/** Test class for {@link ExportReleasePerformanceDataMojo}. */
@RunWith(MockitoJUnitRunner.class)
public class ExportReleasePerformanceDataMojoTest {
	private static final long MAGIC_TIMESTAMP = 1619207785372L;

	private ExportReleasePerformanceDataMojo fixture;

	@Mock
	private Connection connection;
	@Mock
	private Statement statement;
	@Mock
	private ResultSet uniqueEPCVersionsResultSet;
	@Mock
	private ResultSet releaseResultsResultSet;
	private String samplesFolderPath;
	private String metabaseFDumpilePath;

	@Before
	public void init() throws SQLException {
		//required when executing on servers with a different TZ. TZ affects getting db timestamps
		// i.e. different values may be obtained depending on TZ
		TimeZone timeZone = TimeZone.getTimeZone("UTC");
		TimeZone.setDefault(timeZone);

		String baseOutputFolderPath = getClass().getClassLoader().getResource(".").getPath();
		this.samplesFolderPath = baseOutputFolderPath + "samples";
		this.metabaseFDumpilePath = this.samplesFolderPath + "/metabase.dump";

		Timestamp timestamp = new Timestamp(MAGIC_TIMESTAMP);

		when(connection.createStatement()).thenReturn(statement);
		when(statement.executeQuery(UNIQUE_EPC_VERSIONS_QUERY)).thenReturn(uniqueEPCVersionsResultSet);
		when(uniqueEPCVersionsResultSet.next()).thenReturn(true, true, true, false);
		when(uniqueEPCVersionsResultSet.getString(1)).thenReturn("v1", "v2", "v3");
		when(uniqueEPCVersionsResultSet.isLast()).thenReturn(false, false, true);

		when(releaseResultsResultSet.next()).thenReturn(true, true, true, false);
		when(releaseResultsResultSet.isLast()).thenReturn(false, false, true);
		when(releaseResultsResultSet.getString(EPC_VERSION_COL)).thenReturn("v1", "v2", "v3");
		when(releaseResultsResultSet.getString(APPLICATION_COL)).thenReturn("app", "app", "app");
		when(releaseResultsResultSet.getString(TEST_NAME_COL)).thenReturn("test", "test", "test");
		when(releaseResultsResultSet.getString(TOTAL_DB_SELECTS_COL)).thenReturn("10", "20", "30");
		when(releaseResultsResultSet.getString(TOTAL_DB_INSERTS_COL)).thenReturn("10", "20", "30");
		when(releaseResultsResultSet.getString(TOTAL_DB_UPDATES_COL)).thenReturn("10", "20", "30");
		when(releaseResultsResultSet.getString(TOTAL_DB_DELETES_COL)).thenReturn("10", "20", "30");
		when(releaseResultsResultSet.getString(TOTAL_DB_TIME_COL)).thenReturn("100", "200", "300");
		when(releaseResultsResultSet.getString(COMMIT_HASH_COL)).thenReturn("hash1", "hash2", "hash3");
		when(releaseResultsResultSet.getTimestamp(DATE_EXECUTED_COL)).thenReturn(timestamp, timestamp, timestamp);

		fixture = new ExportReleasePerformanceDataMojo() {
			protected Connection getConnection() {
				return connection;
			}
		};
		fixture.setPerformanceMetabaseDumpFilePath(this.metabaseFDumpilePath);
	}

	@Test
	public void shouldExportReleaseDataAsH2Dump() throws MojoExecutionException, SQLException, IOException {
		String exportQuery = readFile(samplesFolderPath, "sampleFindUniqueEPCVersionsQuery.txt");
		when(statement.executeQuery(exportQuery)).thenReturn(releaseResultsResultSet);

		fixture.executeMojo();

		File metabaseDumpFile = new File(this.metabaseFDumpilePath);

		assertThat(metabaseDumpFile)
				.exists();

		String actualMetabaseDumpContent = readFile(this.metabaseFDumpilePath);
		String expectedMetabaseDumpContent = readFile(samplesFolderPath, "sampleMetabaseDump.txt");

		assertThat(actualMetabaseDumpContent)
				.isEqualTo(expectedMetabaseDumpContent);

	}

	@Test
	public void shouldThrowExceptionWhenUniqueVersionsCouldNotBeFound() throws SQLException {
		when(uniqueEPCVersionsResultSet.next()).thenReturn(false);

		assertThatThrownBy(() ->fixture.executeMojo())
				.hasMessage("No releases found. Verify that 'ci-release-performance-data-processing' is running properly")
				.isInstanceOf(MojoExecutionException.class);

	}

	@Test
	public void shouldThrowExceptionWhenAllReleaseRecordsCouldNotBeFound() throws SQLException, IOException {
		String exportQuery = readFile(samplesFolderPath, "sampleFindUniqueEPCVersionsQuery.txt");
		when(statement.executeQuery(exportQuery)).thenReturn(releaseResultsResultSet);
		when(releaseResultsResultSet.next()).thenReturn(false);

		assertThatThrownBy(() ->fixture.executeMojo())
				.hasMessage("No release performance results are found. The export failed")
				.isInstanceOf(MojoExecutionException.class);

	}
}
