/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.importexport.exporter.exporters.impl;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.modifier.ModifierGroup;
import com.elasticpath.importexport.common.adapters.DomainAdapter;
import com.elasticpath.importexport.common.dto.modifier.ModifierGroupDTO;
import com.elasticpath.importexport.common.exception.ConfigurationException;
import com.elasticpath.importexport.common.summary.Summary;
import com.elasticpath.importexport.common.summary.impl.SummaryImpl;
import com.elasticpath.importexport.common.types.JobType;
import com.elasticpath.importexport.exporter.configuration.ExportConfiguration;
import com.elasticpath.importexport.exporter.configuration.search.SearchConfiguration;
import com.elasticpath.importexport.exporter.context.DependencyRegistry;
import com.elasticpath.importexport.exporter.context.ExportContext;
import com.elasticpath.service.modifier.ModifierService;

/**
 * Modifier exporter test.
 */
@RunWith(MockitoJUnitRunner.class)
public class ModifierExporterImplTest {


	private static final String MODIFIER_CODE = "code";
	private ModifierExporterImpl modifierExporter;
	private ExportContext exportContext;

	@Mock
	private ModifierService modifierService;

	/**
	 * Prepare for tests.
	 *
	 * @throws Exception in case of error happens
	 */
	@Before
	public void setUp() throws Exception {
		@SuppressWarnings("unchecked") final DomainAdapter<ModifierGroup, ModifierGroupDTO> modifierGroupAdapter = mock(DomainAdapter.class);


		modifierExporter = new ModifierExporterImpl();
		modifierExporter.setModifierService(modifierService);
		modifierExporter.setDomainAdapter(modifierGroupAdapter);
		ModifierGroupDTO modifierDTO = new ModifierGroupDTO();
		when(modifierGroupAdapter.createDtoObject()).thenReturn(modifierDTO);


		ExportConfiguration exportConfiguration;
		exportConfiguration = new ExportConfiguration();
		final SearchConfiguration searchConfiguration = new SearchConfiguration();
		exportContext = new ExportContext(exportConfiguration, searchConfiguration);

		exportContext.setSummary(new SummaryImpl());
		exportContext.setDependencyRegistry(new DependencyRegistry(Arrays.asList(new Class<?>[]{ModifierGroup.class})));

	}

	/**
	 * Check that during initialization exporter prepares the list of codes for modifier groups to be exported.
	 */
	@Test
	public void testExporterInitialization() throws ConfigurationException {
		initializeExporter();
		verify(modifierService, times(1)).getAllModifierGroups();
		assertThat(modifierExporter.getListExportableIDs()).containsExactly(MODIFIER_CODE);

	}

	private void initializeExporter() throws ConfigurationException {
		final List<String> modifierGroupCodeList = new ArrayList<>();
		modifierGroupCodeList.add(MODIFIER_CODE);

		ModifierGroup modifierGroup = mock(ModifierGroup.class);
		when(modifierGroup.getCode()).thenReturn(MODIFIER_CODE);
		when(modifierService.getAllModifierGroups()).thenReturn(Arrays.asList(modifierGroup));


		modifierExporter.initialize(exportContext);
	}

	/**
	 * Check an export of one modifier without export criteria.
	 */
	@Test
	public void testProcessExportWithoutCriteria() throws ConfigurationException {
		initializeExporter();
		ModifierGroup modifierGroup = mock(ModifierGroup.class);

		when(modifierService.findModifierGroupByCodes(anyList())).thenReturn(Arrays.asList(modifierGroup));

		modifierExporter.processExport(System.out);
		Summary summary = modifierExporter.getContext().getSummary();
		assertThat(summary.getCounters())
				.size()
				.isEqualTo(1);
		assertThat(summary.getCounters())
				.containsKey(JobType.MODIFIERGROUP);
		assertThat(summary.getCounters().get(JobType.MODIFIERGROUP))
				.isEqualTo(1);
		assertThat(summary.getFailures())
				.size()
				.isEqualTo(0);
		assertThat(summary.getStartDate())
				.isNotNull();
		assertThat(summary.getElapsedTime())
				.isNotNull();
		assertThat(summary.getElapsedTime().toString())
				.isNotNull();

	}

}
