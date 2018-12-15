/**
 * Copyright (c) Elastic Path Software Inc., 2008
 */
package com.elasticpath.persistence.openjpa.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.FlushModeType;
import javax.persistence.Query;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.apache.openjpa.kernel.Broker;
import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.OpenJPAEntityManagerSPI;
import org.apache.openjpa.persistence.OpenJPAQuery;
import org.apache.openjpa.persistence.QueryOperationType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.transaction.PlatformTransactionManager;

import com.elasticpath.persistence.api.ChangeType;
import com.elasticpath.persistence.api.Entity;
import com.elasticpath.persistence.api.FlushMode;
import com.elasticpath.persistence.api.Persistable;
import com.elasticpath.persistence.api.PersistenceEngineOperationListener;
import com.elasticpath.persistence.api.PersistenceSessionFactory;
import com.elasticpath.persistence.openjpa.JpaPersistenceSession;
import com.elasticpath.persistence.openjpa.PersistenceInterceptor;

/**
 * Test that the methods of the JPA Persistence Engine behave as expected.
 */
@SuppressWarnings({"unchecked", "PMD.TooManyMethods", "PMD.ExcessiveClassLength", "PMD.UselessOverridingMethod", "PMD.GodClass"})
@RunWith(MockitoJUnitRunner.class)
public class JpaPersistenceEngineImplTest {

	private static final String MERGED_OBJECT = "mergedObject";

	private static final String LIST_PARAMETER_NAME = "list";

	private static final String SOME_PARAMETER = "someParameter";

	private static final String SOME_OTHER_PARAMETER = "someOtherParameter";

	private static final String NAMED_QUERY = "NAMED_QUERY";

	private static final String NAMED_QUERY_WITH_LIST = "SELECT FROM SOMETHING s WHERE s.uidPk IN (:list)";

	private static final String NAMED_QUERY_WITH_LIST_INSERTED = "SELECT FROM SOMETHING s WHERE s.uidPk IN (1000,1001)";

	private JpaPersistenceEngineImpl persistenceEngine;

	private JpaPersistenceEngineImpl listeningPersistenceEngine;

	@Mock
	private EntityManager entityManager;

	@Mock
	private OpenJPAEntityManager openJPAEntityManager;

	@Mock
	private PersistenceSessionFactory sessionFactory;

	@Mock
	private Broker broker;

	private static final long UIDPK = 1000L;

	private static final long UIDPK2 = 1001L;

	/**
	 * Set up mocks etc required by all tests.
	 *
	 * @throws Exception in case of error during setup
	 */
	@Before
	public void setUp() throws Exception {

		// Set up the persistence engine to be tested, stub out OpenJPA internals (e.g. broker).
		persistenceEngine = new JpaPersistenceEngineImpl() {
			@Override
			public Broker getBroker() {
				return broker;
			}

			@Override
			public <T> T escapeParameter(final T parameter) {
				return super.escapeParameter(parameter);
			}

		};
		persistenceEngine.setEntityManager(entityManager);
		persistenceEngine.setSessionFactory(sessionFactory);

		// Set up a persistence engine to test for listening.
		listeningPersistenceEngine = new JpaPersistenceEngineImpl() {
			@Override
			public Broker getBroker() {
				return broker;
			}
		};
		listeningPersistenceEngine.setEntityManager(entityManager);
		listeningPersistenceEngine.setSessionFactory(sessionFactory);

		when(entityManager.getDelegate()).thenReturn(openJPAEntityManager);
	}

	/**
	 * Test that clear calls the entity manager clear.
	 */
	@Test
	public final void testClear() {
		persistenceEngine.clear();
		verify(entityManager).clear();
	}

	/**
	 * Test the delete method gets a reference to the object and calls the entity manager's remove
	 * and then calls a flush operation.
	 */
	@Test
	public final void testDelete() {
		final Persistable object = mock(Persistable.class);
		when(object.getUidPk()).thenReturn(UIDPK);
		when(entityManager.getReference(object.getClass(), UIDPK)).thenAnswer(answer -> object);
		persistenceEngine.delete(object);
		verify(object).getUidPk();
		verify(entityManager).remove(object);
	}

	/**
	 * Test execute session update generates a query and executes update on it.
	 */
	@Test
	public final void testExecuteSessionUpdate() {
		final String sql = "UPDATE SomeObjectImpl so SET so.one = 1";
		final OpenJPAQuery<?> query = mock(OpenJPAQuery.class);
		when(entityManager.createQuery(sql)).thenReturn(query);
		when(query.getOperation()).thenReturn(QueryOperationType.UPDATE);

		persistenceEngine.executeSessionUpdate(sql);
		verify(query).executeUpdate();
	}

	/**
	 * Test flush calls the entity manager's flush.
	 */
	@Test
	public final void testFlush() {
		persistenceEngine.flush();
		verify(entityManager).flush();
	}

	/**
	 * Test that the get method calls the eneity manager's find method.
	 */
	@Test
	public final void testGet() {
		final Class<Persistable> persistenceClass = Persistable.class;
		final Persistable object = mock(Persistable.class);
		when(entityManager.find(persistenceClass, UIDPK)).thenReturn(object);

		Persistable result = persistenceEngine.get(persistenceClass, UIDPK);
		verify(entityManager).find(persistenceClass, UIDPK);
		assertThat(result)
			.as("Returned object should be the result of the entity manager call")
			.isEqualTo(object);
	}

	/**
	 * Test Initialize calls the entity manager's refresh method.
	 */
	@Test
	public final void testInitialize() {
		final Persistable object = mock(Persistable.class);
		persistenceEngine.initialize(object);
		verify(entityManager).refresh(object);
	}

	/**
	 * Test the load method calls the entity manager's find method.
	 */
	@Test
	public final void testLoad() {
		final Class<Persistable> persistenceClass = Persistable.class;
		final Persistable object = mock(Persistable.class);
		when(entityManager.find(persistenceClass, UIDPK)).thenReturn(object);

		Persistable result = persistenceEngine.load(persistenceClass, UIDPK);
		verify(entityManager).find(persistenceClass, UIDPK);
		assertThat(result)
			.as("Returned object should be the result of the entity manager call")
			.isEqualTo(object);
	}

	/**
	 * Test load with new session creates a new session, calls the find method
	 * on the new entity manager and finally closes the new entity manager.
	 */
	@Test
	public final void testLoadWithNewSession() {
		final Class<Persistable> persistenceClass = Persistable.class;
		final Persistable object = mock(Persistable.class);
		final EntityManager newEntityManager = mock(EntityManager.class, "newEntityManager");
		final PlatformTransactionManager txManager = mock(PlatformTransactionManager.class);
		final JpaSessionImpl session = new JpaSessionImpl(entityManager, txManager, false) {

			@Override
			public EntityManager getEntityManager() {
				return newEntityManager;
			}

		};
		when(sessionFactory.createPersistenceSession()).thenReturn(session);
		when(newEntityManager.find(persistenceClass, UIDPK)).thenReturn(object);

		Persistable result = persistenceEngine.loadWithNewSession(persistenceClass, UIDPK);
		verify(newEntityManager).close();
		assertThat(result)
			.as("Returned object should be the result of the entity manager call")
			.isEqualTo(object);
	}


	/**
	 * Test that bulk update creates a query and executes update on that query.
	 */
	@Test
	public final void testBulkUpdate() {
		final String queryString = "UPDATE SomeObjectImpl so SET so.one = 1";
		final OpenJPAQuery<?> query = mock(OpenJPAQuery.class);

		when(entityManager.createQuery(queryString)).thenReturn(query);
		when(query.getOperation()).thenReturn(QueryOperationType.UPDATE);

		persistenceEngine.bulkUpdate(queryString);
		verify(entityManager).createQuery(queryString);
		verify(query).getOperation();
		verify(query).executeUpdate();
	}

	/**
	 * Test that bulk update with a parameter creates a query, sets the parameter and executes
	 * update on that query.
	 */
	@Test
	public final void testBulkUpdateWithParameter() {
		final String queryString = "UPDATE SomeObjectImpl so SET so.one = 1 WHERE so.something = ?1";
		final OpenJPAQuery<?> query = mock(OpenJPAQuery.class);
		final Object[] parameters = new Object[] { SOME_PARAMETER };

		when(entityManager.createQuery(queryString)).thenReturn(query);
		when(query.getOperation()).thenReturn(QueryOperationType.UPDATE);

		persistenceEngine.bulkUpdate(queryString, parameters);
		verify(entityManager).createQuery(queryString);
		verify(query).getOperation();
		verify(query).executeUpdate();
		verify(query).setParameter(1, SOME_PARAMETER);
	}

	/**
	 * Test that the merge method will execute the object's before persist action and then call the entity manager's
	 * merge function and flush. Confirm that the return value is a different object to that passed in.
	 */
	@Test
	public final void testMerge() {
		final FakePersistablePersistenceInterceptor object = mock(FakePersistablePersistenceInterceptor.class);
		final FakePersistablePersistenceInterceptor mergedObject = mock(FakePersistablePersistenceInterceptor.class, MERGED_OBJECT);

		when(entityManager.merge(object)).thenReturn(mergedObject);

		Persistable result = persistenceEngine.merge(object);
		verify(object).executeBeforePersistAction();
		verify(entityManager).merge(object);
		assertThat(result)
			.as("Returned object should be different to the one given")
			.isNotSameAs(object);
	}

	/**
	 * Test that calling retrieve with a collection of queries will create the query objects and
	 * consolidate the results.
	 */
	@Test
	public final void testRetrieveListOfQueries() {
		// Create the list of query strings
		final String query1 = "SELECT fo FROM FirstObjectImpl fo";
		final String query2 = "SELECT so FROM SecondObjectImpl so";
		final List<String> queries = new ArrayList<>();
		queries.add(query1);
		queries.add(query2);

		// Mock the Query objects and Query results
		final Query query1Query = mock(Query.class, "query1");
		final Query query2Query = mock(Query.class, "query2");
		final List<Persistable> results1 = new ArrayList<>();
		final List<Persistable> results2 = new ArrayList<>();
		final Persistable obj1 = mock(Persistable.class, "result1");
		final Persistable obj2 = mock(Persistable.class, "result2");
		results1.add(obj1);
		results2.add(obj2);

		// Set expectations
		when(entityManager.createQuery(query1)).thenReturn(query1Query);
		when(entityManager.createQuery(query2)).thenReturn(query2Query);
		when(query1Query.getResultList()).thenReturn(results1);
		when(query2Query.getResultList()).thenReturn(results2);

		List<Persistable> results = persistenceEngine.retrieve(queries);
		assertThat(results).containsExactlyInAnyOrder(obj1, obj2);
	}

	/**
	 * Test that calling retrieve with a collection of queries that take parameters will create the query objects,
	 * set the parameters on each, and consolidate the results.
	 */
	@Test
	public final void testRetrieveListofQueriesWithParameters() {
		// Create the list of query strings
		final String query1 = "SELECT fo FROM FirstObjectImpl fo WHERE fo.field = ?1";
		final String query2 = "SELECT so FROM SecondObjectImpl so WHERE fo.field = ?1";
		final List<String> queries = new ArrayList<>();
		queries.add(query1);
		queries.add(query2);

		// Mock the Query objects and Query results
		final Query query1Query = mock(Query.class, "query1");
		final Query query2Query = mock(Query.class, "query2");
		final List<Persistable> results1 = new ArrayList<>();
		final List<Persistable> results2 = new ArrayList<>();
		final Persistable obj1 = mock(Persistable.class, "result1");
		final Persistable obj2 = mock(Persistable.class, "result2");
		results1.add(obj1);
		results2.add(obj2);

		when(entityManager.createQuery(query1)).thenReturn(query1Query);
		when(entityManager.createQuery(query2)).thenReturn(query2Query);
		when(query1Query.getResultList()).thenReturn(results1);
		when(query2Query.getResultList()).thenReturn(results2);

		final Object[] parameters = new Object[]{SOME_PARAMETER};
		List<Persistable> results = persistenceEngine.retrieve(queries, parameters);
		assertThat(results).containsExactlyInAnyOrder(obj1, obj2);

		verify(query1Query).setParameter(1, SOME_PARAMETER);
		verify(query2Query).setParameter(1, SOME_PARAMETER);
	}

	/**
	 * Test that the save method calls the object's executeBeforePersistAction method
	 * followed by the entity manager's persist method.
	 */
	@Test
	public final void testSave() {
		final FakePersistablePersistenceInterceptor object = mock(FakePersistablePersistenceInterceptor.class);

		persistenceEngine.save(object);
		verify(object).executeBeforePersistAction();
		verify(entityManager).persist(object);
	}

	/**
	 * Test that calling save or merge with a new object will result in a call to
	 * the object's executeBeforePersistAction method followed by the entity manager's persist method.
	 */
	@Test
	public final void testSaveOrMergeWithNewObject() {
		final FakePersistablePersistenceInterceptor object = mock(FakePersistablePersistenceInterceptor.class);
		when(object.isPersisted()).thenReturn(false);

		Persistable result = persistenceEngine.saveOrMerge(object);
		verify(object).isPersisted();
		verify(object).executeBeforePersistAction();
		verify(entityManager).persist(object);
		assertThat(result)
			.as("Returned object should be the same as the one passed in")
			.isSameAs(object);
	}

	/**
	 * Test that calling save or merge with an existing object will result in a call to
	 * the object's executeBeforePersistAction method followed by the entity manager's merge method.
	 */
	@Test
	public final void testSaveOrMergeWithExistingObject() {
		final FakePersistablePersistenceInterceptor object = mock(FakePersistablePersistenceInterceptor.class);
		final FakePersistablePersistenceInterceptor mergedObject = mock(FakePersistablePersistenceInterceptor.class, MERGED_OBJECT);

		when(object.isPersisted()).thenReturn(true);
		when(entityManager.merge(object)).thenReturn(mergedObject);

		Persistable result = persistenceEngine.saveOrMerge(object);
		verify(object).isPersisted();
		verify(object).executeBeforePersistAction();
		verify(entityManager).merge(object);
		assertThat(result)
			.as("Returned object should be different to the one passed in")
			.isNotSameAs(object);
	}

	/**
	 * Test that calling update results in a call to the object's executeBeforePersistAction
	 * followed by a call to the entity manager's merge method.
	 */
	@Test
	public final void testUpdate() {
		final FakePersistablePersistenceInterceptor object = mock(FakePersistablePersistenceInterceptor.class);
		final FakePersistablePersistenceInterceptor mergedObject = mock(FakePersistablePersistenceInterceptor.class, MERGED_OBJECT);

		when(entityManager.merge(object)).thenReturn(mergedObject);

		Persistable result = persistenceEngine.update(object);
		verify(object).executeBeforePersistAction();
		verify(entityManager).merge(object);
		assertThat(result)
			.as("Returned object should be different to the one passed in")
			.isNotSameAs(object);
	}

	/**
	 * Test that a call to set query parameters sets all of the query's parameters.
	 */
	@Test
	public final void testSetQueryParameters() {
		final Query query = mock(Query.class);
		final String param1 = "parameter1";
		final Long param2 = 10L;
		final Boolean param3 = false;
		final int three = 3;
		final Object[] parameters = new Object[] { param1, param2, param3 };

		persistenceEngine.setQueryParameters(query, parameters);
		verify(query).setParameter(1, param1);
		verify(query).setParameter(2, param2);
		verify(query).setParameter(three, param3);
	}

	/**
	 * Test the execute named query method creates a named query and then executes it.
	 */
	@Test
	public final void testExecuteNamedQuery() {
		final OpenJPAQuery<Integer> query = mock(OpenJPAQuery.class);
		final int rowsUpdated = 103;

		when(entityManager.createNamedQuery(NAMED_QUERY)).thenReturn(query);
		when(query.getOperation()).thenReturn(QueryOperationType.UPDATE);
		when(query.executeUpdate()).thenReturn(rowsUpdated);

		int result = persistenceEngine.executeNamedQuery(NAMED_QUERY);
		verify(entityManager).createNamedQuery(NAMED_QUERY);
		verify(query).getOperation();
		verify(query).executeUpdate();
		assertThat(result)
			.as("The return value should be the row count returned by executeUpdate")
			.isEqualTo(rowsUpdated);
	}

	/**
	 * Test that the insert list into query method substitutes the list of parameters for the named parameter
	 * when it calls the entity manager's create query method.
	 */
	@Test
	public final void testInsertListIntoQuery() {
		final String queryString = "SELECT so FROM SomeObjectImpl so WHERE so.uidPk in (:list) AND so.field = :field";
		final String parameters = "1001, 1002, 1003";
		final OpenJPAQuery<?> query = mock(OpenJPAQuery.class);

		when(query.getQueryString()).thenReturn(queryString);

		persistenceEngine.insertListIntoQuery(query, LIST_PARAMETER_NAME, parameters);
		verify(query).getQueryString();
		verify(entityManager).createQuery("SELECT so FROM SomeObjectImpl so WHERE so.uidPk in (1001, 1002, 1003) AND so.field = :field");
	}


	/**
	 * Test that splitting a large collection (i.e. > 900 parameters) creates the correct number of
	 * resultant collection strings.
	 */
	@Test
	public final void testSplitCollectionWithLargeCollection() {
		final int parameterCount = 1000;
		final Set<Long> values = new HashSet<>();
		for (long l = 0; l < parameterCount; l++) {
			values.add(l);
		}
		List<String> result = persistenceEngine.splitCollection(values, parameterCount);
		assertThat(result)
			.as("The method should have split the collection into 2 strings")
			.hasSize(2);
	}

	/**
	 * Test that splitting a small collection doesn't actually split the collection but just returns
	 * a single collection string.
	 */
	@Test
	public final void testSplitCollectionWithSmallCollection() {
		final int parameterCount = 3;
		final List<Long> values = new ArrayList<>();
		for (long l = 1; l <= parameterCount; l++) {
			values.add(l);
		}
		List<String> result = persistenceEngine.splitCollection(values, parameterCount);
		assertThat(result).containsOnly("1,2,3");
	}

	/**
	 * Test that splitting a small collection doesn't actually split the collection but just returns
	 * a single collection string. A single quote ' should be escaped to '' according to SQL escaping rules.
	 */
	@Test
	public final void testSplitCollectionWithEscaping() {
		final int parameterCount = 3;
		final List<String> values = new ArrayList<>();
		values.add("SNAP' IT UP");
		values.add("SNAP IT UP UK");
		values.add("SLR' WORLD");
		List<String> result = persistenceEngine.splitCollection(values, parameterCount);
		assertThat(result).containsOnly("'SNAP'' IT UP','SNAP IT UP UK','SLR'' WORLD'");
	}

	/**
	 * Tests that if a parameter is a string and it contains single quotes they will be escaped according to SQL rules.
	 */
	@Test
	public final void testEscapeParameterWithStrings() {
		final String noSingleQuotes = "some text";
		final String hasSingleQuotes = "some' text'";
		String result = persistenceEngine.escapeParameter(noSingleQuotes);
		assertThat(result)
			.as("The string should be the same as before")
			.isEqualTo(noSingleQuotes);
		result = persistenceEngine.escapeParameter(hasSingleQuotes);
		assertThat(result)
			.as("Single quotes should be escaped")
			.isEqualTo("some'' text''");
	}

	/**
	 * Currently only string parameters are escaped. Parameters of other types should not be processed.
	 */
	@Test
	public final void testEscapeParameterWithNonStrings() {
		Integer one = 1;
		Integer result = persistenceEngine.escapeParameter(one);
		assertThat(result).isSameAs(one);
	}

	/**
	 * Test that when save is called that beginSingleOperation() and endSingleOperation() events are fired.
	 */
	@Test
	public void testBeginEndSingleOperationEventsAreFiredOnSave() {
		final FakePersistablePersistenceInterceptor persistableObject = mock(FakePersistablePersistenceInterceptor.class);
		final PersistenceEngineOperationListener listener = mock(PersistenceEngineOperationListener.class);

		listeningPersistenceEngine.addPersistenceEngineOperationListener(listener);

		listeningPersistenceEngine.save(persistableObject);
		verify(listener).beginSingleOperation(persistableObject, ChangeType.CREATE);
		verify(persistableObject).executeBeforePersistAction();
		verify(entityManager).persist(persistableObject);
		verify(listener).endSingleOperation(persistableObject, ChangeType.CREATE);
	}

	/**
	 * Test that when merge is called that beginSingleOperation() and endSingleOperation() events are fired.
	 */
	@Test
	public void testBeginEndSingleOperationEventsAreFiredOnMerge() {
		final FakePersistablePersistenceInterceptor persistableObject = mock(FakePersistablePersistenceInterceptor.class);
		final Persistable mergedObject = mock(Persistable.class, MERGED_OBJECT);
		final PersistenceEngineOperationListener listener = mock(PersistenceEngineOperationListener.class);

		listeningPersistenceEngine.addPersistenceEngineOperationListener(listener);

		when(entityManager.merge(persistableObject)).thenAnswer(answer -> mergedObject);

		listeningPersistenceEngine.merge(persistableObject);
		verify(listener).beginSingleOperation(persistableObject, ChangeType.UPDATE);
		verify(persistableObject).executeBeforePersistAction();
		verify(entityManager).merge(persistableObject);
		verify(listener).endSingleOperation(persistableObject, ChangeType.UPDATE);

	}

	/**
	 * Test that when delete is called that beginSingleOperation() and endSingleOperation() events are fired.
	 */
	@Test
	public void testBeginEndSingleOperationEventsAreFiredOnDelete() {
		final Entity entity = mock(Entity.class);
		final PersistenceEngineOperationListener listener = mock(PersistenceEngineOperationListener.class);

		listeningPersistenceEngine.addPersistenceEngineOperationListener(listener);

		when(entity.getUidPk()).thenReturn(UIDPK);
		when(entityManager.getReference(entity.getClass(), UIDPK)).thenAnswer(answer -> entity);

		listeningPersistenceEngine.delete(entity);

		verify(listener).beginSingleOperation(entity, ChangeType.DELETE);
		verify(entity).getUidPk();
		verify(entityManager).getReference(entity.getClass(), UIDPK);
		verify(entityManager).remove(entity);
		verify(listener).endSingleOperation(entity, ChangeType.DELETE);
	}

	/**
	 * Test that when we make two changes, one after the other, that the start and stop is only called once for each change.
	 */
	@Test
	public final void testConsecutiveChanges() {
		final FakePersistablePersistenceInterceptor persistableObject = mock(FakePersistablePersistenceInterceptor.class);
		final PersistenceEngineOperationListener listener = mock(PersistenceEngineOperationListener.class);

		listeningPersistenceEngine.addPersistenceEngineOperationListener(listener);

		listeningPersistenceEngine.save(persistableObject);
		listeningPersistenceEngine.save(persistableObject);

		verify(listener, times(2)).beginSingleOperation(persistableObject, ChangeType.CREATE);
		verify(persistableObject, times(2)).executeBeforePersistAction();
		verify(entityManager, times(2)).persist(persistableObject);
		verify(listener, times(2)).endSingleOperation(persistableObject, ChangeType.CREATE);
	}

	/**
	 * Test method for {@link JpaPersistenceEngineImpl#retrieveByNamedQuery(java.lang.String, java.lang.Object[])}.
	 */
	@Test
	public final void testRetrieveByNamedQueryStringFlushModeObjectArray() {
		final Query query = mock(Query.class);

		when(entityManager.createNamedQuery(NAMED_QUERY)).thenReturn(query);
		when(query.getResultList()).thenReturn(ImmutableList.of("result"));

		final Object[] parameters = new Object[] { SOME_PARAMETER, SOME_OTHER_PARAMETER };
		List<Object> result = persistenceEngine.retrieveByNamedQuery(NAMED_QUERY, FlushMode.COMMIT, parameters);
		verify(entityManager).createNamedQuery(NAMED_QUERY);
		verify(query).setParameter(1, SOME_PARAMETER);
		verify(query).setParameter(2, SOME_OTHER_PARAMETER);
		verify(query).setFlushMode(FlushModeType.COMMIT);
		verify(query).getResultList();
		assertThat(result)
			.as("Retrieve gives back results from OpenJPA")
			.containsOnly("result");
	}

	@Test
	@SuppressWarnings("rawtypes")
	public void testRetrieveWithNamedParametersHappyPath() {
		// Given
		final String adhocQueryString = "SELECT Entity e WHERE e.foo = :foo AND e.bar = :bar";
		final List<String> entities = Collections.singletonList("entity1");
		final Query adhocQuery = mock(Query.class);

		when(entityManager.createQuery(adhocQueryString)).thenReturn(adhocQuery);
		when(adhocQuery.setParameter("foo", "fooParam")).thenReturn(adhocQuery);
		when(adhocQuery.setParameter("bar", "barParam")).thenReturn(adhocQuery);
		when(adhocQuery.getResultList()).thenReturn(entities);

		// When
		Map<String, String> parameters = ImmutableMap.of("foo", "fooParam", "bar", "barParam");
		List<String> results = persistenceEngine.retrieveWithNamedParameters(adhocQueryString, parameters);

		// Then
		verify(entityManager).createQuery(adhocQueryString);
		verify(adhocQuery).setParameter("foo", "fooParam");
		verify(adhocQuery).setParameter("bar", "barParam");
		verify(adhocQuery).getResultList();
		assertThat(results)
			.as("Query should have been evaluated with the proper params")
			.isEqualTo(entities);
	}

	@Test
	public void testRetrieveWithNamedParametersWorksFineWithoutParameters() {
		// Given
		final List<String> entities = Collections.singletonList("entity1");
		final String adhocQueryString = "SELECT Entity e WHERE e.code = 'hardcodedValue'";
		final Query adhocQuery = mock(Query.class);

		when(entityManager.createQuery(adhocQueryString)).thenReturn(adhocQuery);
		when(adhocQuery.getResultList()).thenReturn(entities);

		// When
		List<String> results = persistenceEngine.retrieve(adhocQueryString, Collections.emptyMap());

		// Then
		verify(entityManager).createQuery(adhocQueryString);
		verify(adhocQuery).getResultList();
		assertThat(results)
			.as("Query should have been evaluated with the proper params")
			.isEqualTo(entities);
	}

	@Test
	public void testDetachWithAttachedObject() {
		// Given
		final OpenJPAEntityManagerSPI openJPAEntityManager = mock(OpenJPAEntityManagerSPI.class);
		persistenceEngine.setEntityManager(openJPAEntityManager);

		final Object entity = new Object();
		when(openJPAEntityManager.isDetached(entity)).thenReturn(false);
		when(openJPAEntityManager.detachCopy(entity)).thenReturn("detached");

		// When
		Object detached = persistenceEngine.detach(entity);

		// Then
		verify(openJPAEntityManager).isDetached(entity);
		verify(openJPAEntityManager).detachCopy(entity);
		assertThat(detached)
			.as("Object should have been detached")
			.isEqualTo("detached");
	}

	@Test
	public void testDetachWithPreviouslyDetachedObjectDoesNothing() {
		// Given
		final OpenJPAEntityManagerSPI openJPAEntityManager = mock(OpenJPAEntityManagerSPI.class);
		persistenceEngine.setEntityManager(openJPAEntityManager);

		final Object entity = new Object();
		when(openJPAEntityManager.isDetached(entity)).thenReturn(true);

		// When
		Object detached = persistenceEngine.detach(entity);

		// Then
		verify(openJPAEntityManager).isDetached(entity);
		verify(openJPAEntityManager, never()).detachCopy(any());
		assertThat(detached)
			.as("Object is already detached so detach() should do nothing")
			.isSameAs(entity);
	}

	/**
	 * Test method for {@link JpaPersistenceEngineImpl#retrieve(java.lang.String, int, int)}.
	 */
	@Test
	public final void testRetrieveStringIntInt() {
		final String queryStr = "SELECT slo FROM SomeLargeObjectImpl slo";
		final int firstResult = 10;
		final int maxResults = 100;
		final Query query = mock(Query.class);
		when(entityManager.createQuery(queryStr)).thenReturn(query);

		persistenceEngine.retrieve(queryStr, firstResult, maxResults);
		verify(query).setFirstResult(firstResult);
		verify(query).setMaxResults(maxResults);
		verify(query).getResultList();
	}

	/**
	 * Test method for retrieve with named parameters.
	 */
	@Test
	public final void testRetrieveStringNamedParameter() {
		final String query = "SELECT so FROM SomeObjectImpl so WHERE so.field = ?1";
		final Object[] parameters = new Object[] { SOME_PARAMETER };
		final Query jpaQuery = mock(Query.class);
		when(entityManager.createQuery(query)).thenReturn(jpaQuery);
		persistenceEngine.retrieve(query, parameters);
		verify(jpaQuery).setParameter(1, SOME_PARAMETER);
		verify(jpaQuery).getResultList();
	}

	/**
	 * Test method for {@link JpaPersistenceEngineImpl#retrieveWithNewSession(String, Object...)}.
	 */
	@Test
	public final void testRetrieveWithNewSessionWithNamedParameters() {
		final String query = "SELECT so FROM SomeObjectImpl so WHERE so.field = ?1";
		final Object[] parameters = new Object[] { SOME_PARAMETER };
		final JpaPersistenceSession session = mock(JpaPersistenceSession.class);
		final Query jpaQuery = mock(Query.class);

		when(sessionFactory.createPersistenceSession()).thenReturn(session);
		when(session.getEntityManager()).thenReturn(entityManager);
		when(entityManager.createQuery(query)).thenReturn(jpaQuery);

		persistenceEngine.retrieveWithNewSession(query, parameters);
		verify(sessionFactory).createPersistenceSession();
		verify(session).getEntityManager();
		verify(entityManager).createQuery(query);
		verify(jpaQuery).setParameter(1, SOME_PARAMETER);
		verify(jpaQuery).getResultList();
	}

	/**
	 * Test method for {@link com.elasticpath.persistence.openjpa.impl.JpaPersistenceEngineImpl
	 * #retrieveWithListWithNewSession(java.lang.String, java.lang.String, java.util.Collection, java.lang.Object[])}.
	 */
	@Test
	public final void testRetrieveWithListWithNewSession() {
		final String query = "SELECT so FROM SomeObjectImpl so WHERE so.uidPk in (:list) AND so.field = :someField";
		final String expected = "SELECT so FROM SomeObjectImpl so WHERE so.uidPk in (1000,1001) AND so.field = :someField";
		final Map<String, Object> parameters = ImmutableMap.of("somefield", SOME_PARAMETER);
		final Set<Long> values = ImmutableSet.of(UIDPK, UIDPK2);
		final Query jpaQuery = mock(Query.class);
		final OpenJPAQuery<Entity> newQuery = mock(OpenJPAQuery.class);
		final JpaPersistenceSession session = mock(JpaPersistenceSession.class);

		when(sessionFactory.createPersistenceSession()).thenReturn(session);
		when(session.getEntityManager()).thenReturn(entityManager);
		when(entityManager.createQuery(query)).thenReturn(newQuery);
		when(newQuery.getQueryString()).thenReturn(query);
		when(entityManager.createQuery(expected)).thenReturn(jpaQuery);

		persistenceEngine.retrieveWithListWithNewSession(query, LIST_PARAMETER_NAME, values, parameters);
		verify(entityManager).createQuery(expected);
		verify(jpaQuery).getResultList();
	}

	/**
	 * Test method for retrieveWithNewSession.
	 */
	@Test
	public final void testRetrieveWithNewSessionString() {
		final String query = "SELECT so FROM SomeObjectImpl so";
		final JpaPersistenceSession session = mock(JpaPersistenceSession.class);
		final Query jpaQuery = mock(Query.class);

		when(sessionFactory.createPersistenceSession()).thenReturn(session);
		when(session.getEntityManager()).thenReturn(entityManager);
		when(entityManager.createQuery(query)).thenReturn(jpaQuery);

		persistenceEngine.retrieveWithNewSession(query, Collections.emptyMap());
		verify(sessionFactory).createPersistenceSession();
		verify(session).getEntityManager();
		verify(entityManager).createQuery(query);
		verify(jpaQuery).getResultList();
	}

	/**
	 * Test method for retrieveByNamedQuery.
	 */
	@Test
	public final void testRetrieveByNamedQueryString() {
		final Query query = mock(Query.class);

		when(entityManager.createNamedQuery(NAMED_QUERY)).thenReturn(query);

		persistenceEngine.retrieveByNamedQuery(NAMED_QUERY);
		verify(entityManager).createNamedQuery(NAMED_QUERY);
		verify(query).getResultList();
	}

	/**
	 * Test method for {@link JpaPersistenceEngineImpl#retrieveByNamedQuery(java.lang.String, java.lang.Object[])}.
	 */
	@Test
	public final void testRetrieveByNamedQueryStringObjectArray() {
		final Object[] parameters = new Object[] { SOME_PARAMETER };
		final Query query = mock(Query.class);

		when(entityManager.createNamedQuery(NAMED_QUERY)).thenReturn(query);

		persistenceEngine.retrieveByNamedQuery(NAMED_QUERY, parameters);
		verify(entityManager).createNamedQuery(NAMED_QUERY);
		verify(query).setParameter(1, SOME_PARAMETER);
		verify(query).getResultList();
	}

	/**
	 * Test method for {@link JpaPersistenceEngineImpl#retrieveByNamedQuery(java.lang.String, java.util.Map)}.
	 */
	@Test
	public final void testRetrieveByNamedQueryStringMapOfStringObject() {
		Map<String, Object> parameters = ImmutableMap.of("parameter", SOME_PARAMETER);
		final Query query = mock(Query.class);

		when(entityManager.createNamedQuery(NAMED_QUERY)).thenReturn(query);

		persistenceEngine.retrieveByNamedQuery(NAMED_QUERY, parameters);
		verify(entityManager).createNamedQuery(NAMED_QUERY);
		verify(query).setParameter("parameter", SOME_PARAMETER);
		verify(query).getResultList();
	}

	/**
	 * Test method for {@link JpaPersistenceEngineImpl#retrieveByNamedQuery(java.lang.String, int, int)}.
	 */
	@Test
	public final void testRetrieveByNamedQueryStringIntInt() {
		final int firstResult = 1;
		final int maxResults = 10;
		final Query query = mock(Query.class);

		when(entityManager.createNamedQuery(NAMED_QUERY)).thenReturn(query);

		persistenceEngine.retrieveByNamedQuery(NAMED_QUERY, firstResult, maxResults);
		verify(entityManager).createNamedQuery(NAMED_QUERY);
		verify(query).setFirstResult(firstResult);
		verify(query).setMaxResults(maxResults);
		verify(query).getResultList();
	}

	/**
	 * Test method for {@link com.elasticpath.persistence.openjpa.impl.JpaPersistenceEngineImpl
	 * #retrieveByNamedQuery(java.lang.String, java.lang.Object[], int, int)}.
	 */
	@Test
	public final void testRetrieveByNamedQueryStringObjectArrayIntInt() {
		final Object[] parameters = new Object[] { SOME_PARAMETER };
		final int firstResult = 1;
		final int maxResults = 10;
		final Query query = mock(Query.class);

		when(entityManager.createNamedQuery(NAMED_QUERY)).thenReturn(query);

		persistenceEngine.retrieveByNamedQuery(NAMED_QUERY, parameters, firstResult, maxResults);
		verify(entityManager).createNamedQuery(NAMED_QUERY);
		verify(query).setParameter(1, SOME_PARAMETER);
		verify(query).setFirstResult(firstResult);
		verify(query).setMaxResults(maxResults);
		verify(query).getResultList();
	}

	/**
	 * Test method for {@link com.elasticpath.persistence.openjpa.impl.JpaPersistenceEngineImpl
	 * #retrieveByNamedQueryWithList(java.lang.String, java.lang.String, java.util.Collection)}.
	 */
	@Test
	public final void testRetrieveByNamedQueryWithListStringStringCollectionOfE() {
		final Set<Long> values = ImmutableSet.of(UIDPK, UIDPK2);
		final OpenJPAQuery<Entity> query = mock(OpenJPAQuery.class);
		final Query newQuery = mock(Query.class);

		when(openJPAEntityManager.createNamedQuery(NAMED_QUERY_WITH_LIST)).thenReturn(query);
		when(query.getQueryString()).thenReturn(NAMED_QUERY_WITH_LIST);
		when(entityManager.createQuery(NAMED_QUERY_WITH_LIST_INSERTED)).thenReturn(newQuery);

		persistenceEngine.retrieveByNamedQueryWithList(NAMED_QUERY_WITH_LIST, LIST_PARAMETER_NAME, values);
		verify(openJPAEntityManager).createNamedQuery(NAMED_QUERY_WITH_LIST);
		verify(entityManager).createQuery(NAMED_QUERY_WITH_LIST_INSERTED);
		verify(newQuery).getResultList();
	}

	/**
	 * Test method for {@link com.elasticpath.persistence.openjpa.impl.JpaPersistenceEngineImpl
	 * #retrieveByNamedQueryWithList(java.lang.String, java.lang.String, java.util.Collection, java.lang.Object[])}.
	 */
	@Test
	public final void testRetrieveByNamedQueryWithListStringStringCollectionOfEObjectArray() {
		final Object[] parameters = new Object[] { SOME_PARAMETER };
		final Set<Long> values = ImmutableSet.of(UIDPK, UIDPK2);
		final OpenJPAQuery<Entity> query = mock(OpenJPAQuery.class);
		final Query newQuery = mock(Query.class);

		when(openJPAEntityManager.createNamedQuery(NAMED_QUERY_WITH_LIST)).thenReturn(query);
		when(entityManager.createQuery(NAMED_QUERY_WITH_LIST_INSERTED)).thenReturn(newQuery);
		when(query.getQueryString()).thenReturn(NAMED_QUERY_WITH_LIST);

		persistenceEngine.retrieveByNamedQueryWithList(NAMED_QUERY_WITH_LIST, LIST_PARAMETER_NAME, values, parameters);
		verify(openJPAEntityManager).createNamedQuery(NAMED_QUERY_WITH_LIST);
		verify(entityManager).createQuery(NAMED_QUERY_WITH_LIST_INSERTED);
		verify(newQuery).setParameter(1, SOME_PARAMETER);
		verify(newQuery).getResultList();
	}

	/**
	 * Test method for {@link JpaPersistenceEngineImpl#executeNamedQuery(java.lang.String, java.lang.Object[])}.
	 */
	@Test
	public final void testExecuteNamedQueryStringObjectArray() {
		final Object[] parameters = new Object[] { SOME_PARAMETER };

		final OpenJPAQuery<Entity> query = mock(OpenJPAQuery.class);

		when(entityManager.createNamedQuery(NAMED_QUERY)).thenReturn(query);
		when(query.getOperation()).thenReturn(QueryOperationType.SELECT);

		persistenceEngine.executeNamedQuery(NAMED_QUERY, parameters);
		verify(entityManager).createNamedQuery(NAMED_QUERY);
		verify(query).setParameter(1, SOME_PARAMETER);
		verify(query).executeUpdate();
	}

	/**
	 * Test method for {@link com.elasticpath.persistence.openjpa.impl.JpaPersistenceEngineImpl
	 * #executeNamedQueryWithList(java.lang.String, java.lang.String, java.util.Collection)}.
	 */
	@Test
	public final void testExecuteNamedQueryWithListStringStringCollectionOfE() {
		final Set<Long> values = ImmutableSet.of(UIDPK, UIDPK2);
		final OpenJPAQuery<Entity> query = mock(OpenJPAQuery.class);
		final Query newQuery = mock(Query.class);

		when(openJPAEntityManager.createNamedQuery(NAMED_QUERY_WITH_LIST)).thenReturn(query);
		when(query.getQueryString()).thenReturn(NAMED_QUERY_WITH_LIST);
		when(entityManager.createQuery(NAMED_QUERY_WITH_LIST_INSERTED)).thenReturn(newQuery);
		when(query.getOperation()).thenReturn(QueryOperationType.SELECT);

		persistenceEngine.executeNamedQueryWithList(NAMED_QUERY_WITH_LIST, LIST_PARAMETER_NAME, values);
		verify(entityManager).createQuery(NAMED_QUERY_WITH_LIST_INSERTED);
		verify(newQuery).executeUpdate();
	}

	/**
	 * Test method for {@link com.elasticpath.persistence.openjpa.impl.JpaPersistenceEngineImpl
	 * #executeNamedQueryWithList(java.lang.String, java.lang.String, java.util.Collection, java.lang.Object[])}.
	 */
	@Test
	public final void testExecuteNamedQueryWithListStringStringCollectionOfEObjectArray() {
		final Object[] parameters = new Object[] { SOME_PARAMETER };
		final Set<Long> values = ImmutableSet.of(UIDPK, UIDPK2);
		final OpenJPAQuery<Entity> query = mock(OpenJPAQuery.class);
		final Query newQuery = mock(Query.class);

		when(openJPAEntityManager.createNamedQuery(NAMED_QUERY_WITH_LIST)).thenReturn(query);
		when(query.getQueryString()).thenReturn(NAMED_QUERY_WITH_LIST);
		when(entityManager.createQuery(NAMED_QUERY_WITH_LIST_INSERTED)).thenReturn(newQuery);
		when(query.getOperation()).thenReturn(QueryOperationType.SELECT);

		persistenceEngine.executeNamedQueryWithList(NAMED_QUERY_WITH_LIST, LIST_PARAMETER_NAME, values, parameters);
		verify(openJPAEntityManager).createNamedQuery(NAMED_QUERY_WITH_LIST);
		verify(entityManager).createQuery(NAMED_QUERY_WITH_LIST_INSERTED);
		verify(newQuery).setParameter(1, SOME_PARAMETER);
		verify(newQuery).executeUpdate();
	}

	/**
	 * Interface which extends both Persistable and PersistenceInterceptor.
	 */
	private interface FakePersistablePersistenceInterceptor extends Persistable, PersistenceInterceptor {

	}
}
