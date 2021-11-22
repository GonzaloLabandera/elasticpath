/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package liquibase.sqlgenerator.ext;

import java.util.ArrayList;

import liquibase.database.Database;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.core.InsertGenerator;
import liquibase.sqlgenerator.core.InsertSetGenerator;
import liquibase.statement.core.InsertSetStatement;
import liquibase.statement.core.InsertStatement;

/**
 * Replace {@link InsertSetGenerator} to fix generating inserts without values.
 * It should be in the package "liquibase.sqlgenerator.ext" to automatically registered.
 */
@SuppressWarnings({"squid:S1149", "rawtypes"})
public class UpdatedInsertSetGenerator extends InsertSetGenerator {
	private InsertGenerator myGenerator = new InsertGenerator();

	/**
	 * Returns priority higher than {@link InsertSetGenerator} to be call before {@link InsertSetGenerator}.
	 *
	 * @return priority value.
	 */
	@Override
	public int getPriority() {
		return super.getPriority() + 1;
	}


	@Override
	public Sql[] generateSql(InsertSetStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {

		if (statement.peek() == null) {
			return new UnparsedSql[0];
		}
		StringBuffer sql = new StringBuffer();
		generateHeader(sql, statement, database);

		ArrayList<Sql> result = new ArrayList<>();
		int index = 0;
		int total = 0;

		for (InsertStatement sttmnt : statement.getStatements()) {
			index++;
			total++;
			myGenerator.generateValues(sql, sttmnt, database);
			sql.append(",");
			if (index > statement.getBatchThreshold()) {
				result.add(completeSQLStatement(statement, sql));

				index = 0;
				sql = new StringBuffer();
				if (total != statement.getStatements().size()) {
					generateHeader(sql, statement, database);
				}
			}
		}
		result.add(completeSQLStatement(statement, sql));

		return result.toArray(new UnparsedSql[result.size()]);
	}

	private Sql completeSQLStatement(final InsertSetStatement statement, final StringBuffer sql) {
		final int commaIndex = sql.lastIndexOf(",");
		if (commaIndex > 0) {
			sql.deleteCharAt(sql.lastIndexOf(","));
			sql.append(";\n");
		}
		return new UnparsedSql(sql.toString(), getAffectedTable(statement));
	}
}
