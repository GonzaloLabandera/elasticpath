/**
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package liquibase.ext.elasticpath;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import liquibase.database.Database;
import liquibase.exception.CustomChangeException;
import liquibase.exception.DatabaseException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;
import liquibase.statement.SqlStatement;

/**
 * Custom liquibase change that populate a closure table
 */
public class PopulateCustomerClosure extends AbstractEpCustomSqlChange  {

	private static final String CLOSURETABLE = "TCUSTOMERCLOSURE";
	private static final String TABLE_JPA_GENERATED_KEYS = "JPA_GENERATED_KEYS";
	private static final String JPA_GENERATED_COLUMNS = "ID, LAST_VALUE";

	private static final String SELECT_CUSTOMERS_WITH_PARENT = "SELECT GUID, PARENT_CUSTOMER_GUID FROM TCUSTOMER WHERE PARENT_CUSTOMER_GUID IS NOT NULL";
	private static final String SELECT_CUSTOMER = "SELECT GUID, PARENT_CUSTOMER_GUID FROM TCUSTOMER WHERE GUID = ?";
	private static final String SELECT_DUPLICATE_ANCESTOR = "SELECT ANCESTOR_GUID, ? AS DESCENDANT_GUID, ANCESTOR_DEPTH FROM TCUSTOMERCLOSURE "
			+ "WHERE DESCENDANT_GUID = ?";
	private static final String ANCESTOR_DEPTH = "SELECT MAX(ANCESTOR_DEPTH) + 1 FROM TCUSTOMERCLOSURE WHERE DESCENDANT_GUID = ?";
	private static final String INSERT_CLOSURE = "INSERT INTO TCUSTOMERCLOSURE(UIDPK, ANCESTOR_GUID, DESCENDANT_GUID, ANCESTOR_DEPTH) "
			+ "VALUES (?, ?, ?, ?)";
	private static final String INSERT_JPA_GENERATED_KEYS = "INSERT INTO %s (%s) VALUES (?, ?)";
	private static final String GUID = "GUID";
	private static final String PARENT_CUSTOMER_GUID = "PARENT_CUSTOMER_GUID";
	private static final String ACCOUNT_CHILDREN_GUIDS_BY_PARENT_GUIDS = "SELECT C.GUID FROM TCUSTOMER C WHERE C.PARENT_CUSTOMER_GUID = ?";

	private static final int UID_INDEX = 1;
	private static final int PARENT_GUID_INDEX = 2;
	private static final int CHILD_GUID_INDEX = 3;
	private static final int DEPTH_INDEX = 4;

	private PreparedStatement selectCustomer;
	private PreparedStatement selectCustomersWithParentPreparedStatement;
	private PreparedStatement selectDuplicateAncestorPreparedStatement;
	private PreparedStatement ancestorDepthPreparedStatement;
	private PreparedStatement insertClosurePreparedStatement;
	private PreparedStatement insertJpaGeneratedKeys;
	private PreparedStatement selectCustomerByParent;

	private long lastUid;
	private long closureCount;

	@Override
	public SqlStatement[] generateStatements(Database database) throws CustomChangeException {
		super.init(database);

		try {

			createPreparedStatements();

			populateClosureTable();

			updateJpaGeneratedKeys(closureCount);

			connection.commit();
		} catch (Exception e) {
			throw new CustomChangeException(e);
		}

		return new SqlStatement[0];
	}

	private void populateClosureTable() throws SQLException {
		List<String> customerRootAccounts = getCustomerRootAccounts();
		for (String customerRootAccount : customerRootAccounts) {
			fetchSubtree(customerRootAccount);
		}
	}

	private void fetchSubtree(final String rootGuid) throws SQLException {
		final List<String> subtreeGuids = new ArrayList<>();
		fetchSubtree(Collections.singletonList(rootGuid), subtreeGuids);
	}

	private void fetchSubtree(final List<String> parentGuids, final List<String> subtreeGuids) throws SQLException {
		final List<String> childrenGuids = getChildrenGuids(parentGuids);
		if (!childrenGuids.isEmpty()) {
			subtreeGuids.addAll(childrenGuids);
			fetchSubtree(childrenGuids, subtreeGuids);
		}
	}

	private List<String> getChildrenGuids(final List<String> parentGuids) throws SQLException {
		List<String> childrenGuids = new ArrayList<>();
		for (String parentGuid : parentGuids) {
			selectCustomerByParent.setString(1, parentGuid);
			try (ResultSet resultSet = selectCustomerByParent.executeQuery()) {
				while (resultSet.next()) {
					childrenGuids.add(resultSet.getString(GUID));
					closureCount += duplicateAncestors(resultSet.getString(GUID), parentGuid);
					insertClosure(resultSet.getString(GUID), parentGuid);
					closureCount++;
				}
			}
		}

		return childrenGuids;
	}

	private List<String> getCustomerRootAccounts() throws SQLException {
		Set<String> customerRootAccounts = new HashSet<>();

		try (ResultSet selectCustomerClosureResultSet = selectCustomersWithParentPreparedStatement.executeQuery()) {
			while (selectCustomerClosureResultSet.next()) {
				selectCustomer.setString(1, selectCustomerClosureResultSet.getString(PARENT_CUSTOMER_GUID));
				try (ResultSet customerResultSet = selectCustomer.executeQuery()) {
					customerResultSet.next();
					String customerParentGuid = customerResultSet.getString(PARENT_CUSTOMER_GUID);
					if (customerParentGuid == null) {
						customerRootAccounts.add(customerResultSet.getString(GUID));
					}
				}
			}
		}

		return new ArrayList<>(customerRootAccounts);
	}

	private void updateJpaGeneratedKeys(long count) throws SQLException {
		insertJpaGeneratedKeys.setString(1, CLOSURETABLE);
		insertJpaGeneratedKeys.setLong(2, count);
		insertJpaGeneratedKeys.execute();
	}

	private long duplicateAncestors(String accountGuid, String parentGuid) throws SQLException {

		long duplicatedClosureCount = 0;

		selectDuplicateAncestorPreparedStatement.setString(1, accountGuid);
		selectDuplicateAncestorPreparedStatement.setString(2, parentGuid);
		try (ResultSet duplicateAncestorResultSet = selectDuplicateAncestorPreparedStatement.executeQuery()) {
			while (duplicateAncestorResultSet.next()) {
				insertClosure(duplicateAncestorResultSet.getString("DESCENDANT_GUID"), duplicateAncestorResultSet.getString("ANCESTOR_GUID"),
						duplicateAncestorResultSet.getLong("ANCESTOR_DEPTH"));
				duplicatedClosureCount++;
			}
		}

		return duplicatedClosureCount;
	}

	private void insertClosure(String childGuid, String parentGuid, long depth) throws SQLException {
		insertClosurePreparedStatement.setLong(UID_INDEX, lastUid);
		insertClosurePreparedStatement.setString(PARENT_GUID_INDEX, parentGuid);
		insertClosurePreparedStatement.setString(CHILD_GUID_INDEX, childGuid);
		insertClosurePreparedStatement.setLong(DEPTH_INDEX, depth);

		lastUid++;

		insertClosurePreparedStatement.execute();
	}

	private void insertClosure(String childGuid, String parentGuid) throws SQLException {
		ancestorDepthPreparedStatement.setString(1, parentGuid);

		try (ResultSet resultSet = ancestorDepthPreparedStatement.executeQuery()) {
			resultSet.next();
			long depth = resultSet.getLong(1);
			insertClosure(childGuid, parentGuid, depth);
		}
	}

	private void createPreparedStatements() throws DatabaseException {
		selectCustomersWithParentPreparedStatement = connection.prepareStatement(SELECT_CUSTOMERS_WITH_PARENT);
		selectDuplicateAncestorPreparedStatement = connection.prepareStatement(SELECT_DUPLICATE_ANCESTOR);
		ancestorDepthPreparedStatement = connection.prepareStatement(ANCESTOR_DEPTH);
		insertClosurePreparedStatement = connection.prepareStatement(INSERT_CLOSURE);

		String formattedInsertQuery = String.format(INSERT_JPA_GENERATED_KEYS,
				quoteTableName(TABLE_JPA_GENERATED_KEYS),
				quoteColumnNames(JPA_GENERATED_COLUMNS));

		insertJpaGeneratedKeys = connection.prepareStatement(formattedInsertQuery);
		selectCustomer = connection.prepareStatement(SELECT_CUSTOMER);
		selectCustomerByParent = connection.prepareStatement(ACCOUNT_CHILDREN_GUIDS_BY_PARENT_GUIDS);
	}

	@Override
	public String getConfirmationMessage() {
		return "Finished updating TCUSTOMERCLOSURE table";
	}

	@Override
	public void setUp() throws SetupException {
		// no setup
	}

	@Override
	public void setFileOpener(ResourceAccessor resourceAccessor) {
		// not used
	}

	@Override
	public ValidationErrors validate(Database database) {
		return null;
	}
}
