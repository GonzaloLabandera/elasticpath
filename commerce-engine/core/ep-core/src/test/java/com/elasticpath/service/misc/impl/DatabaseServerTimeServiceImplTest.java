/**
 * Copyright (c) Elastic Path Software Inc., 2013
 */
package com.elasticpath.service.misc.impl;

import static com.elasticpath.service.misc.impl.DatabaseServerTimeServiceImpl.POSTGRESQL_TIME_RETRIEVE_QUERY;
import static com.elasticpath.service.misc.impl.DatabaseServerTimeServiceImpl.TIME_RETRIEVE_QUERY;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.elasticpath.persistence.api.PersistenceEngine;
import com.elasticpath.persistence.api.PersistenceSession;
import com.elasticpath.persistence.api.Query;
import com.elasticpath.persistence.openjpa.support.JPAUtil;

/**
 * Test <code>DatabaseServerTimeServiceImpl</code>.
 */
@SuppressWarnings("unchecked")
public class DatabaseServerTimeServiceImplTest {

	private static final long THRESHOLD = 1000;

	private DatabaseServerTimeServiceImpl timeService;

	@Rule
	public final JUnitRuleMockery context = new JUnitRuleMockery();

	private PersistenceEngine mockPersistenceEngine;
	private PersistenceSession mockPersistenceSession;
	private Query<Date> mockQuery;
	private DatabaseMetaData mockMetaData;
	private Connection mockConnection;

	/**
	 * Prepares for a test.
	 *
	 * @throws Exception on error
	 */
	@Before
	public void setUp() throws Exception {
		mockPersistenceEngine = context.mock(PersistenceEngine.class);
		mockPersistenceSession = context.mock(PersistenceSession.class);
		mockConnection = context.mock(Connection.class);
		mockMetaData = context.mock(DatabaseMetaData.class);

		context.checking(new Expectations() {
			{
				allowing(mockPersistenceEngine).getPersistenceSession();
				will(returnValue(mockPersistenceSession));

				allowing(mockPersistenceEngine).getConnection();
				will(returnValue(mockConnection));

				allowing(mockConnection).getMetaData();
				will(returnValue(mockMetaData));

				allowing(mockConnection).close();
			}
		});

		this.timeService = new DatabaseServerTimeServiceImpl();
		this.timeService.setPersistenceEngine(mockPersistenceEngine);
	}

	/**
	 * Close mock connection.
	 */
	@After
	public void tearDown() {
		try {
			mockConnection.close();
		} catch (SQLException exception) {
			//do nothing
		}
	}

	/**
	 * Test getting the current date and time using the database time service.
	 */
	@Test
	public void testGetCurrentTime() throws Exception {
		mockQuery = context.mock(Query.class);
		context.checking(new Expectations() {
			{
				oneOf(mockPersistenceSession).createSQLQuery(TIME_RETRIEVE_QUERY);
				will(returnValue(mockQuery));

				allowing(mockMetaData).getDatabaseProductName();
				will(returnValue("someDb"));
			}
		});

		final List<Date> results = new ArrayList<>();
		final Date now = new Date();
		results.add(now);
		context.checking(new Expectations() {
			{
				allowing(mockQuery).list();
				will(returnValue(results));
			}
		});
		assertSame(now, this.timeService.getCurrentTime());
	}

	/**
	 * Test getting no results from the database for the current date and time.
	 * This should then return the current date and time from the application server.
	 */
	@Test
	public void testGetCurrentTimeNoResults() throws Exception {
		mockQuery = context.mock(Query.class);
		context.checking(new Expectations() {
			{
				oneOf(mockPersistenceSession).createSQLQuery(TIME_RETRIEVE_QUERY);
				will(returnValue(mockQuery));

				allowing(mockMetaData).getDatabaseProductName();
				will(returnValue("someDb"));
			}
		});

		final List<Date> results = new ArrayList<>();
		final Date now = new Date();
		context.checking(new Expectations() {
			{
				allowing(mockQuery).list();
				will(returnValue(results));
			}
		});
		final Date currentTime = this.timeService.getCurrentTime();
		assertNotNull(currentTime);

		// Verify that the fallback date returned is within a second of the date of the call
		final Date minThreshold = new Date(now.getTime() - THRESHOLD);
		assertTrue(currentTime.after(minThreshold));
		final Date maxThreshold = new Date(now.getTime() + THRESHOLD);
		assertTrue(currentTime.before(maxThreshold));
	}

	/**
	 * Test getting null for results from the database for the current date and time.
	 * This should then return the current date and time from the application server.
	 */
	@Test
	public void testGetCurrentTimeNullResults() throws Exception {
		mockQuery = context.mock(Query.class);
		context.checking(new Expectations() {
			{
				oneOf(mockPersistenceSession).createSQLQuery(TIME_RETRIEVE_QUERY);
				will(returnValue(mockQuery));

				allowing(mockMetaData).getDatabaseProductName();
				will(returnValue("someDb"));
			}
		});

		final List<Date> results = null;
		final Date now = new Date();
		context.checking(new Expectations() {
			{
				allowing(mockQuery).list();
				will(returnValue(results));
			}
		});
		final Date currentTime = this.timeService.getCurrentTime();
		assertNotNull(currentTime);

		// Verify that the fallback date returned is within a second of the date of the call
		final Date minThreshold = new Date(now.getTime() - THRESHOLD);
		assertTrue(currentTime.after(minThreshold));
		final Date maxThreshold = new Date(now.getTime() + THRESHOLD);
		assertTrue(currentTime.before(maxThreshold));
	}

	/**
	 * Test getting the current date and time using the database time service.
	 */
	@Test
	public void testGetCurrentTimeFromPostgresDb() throws Exception {
		mockQuery = context.mock(Query.class);
		context.checking(new Expectations() {
			{
				oneOf(mockPersistenceSession).createSQLQuery(POSTGRESQL_TIME_RETRIEVE_QUERY);
				will(returnValue(mockQuery));

				allowing(mockMetaData).getDatabaseProductName();
				will(returnValue(JPAUtil.POSTGRESQL_DB_TYPE));
			}
		});

		final List<Date> results = new ArrayList<>();
		final Date now = new Date();
		results.add(now);
		context.checking(new Expectations() {
			{
				allowing(mockQuery).list();
				will(returnValue(results));
			}
		});
		assertSame(now, this.timeService.getCurrentTime());
	}
}
