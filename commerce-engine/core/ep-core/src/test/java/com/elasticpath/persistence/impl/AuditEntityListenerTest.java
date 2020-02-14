/*
 * Copyright (c) Elastic Path Software Inc., 2014
 */
package com.elasticpath.persistence.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.FlushModeType;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.apache.openjpa.ee.ManagedRuntime;
import org.apache.openjpa.kernel.Broker;
import org.apache.openjpa.meta.FieldMetaData;
import org.apache.openjpa.meta.ValueMetaData;
import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.commons.ThreadLocalMap;
import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.audit.ChangeOperation;
import com.elasticpath.domain.audit.ChangeTransaction;
import com.elasticpath.domain.catalog.impl.ProductSkuImpl;
import com.elasticpath.persistence.api.ChangeType;
import com.elasticpath.persistence.api.Entity;
import com.elasticpath.persistence.api.Persistable;
import com.elasticpath.persistence.api.PersistenceSession;
import com.elasticpath.persistence.api.Transaction;
import com.elasticpath.persistence.openjpa.JpaPersistenceEngine;
import com.elasticpath.persistence.openjpa.impl.JpaPersistenceEngineImpl;
import com.elasticpath.persistence.openjpa.impl.QueryReader;
import com.elasticpath.service.audit.AuditDao;

/**
 * Unit test for the {@code AuditEntityListener} class.
 */
@RunWith(MockitoJUnitRunner.class)
public class AuditEntityListenerTest {

	private static final long UIDPK = 1000L;

	private AuditEntityListener auditEntityListener;

	@Mock
	private AuditDao auditDao;

	@Mock
	private BeanFactory beanFactory;

	private final JpaPersistenceEngineImpl realJpaPersistenceEngine = new JpaPersistenceEngineImpl();

	@Mock
	private QueryReader queryReader;

	@Mock
	private JpaPersistenceEngine mockJpaPersistenceEngine;

	@Mock
	private OpenJPAEntityManager openJPAEntityManager;

	@Mock
	private ThreadLocalMap<String, Object> metadataMap;

	/**
	 * Interface for mocking an Entity with a method that provides a way to get a mocked field
	 * to avoid calling the OpenJPA Reflection methods.
	 */
	protected interface EntityWithField extends Entity {
		/**
		 * Method for use in mock objects as an easy way to get a field.
		 * @return an object that represents a field
		 */
		Object getField();
	}

	/**
	 * Set up mocks and stubs required by all tests.
	 *
	 * @throws java.lang.Exception in case of errors in test setup
	 */
	@Before
	public void setUp() throws Exception {
		// Instantiate the object under test, mocking out unrequired parts.
		auditEntityListener = new AuditEntityListener() {

			@SuppressWarnings("unchecked")
			@Override
			protected <T> T getField(final Object object, final String fieldName) {
					return (T) ((EntityWithField) object).getField();
			}

			@Override
			protected OpenJPAEntityManager getOpenJPAEntityManager() {
				return openJPAEntityManager;
			}

		};
		auditEntityListener.setBeanFactory(beanFactory);
		auditEntityListener.setMetadataMap(metadataMap);

		realJpaPersistenceEngine.setQueryReader(queryReader);

		when(beanFactory.getSingletonBean(ContextIdNames.AUDIT_DAO, AuditDao.class)).thenReturn(auditDao);
	}

	private void givenRealJpaPersistenceEngine() {
		when(beanFactory.getSingletonBean(ContextIdNames.PERSISTENCE_ENGINE, JpaPersistenceEngine.class)).thenReturn(realJpaPersistenceEngine);
	}

	private void givenMockJpaPersistenceEngine() {
		when(beanFactory.getSingletonBean(ContextIdNames.PERSISTENCE_ENGINE, JpaPersistenceEngine.class)).thenReturn(mockJpaPersistenceEngine);
	}

	/**
	 * Test the rules for whether a Primary Key field is auditable.
	 */
	@Test
	public void testIsPrimaryKeyFieldAuditable() {
		final FieldMetaData fieldMetaData = mock(FieldMetaData.class);
		when(fieldMetaData.isPrimaryKey()).thenReturn(true);
		assertThat(auditEntityListener.isFieldAuditable(fieldMetaData, null))
			.as("Primary key fields should not be auditable")
			.isFalse();
		verify(fieldMetaData).isPrimaryKey();
	}

	/**
	 * Test the rules for whether a Version field is auditable.
	 */
	@Test
	public void testIsVersionFieldAuditable() {
		final FieldMetaData fieldMetaData = mock(FieldMetaData.class);
		when(fieldMetaData.isPrimaryKey()).thenReturn(false);
		when(fieldMetaData.isVersion()).thenReturn(true);
		assertThat(auditEntityListener.isFieldAuditable(fieldMetaData, null))
			.as("Version fields should not be auditable")
			.isFalse();
		verify(fieldMetaData).isVersion();
	}

	/**
	 * Test the rules for whether a transient field is auditable.
	 */
	@Test
	public void testIsTransientFieldAuditable() {
		final FieldMetaData fieldMetaData = mock(FieldMetaData.class);
		when(fieldMetaData.isPrimaryKey()).thenReturn(false);
		when(fieldMetaData.isVersion()).thenReturn(false);
		when(fieldMetaData.isTransient()).thenReturn(true);
		assertThat(auditEntityListener.isFieldAuditable(fieldMetaData, null))
			.as("Transient fields should not be auditable")
			.isFalse();
		verify(fieldMetaData).isTransient();
	}

	/**
	 * Test that non-collection objects are not identified as non-auditable collections.
	 */
	@Test
	public void testIsNonAuditableCollectionOrMapForNormalField() {
		final FieldMetaData fieldMetaData = mock(FieldMetaData.class);
		final Object nonCollectionField = new Object();
		assertThat(auditEntityListener.isNonAuditableCollectionOrMap(fieldMetaData, nonCollectionField))
			.as("Non-collections should be auditable")
			.isFalse();
	}

	/**
	 * Test that collections of non-embedded basic objects are not audited.
	 */
	@Test
	public void testIsNonAuditableCollectionOrMapForBasicCollection() {
		final FieldMetaData fieldMetaData = mock(FieldMetaData.class);
		final ValueMetaData valueMetaData = mock(ValueMetaData.class);
		final Object collectionField = mock(Collection.class);
		when(fieldMetaData.getElement()).thenReturn(valueMetaData);

		when(fieldMetaData.isEmbedded()).thenReturn(false);

		when(valueMetaData.isDeclaredTypePC()).thenReturn(false);
		assertThat(auditEntityListener.isNonAuditableCollectionOrMap(fieldMetaData, collectionField))
			.as("Collections of non-PC objects should not be auditable")
			.isTrue();
		verify(fieldMetaData).getElement();
		verify(fieldMetaData).isEmbedded();
		verify(valueMetaData).isDeclaredTypePC();
	}

	/**
	 * Test that collections of embedded basic objects are audited.
	 */
	@Test
	public void testIsNonAuditableCollectionOrMapForEmbeddedCollection() {
		final FieldMetaData fieldMetaData = mock(FieldMetaData.class);
		final ValueMetaData valueMetaData = mock(ValueMetaData.class);
		final Object collectionField = mock(Collection.class);
		when(fieldMetaData.getElement()).thenReturn(valueMetaData);

		when(fieldMetaData.isEmbedded()).thenReturn(true);

		when(valueMetaData.isDeclaredTypePC()).thenReturn(false);
		assertThat(auditEntityListener.isNonAuditableCollectionOrMap(fieldMetaData, collectionField))
			.as("Collections of embedded objects should be auditable")
			.isFalse();
		verify(fieldMetaData).getElement();
		verify(fieldMetaData).isEmbedded();
	}

	/**
	 * Test that collections of persistable objects are audited.
	 */
	@Test
	public void testIsNonAuditableCollectionOrMapForPCCollection() {
		final FieldMetaData fieldMetaData = mock(FieldMetaData.class);
		final ValueMetaData valueMetaData = mock(ValueMetaData.class);
		final Object collectionField = mock(Collection.class);
		when(fieldMetaData.getElement()).thenReturn(valueMetaData);

		when(valueMetaData.isDeclaredTypePC()).thenReturn(true);
		assertThat(auditEntityListener.isNonAuditableCollectionOrMap(fieldMetaData, collectionField))
			.as("Collections of Persistence objects should be auditable")
			.isFalse();
		verify(fieldMetaData).getElement();
		verify(valueMetaData).isDeclaredTypePC();
	}

	/**
	 * Test that maps of persistable objects are audited.
	 */
	@Test
	public void testIsNonAuditableCollectionOrMapForPCMap() {
		final FieldMetaData fieldMetaData = mock(FieldMetaData.class);
		final ValueMetaData valueMetaData = mock(ValueMetaData.class);
		final Object mapField = mock(Map.class);
		when(fieldMetaData.getElement()).thenReturn(valueMetaData);

		when(valueMetaData.isDeclaredTypePC()).thenReturn(true);
		assertThat(auditEntityListener.isNonAuditableCollectionOrMap(fieldMetaData, mapField))
			.as("Maps of Persistence objects should be auditable")
			.isFalse();
		verify(fieldMetaData).getElement();
		verify(valueMetaData).isDeclaredTypePC();
	}

	/**
	 * Test that a CREATE change type records the new field value.
	 */
	@Test
	public void testRecordFieldChangedCreate() {
		final Entity entity = mock(Entity.class);
		final String field = "Create Field";
		final ChangeOperation operation = mock(ChangeOperation.class);

		auditEntityListener.setCurrentOperation(operation);

		auditEntityListener.recordFieldChanged(entity, "createField", field, ChangeType.CREATE);
		verify(auditDao).persistDataChanged(entity, "createField", ChangeType.CREATE, null, field, operation);
	}

	/**
	 * Test that a DELETE change type records the old field value.
	 */
	@Test
	public void testRecordFieldChangedDelete() {
		final Entity entity = mock(Entity.class);
		final String field = "Delete Field";
		final ChangeOperation operation = mock(ChangeOperation.class);

		auditEntityListener.setCurrentOperation(operation);

		auditEntityListener.recordFieldChanged(entity, "deleteField", field, ChangeType.DELETE);
		verify(auditDao).persistDataChanged(entity, "deleteField", ChangeType.DELETE, field, null, operation);
	}

	/**
	 * Test that changing collection members audits the references.
	 */
	@Test
	public void testRecordCollectionChangedUpdate() {
		givenRealJpaPersistenceEngine();

		final Entity entity = mock(Entity.class, "new");
		final EntityWithField oldEntity = mock(EntityWithField.class, "old");

		final Set<Persistable> newCollection = new HashSet<>();
		final Set<Persistable> oldCollection = new HashSet<>();
		final Entity unchangedMember = mock(Entity.class, "unchangedMember");
		final Entity newMember = mock(Entity.class, "newMember");
		final Entity removedMember = mock(Entity.class, "removedMember");
		oldCollection.add(unchangedMember);
		oldCollection.add(removedMember);
		newCollection.add(unchangedMember);
		newCollection.add(newMember);

		final String fieldName = "changedCollection";

		final ChangeOperation operation = mock(ChangeOperation.class);
		auditEntityListener.setCurrentOperation(operation);

		final EntityManager entityManager = mock(EntityManager.class);
		realJpaPersistenceEngine.setEntityManager(entityManager);

		when(entity.getUidPk()).thenReturn(UIDPK);
		doReturn(oldEntity).when(queryReader).load(entity.getClass(), UIDPK);
		when(oldEntity.getField()).thenReturn(oldCollection);
		when(removedMember.getGuid()).thenReturn("OLDGUID");
		when(newMember.getGuid()).thenReturn("NEWGUID");

		auditEntityListener.recordCollectionChanged(entity, fieldName, newCollection, ChangeType.UPDATE);

		verify(entity).getUidPk();
		verify(queryReader).load(entity.getClass(), UIDPK);
		verify(oldEntity).getField();
		verify(removedMember).getGuid();
		verify(newMember).getGuid();
		verify(auditDao).persistDataChanged(entity, fieldName, ChangeType.DELETE, "OLDGUID", null, operation);
		verify(auditDao).persistDataChanged(entity, fieldName, ChangeType.CREATE, null, "NEWGUID", operation);
	}

	/**
	 * Test that a UPDATE change type records the old and new field values.
	 */
	@Test
	public void testRecordFieldChangedUpdate() {
		givenRealJpaPersistenceEngine();

		final Entity entity = mock(Entity.class, "new");
		final EntityWithField oldEntity = mock(EntityWithField.class, "old");
		final String field = "Update Field";
		final String oldField = "Old Field";

		final ChangeOperation operation = mock(ChangeOperation.class);

		auditEntityListener.setCurrentOperation(operation);

		final EntityManager entityManager = mock(EntityManager.class);
		realJpaPersistenceEngine.setEntityManager(entityManager);

		when(entity.getUidPk()).thenReturn(UIDPK);

		doReturn(oldEntity).when(queryReader).load(entity.getClass(), UIDPK);

		when(oldEntity.getField()).thenReturn(oldField);

		auditEntityListener.recordFieldChanged(entity, "updateField", field, ChangeType.UPDATE);

		verify(entity).getUidPk();
		verify(queryReader).load(entity.getClass(), UIDPK);
		verify(oldEntity).getField();
		verify(auditDao).persistDataChanged(entity, "updateField", ChangeType.UPDATE, oldField, field, operation);
	}

	/**
	 * Test that creating a new collection records new references.
	 */
	@Test
	public void testRecordCollectionChangedCreate() {
		givenRealJpaPersistenceEngine();

		final Entity entity = mock(Entity.class);
		final Persistable member = mock(Persistable.class);
		final Set<Persistable> collection = new HashSet<>();
		collection.add(member);
		final long memberUid = 12345L;

		final ChangeOperation operation = mock(ChangeOperation.class);

		auditEntityListener.setCurrentOperation(operation);

		when(member.getUidPk()).thenReturn(memberUid);
		auditEntityListener.recordCollectionChanged(entity, "newCollection", collection, ChangeType.CREATE);

		verify(member).getUidPk();
		verify(auditDao).persistDataChanged(entity, "newCollection", ChangeType.CREATE, null, String.valueOf(memberUid), operation);
	}

	/**
	 * Test that creating a new collection records old references.
	 */
	@Test
	public void testRecordCollectionChangedDelete() {
		final Entity entity = mock(Entity.class);
		final Set<Persistable> collection = new HashSet<>();
		final Persistable member = mock(Persistable.class);
		collection.add(member);
		final long memberUid = 12345L;

		final ChangeOperation operation = mock(ChangeOperation.class);

		auditEntityListener.setCurrentOperation(operation);

		when(member.getUidPk()).thenReturn(memberUid);
		auditEntityListener.recordCollectionChanged(entity, "deletedCollection", collection, ChangeType.DELETE);

		verify(member).getUidPk();
		verify(auditDao).persistDataChanged(entity, "deletedCollection", ChangeType.DELETE, String.valueOf(memberUid), null, operation);
	}

	/**
	 * Test that creating a new map records the new key=value pair values.
	 */
	@Test
	public void testRecordMapChangedCreate() {
		final Entity entity = mock(Entity.class);
		final Persistable member = mock(Persistable.class);
		final Map<Object, Persistable> map = new HashMap<>();
		map.put("key", member);
		final long memberUid = 12345L;

		final ChangeOperation operation = mock(ChangeOperation.class);

		auditEntityListener.setCurrentOperation(operation);

		when(member.getUidPk()).thenReturn(memberUid);
		auditEntityListener.recordMapChanged(entity, "newMap", map, ChangeType.CREATE);

		verify(member).getUidPk();
		verify(auditDao).persistDataChanged(entity, "newMap", ChangeType.CREATE, null, "key=" + memberUid, operation);
	}

	/**
	 * Test that deleting a map records the old key=value pair values.
	 */
	@Test
	public void testRecordMapChangedDelete() {
		final Entity entity = mock(Entity.class);
		final Persistable member = mock(Persistable.class);
		final Map<Object, Persistable> map = new HashMap<>();
		map.put("key", member);
		final long memberUid = 12345L;

		final ChangeOperation operation = mock(ChangeOperation.class);

		auditEntityListener.setCurrentOperation(operation);

		when(member.getUidPk()).thenReturn(memberUid);
		auditEntityListener.recordMapChanged(entity, "deletedMap", map, ChangeType.DELETE);

		verify(member).getUidPk();
		verify(auditDao).persistDataChanged(entity, "deletedMap", ChangeType.DELETE, "key=" + memberUid, null, operation);
	}

	/**
	 * Test that updating a map records the removed and added key=value pair values.
	 */
	@Test
	public void testRecordMapChangedUpdate() {
		givenRealJpaPersistenceEngine();

		final Entity entity = mock(Entity.class, "new");
		final EntityWithField oldEntity = mock(EntityWithField.class, "old");
		final String fieldName = "updatedMap";

		final Entity oldMember = mock(Entity.class, "oldMember");
		final Entity orphanMember = mock(Entity.class, "orphanMember");
		final Entity newMember = mock(Entity.class, "newMember");
		final Entity unchangedMember = mock(Entity.class, "unchangedMember");
		final Entity adoptedMember = mock(Entity.class, "adoptedMember");

		final Map<Object, Persistable> oldMap = new HashMap<>();
		oldMap.put("oldKey", oldMember);
		oldMap.put("unchangedKey", unchangedMember);
		oldMap.put("changedKey", orphanMember);

		final Map<Object, Persistable> newMap = new HashMap<>();
		newMap.put("unchangedKey", unchangedMember);
		newMap.put("changedKey", adoptedMember);
		newMap.put("newKey", newMember);

		final ChangeOperation operation = mock(ChangeOperation.class);
		auditEntityListener.setCurrentOperation(operation);

		final EntityManager entityManager = mock(EntityManager.class);
		realJpaPersistenceEngine.setEntityManager(entityManager);

		when(entity.getUidPk()).thenReturn(UIDPK);
		doReturn(oldEntity).when(queryReader).load(entity.getClass(), UIDPK);
		when(oldEntity.getField()).thenReturn(oldMap);

		when(oldMember.getGuid()).thenReturn("OLDGUID");
		when(orphanMember.getGuid()).thenReturn("ORPHAN");
		when(newMember.getGuid()).thenReturn("NEWGUID");
		when(adoptedMember.getGuid()).thenReturn("ADOPTEE");

		auditEntityListener.recordMapChanged(entity, fieldName, newMap, ChangeType.UPDATE);

		verify(entity).getUidPk();
		verify(queryReader).load(entity.getClass(), UIDPK);
		verify(oldEntity).getField();
		verify(oldMember).getGuid();
		verify(orphanMember).getGuid();
		verify(newMember).getGuid();
		verify(adoptedMember).getGuid();
		verify(auditDao).persistDataChanged(entity, fieldName, ChangeType.CREATE, null, "newKey=NEWGUID", operation);
		verify(auditDao).persistDataChanged(entity, fieldName, ChangeType.DELETE, "oldKey=OLDGUID", null, operation);
		verify(auditDao).persistDataChanged(entity, fieldName, ChangeType.UPDATE, "changedKey=ORPHAN", "changedKey=ADOPTEE", operation);
	}

	/**
	 * Test that beginning a change operation stores the operation details.
	 */
	@Test
	public void testBeginOperation() throws Exception {
		givenMockJpaPersistenceEngine();

		final Entity entity = new ProductSkuImpl();  //Any entity object
		final Object transactionKey = new Object();
		final Transaction transaction = mock(Transaction.class);
		final ChangeTransaction changeTransaction = mock(ChangeTransaction.class);
		final PersistenceSession persistenceSession = mock(PersistenceSession.class);


		List<String> auditableClasses = new ArrayList<>();
		auditableClasses.add("com.elasticpath.domain.catalog.impl.ProductSkuImpl");
		auditEntityListener.setAuditableClasses(auditableClasses);

		final Broker broker = mock(Broker.class);
		final ManagedRuntime managedRuntime = mock(ManagedRuntime.class);

		when(mockJpaPersistenceEngine.getPersistenceSession()).thenReturn(persistenceSession);
		when(persistenceSession.beginTransaction()).thenReturn(transaction);
		when(openJPAEntityManager.getFlushMode()).thenReturn(FlushModeType.AUTO);

		when(auditDao.persistChangeSetTransaction(String.valueOf(transactionKey.hashCode()), entity, metadataMap)).thenReturn(changeTransaction);

		when(mockJpaPersistenceEngine.getBroker()).thenReturn(broker);
		when(broker.getManagedRuntime()).thenReturn(managedRuntime);
		when(managedRuntime.getTransactionKey()).thenReturn(transactionKey);
		auditEntityListener.beginSingleOperation(entity, ChangeType.CREATE);

		verify(mockJpaPersistenceEngine).getPersistenceSession();
		verify(persistenceSession).beginTransaction();
		verify(openJPAEntityManager).getFlushMode();
		verify(openJPAEntityManager).setFlushMode(FlushModeType.COMMIT);
		verify(auditDao).persistChangeSetTransaction(String.valueOf(transactionKey.hashCode()), entity, metadataMap);
		verify(auditDao).persistSingleChangeOperation(entity, ChangeType.CREATE, changeTransaction, 1);
		verify(transaction).commit();
		verify(openJPAEntityManager).setFlushMode(FlushModeType.AUTO);
	}
}
