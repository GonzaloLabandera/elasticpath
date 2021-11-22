/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */

package com.elasticpath.datapopulation.core.changelog;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import liquibase.ContextExpression;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Labels;
import liquibase.change.CheckSum;
import liquibase.change.ColumnConfig;
import liquibase.changelog.AbstractChangeLogHistoryService;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.RanChangeSet;
import liquibase.database.Database;
import liquibase.database.core.MSSQLDatabase;
import liquibase.database.core.SQLiteDatabase;
import liquibase.exception.DatabaseException;
import liquibase.exception.DatabaseHistoryException;
import liquibase.exception.LiquibaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.executor.Executor;
import liquibase.executor.ExecutorService;
import liquibase.snapshot.InvalidExampleException;
import liquibase.snapshot.SnapshotControl;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.AddColumnStatement;
import liquibase.statement.core.CreateDatabaseChangeLogTableStatement;
import liquibase.statement.core.DropTableStatement;
import liquibase.statement.core.GetNextChangeSetSequenceValueStatement;
import liquibase.statement.core.MarkChangeSetRanStatement;
import liquibase.statement.core.ModifyDataTypeStatement;
import liquibase.statement.core.RawSqlStatement;
import liquibase.statement.core.RemoveChangeSetRanStatusStatement;
import liquibase.statement.core.SelectFromDatabaseChangeLogStatement;
import liquibase.statement.core.SetNullableStatement;
import liquibase.statement.core.TagDatabaseStatement;
import liquibase.statement.core.UpdateChangeSetChecksumStatement;
import liquibase.statement.core.UpdateStatement;
import liquibase.structure.core.Column;
import liquibase.structure.core.DataType;
import liquibase.structure.core.Table;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The service class for handling changelog history.
 */
@SuppressWarnings({"PMD.RedundantFieldInitializer", "PMD.GodClass"})
public class StandardChangeLogHistoryService extends AbstractChangeLogHistoryService {
	private static final Logger LOG = LogManager.getLogger(StandardChangeLogHistoryService.class);
	private static final String VARCHAR_LEN_255 = "(255)";
	private static final String DESCRIPTION_COLUMN = "DESCRIPTION";
	private static final String COMMENTS_COLUMN = "COMMENTS";
	private static final String TAG_COLUMN = "TAG";
	private static final String LIQUIBASE_COLUMN = "LIQUIBASE";
	private static final String ORDEREXECUTED_COLUMN = "ORDEREXECUTED";
	private static final String MD5SUM_COLUMN = "MD5SUM";
	private static final String EXECTYPE_COLUMN = "EXECTYPE";

	private static final int PRIORITY = 100;
	private static final int LIQUIBASE_COLUMN_SIZE_LIMIT = 20;
	private static final int MD5SUM_COLUMN_SIZE_LIMIT = 35;

	private List<RanChangeSet> ranChangeSetList;
	private boolean serviceInitialized;
	private Boolean hasDatabaseChangeLogTable;
	private boolean databaseChecksumsCompatible = true;
	private Integer lastChangeSetSequenceValue;
	private boolean isDeploymentIdColumnAdded;

	@Override
	public int getPriority() {
		return PRIORITY;
	}

	@Override
	public boolean supports(final Database database) {
		return true;
	}

	private String getDatabaseChangeLogTableName() {
		return getDatabase().getDatabaseChangeLogTableName();
	}

	private String getLiquibaseSchemaName() {
		return getDatabase().getLiquibaseSchemaName();
	}

	private String getLiquibaseCatalogName() {
		return getDatabase().getLiquibaseCatalogName();
	}

	private boolean canCreateChangeLogTable()  {
		return true;
	}

	@Override
	public void reset() {
		this.ranChangeSetList = null;
		this.serviceInitialized = false;
	}

	private boolean verifyHasDatabaseChangeLogTable() {
		if (hasDatabaseChangeLogTable == null) {
			try {
				hasDatabaseChangeLogTable = SnapshotGeneratorFactory.getInstance().hasDatabaseChangeLogTable(getDatabase());
			} catch (LiquibaseException e) {
				throw new UnexpectedLiquibaseException(e);
			}
		}
		return hasDatabaseChangeLogTable;
	}

	private String getCharTypeName() {
		if (getDatabase() instanceof MSSQLDatabase && ((MSSQLDatabase) getDatabase()).sendsStringParametersAsUnicode()) {
			return "nvarchar";
		}
		return "varchar";
	}

	/**
	 * Initialize class.
	 *
	 * @throws DatabaseException the exception
	 */
	@SuppressWarnings({"unchecked", "PMD.ConfusingTernary", "PMD.NPathComplexity", "PMD.ExcessiveMethodLength"})
	public void init() throws DatabaseException {
		if (serviceInitialized) {
			return;
		}
		Database database = getDatabase();

		Table changeLogTable;
		try {
			changeLogTable = SnapshotGeneratorFactory.getInstance()
					.getDatabaseChangeLogTable(new SnapshotControl(database, false, Table.class, Column.class), database);
		} catch (LiquibaseException e) {
			throw new UnexpectedLiquibaseException(e);
		}

		List<SqlStatement> statementsToExecute = new ArrayList<>();
		Executor executor = ExecutorService.getInstance().getExecutor(database);
		boolean changeLogCreateAttempted = false;
		if (changeLogTable != null) {
			boolean hasDescription = changeLogTable.getColumn(DESCRIPTION_COLUMN) != null;
			boolean hasComments = changeLogTable.getColumn(COMMENTS_COLUMN) != null;
			boolean hasTag = changeLogTable.getColumn(TAG_COLUMN) != null;
			boolean hasLiquibase = changeLogTable.getColumn(LIQUIBASE_COLUMN) != null;
			boolean hasContexts = changeLogTable.getColumn("CONTEXTS") != null;
			boolean hasLabels = changeLogTable.getColumn("LABELS") != null;
			boolean liquibaseColumnNotRightSize = false;
			if (!(this.getDatabase() instanceof SQLiteDatabase)) {
				DataType type = changeLogTable.getColumn(LIQUIBASE_COLUMN).getType();
				if (type.getTypeName().toLowerCase().startsWith("varchar")) {
					Integer columnSize = type.getColumnSize();
					liquibaseColumnNotRightSize = columnSize != null && columnSize < LIQUIBASE_COLUMN_SIZE_LIMIT;
				} else {
					liquibaseColumnNotRightSize = false;
				}
			}
			boolean hasOrderExecuted = changeLogTable.getColumn(ORDEREXECUTED_COLUMN) != null;
			boolean checksumNotRightSize = false;
			if (!(this.getDatabase() instanceof SQLiteDatabase)) {
				DataType type = changeLogTable.getColumn(MD5SUM_COLUMN).getType();
				if (type.getTypeName().toLowerCase().startsWith("varchar")) {
					Integer columnSize = type.getColumnSize();
					checksumNotRightSize = columnSize != null && columnSize < MD5SUM_COLUMN_SIZE_LIMIT;
				} else {
					liquibaseColumnNotRightSize = false;
				}
			}
			boolean hasExecTypeColumn = changeLogTable.getColumn(EXECTYPE_COLUMN) != null;
			String charTypeName = getCharTypeName();

			if (!hasDescription) {
				executor.comment("Adding missing databasechangelog.description column");
				statementsToExecute.add(new AddColumnStatement(getLiquibaseCatalogName(), getLiquibaseSchemaName(), getDatabaseChangeLogTableName(),
						DESCRIPTION_COLUMN, charTypeName + VARCHAR_LEN_255, null));
			}
			if (!hasTag) {
				executor.comment("Adding missing databasechangelog.tag column");
				statementsToExecute.add(new AddColumnStatement(getLiquibaseCatalogName(), getLiquibaseSchemaName(), getDatabaseChangeLogTableName(),
						TAG_COLUMN, charTypeName + VARCHAR_LEN_255, null));
			}
			if (!hasComments) {
				executor.comment("Adding missing databasechangelog.comments column");
				statementsToExecute.add(new AddColumnStatement(getLiquibaseCatalogName(), getLiquibaseSchemaName(),
						getDatabaseChangeLogTableName(), COMMENTS_COLUMN, charTypeName + VARCHAR_LEN_255, null));
			}
			if (!hasLiquibase) {
				executor.comment("Adding missing databasechangelog.liquibase column");
				statementsToExecute.add(new AddColumnStatement(getLiquibaseCatalogName(), getLiquibaseSchemaName(),
						getDatabaseChangeLogTableName(), LIQUIBASE_COLUMN, charTypeName + "(20)", null));
			}
			if (!hasOrderExecuted) {
				executor.comment("Adding missing databasechangelog.orderexecuted column");
				statementsToExecute.add(new AddColumnStatement(getLiquibaseCatalogName(), getLiquibaseSchemaName(),
						getDatabaseChangeLogTableName(), ORDEREXECUTED_COLUMN, "int", null));
				statementsToExecute.add(new UpdateStatement(getLiquibaseCatalogName(), getLiquibaseSchemaName(),
						getDatabaseChangeLogTableName()).addNewColumnValue(ORDEREXECUTED_COLUMN, -1));
				statementsToExecute.add(new SetNullableStatement(getLiquibaseCatalogName(), getLiquibaseSchemaName(),
						getDatabaseChangeLogTableName(), ORDEREXECUTED_COLUMN, "int", false));
			}
			if (checksumNotRightSize) {
				executor.comment("Modifying size of databasechangelog.md5sum column");

				statementsToExecute.add(new ModifyDataTypeStatement(getLiquibaseCatalogName(), getLiquibaseSchemaName(),
						getDatabaseChangeLogTableName(), MD5SUM_COLUMN, charTypeName + "(" + MD5SUM_COLUMN_SIZE_LIMIT + ")"));
			}
			if (liquibaseColumnNotRightSize) {
				executor.comment("Modifying size of databasechangelog.liquibase column");

				statementsToExecute.add(new ModifyDataTypeStatement(getLiquibaseCatalogName(), getLiquibaseSchemaName(),
						getDatabaseChangeLogTableName(), LIQUIBASE_COLUMN, charTypeName + "(" + LIQUIBASE_COLUMN_SIZE_LIMIT + ")"));
			}
			if (!hasExecTypeColumn) {
				executor.comment("Adding missing databasechangelog.exectype column");
				statementsToExecute.add(new AddColumnStatement(getLiquibaseCatalogName(), getLiquibaseSchemaName(),
						getDatabaseChangeLogTableName(), EXECTYPE_COLUMN, charTypeName + "(10)", null));
				statementsToExecute.add(new UpdateStatement(getLiquibaseCatalogName(), getLiquibaseSchemaName(),
						getDatabaseChangeLogTableName()).addNewColumnValue(EXECTYPE_COLUMN, "EXECUTED"));
				statementsToExecute.add(new SetNullableStatement(getLiquibaseCatalogName(), getLiquibaseSchemaName(), getDatabaseChangeLogTableName(),
						EXECTYPE_COLUMN, charTypeName + "(10)", false));
			}

			if (!hasContexts) {
				executor.comment("Adding missing databasechangelog.contexts column");
				statementsToExecute.add(new AddColumnStatement(getLiquibaseCatalogName(), getLiquibaseSchemaName(), getDatabaseChangeLogTableName(),
						"CONTEXTS", charTypeName + VARCHAR_LEN_255, null));
			}
			if (!hasLabels) {
				executor.comment("Adding missing databasechangelog.labels column");
				statementsToExecute.add(new AddColumnStatement(getLiquibaseCatalogName(), getLiquibaseSchemaName(), getDatabaseChangeLogTableName(),
						"LABELS", charTypeName + VARCHAR_LEN_255, null));
			}

			List<Map<String, ?>> md5sumRS = ExecutorService.getInstance().getExecutor(database)
					.queryForList(new SelectFromDatabaseChangeLogStatement(new SelectFromDatabaseChangeLogStatement.ByNotNullCheckSum(),
							new ColumnConfig().setName(MD5SUM_COLUMN)).setLimit(1));

			if (!md5sumRS.isEmpty()) {
				String md5sum = md5sumRS.get(0).get(MD5SUM_COLUMN).toString();
				if (!md5sum.startsWith(CheckSum.getCurrentVersion() + ":")) {
					executor.comment("DatabaseChangeLog checksums are an incompatible version.  "
							+ "Setting them to null so they will be updated on next database update");
					databaseChecksumsCompatible = false;
					statementsToExecute.add(new RawSqlStatement(
							"UPDATE " + getDatabase().escapeTableName(getLiquibaseCatalogName(), getLiquibaseSchemaName(),
									getDatabaseChangeLogTableName()) + " SET " +  getDatabase().escapeObjectName(MD5SUM_COLUMN, Column.class)
									+ " = NULL"));
				}
			}


		} else if (!changeLogCreateAttempted) {
			executor.comment("Create Database Change Log Table");
			SqlStatement createTableStatement = new CreateDatabaseChangeLogTableStatement();
			if (!canCreateChangeLogTable()) {
				throw new DatabaseException("Cannot create " + getDatabase().escapeTableName(getLiquibaseCatalogName(), getLiquibaseSchemaName(),
						getDatabaseChangeLogTableName()) + " table for your getDatabase().\n\n"
						+ "Please construct it manually using the following SQL as a base and re-run Liquibase:\n\n"
						+ createTableStatement);
			}
			// If there is no table in the database for recording change history create one.
			statementsToExecute.add(createTableStatement);
			LOG.info("Creating database history table with name: {}", getDatabase().escapeTableName(getLiquibaseCatalogName(),
					getLiquibaseSchemaName(), getDatabaseChangeLogTableName()));
		}
		executeStatements(statementsToExecute, database, executor);
	}

	private void executeStatements(final List<SqlStatement> statementsToExecute, final Database database, final Executor executor)
			throws DatabaseException {

		for (SqlStatement sql : statementsToExecute) {
			if (SqlGeneratorFactory.getInstance().supports(sql, database)) {
				executor.execute(sql);
				getDatabase().commit();
			} else {
				LOG.info("Cannot run {}  on {} when checking databasechangelog table", sql.getClass().getSimpleName(),
						getDatabase().getShortName());
			}
		}
		serviceInitialized = true;
	}

	@Override
	public void upgradeChecksums(final DatabaseChangeLog databaseChangeLog, final Contexts contexts, final LabelExpression labels)
			throws DatabaseException {
		super.upgradeChecksums(databaseChangeLog, contexts, labels);
		getDatabase().commit();
	}

	/**
	 * Returns the ChangeSets that have been run against the current getDatabase().
	 * @return the list of changesets
	 * @throws DatabaseException the exception
	 */
	@SuppressWarnings({"rawtypes", "PMD.NPathComplexity"})
	public List<RanChangeSet> getRanChangeSets() throws DatabaseException {
		if (this.ranChangeSetList == null) {
			Database database = getDatabase();
			String databaseChangeLogTableName = getDatabase().escapeTableName(getLiquibaseCatalogName(), getLiquibaseSchemaName(),
					getDatabaseChangeLogTableName());
			List<RanChangeSet> ranChangeSetList = new ArrayList<RanChangeSet>();
			if (verifyHasDatabaseChangeLogTable()) {
				LOG.info("Reading from {}", databaseChangeLogTableName);
				List<Map<String, ?>> results = queryDatabaseChangeLogTable(database);

				if (isDeploymentIdColumnMissing(results)) {
					LOG.info("DEPLOYMENT_ID column is missing in {} table.", databaseChangeLogTableName);
					addDeploymentIdColumn(database, databaseChangeLogTableName);
					LOG.info("DEPLOYMENT_ID column is added into {} table.", databaseChangeLogTableName);
				}

				for (Map rs : results) {
					String fileName = rs.get("FILENAME").toString();
					String author = rs.get("AUTHOR").toString();
					String changeLogId = rs.get("ID").toString();
					String md5sum = rs.get(MD5SUM_COLUMN) == null || !databaseChecksumsCompatible ? null : rs.get(MD5SUM_COLUMN).toString();
					String description = rs.get(DESCRIPTION_COLUMN) == null ? null : rs.get(DESCRIPTION_COLUMN).toString();
					String comments = rs.get(COMMENTS_COLUMN) == null ? null : rs.get(COMMENTS_COLUMN).toString();
					Object tmpDateExecuted = rs.get("DATEEXECUTED");
					Date dateExecuted = null;
					if (tmpDateExecuted instanceof Date) {
						dateExecuted = (Date) tmpDateExecuted;
					} else if (tmpDateExecuted instanceof LocalDateTime) {
						dateExecuted = Date.from(((LocalDateTime) tmpDateExecuted).atZone(ZoneId.systemDefault()).toInstant());
					} else {
						DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
						try {
							dateExecuted = dateFormat.parse((String) tmpDateExecuted);
						} catch (ParseException e) {
							// Ignore ParseException and assume dateExecuted == null instead of aborting.
						}
					}
					String tag = rs.get(TAG_COLUMN) == null ? null : rs.get(TAG_COLUMN).toString();
					String execType = rs.get(EXECTYPE_COLUMN) == null ? null : rs.get(EXECTYPE_COLUMN).toString();
					ContextExpression contexts = new ContextExpression((String) rs.get("CONTEXTS"));
					Labels labels = new Labels((String) rs.get("LABELS"));
					String deploymentId = (String) rs.get("DEPLOYMENT_ID");

					try {
						RanChangeSet ranChangeSet = new RanChangeSet(fileName, changeLogId, author, CheckSum.parse(md5sum), dateExecuted, tag,
								ChangeSet.ExecType.valueOf(execType), description, comments, contexts, labels, deploymentId);
						ranChangeSetList.add(ranChangeSet);
					} catch (IllegalArgumentException e) {
						LOG.error("Unknown EXECTYPE from database: {}", execType, e);
						throw e;
					}
				}
			}

			this.ranChangeSetList = ranChangeSetList;
		}
		return Collections.unmodifiableList(ranChangeSetList);
	}

	private boolean isDeploymentIdColumnMissing(final List<Map<String, ?>> results) {
		return !isDeploymentIdColumnAdded && !results.isEmpty() && !results.get(0).containsKey("DEPLOYMENT_ID");
	}

	private void addDeploymentIdColumn(final Database database, final String databaseChangeLogTableName)
			throws DatabaseException {
		try {
			SqlStatement addDeploymentIdColumnStatement = new RawSqlStatement("ALTER TABLE "
					+ databaseChangeLogTableName + " ADD DEPLOYMENT_ID varchar(10) NULL");
			ExecutorService.getInstance().getExecutor(database).execute(addDeploymentIdColumnStatement);
			isDeploymentIdColumnAdded = true;
		} catch (DatabaseException dbe) {
			if (!dbe.getMessage().contains("Duplicate column name 'DEPLOYMENT_ID'")) {
				throw dbe;
			}
		}
	}

	/**
	 * Get a list of db change logs.
	 *
	 * @param database the database instance
	 * @return the list of change logs
	 * @throws DatabaseException the exception
	 */
	public List<Map<String, ?>> queryDatabaseChangeLogTable(final Database database) throws DatabaseException {
		SelectFromDatabaseChangeLogStatement select = new SelectFromDatabaseChangeLogStatement(new ColumnConfig().setName("*")
				.setComputed(true)).setOrderBy("DATEEXECUTED ASC", "ORDEREXECUTED ASC");
		return ExecutorService.getInstance().getExecutor(database).queryForList(select);
	}

	@Override
	protected void replaceChecksum(final ChangeSet changeSet) throws DatabaseException {
		ExecutorService.getInstance().getExecutor(getDatabase()).execute(new UpdateChangeSetChecksumStatement(changeSet));

		getDatabase().commit();
		reset();
	}

	@Override
	public RanChangeSet getRanChangeSet(final ChangeSet changeSet) throws DatabaseException, DatabaseHistoryException {
		if (!verifyHasDatabaseChangeLogTable()) {
			return null;
		}

		return super.getRanChangeSet(changeSet);
	}

	@Override
	public void setExecType(final ChangeSet changeSet, final ChangeSet.ExecType execType) throws DatabaseException {
		Database database = getDatabase();

		ExecutorService.getInstance().getExecutor(database).execute(new MarkChangeSetRanStatement(changeSet, execType));
		getDatabase().commit();
		if (this.ranChangeSetList != null) {
			this.ranChangeSetList.add(new RanChangeSet(changeSet, execType, null, null));
		}

	}

	@Override
	public void removeFromHistory(final ChangeSet changeSet) throws DatabaseException {
		Database database = getDatabase();
		ExecutorService.getInstance().getExecutor(database).execute(new RemoveChangeSetRanStatusStatement(changeSet));
		getDatabase().commit();

		if (this.ranChangeSetList != null) {
			this.ranChangeSetList.remove(new RanChangeSet(changeSet));
		}
	}

	@Override
	public int getNextSequenceValue() throws LiquibaseException {
		if (lastChangeSetSequenceValue == null) {
			if (getDatabase().getConnection() == null) {
				lastChangeSetSequenceValue = 0;
			} else {
				lastChangeSetSequenceValue = ExecutorService.getInstance().getExecutor(getDatabase())
						.queryForInt(new GetNextChangeSetSequenceValueStatement());
			}
		}

		return ++lastChangeSetSequenceValue;
	}

	/**
	 * Tags the database changelog with the given string.
	 */
	@Override
	public void tag(final String tagString) throws DatabaseException {
		Database database = getDatabase();
		Executor executor = ExecutorService.getInstance().getExecutor(database);
		try {
			int totalRows = ExecutorService.getInstance().getExecutor(database)
					.queryForInt(new SelectFromDatabaseChangeLogStatement(new ColumnConfig().setName("COUNT(*)", true)));
			if (totalRows == 0) {
				ChangeSet emptyChangeSet = new ChangeSet(String.valueOf(new Date().getTime()), "liquibase", false, false,
						"liquibase-internal", null, null, getDatabase().getObjectQuotingStrategy(), null);
				this.setExecType(emptyChangeSet, ChangeSet.ExecType.EXECUTED);
			}

			executor.execute(new TagDatabaseStatement(tagString));
			getDatabase().commit();

			if (this.ranChangeSetList != null) {
				ranChangeSetList.get(ranChangeSetList.size() - 1).setTag(tagString);
			}
		} catch (Exception e) {
			throw new DatabaseException(e);
		}
	}

	@Override
	public boolean tagExists(final String tag) throws DatabaseException {
		int count = ExecutorService.getInstance().getExecutor(getDatabase())
				.queryForInt(new SelectFromDatabaseChangeLogStatement(new SelectFromDatabaseChangeLogStatement.ByTag(tag),
						new ColumnConfig().setName("COUNT(*)", true)));
		return count > 0;
	}

	@Override
	public void clearAllCheckSums() throws LiquibaseException {
		Database database = getDatabase();
		UpdateStatement updateStatement = new UpdateStatement(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName(),
				database.getDatabaseChangeLogTableName());
		updateStatement.addNewColumnValue(MD5SUM_COLUMN, null);
		ExecutorService.getInstance().getExecutor(database).execute(updateStatement);
		database.commit();
	}

	@Override
	public void destroy() throws DatabaseException {
		Database database = getDatabase();
		try {
			if (SnapshotGeneratorFactory.getInstance().has(new Table().setName(database.getDatabaseChangeLogTableName())
					.setSchema(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName()), database)) {
				ExecutorService.getInstance().getExecutor(database).execute(new DropTableStatement(database.getLiquibaseCatalogName(),
						database.getLiquibaseSchemaName(), database.getDatabaseChangeLogTableName(), false));
			}
			reset();
		} catch (InvalidExampleException e) {
			throw new UnexpectedLiquibaseException(e);
		}
	}
}