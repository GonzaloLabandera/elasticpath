/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package liquibase.ext.elasticpath.migration.modifierfields;


import liquibase.change.custom.CustomSqlChange;
import liquibase.database.Database;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.CustomChangeException;
import liquibase.exception.DatabaseException;
import liquibase.exception.SetupException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.exception.ValidationErrors;
import liquibase.ext.elasticpath.migration.modifierfields.migrators.DataTableMigrator;
import liquibase.logging.LogFactory;
import liquibase.logging.Logger;
import liquibase.resource.ResourceAccessor;
import liquibase.statement.SqlStatement;
/**
 * Migrate modifier fields either from *DATA tables or a custom LOB field.
 *
 * E.g. TORDERDATA -> TORDER.MODIFIER_FIELDS
 * E.g. TORDER.CUSTOM_ORDER_DATA -> TORDER.MODIFIER_FIELDS
 *
 * In case that a LOB field, with a different name, already exists in the parent table, the data will be
 * moved to MODIFIER_FIELDS and the column DELETED. The custom field needs to be removed from the extension code and custom queries updated
 * accordingly.
 */
public class ClobModifierFields implements CustomSqlChange {
	private static final Logger LOG = LogFactory.getInstance().getLog();

	/** Default batch size. */
	public static final int DEFAULT_BATCH_SIZE = 1000;

	private String tableName;

	public void setTableName(final String tableName) {
		this.tableName = tableName;
	}

	@Override
	public SqlStatement[] generateStatements(final Database database) throws CustomChangeException {
		if (!(database.getConnection() instanceof JdbcConnection)) {
			throw new UnexpectedLiquibaseException("Unable to get connection from database");
		}

		JdbcConnection connection = (JdbcConnection) database.getConnection();

		try (DataTableMigrator migrator =  new DataTableMigrator(connection, tableName)) {
			connection.setAutoCommit(false);

			migrator.migrate();
			migrator.printStats();

		} catch (Exception e) {
			try {
				connection.rollback();
			} catch (DatabaseException dbexc) {
				LOG.severe("Can't rollback transaction", dbexc);
			}

			throw new CustomChangeException(e);
		}

		return new SqlStatement[0];
	}

	@Override
	public String getConfirmationMessage() {
		return "Finished updating TORDERSKU table";
	}

	@Override
	public void setUp() throws SetupException {
		// no setup
	}

	@Override
	public void setFileOpener(final ResourceAccessor resourceAccessor) {
		// not used
	}

	@Override
	public ValidationErrors validate(final Database database) {
		return null; // no validation needed
	}
}
