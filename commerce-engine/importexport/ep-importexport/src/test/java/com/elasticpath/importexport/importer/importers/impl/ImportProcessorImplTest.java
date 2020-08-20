/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.importexport.importer.importers.impl;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.common.dto.customer.CustomerDTO;
import com.elasticpath.importexport.common.exception.runtime.ImportDuplicateAssociatedEntityRuntimeException;
import com.elasticpath.importexport.common.exception.runtime.ImportDuplicateEntityRuntimeException;
import com.elasticpath.importexport.common.exception.runtime.PopulationRollbackException;
import com.elasticpath.importexport.common.exception.runtime.PopulationRuntimeException;
import com.elasticpath.importexport.common.summary.Summary;
import com.elasticpath.importexport.common.summary.impl.SummaryImpl;
import com.elasticpath.importexport.common.types.JobType;
import com.elasticpath.importexport.importer.configuration.ImportConfiguration;
import com.elasticpath.importexport.importer.context.ImportContext;
import com.elasticpath.importexport.importer.importers.ImporterFactory;
import com.elasticpath.persistence.api.Persistable;
import com.elasticpath.persistence.api.PersistenceEngine;
import com.elasticpath.persistence.api.PersistenceSession;
import com.elasticpath.persistence.api.Transaction;

/**
 * Tests {@link ImportProcessorImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ImportProcessorImplTest {

	private static final JobType JOB_TYPE = JobType.CUSTOMER;
	private static final int COMMIT_UNIT = 2;
	private static final String IMPORTER_OBJECT_NAME = CustomerDTO.ROOT_ELEMENT;
	private static final String CUSTOMER_GUID1 = "customerGuid1";
	private static final String CUSTOMER_GUID2 = "customerGuid2";
	private static final Class<CustomerDTO> DTO_CLASS = CustomerDTO.class;

	@InjectMocks
	private ImportProcessorImpl importProcessor;

	@Mock
	private ImporterFactory importerFactory;
	@Mock
	private SavingManager<? extends Persistable> savingManager;
	@Mock
	private PersistenceEngine persistenceEngine;
	@Mock
	private PersistenceSession persistenceSession;
	@Mock
	private Transaction transaction;
	@Mock
	private CustomerImporter customerImporter;

	private ImportContext importContext;
	private ImportConfiguration importConfiguration;
	private Summary summary;
	private InputStream inputStream;
	private CustomerDTO customerDTO1;
	private CustomerDTO customerDTO2;
	private ImportStatusHolder importStatusHolder;

	@Before
	public void setUp() throws Exception {
		importConfiguration = new ImportConfiguration();
		importConfiguration.setXmlValidation(false);
		importContext = new ImportContext(importConfiguration);
		summary = new SummaryImpl();
		importContext.setSummary(summary);
		inputStream = getClass().getClassLoader().getResourceAsStream("customers/testCustomersXmlRepresentation.xml");
		importStatusHolder = new ImportStatusHolder();

		customerDTO1 = createCustomerDTO(CUSTOMER_GUID1);
		customerDTO2 = createCustomerDTO(CUSTOMER_GUID2);

		given(customerImporter.getCommitUnit()).willReturn(COMMIT_UNIT);
		given(customerImporter.getImportedObjectName()).willReturn(IMPORTER_OBJECT_NAME);
		given(customerImporter.getStatusHolder()).willReturn(importStatusHolder);
		given(customerImporter.executeImport(customerDTO1)).willReturn(true);
		given(customerImporter.executeImport(customerDTO2)).willReturn(true);
		willReturn(DTO_CLASS).given(customerImporter).getDtoClass();

		willReturn(customerImporter).given(importerFactory).createImporter(JOB_TYPE, importContext, savingManager);
		given(persistenceEngine.getSharedPersistenceSession()).willReturn(persistenceSession);
		given(persistenceSession.beginTransaction()).willReturn(transaction);
	}

	@Test
	public void testProcessSuccess() throws Exception {
		importProcessor.process(inputStream, importContext);

		verify(customerImporter).executeImport(customerDTO1);
		verify(customerImporter).executeImport(customerDTO2);
		verify(transaction).commit();
	}

	@Test
	public void testProcessContinuePartialImportAfterRollback() throws Exception {
		given(customerImporter.executeImport(customerDTO1)).willThrow(new PopulationRollbackException("rollback"));

		importProcessor.process(inputStream, importContext);

		verify(customerImporter).executeImport(customerDTO2);
		verify(transaction).rollback();
		verify(transaction).commit();
	}

	@Test
	public void testProcessImportDuplicateEntityRuntimeException() throws Exception {
		testProcessIgnorableFailure(new ImportDuplicateEntityRuntimeException("duplicate"));
	}

	@Test
	public void testProcessImportDuplicateAssociatedEntityRuntimeException() throws Exception {
		testProcessIgnorableFailure(new ImportDuplicateAssociatedEntityRuntimeException("associated duplicate"));
	}

	@Test
	public void testProcessPopulationRuntimeException() throws Exception {
		testProcessIgnorableFailure(new PopulationRuntimeException("runtime"));
	}

	private CustomerDTO createCustomerDTO(final String guid) {
		final CustomerDTO dto = new CustomerDTO();
		dto.setGuid(guid);
		dto.setStatus(0);
		return dto;
	}

	public void testProcessIgnorableFailure(final Exception exception) throws Exception {
		given(customerImporter.executeImport(customerDTO2)).willThrow(exception);

		importProcessor.process(inputStream, importContext);

		verify(customerImporter).executeImport(customerDTO1);
		verify(transaction, never()).rollback();
		verify(transaction).commit();
	}
}
