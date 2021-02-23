/**
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package liquibase.ext.elasticpath.util;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Util Class for Custom Task Changes that already have methods ready to mock database operations.
 */
public class CustomTaskChangeTestUtil {

	private final JdbcConnection connection;

	public CustomTaskChangeTestUtil(JdbcConnection connection) {
		this.connection = connection;
	}

	public void setupUpdateBatchAddAndClear(final String updateSQL, PreparedStatement updatePreparedStatement)
			throws DatabaseException, SQLException {

		when(connection.prepareStatement(updateSQL)).thenReturn(updatePreparedStatement);
		doNothing().when(updatePreparedStatement).addBatch();
		when(updatePreparedStatement.executeBatch()).thenReturn(new int[1]);
		doNothing().when(updatePreparedStatement).clearBatch();
	}

	public int getNumberOfRegistersToProcess(int batchSize, int pendingToProcess) {
		return (pendingToProcess > batchSize) ? batchSize : pendingToProcess;
	}

	public void setupUpdateRegistersBatchResponse(ResultSet customersResultSet, int numberOfIterations, long firstIdentifier)
			throws SQLException {

		when(customersResultSet.next()).thenAnswer(answerTrueNTimes(numberOfIterations));
		when(customersResultSet.getLong(1)).thenAnswer(new Answer<Long>() {
			private int count = 0;

			@Override
			public Long answer(final InvocationOnMock invocationOnMock) throws Throwable {
				if (count >= numberOfIterations) {
					return Long.valueOf(firstIdentifier + count);
				} else {
					count++;
					return Long.valueOf(firstIdentifier + count);
				}
			}
		});
	}

	public void verifyUpdateWasMadeForRegisters(PreparedStatement updatePreparedStatement, final int numberOfRegisters, final long firstIdentifier)
			throws SQLException {

		for (int count = 1; count <= numberOfRegisters; count++) {
			verify(updatePreparedStatement).setLong(1, firstIdentifier + count);
		}
	}

	public Answer<Boolean> answerTrueNTimes(final int times) {
		return new Answer<Boolean>() {
			private int count = 0;

			@Override
			public Boolean answer(final InvocationOnMock invocationOnMock) throws Throwable {
				if (count >= times) {
					return false;
				} else {
					count++;
					return true;
				}
			}
		};
	}

}
