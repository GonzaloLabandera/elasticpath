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

import com.elasticpath.common.dto.customer.AttributePolicyDTO;
import com.elasticpath.domain.customer.AttributePolicy;
import com.elasticpath.domain.customer.PolicyKey;
import com.elasticpath.domain.customer.PolicyPermission;
import com.elasticpath.importexport.common.adapters.DomainAdapter;
import com.elasticpath.importexport.importer.configuration.ImportConfiguration;
import com.elasticpath.importexport.importer.context.ImportContext;
import com.elasticpath.importexport.importer.importers.SavingStrategy;
import com.elasticpath.service.customer.AttributePolicyService;

/**
 * Tests for {@link AttributePolicyImporter}.
 */
@RunWith(MockitoJUnitRunner.class)
public class AttributePolicyImporterTest {

	private static final String GUID = "GUID";

	private static final String POLICY_KEY = PolicyKey.READ_ONLY.getName();

	private static final String POLICY_PERMISSION = PolicyPermission.EMIT.getName();

	@InjectMocks
	private AttributePolicyImporter attributePolicyImporter;

	@Mock
	private AttributePolicyService attributePolicyService;

	@Mock
	private SavingStrategy<AttributePolicy, AttributePolicyDTO> savingStrategy;

	@Mock
	private ImportStatusHolder statusHolder;

	@Mock
	private AttributePolicy attributePolicy;

	@Mock
	private DomainAdapter<AttributePolicy, AttributePolicyDTO> domainAdapter;

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
		given(attributePolicyService.findByGuid(GUID)).willReturn(Optional.ofNullable(attributePolicy));
		final AttributePolicyDTO dto = createCustomerProfilePolicyDTO();
		given(savingStrategy.populateAndSaveObject(attributePolicy, dto)).willReturn(attributePolicy);

		attributePolicyImporter.initialize(importContext, savingStrategy);
		final boolean imported = attributePolicyImporter.executeImport(dto);

		assertThat(imported).isTrue();
	}

	/**
	 * Test for persistent object loading.
	 */
	@Test
	public void testFindPersistentObject() {
		given(attributePolicyService.findByGuid(GUID)).willReturn(Optional.ofNullable(attributePolicy));

		assertThat(attributePolicyImporter.findPersistentObject(createCustomerProfilePolicyDTO()))
				.isEqualTo(attributePolicy);
	}

	/**
	 * Test for dto guid.
	 */
	@Test
	public void testGetDtoGuid() {
		final AttributePolicyDTO dto = createCustomerProfilePolicyDTO();

		assertThat(GUID).isEqualTo(attributePolicyImporter.getDtoGuid(dto));
	}

	/**
	 * Test method for the domain adapter.
	 */
	@Test
	public void testGetDomainAdapter() {
		assertThat(domainAdapter)
				.isEqualTo(attributePolicyImporter.getDomainAdapter());
	}

	/**
	 * Test method for the imported object name.
	 */
	@Test
	public void testGetImportedObjectName() {
		assertThat(AttributePolicyDTO.ROOT_ELEMENT)
				.isEqualTo(attributePolicyImporter.getImportedObjectName());
	}

	/**
	 * The import classes should at least contain the DTO class we are operating on.
	 */
	@Test
	public void testGetDtoClass() {
		assertThat(AttributePolicyDTO.class)
				.isEqualTo(attributePolicyImporter.getDtoClass());
	}

	/**
	 * The auxiliary JAXB class list must not be null (can be empty).
	 */
	@Test
	public void testAuxiliaryJaxbClasses() {
		assertThat(attributePolicyImporter.getAuxiliaryJaxbClasses())
				.isEmpty();
	}

	private AttributePolicyDTO createCustomerProfilePolicyDTO() {
		final AttributePolicyDTO dto = new AttributePolicyDTO();
		dto.setGuid(GUID);
		dto.setPolicyPermission(POLICY_PERMISSION);
		dto.setPolicyKey(POLICY_KEY);
		return dto;
	}
}
