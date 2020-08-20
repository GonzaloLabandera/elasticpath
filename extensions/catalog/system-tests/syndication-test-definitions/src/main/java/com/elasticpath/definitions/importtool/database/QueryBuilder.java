package com.elasticpath.definitions.importtool.database;

import static org.assertj.core.api.Assertions.fail;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.elasticpath.selenium.util.DBConnector;

/**
 * SQL query builder.
 */
public class QueryBuilder extends DBConnector {

	private ResultSet result;

	/**
	 * @param type domain object type
	 * @return the list (list size = amount) of retrieved from database domain object codes.
	 * @throws SQLException if SQL query failed
	 */
	public List<String> selectObjects(final String type) throws SQLException {
		String query;
		String table = "";
		String tableKeyColumn = "CODE";
		List<String> codes = new ArrayList<>();
		switch (type) {
			case "brand":
				table = "TBRAND";
				break;
			case "option":
				table = "TSKUOPTION";
				tableKeyColumn = "OPTION_KEY";
				break;
			case "fieldMetadata":
				table = "TMODIFIERGROUP";
				break;
			case "attribute":
				table = "TATTRIBUTE";
				tableKeyColumn = "ATTRIBUTE_KEY";
				break;
			default:
				fail("Unsupported domain object type");
				break;
		}
		query = "SELECT t." + tableKeyColumn + " FROM " + table + " t";
		if ("attribute".equals(type)) {
			query = query + " WHERE t.ATTRIBUTE_USAGE NOT IN ('4', '5')";
		}
		try {
			result = executeQuery(query);
			while (result.next()) {
				codes.add(result.getString(1));
			}
		} finally {
			closeAll();
		}
		return codes;
	}

	/**
	 * @param code code of domain object which is used to find corresponding projection.
	 * @param type code of projection which is used to find corresponding projection.
	 * @return true if there are 1 or more projections for specified domain object code.
	 * @throws SQLException if SQL query failed
	 */
	public boolean ifProjectionExists(final String code, final String type) throws SQLException {
		String query;
		boolean ifExist = false;
		query = "SELECT COUNT(*) FROM TCATALOGPROJECTIONS t WHERE t.CODE='" + code + "' AND t.TYPE='" + type + "'";
		try {
			result = executeQuery(query);
			if (result.next() && result.getInt(1) >= 1) {
				ifExist = true;
			}
		} finally {
			closeAll();
		}

		return ifExist;
	}
}
