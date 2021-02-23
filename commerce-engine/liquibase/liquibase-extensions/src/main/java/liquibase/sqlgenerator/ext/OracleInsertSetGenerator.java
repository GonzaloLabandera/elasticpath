/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package liquibase.sqlgenerator.ext;

import java.util.ArrayList;

import liquibase.database.Database;
import liquibase.database.core.OracleDatabase;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.core.InsertGenerator;
import liquibase.sqlgenerator.core.InsertSetGenerator;
import liquibase.statement.core.InsertSetStatement;
import liquibase.statement.core.InsertStatement;

/**
 * Replace {@link InsertSetGenerator} to support Oracle database.
 * It should be in the package "liquibase.sqlgenerator.ext" to automatically registered.
 */
@SuppressWarnings("squid:S1149")
public class OracleInsertSetGenerator extends InsertSetGenerator {
	private InsertGenerator myGenerator = new InsertGenerator();

	/**
	 * Returns priority higher than {@link InsertSetGenerator} to be call before {@link InsertSetGenerator}.
	 *
	 * @return priority value.
	 */
	@Override
	public int getPriority() {
		return super.getPriority() + 2;
	}

	@Override
	public Sql[] generateSql(final InsertSetStatement statement, final Database database, final SqlGeneratorChain sqlGeneratorChain) {
		if (statement.peek() == null) {
			return new UnparsedSql[0];
		}

		if (database instanceof OracleDatabase) {

			StringBuffer sql = new StringBuffer("INSERT ALL ");

			final ArrayList<Sql> result = new ArrayList<>();
			int index = 0;
			int total = 0;
			for (InsertStatement sttmnt : statement.getStatements()) {
				index++;
				total++;
				generateOracleHeader(sql, statement, database);
				myGenerator.generateValues(sql, sttmnt, database);
				if (index > statement.getBatchThreshold()) {
					result.add(completeOracleStatement(statement, sql));

					index = 0;
					sql = total == statement.getStatements().size()
							? new StringBuffer()
							: new StringBuffer("INSERT ALL ");
				}
			}
			result.add(completeOracleStatement(statement, sql));

			return result.toArray(new UnparsedSql[result.size()]);
		}
		return super.generateSql(statement, database, sqlGeneratorChain);
	}

	private void generateOracleHeader(final StringBuffer sql, final InsertSetStatement statement, final Database database) {
		final InsertStatement insert = statement.peek();
		generateHeader(sql, insert, database);
	}

	private Sql completeOracleStatement(final InsertSetStatement statement, final StringBuffer sql) {
		sql.append(" SELECT * FROM DUAL ");
		return new UnparsedSql(sql.toString(), getAffectedTable(statement));
	}

	private void generateHeader(final StringBuffer sql, final InsertStatement statement, final Database database) {
		sql.append(" INTO ")
				.append(database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()))
				.append(" (");
		for (String column : statement.getColumnValues().keySet()) {
			sql.append(database.escapeColumnName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(), column)).append(","
					+ " ");
		}
		sql.deleteCharAt(sql.lastIndexOf(" "));
		int lastComma = sql.lastIndexOf(",");
		if (lastComma >= 0) {
			sql.deleteCharAt(lastComma);
		}

		sql.append(") VALUES ");
	}
}