/**
 * Copyright (c) Elastic Path Software Inc., 2008
 */
package com.elasticpath.persistence.openjpa.impl;

import static com.elasticpath.persistence.api.PersistenceConstants.LIST_PARAMETER_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

import org.apache.openjpa.persistence.OpenJPAEntityManagerSPI;
import org.apache.openjpa.persistence.OpenJPAQuery;
import org.apache.openjpa.persistence.QueryOperationType;
import org.mockito.InjectMocks;
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
import com.elasticpath.persistence.openjpa.util.QueryUtil;

/**
 * Test that the methods of the JPA Persistence Engine behave as expected.
 */
@SuppressWarnings({"unchecked", "PMD.TooManyMethods", "PMD.ExcessiveClassLength", "PMD.UselessOverridingMethod", "PMD.GodClass"})
@RunWith(MockitoJUnitRunner.class)
public class JpaPersistenceEngineImplTest {

	private static final String MERGED_OBJECT = "mergedObject";

	private static final String SOME_PARAMETER = "someParameter";

	private static final String SOME_OTHER_PARAMETER = "someOtherParameter";

	private static final String NAMED_QUERY = "NAMED_QUERY";

	private static final String NAMED_QUERY_WITH_LIST = "SELECT FROM SOMETHING s WHERE s.uidPk IN (:list)";

	@InjectMocks
	private JpaPersistenceEngineImpl persistenceEngine;

	@Mock
	private EntityManager entityManager;
	@Mock
	private PersistenceSessionFactory sessionFactory;
	@Mock
	private QueryReader queryReader;
	@Mock
	private QueryUtil queryUtil;

	private static final long UIDPK = 1000L;

	private static final long UIDPK2 = 1001L;

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

		when(queryReader.load(persistenceClass, UIDPK)).thenReturn(object);

		Persistable result = persistenceEngine.get(persistenceClass, UIDPK);

		verify(queryReader).load(persistenceClass, UIDPK);
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

		when(queryReader.load(persistenceClass, UIDPK)).thenReturn(object);

		Persistable result = persistenceEngine.load(persistenceClass, UIDPK);

		verify(queryReader).load(persistenceClass, UIDPK);
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
		verify(newEntityManager).find(persistenceClass, UIDPK);
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
		verify(queryUtil).setQueryParameters(query, parameters);
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
		final List<Object> results1 = new ArrayList<>();
		final List<Object> results2 = new ArrayList<>();
		final Persistable obj1 = mock(Persistable.class, "result1");
		final Persistable obj2 = mock(Persistable.class, "result2");
		results1.add(obj1);
		results2.add(obj2);

		// Set expectations
		when(queryReader.retrieve(query1)).thenReturn(results1);
		when(queryReader.retrieve(query2)).thenReturn(results2);

		List<Persistable> results = persistenceEngine.retrieve(queries);

		assertThat(results).containsExactlyInAnyOrder(obj1, obj2);

		verify(queryReader).retrieve(query1);
		verify(queryReader).retrieve(query2);
	}

	/**
	 * Test that calling retrieve with a collection of queries that take parameters will create the query objects,
	 * set the parameters on each, and consolidate the results.
	 */
	@Test
	public final void testRetrieveListOfQueriesWithParameters() {
		// Create the list of query strings
		final Object[] parameters = new Object[]{SOME_PARAMETER};
		final String query1 = "SELECT fo FROM FirstObjectImpl fo WHERE fo.field = ?1";
		final String query2 = "SELECT so FROM SecondObjectImpl so WHERE fo.field = ?1";
		final List<String> queries = new ArrayList<>();
		queries.add(query1);
		queries.add(query2);

		// Mock the Query objects and Query results
		final List<Object> results1 = new ArrayList<>();
		final List<Object> results2 = new ArrayList<>();
		final Persistable obj1 = mock(Persistable.class, "result1");
		final Persistable obj2 = mock(Persistable.class, "result2");
		results1.add(obj1);
		results2.add(obj2);

		when(queryReader.retrieve(query1, parameters)).thenReturn(results1);
		when(queryReader.retrieve(query2, parameters)).thenReturn(results2);


		List<Persistable> results = persistenceEngine.retrieve(queries, parameters);

		assertThat(results).containsExactlyInAnyOrder(obj1, obj2);

		verify(queryReader).retrieve(query1, parameters);
		verify(queryReader).retrieve(query2, parameters);
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
		verify(queryUtil).setQueryParameters(query, new Object[0]);
		assertThat(result)
			.as("The return value should be the row count returned by executeUpdate")
			.isEqualTo(rowsUpdated);
	}

	/**
	 * Test that when save is called that beginSingleOperation() and endSingleOperation() events are fired.
	 */
	@Test
	public void testBeginEndSingleOperationEventsAreFiredOnSave() {
		final FakePersistablePersistenceInterceptor persistableObject = mock(FakePersistablePersistenceInterceptor.class);
		final PersistenceEngineOperationListener listener = mock(PersistenceEngineOperationListener.class);

		persistenceEngine.addPersistenceEngineOperationListener(listener);

		persistenceEngine.save(persistableObject);
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

		persistenceEngine.addPersistenceEngineOperationListener(listener);

		when(entityManager.merge(persistableObject)).thenAnswer(answer -> mergedObject);

		persistenceEngine.merge(persistableObject);
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

		persistenceEngine.addPersistenceEngineOperationListener(listener);

		when(entity.getUidPk()).thenReturn(UIDPK);
		when(entityManager.getReference(entity.getClass(), UIDPK)).thenAnswer(answer -> entity);

		persistenceEngine.delete(entity);

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

		persistenceEngine.addPersistenceEngineOperationListener(listener);

		persistenceEngine.save(persistableObject);
		persistenceEngine.save(persistableObject);

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
		final Object[] parameters = new Object[] { SOME_PARAMETER, SOME_OTHER_PARAMETER };

		when(queryReader.retrieveByNamedQuery(NAMED_QUERY, FlushMode.COMMIT, parameters)).thenReturn(ImmutableList.of("result"));

		List<Object> result = persistenceEngine.retrieveByNamedQuery(NAMED_QUERY, FlushMode.COMMIT, parameters);

		verify(queryReader).retrieveByNamedQuery(NAMED_QUERY, FlushMode.COMMIT, parameters);

		assertThat(result)
			.as("Retrieve gives back results from OpenJPA")
			.containsOnly("result");
	}

	@Test
	public void testRetrieveWithNamedParametersHappyPath() {
		// Given
		final String adhocQueryString = "SELECT Entity e WHERE e.foo = :foo AND e.bar = :bar";
		final List<Object> entities = Collections.singletonList("entity1");

		Map<String, String> parameters = ImmutableMap.of("foo", "fooParam", "bar", "barParam");

		when(queryReader.retrieveWithNamedParameters(adhocQueryString, parameters)).thenReturn(entities);

		// When
		List<String> results = persistenceEngine.retrieveWithNamedParameters(adhocQueryString, parameters);

		// Then
		verify(queryReader).retrieveWithNamedParameters(adhocQueryString, parameters);

		assertThat(results)
			.as("Query should have been evaluated with the proper params")
			.isEqualTo(entities);
	}

	@Test
	public void testRetrieveWithNamedParametersWorksFineWithoutParameters() {
		// Given
		final List<Object> entities = Collections.singletonList("entity1");
		final String adhocQueryString = "SELECT Entity e WHERE e.code = 'hardcodedValue'";
		final Map<String, String> parameters = Collections.emptyMap();

		when(queryReader.retrieve(adhocQueryString, parameters)).thenReturn(entities);

		// When
		List<String> results = persistenceEngine.retrieve(adhocQueryString, parameters);

		// Then
		verify(queryReader).retrieve(adhocQueryString, parameters);
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

		persistenceEngine.retrieve(queryStr, firstResult, maxResults);

		verify(queryReader).retrieve(queryStr, firstResult, maxResults);
	}

	/**
	 * Test method for retrieve with named parameters.
	 */
	@Test
	public final void testRetrieveStringNamedParameter() {
		final String query = "SELECT so FROM SomeObjectImpl so WHERE so.field = ?1";
		final Object[] parameters = new Object[] { SOME_PARAMETER };

		when(queryReader.retrieve(query, parameters)).thenReturn(Collections.emptyList());

		persistenceEngine.retrieve(query, parameters);

		verify(queryReader).retrieve(query, parameters);
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
		verify(queryUtil).setQueryParameters(jpaQuery, parameters);
		verify(queryUtil).getResults(jpaQuery);
	}

	/**
	 * Test method for {@link com.elasticpath.persistence.openjpa.impl.JpaPersistenceEngineImpl
	 * #retrieveWithListWithNewSession(java.lang.String, java.lang.String, java.util.Collection, java.lang.Object[])}.
	 */
	@Test
	public final void testRetrieveWithListWithNewSession() {
		final String query = "SELECT so FROM SomeObjectImpl so WHERE so.uidPk in (:list) AND so.field = :someField";
		final Map<String, Object> parameters = ImmutableMap.of("somefield", SOME_PARAMETER);
		final Set<Long> values = ImmutableSet.of(UIDPK, UIDPK2);
		final Query newQuery = mock(Query.class);
		final OpenJPAQuery<Entity> jpaQuery = mock(OpenJPAQuery.class);
		final JpaPersistenceSession session = mock(JpaPersistenceSession.class);

		when(sessionFactory.createPersistenceSession()).thenReturn(session);
		when(session.getEntityManager()).thenReturn(entityManager);
		when(entityManager.createQuery(query)).thenReturn(jpaQuery);
		when(queryUtil.insertListIntoQuery(jpaQuery, LIST_PARAMETER_NAME, "1000,1001")).thenReturn(newQuery);
		when(queryUtil.splitCollection(values, parameters.size())).thenReturn(Lists.newArrayList("1000,1001"));

		persistenceEngine.retrieveWithListWithNewSession(query, LIST_PARAMETER_NAME, values, parameters);

		verify(queryUtil).setQueryParameters(newQuery, new Object[]{parameters});
		verify(queryUtil).getResults(newQuery);
	}

	/**
	 * Test method for retrieveWithNewSession.
	 */
	@Test
	public final void testRetrieveWithNewSessionString() {
		final String query = "SELECT so FROM SomeObjectImpl so";
		final JpaPersistenceSession session = mock(JpaPersistenceSession.class);
		final Query jpaQuery = mock(Query.class);
		final Map<String, String> parameters = Collections.emptyMap();

		when(sessionFactory.createPersistenceSession()).thenReturn(session);
		when(session.getEntityManager()).thenReturn(entityManager);
		when(entityManager.createQuery(query)).thenReturn(jpaQuery);

		persistenceEngine.retrieveWithNewSession(query, parameters);

		verify(sessionFactory).createPersistenceSession();
		verify(session).getEntityManager();
		verify(entityManager).createQuery(query);
		verify(queryUtil).setQueryParameters(jpaQuery, new Object[]{parameters});
		verify(queryUtil).getResults(jpaQuery);
	}

	/**
	 * Test method for retrieveByNamedQuery.
	 */
	@Test
	public final void testRetrieveByNamedQueryString() {
		persistenceEngine.retrieveByNamedQuery(NAMED_QUERY);

		verify(queryReader).retrieveByNamedQuery(NAMED_QUERY);
	}

	/**
	 * Test method for {@link JpaPersistenceEngineImpl#retrieveByNamedQuery(java.lang.String, java.lang.Object[])}.
	 */
	@Test
	public final void testRetrieveByNamedQueryStringObjectArray() {
		final Object[] parameters = new Object[] { SOME_PARAMETER };

		persistenceEngine.retrieveByNamedQuery(NAMED_QUERY, parameters);

		verify(queryReader).retrieveByNamedQuery(NAMED_QUERY, parameters);
	}

	/**
	 * Test method for {@link JpaPersistenceEngineImpl#retrieveByNamedQuery(java.lang.String, java.util.Map)}.
	 */
	@Test
	public final void testRetrieveByNamedQueryStringMapOfStringObject() {
		Map<String, Object> parameters = ImmutableMap.of("parameter", SOME_PARAMETER);

		persistenceEngine.retrieveByNamedQuery(NAMED_QUERY, parameters);

		verify(queryReader).retrieveByNamedQuery(NAMED_QUERY, parameters);
	}

	/**
	 * Test method for {@link JpaPersistenceEngineImpl#retrieveByNamedQuery(java.lang.String, int, int)}.
	 */
	@Test
	public final void testRetrieveByNamedQueryStringIntInt() {
		final int firstResult = 1;
		final int maxResults = 10;

		persistenceEngine.retrieveByNamedQuery(NAMED_QUERY, firstResult, maxResults);

		verify(queryReader).retrieveByNamedQuery(NAMED_QUERY, firstResult, maxResults);
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

		persistenceEngine.retrieveByNamedQuery(NAMED_QUERY, parameters, firstResult, maxResults);

		verify(queryReader).retrieveByNamedQuery(NAMED_QUERY, parameters, firstResult, maxResults);
	}

	/**
	 * Test method for {@link com.elasticpath.persistence.openjpa.impl.JpaPersistenceEngineImpl
	 * #retrieveByNamedQueryWithList(java.lang.String, java.lang.String, java.util.Collection)}.
	 */
	@Test
	public final void testRetrieveByNamedQueryWithListStringStringCollectionOfE() {
		final Set<Long> values = ImmutableSet.of(UIDPK, UIDPK2);

		persistenceEngine.retrieveByNamedQueryWithList(NAMED_QUERY_WITH_LIST, LIST_PARAMETER_NAME, values);

		verify(queryReader).retrieveByNamedQueryWithList(NAMED_QUERY_WITH_LIST, LIST_PARAMETER_NAME, values);
	}

	/**
	 * Test method for {@link com.elasticpath.persistence.openjpa.impl.JpaPersistenceEngineImpl
	 * #retrieveByNamedQueryWithList(java.lang.String, java.lang.String, java.util.Collection, java.lang.Object[])}.
	 */
	@Test
	public final void testRetrieveByNamedQueryWithListStringStringCollectionOfEObjectArray() {
		final Object[] parameters = new Object[] { SOME_PARAMETER };
		final Set<Long> values = ImmutableSet.of(UIDPK, UIDPK2);

		persistenceEngine.retrieveByNamedQueryWithList(NAMED_QUERY_WITH_LIST, LIST_PARAMETER_NAME, values, parameters);

		verify(queryReader).retrieveByNamedQueryWithList(NAMED_QUERY_WITH_LIST, LIST_PARAMETER_NAME, values, parameters);
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
		verify(queryUtil).setQueryParameters(query, parameters);
		verify(query).executeUpdate();
	}

	/**
	 * Test method for {@link com.elasticpath.persistence.openjpa.impl.JpaPersistenceEngineImpl
	 * #executeNamedQueryWithList(java.lang.String, java.lang.String, java.util.Collection)}.
	 */
	@Test
	public final void testExecuteNamedQueryWithListStringStringCollectionOfE() {
		final Set<Long> values = ImmutableSet.of(UIDPK, UIDPK2);
		final String csvValues = UIDPK + "," + UIDPK2;
		final OpenJPAQuery<Entity> query = mock(OpenJPAQuery.class);
		final Query newQuery = mock(Query.class);

		when(entityManager.createNamedQuery(NAMED_QUERY_WITH_LIST)).thenReturn(query);
		when(queryUtil.insertListIntoQuery(query, LIST_PARAMETER_NAME, csvValues)).thenReturn(newQuery);
		when(queryUtil.splitCollection(values, 0)).thenReturn(Lists.newArrayList(csvValues));
		when(query.getOperation()).thenReturn(QueryOperationType.SELECT);

		persistenceEngine.executeNamedQueryWithList(NAMED_QUERY_WITH_LIST, LIST_PARAMETER_NAME, values);

		verify(entityManager).createNamedQuery(NAMED_QUERY_WITH_LIST);
		verify(newQuery).executeUpdate();
		verify(queryUtil).setQueryParameters(newQuery, new Object[0]);
	}

	/**
	 * Test method for {@link com.elasticpath.persistence.openjpa.impl.JpaPersistenceEngineImpl
	 * #executeNamedQueryWithList(java.lang.String, java.lang.String, java.util.Collection, java.lang.Object[])}.
	 */
	@Test
	public final void testExecuteNamedQueryWithListStringStringCollectionOfEObjectArray() {
		final Object[] parameters = new Object[] { SOME_PARAMETER };
		final Set<Long> values = ImmutableSet.of(UIDPK, UIDPK2);
		final String csvValues = UIDPK + "," + UIDPK2;
		final OpenJPAQuery<Entity> query = mock(OpenJPAQuery.class);
		final Query newQuery = mock(Query.class);

		when(entityManager.createNamedQuery(NAMED_QUERY_WITH_LIST)).thenReturn(query);
		when(queryUtil.insertListIntoQuery(query, LIST_PARAMETER_NAME, csvValues)).thenReturn(newQuery);
		when(queryUtil.splitCollection(values, 0)).thenReturn(Lists.newArrayList(csvValues));
		when(query.getOperation()).thenReturn(QueryOperationType.SELECT);

		persistenceEngine.executeNamedQueryWithList(NAMED_QUERY_WITH_LIST, LIST_PARAMETER_NAME, values, parameters);

		verify(entityManager).createNamedQuery(NAMED_QUERY_WITH_LIST);
		verify(newQuery).executeUpdate();
		verify(queryUtil).setQueryParameters(newQuery, parameters);
	}

	/**
	 * Interface which extends both Persistable and PersistenceInterceptor.
	 */
	private interface FakePersistablePersistenceInterceptor extends Persistable, PersistenceInterceptor {

	}
}
