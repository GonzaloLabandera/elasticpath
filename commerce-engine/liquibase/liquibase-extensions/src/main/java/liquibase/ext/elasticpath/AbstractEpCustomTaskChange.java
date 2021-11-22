/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */

package liquibase.ext.elasticpath;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import liquibase.change.custom.CustomTaskChange;
import liquibase.database.AbstractJdbcDatabase;
import liquibase.database.Database;
import liquibase.database.core.H2Database;
import liquibase.database.core.MySQLDatabase;
import liquibase.database.core.OracleDatabase;
import liquibase.database.core.PostgresDatabase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.CustomChangeException;
import liquibase.exception.DatabaseException;
import liquibase.exception.SetupException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.exception.ValidationErrors;
import liquibase.logging.LogService;
import liquibase.logging.Logger;
import liquibase.resource.ResourceAccessor;
import org.apache.commons.lang3.time.StopWatch;

/**
 * Abstract Liquibase custom task that provides all required methods that future tasks may need.
 *
 * It provides the information about underlying database and a general execution workflow.
 */
public abstract class AbstractEpCustomTaskChange implements CustomTaskChange {
	private static final Logger LOG = LogService.getLog(AbstractEpCustomTaskChange.class);

	private final StopWatch stopWatch = new StopWatch();

	private static final int MAX_BATCH_SIZE = 1000;
	protected int batchSize = MAX_BATCH_SIZE;

	private boolean isMySQLDatabase;
	private boolean isOracleDatabase;
	private boolean isPostgresDatabase;
	private boolean isH2Database;
	private int totalProcessedRecords;
	private long totalRecordsToProcess;
	private Database database;
	private JdbcConnection connection;
	private String confirmationMessage;

	private void init(final Database database) throws CustomChangeException {
		if (!(database.getConnection() instanceof JdbcConnection)) {
			throw new UnexpectedLiquibaseException("Unable to get database connection");
		}
		this.connection = (JdbcConnection) database.getConnection();

		try {
			this.connection.setAutoCommit(false);
		} catch (Exception e) {
			throw new CustomChangeException(e);
		}

		this.database = database;
		this.isMySQLDatabase = isDbClassAssignableFrom(MySQLDatabase.class);
		this.isOracleDatabase = isDbClassAssignableFrom(OracleDatabase.class);
		this.isPostgresDatabase = isDbClassAssignableFrom(PostgresDatabase.class);
		this.isH2Database = isDbClassAssignableFrom(H2Database.class);

		stopWatch.start();
		LOG.info("Liquibase task [" + getClass().getName() + "] started..");
	}

	@Override
	public void execute(final Database database) throws CustomChangeException {
		init(database);

		try {
			setTotalRecordsToProcess();

			if (totalRecordsToProcess > 0) {
				process();
			}
		} catch (Exception e) {
			try {
				connection.rollback();
			} catch (DatabaseException dbexc) {
				LOG.severe("Can't rollback transaction", dbexc);
			}
			throw new CustomChangeException(e);
		} finally {
			finalizeTask();
		}
	}

	@Override
	public String getConfirmationMessage() {
		return String.format(confirmationMessage, totalProcessedRecords);
	}

	@Override
	public void setUp() throws SetupException {
		//do nothing
	}

	@Override
	public void setFileOpener(final ResourceAccessor resourceAccessor) {
		//do nothing
	}

	@Override
	public ValidationErrors validate(final Database database) {
		return null;
	}

	/**
	 * @return true if underlying db is MySQL
	 */
	protected boolean isMySQLDatabase() {
		return isMySQLDatabase;
	}

	/**
	 * @return true if underlying db is Oracle
	 */
	protected boolean isOracleDatabase() {
		return isOracleDatabase;
	}

	/**
	 * @return true if underlying db is PostgreSQL
	 */
	protected boolean isPostgresDatabase() {
		return isPostgresDatabase;
	}

	/**
	 * @return true if underlying db is H2
	 */
	protected boolean isH2Database() {
		return isH2Database;
	}

	/**
	 * Increment counter for counting total number of processed records.
	 * @param increment the value to increment the counter by
	 */
	protected void incrementTotalProcessedRecords(final int increment) {
		totalProcessedRecords += increment;
	}

	/**
	 * Close given prepared statement.
	 *
	 * @param preparedStatement the statement to close.
	 */
	protected void closePreparedStatement(final PreparedStatement preparedStatement) {
		if (preparedStatement != null) {
			try {
				preparedStatement.close();
			} catch (final Exception exception) {
				LOG.warning("Exception during closing statement", exception);
			}
		}
	}

	private void finalizeTask() {
		stopWatch.stop();
		if (totalRecordsToProcess == 0) {
			LOG.info("Nothing to process. Skipping");
		} else {
			cleanup();
			LOG.info("Processed [" + totalProcessedRecords + "] of [" + totalRecordsToProcess + "] records");
		}

		LOG.info("Liquibase task [" + getClass().getName() + "] completed in [" + stopWatch.getTime() + "] ms.");
	}

	private void setTotalRecordsToProcess() throws CustomChangeException {
		try (PreparedStatement selectTotalCountOfRecordsToProcessStatement = connection.prepareStatement(getTotalCountOfRecordsQuery())) {
			try (ResultSet resultSet = selectTotalCountOfRecordsToProcessStatement.executeQuery()) {
				if (resultSet.next()) {
					totalRecordsToProcess = resultSet.getLong(1);
				}
			}
		} catch (Exception e) {
			throw new CustomChangeException("Exception occurred while getting the total count of records to process", e);
		}
	}

	private boolean isDbClassAssignableFrom(final Class<? extends AbstractJdbcDatabase> fromClass) {
		return this.database.getClass().isAssignableFrom(fromClass);
	}

	public Database getDatabase() {
		return database;
	}

	public JdbcConnection getConnection() {
		return connection;
	}

	// abstract methods

	/**
	 * Perform final cleanup.
	 */
	protected abstract void cleanup();

	/**
	 * Use this method to implement the main processing logic.
	 *
	 * @throws CustomChangeException the exception.
	 */
	protected abstract void process() throws CustomChangeException;

	/**
	 * The query name used for getting the total number of records for processing.
	 *
	 * @return the query
	 */
	protected abstract String getTotalCountOfRecordsQuery();

	protected void setConfirmationMessage(String confirmationMessage) {
		this.confirmationMessage = confirmationMessage;
	}

	protected String getSelectQueryWithOffsetAndLimit(final String selectQuery, final int offset) throws DatabaseException, CustomChangeException {
		String formattedLimit;
		if (isMySQLDatabase() || isH2Database()) {
			formattedLimit = " LIMIT %d, %d";
		} else if (isOracleDatabase()) {
			formattedLimit = " OFFSET %d ROWS FETCH NEXT %d ROWS ONLY";
		} else if (isPostgresDatabase()) {
			formattedLimit = " OFFSET %d LIMIT %d";
		} else {
			throw new CustomChangeException("Unsupported database [" + getConnection().getDatabaseProductName() + "]");
		}

		return selectQuery + String.format(formattedLimit, offset, batchSize);
	}
}