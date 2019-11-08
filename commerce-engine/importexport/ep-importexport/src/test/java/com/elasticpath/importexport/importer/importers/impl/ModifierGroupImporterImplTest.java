/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.importexport.importer.importers.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.modifier.ModifierGroup;
import com.elasticpath.importexport.common.adapters.modifier.ModifierGroupAdapter;
import com.elasticpath.importexport.common.dto.modifier.ModifierGroupDTO;
import com.elasticpath.importexport.common.exception.runtime.ImportRuntimeException;
import com.elasticpath.importexport.importer.configuration.ImportConfiguration;
import com.elasticpath.importexport.importer.context.ImportContext;
import com.elasticpath.importexport.importer.importers.SavingStrategy;
import com.elasticpath.importexport.importer.types.ImportStrategyType;
import com.elasticpath.service.modifier.ModifierService;

/**
 * Test for <code>{@link ModifierGroupImporterImpl}</code>.
 */
@RunWith(MockitoJUnitRunner.class)
public class ModifierGroupImporterImplTest {

	public static final String CODE = "CODE";
	private ModifierGroupImporterImpl modifierGroupImporter;

	@Mock
	private ModifierGroupAdapter modifierAdapter;

	@Mock
	private ModifierService modifierService;

	private ModifierGroupDTO modifierGroupDto;
	@Mock
	private ModifierGroup modifierGroup;
	private SavingStrategy<ModifierGroup, ModifierGroupDTO> savingStrategy;


	/**
	 * Prepare for tests.
	 * 
	 * @throws Exception in case of error happens
	 */
	@Before
	public void setUp() throws Exception {

		modifierGroupImporter = new ModifierGroupImporterImpl();
		modifierGroupImporter.setModifierGroupAdapter(modifierAdapter);
		modifierGroupImporter.setModifierService(modifierService);
		modifierGroupImporter.setStatusHolder(new ImportStatusHolder());

		SavingManager<ModifierGroup> savingManager = new SavingManager<ModifierGroup>() {
			@Override
			public void save(final ModifierGroup persistable) {
				modifierService.saveOrUpdate(persistable);
			}

			@Override
			public ModifierGroup update(final ModifierGroup persistable) {
				return modifierService.saveOrUpdate(persistable);
			}
		};
		savingStrategy = AbstractSavingStrategy.createStrategy(ImportStrategyType.INSERT_OR_UPDATE, savingManager);

		modifierGroupDto = new ModifierGroupDTO();
		modifierGroupDto.setCode(CODE);
		when(modifierAdapter.buildDomain(modifierGroupDto, modifierGroup)).thenReturn(modifierGroup);

	}

	/**
	 * Check an import with non-initialized importer.
	 */
	@Test(expected = ImportRuntimeException.class)
	public void testExecuteNonInitializedImport() {


		modifierGroupImporter.executeImport(modifierGroupDto);
	}

	/**
	 * Check an import of one product.
	 */
	@Test
	public void testExecuteImport() {

		ImportConfiguration importConfiguration = new ImportConfiguration();
		importConfiguration.setImporterConfigurationMap(new HashMap<>());

		when(modifierService.saveOrUpdate(modifierGroup)).thenReturn(modifierGroup);
		when(modifierService.findModifierGroupByCode(CODE)).thenReturn(modifierGroup);
		modifierGroupImporter.initialize(new ImportContext(importConfiguration), savingStrategy);

		boolean result = modifierGroupImporter.executeImport(modifierGroupDto);
		assertThat(modifierGroupImporter.getSavingStrategy()).isNotNull();
		assertThat(result).isTrue();
		verify(modifierService, times(1)).saveOrUpdate(modifierGroup);
	}

	/** The import classes should at least contain the DTO class we are operating on. */
	@Test
	public void testDtoClass() {
		assertEquals("Incorrect DTO class", ModifierGroupDTO.class, modifierGroupImporter.getDtoClass());
	}

	/** The auxiliary JAXB class list must not be null (can be empty). */
	@Test
	public void testAuxiliaryJaxbClasses() {
		assertNotNull(modifierGroupImporter.getAuxiliaryJaxbClasses());
	}


}
