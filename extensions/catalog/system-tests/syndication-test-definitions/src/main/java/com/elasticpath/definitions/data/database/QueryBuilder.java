package com.elasticpath.definitions.data.database;

import com.elasticpath.definitions.stateobjects.Projection;
import com.elasticpath.selenium.util.DBConnector;

/**
 * SQL query builder.
 */
public class QueryBuilder extends DBConnector {

	private static final String QUERY_DELIMITER = "', '";
	private static final String UPDATE_QUERY_DELIMITER = "', ";

	/**
	 * Creates projection by inserting a new record in Db.
	 *
	 * @param type               projection type
	 * @param store              a store code
	 * @param code               domain object code
	 * @param version            projection version
	 * @param projectionDateTime projection last modified date time
	 * @param deleted            is deleted flag
	 * @param schemaVersion      projection schema version
	 * @param contentHash        projection content hash
	 * @param content            projection JSON content
	 */
	//	CHECKSTYLE:OFF: checkstyle:too many parameters
	public void createProjection(
			final String type, final String store, final String code, final String version, final String projectionDateTime, final int deleted,
			final String schemaVersion, final String contentHash, final String content) {
		String query;
		query = "INSERT INTO TCATALOGPROJECTIONS (TYPE, STORE, CODE, VERSION, PROJECTION_DATE_TIME, DELETED, SCHEMA_VERSION, CONTENT_HASH, CONTENT) "
				+ "VALUES ('" + type + QUERY_DELIMITER + store + QUERY_DELIMITER + code + QUERY_DELIMITER + version
				+ QUERY_DELIMITER + projectionDateTime + QUERY_DELIMITER
				+ deleted + QUERY_DELIMITER + schemaVersion + QUERY_DELIMITER + contentHash + QUERY_DELIMITER + content + "');";
		executeUpdateQuery(query);
	}

	/**
	 * Updates existing record of option projection.
	 *
	 * @param type               projection type
	 * @param store              a store code
	 * @param code               sku option code
	 * @param version            projection version
	 * @param projectionDateTime projection last modified date time
	 * @param deleted            is deleted flag
	 * @param schemaVersion      projection schema version
	 * @param contentHash        projection content hash
	 * @param content            projection JSON content
	 */
	//	CHECKSTYLE:OFF: checkstyle:too many parameters
	public void updateOptionProjection(final String type,
									   final String store,
									   final String code,
									   final String version,
									   final String projectionDateTime,
									   final int deleted,
									   final String schemaVersion,
									   final String contentHash,
									   final String content) {
		String schema = "NULL";
		String hash = "NULL";
		String projectionContent = "NULL";
		if (!"".equals(schemaVersion)) {
			schema = schemaVersion;
		}
		if (!"".equals(contentHash)) {
			hash = contentHash;
		}
		if (!"".equals(content)) {
			projectionContent = content;
		}
		String query;
		query = "UPDATE TCATALOGPROJECTIONS SET "
				+ "TYPE = '" + type + UPDATE_QUERY_DELIMITER
				+ "STORE = '" + store + UPDATE_QUERY_DELIMITER
				+ "CODE = '" + code + UPDATE_QUERY_DELIMITER
				+ "VERSION = '" + version + UPDATE_QUERY_DELIMITER
				+ "PROJECTION_DATE_TIME = '" + projectionDateTime + UPDATE_QUERY_DELIMITER
				+ "DELETED = '" + deleted + UPDATE_QUERY_DELIMITER
				+ "SCHEMA_VERSION = '" + schema + UPDATE_QUERY_DELIMITER
				+ "CONTENT_HASH = '" + hash + UPDATE_QUERY_DELIMITER
				+ "CONTENT = '" + projectionContent + "'"
				+ "WHERE TYPE = '" + type + "' AND STORE = '" + store + "' AND CODE = '" + code + "';";
		executeUpdateQuery(query);
	}

	/**
	 * Deletes option projection by deleting a record in Db.
	 *
	 * @param store   a store code
	 * @param code    sku option code
	 * @param version projection version
	 */
	public void deleteOptionProjection(final String store, final String code, final String version) {
		String query;
		query = "DELETE FROM TCATALOGPROJECTIONS WHERE"
				+ " TYPE = '" + Projection.OPTION_TYPE + "' AND STORE = '" + store + "' AND CODE = '" + code + "' AND VERSION = '" + version
				+ "'";
		executeUpdateQuery(query);
	}
}
