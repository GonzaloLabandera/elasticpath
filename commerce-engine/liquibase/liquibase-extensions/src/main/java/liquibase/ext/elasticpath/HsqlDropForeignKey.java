/**
 * Copyright (c) Elastic Path Software Inc., 2013
 */
package liquibase.ext.elasticpath;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import liquibase.change.custom.CustomSqlChange;
import liquibase.database.Database;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.CustomChangeException;
import liquibase.exception.DatabaseException;
import liquibase.exception.SetupException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.RawSqlStatement;

/**
 * Drops a foreign key constraint for HSQL databases where a constraint name may have not been used. No rollback
 * support.
 */
public class HsqlDropForeignKey implements CustomSqlChange {

	private String tableName;
	private String columnName;

	/**
	 * Sets the table which has the constraint.
	 * 
	 * @param tableName the table name
	 */
	public void setTableName(final String tableName) {
		this.tableName = tableName;
	}

	/**
	 * Sets the column which has a constraint.
	 * 
	 * @param columnName column name
	 */
	public void setColumnName(final String columnName) {
		this.columnName = columnName;
	}

	@Override
	public SqlStatement[] generateStatements(final Database database) throws CustomChangeException {
		if (!(database.getConnection() instanceof JdbcConnection)) {
			throw new UnexpectedLiquibaseException("Unable to get connection from database");
		}
		JdbcConnection connection = (JdbcConnection) database.getConnection();

		SqlStatement[] result = new SqlStatement[1];
		try (
				Statement stmt = connection.createStatement();
				ResultSet resultSet = stmt.executeQuery(String.format("SELECT FK_NAME " +
					"FROM INFORMATION_SCHEMA.SYSTEM_CROSSREFERENCE " +
					"WHERE FKTABLE_NAME = '%s' AND FKCOLUMN_NAME = '%s'", tableName, columnName))
			) {

				if (!resultSet.next()) {
					throw new CustomChangeException(String.format("Unable to find constraint on table <%s> column <%s>", tableName, columnName));
				}
				
				String foreignkey = resultSet.getString(1);
				result[0] = new RawSqlStatement(String.format("ALTER TABLE %s DROP CONSTRAINT %s", tableName, foreignkey));
		} catch (SQLException | DatabaseException e) {
			throw new CustomChangeException(e);
		}
		
		return result;
	}

	@Override
	public String getConfirmationMessage() {
		return String.format("Dropped foreign key constraint on table <%s> column <%s>", tableName, columnName);
	}

	@Override
	public void setUp() throws SetupException {
		// nothing to setup
	}

	@Override
	public void setFileOpener(final ResourceAccessor resourceAccessor) {
		// not used
	}

	@Override
	public ValidationErrors validate(final Database database) {
		ValidationErrors errors = new ValidationErrors();

		if ("hsqldb".equals(database.getDatabaseProductName())) {
			errors.addError("This change only works with hsqldb");
		}

		return errors;
	}
}
