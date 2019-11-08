/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.importexport.importer.importers.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.common.dto.customer.StoreCustomerAttributeDTO;
import com.elasticpath.domain.customer.PolicyKey;
import com.elasticpath.domain.customer.StoreCustomerAttribute;
import com.elasticpath.importexport.common.adapters.DomainAdapter;
import com.elasticpath.importexport.importer.configuration.ImportConfiguration;
import com.elasticpath.importexport.importer.context.ImportContext;
import com.elasticpath.importexport.importer.importers.SavingStrategy;
import com.elasticpath.service.customer.StoreCustomerAttributeService;

/**
 * Tests for {@link StoreCustomerAttributeImporter}.
 */
@RunWith(MockitoJUnitRunner.class)
public class StoreCustomerAttributeImporterTest {

	private static final String GUID = "GUID";

	private static final String STORE_CODE = "storeCode";

	private static final String ATTRIBUTE_KEY = "attributeKey";

	private static final String POLICY_KEY = PolicyKey.READ_ONLY.getName();

	@InjectMocks
	private StoreCustomerAttributeImporter storeCustomerAttributeImporter;

	@Mock
	private StoreCustomerAttributeService storeCustomerAttributeService;

	@Mock
	private SavingStrategy<StoreCustomerAttribute, StoreCustomerAttributeDTO> savingStrategy;

	@Mock
	private ImportStatusHolder statusHolder;

	@Mock
	private StoreCustomerAttribute storeCustomerAttribute;

	@Mock
	private DomainAdapter<StoreCustomerAttribute, StoreCustomerAttributeDTO> domainAdapter;

	private ImportContext importContext;

	/**
	 * Test initialization.
	 */
	@Before
	public void setUp() {
		final ImportConfiguration importConfiguration = new ImportConfiguration();
		importContext = new ImportContext(importConfiguration);

		willDoNothing().given(statusHolder).setImportStatus(anyString());
	}

	/**
	 * Test for import execution.
	 */
	@Test
	public void testExecuteImport() {
		given(storeCustomerAttributeService.findByGuid(GUID)).willReturn(Optional.ofNullable(storeCustomerAttribute));
		final StoreCustomerAttributeDTO storeCustomerAttributeDTO = createStoreCustomerAttributeDTO();
		given(savingStrategy.populateAndSaveObject(storeCustomerAttribute, storeCustomerAttributeDTO)).willReturn(storeCustomerAttribute);

		storeCustomerAttributeImporter.initialize(importContext, savingStrategy);
		final boolean imported = storeCustomerAttributeImporter.executeImport(storeCustomerAttributeDTO);

		assertThat(imported).isTrue();
	}

	/**
	 * Test for persistent object loading.
	 */
	@Test
	public void testFindPersistentObject() {
		given(storeCustomerAttributeService.findByGuid(GUID)).willReturn(Optional.ofNullable(storeCustomerAttribute));

		assertThat(storeCustomerAttributeImporter.findPersistentObject(createStoreCustomerAttributeDTO()))
				.isEqualTo(storeCustomerAttribute);
	}

	/**
	 * Test for dto guid.
	 */
	@Test
	public void testGetDtoGuid() {
		final StoreCustomerAttributeDTO dto = createStoreCustomerAttributeDTO();

		assertThat(GUID).isEqualTo(storeCustomerAttributeImporter.getDtoGuid(dto));
	}

	/**
	 * Test method for the domain adapter.
	 */
	@Test
	public void testGetDomainAdapter() {
		assertThat(domainAdapter)
				.isEqualTo(storeCustomerAttributeImporter.getDomainAdapter());
	}

	/**
	 * Test method for the imported object name.
	 */
	@Test
	public void testGetImportedObjectName() {
		assertThat(StoreCustomerAttributeDTO.ROOT_ELEMENT)
				.isEqualTo(storeCustomerAttributeImporter.getImportedObjectName());
	}

	/**
	 * The import classes should at least contain the DTO class we are operating on.
	 */
	@Test
	public void testGetDtoClass() {
		assertThat(StoreCustomerAttributeDTO.class)
				.isEqualTo(storeCustomerAttributeImporter.getDtoClass());
	}

	/**
	 * The auxiliary JAXB class list must not be null (can be empty).
	 */
	@Test
	public void testAuxiliaryJaxbClasses() {
		assertThat(storeCustomerAttributeImporter.getAuxiliaryJaxbClasses())
				.isEmpty();
	}

	private StoreCustomerAttributeDTO createStoreCustomerAttributeDTO() {
		final StoreCustomerAttributeDTO dto = new StoreCustomerAttributeDTO();
		dto.setGuid(GUID);
		dto.setStoreCode(STORE_CODE);
		dto.setAttributeKey(ATTRIBUTE_KEY);
		dto.setPolicyKey(POLICY_KEY);
		return dto;
	}
}
