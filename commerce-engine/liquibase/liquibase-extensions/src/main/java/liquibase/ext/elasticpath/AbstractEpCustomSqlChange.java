/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */

package liquibase.ext.elasticpath;

import liquibase.change.custom.CustomSqlChange;
import liquibase.database.Database;
import liquibase.database.core.MySQLDatabase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.CustomChangeException;
import liquibase.exception.UnexpectedLiquibaseException;
import org.apache.commons.lang3.StringUtils;

/**
 * Since MySQL 8.0, column names that are actually keywords (NAME, SYSTEM, ROW_NUMBER) or have special characters, must be quoted with backticks (`).
 * MySQL 5.7 doesn't enforce this rule and it works equally fine with or without backticks.
 *
 * This class provides common methods for backticking both column and table names, when required and depending on the db.
 *
 * The column and table names will not be quoted for other dbs because not all dbs accept backticks as quoting character.
 */
public abstract class AbstractEpCustomSqlChange implements CustomSqlChange {
	private static final char BACKTICK_CHAR = '`';

	private char dbEngineColumnQuotingChar = ' ';
	protected JdbcConnection connection;

	/**
	 * Initialize connection instance and db column quoting character.
	 * For all databases, except MySQL, the quoting character will be empty, meaning that quoting of column names is not required.
	 *
	 * @param database the database object
	 * @throws CustomChangeException the exception
	 */
	protected void init(final Database database) throws CustomChangeException {
		if (!(database.getConnection() instanceof JdbcConnection)) {
			throw new UnexpectedLiquibaseException("Unable to get connection from database");
		}
		connection = (JdbcConnection) database.getConnection();

		try {
			connection.setAutoCommit(false);
		} catch (Exception e) {
			throw new CustomChangeException(e);
		}

		if (database.getClass().isAssignableFrom(MySQLDatabase.class)) {
			dbEngineColumnQuotingChar = BACKTICK_CHAR;
		}
	}

	/**
	 * Backtick all column names, provided as a comma-separated value, if required.
	 *
	 * @param unquotedCsvColumnNames the comma-separated list of column names
	 * @return For MySQL, the list like "ID, SOME_FIELD" will be returned as "`ID`, `SOME_FIELD`".
	 * For other databases, the original list will be returned
	 */
	protected String quoteColumnNames(final String unquotedCsvColumnNames) {
		if (dbEngineColumnQuotingChar == ' ') {
			return unquotedCsvColumnNames;
		}

		String[] columnNames = unquotedCsvColumnNames.split(",");
		for (int i = 0; i < columnNames.length; i++) {
			columnNames[i] = StringUtils.wrap(columnNames[i].trim(), dbEngineColumnQuotingChar);
		}

		return String.join(",", columnNames);
	}

	/**
	 * For MySQL db, quote the table name with backticks.
	 * For other dbs, the original table name will be returned.
	 *
	 * @param unquotedTableName unquoted table name
	 * @return backticked table name for MySQL or original for other dbs.
	 */
	protected String quoteTableName(final String unquotedTableName) {
		if (dbEngineColumnQuotingChar == ' ') {
			return unquotedTableName;
		}

		return StringUtils.wrap(unquotedTableName, dbEngineColumnQuotingChar);
	}

}
