/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.importexport.exporter.exporters.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.common.dto.customer.AttributePolicyDTO;
import com.elasticpath.domain.customer.AttributePolicy;
import com.elasticpath.importexport.common.adapters.DomainAdapter;
import com.elasticpath.importexport.common.exception.ConfigurationException;
import com.elasticpath.importexport.common.types.JobType;
import com.elasticpath.importexport.exporter.configuration.ExportConfiguration;
import com.elasticpath.importexport.exporter.configuration.search.SearchConfiguration;
import com.elasticpath.importexport.exporter.context.DependencyRegistry;
import com.elasticpath.importexport.exporter.context.ExportContext;
import com.elasticpath.service.customer.AttributePolicyService;

/**
 * Tests for {@link AttributePolicyExporter}.
 */
@RunWith(MockitoJUnitRunner.class)
public class AttributePolicyExporterTest {

	private static final String GUID1 = "guid1";

	private static final String GUID2 = "guid2";

	private static final String GUID3 = "guid3";

	@InjectMocks
	private AttributePolicyExporter attributePolicyExporter;

	@Mock
	private AttributePolicyService attributePolicyService;

	@Mock
	private AttributePolicy attributePolicy1;

	@Mock
	private AttributePolicy attributePolicy2;

	@Mock
	private DomainAdapter<AttributePolicy, AttributePolicyDTO> attributePolicyAdapter;

	private DependencyRegistry dependencyRegistry;

	/**
	 * Test initialization.
	 *
	 * @throws ConfigurationException in case of errors
	 */
	@Before
	public void setUp() throws ConfigurationException {
		given(attributePolicy1.getGuid()).willReturn(GUID1);
		given(attributePolicy2.getGuid()).willReturn(GUID2);

		when(attributePolicyService.findAll()).thenReturn(Arrays.asList(attributePolicy1,
				attributePolicy2));

		final ExportContext exportContext = new ExportContext(new ExportConfiguration(), new SearchConfiguration());
		dependencyRegistry = new DependencyRegistry(
				Arrays.asList(new Class<?>[]{AttributePolicy.class}));
		exportContext.setDependencyRegistry(dependencyRegistry);

		attributePolicyExporter.initialize(exportContext);
	}

	/**
	 * All guids found during initialization should be exportable.
	 */
	@Test
	public void testGetListExportableIDsContainsInitializedGuids() {
		final List<String> exportableIDs = attributePolicyExporter.getListExportableIDs();

		assertThat(exportableIDs).containsOnly(GUID1, GUID2);
	}

	/**
	 * Test that guid dependencies are included as exportable.
	 */
	@Test
	public void testGetListExportableIDsContainsDependencyGuids() {
		dependencyRegistry.addGuidDependency(AttributePolicy.class, GUID3);

		final List<String> exportableIDs = attributePolicyExporter.getListExportableIDs();

		assertThat(exportableIDs).contains(GUID3);
	}

	/**
	 * Tests loading of policies.
	 */
	@Test
	public void testFindByIDs() {
		final List<String> guids = Arrays.asList(GUID1, GUID2);
		when(attributePolicyService.findByGuids(guids))
				.thenReturn(Arrays.asList(attributePolicy1, attributePolicy2));

		final List<AttributePolicy> policies = attributePolicyExporter.findByIDs(guids);
		assertThat(policies).containsOnly(attributePolicy1, attributePolicy2);
	}

	/**
	 * Tests the job type.
	 */
	@Test
	public void testGetJobType() {
		assertThat(JobType.ATTRIBUTE_POLICY)
				.as("Incorrect job type returned.")
				.isEqualTo(attributePolicyExporter.getJobType());
	}

	/**
	 * Tests the dependent classes.
	 */
	@Test
	public void testGetDependantClasses() {
		assertThat(new Class<?>[]{AttributePolicy.class})
				.as("Incorrect dependent classes returned.")
				.isEqualTo(attributePolicyExporter.getDependentClasses());
	}

	/**
	 * Tests the dto class.
	 */
	@Test
	public void testGetDtoClass() {
		assertThat(attributePolicyExporter.getDtoClass()).isEqualTo(AttributePolicyDTO.class);
	}

	/**
	 * Tests the domain adapter.
	 */
	@Test
	public void testGetDomainAdapter() {
		assertThat(attributePolicyExporter.getDomainAdapter()).isEqualTo(attributePolicyAdapter);
	}
}
