/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.importexport.importer.importers.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.common.dto.customer.UserAccountAssociationDTO;
import com.elasticpath.domain.customer.UserAccountAssociation;
import com.elasticpath.importexport.common.adapters.DomainAdapter;
import com.elasticpath.importexport.importer.configuration.ImportConfiguration;
import com.elasticpath.importexport.importer.context.ImportContext;
import com.elasticpath.importexport.importer.importers.SavingStrategy;
import com.elasticpath.importexport.importer.types.ImportStrategyType;
import com.elasticpath.service.customer.UserAccountAssociationService;

/**
 * Test for <code>UserAccountAssociationImporterImpl</code>.
 */
@RunWith(MockitoJUnitRunner.class)
public class UserAccountAssociationImporterTest {

	private static final String GUID1 = "GUID1";
	private static final String GUID2 = "GUID2";
	private static final String ROLE = "BUYER";

	@Mock
	private DomainAdapter<UserAccountAssociation, UserAccountAssociationDTO> domainAdapter;

	@Mock
	private UserAccountAssociationService userAccountAssociationService;

	@Mock
	private SavingStrategy<UserAccountAssociation, UserAccountAssociationDTO> savingStrategy;

	@InjectMocks
	private UserAccountAssociationImporterImpl userAccountAssociationImporter;

	private final ImportConfiguration importConfiguration = new ImportConfiguration();


	/**
	 * SetUps the test.
	 */
	@Before
	public void setUp() {
		importConfiguration.setImporterConfigurationMap(new HashMap<>());
		userAccountAssociationImporter.initialize(new ImportContext(importConfiguration), savingStrategy);
		userAccountAssociationImporter.setStatusHolder(new ImportStatusHolder());
		userAccountAssociationImporter.setUserAccountAssociationService(userAccountAssociationService);
		userAccountAssociationImporter.setUserAccountAssociationAdapter(domainAdapter);
		savingStrategy = mockSavingStrategy();
	}

	/**
	 * Check an import of user account associations.
	 */
	@Test
	public void testExecuteImport() {
		assertThatCode(() -> userAccountAssociationImporter.executeImport(createUserAccountAssociationDTO())).doesNotThrowAnyException();
	}

	/**
	 * Test method for {@link UserAccountAssociationImporterImpl#getDomainAdapter()}.
	 */
	@Test
	public void testGetDomainAdapter() {
		assertThat(domainAdapter)
				.isEqualTo(userAccountAssociationImporter.getDomainAdapter());
	}

	/**
	 * Test method for {@link UserAccountAssociationImporterImpl#getImportedObjectName()}.
	 */
	@Test
	public void testGetImportedObjectName() {
		assertThat(UserAccountAssociationDTO.ROOT_ELEMENT)
				.isEqualTo(userAccountAssociationImporter.getImportedObjectName());
	}

	/**
	 * The import classes should at least contain the DTO class we are operating on.
	 */
	@Test
	public void testDtoClass() {
		assertThat(UserAccountAssociationDTO.class)
				.isEqualTo(userAccountAssociationImporter.getDtoClass());
	}

	/**
	 * The auxiliary JAXB class list must not be null (can be empty).
	 */
	@Test
	public void testAuxiliaryJaxbClasses() {
		assertThat(userAccountAssociationImporter.getAuxiliaryJaxbClasses())
				.isNotNull();
	}

	private UserAccountAssociationDTO createUserAccountAssociationDTO() {
		final UserAccountAssociationDTO userAccountAssociationDTO = new UserAccountAssociationDTO();
		userAccountAssociationDTO.setAccountGuid(GUID1);
		userAccountAssociationDTO.setUserGuid(GUID2);
		userAccountAssociationDTO.setRole(ROLE);
		return userAccountAssociationDTO;
	}

	private SavingStrategy<UserAccountAssociation, UserAccountAssociationDTO> mockSavingStrategy() {
		return AbstractSavingStrategy.createStrategy(ImportStrategyType.INSERT_OR_UPDATE, new SavingManager<UserAccountAssociation>() {
			@Override
			public void save(final UserAccountAssociation persistable) {
				// do nothing
			}

			@Override
			public UserAccountAssociation update(final UserAccountAssociation persistable) {
				return null;
			}
		});
	}

}
