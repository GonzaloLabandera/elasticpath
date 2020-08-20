/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.importexport.exporter.exporters.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.common.dto.customer.StoreCustomerAttributeDTO;
import com.elasticpath.domain.attribute.Attribute;
import com.elasticpath.domain.customer.AttributePolicy;
import com.elasticpath.domain.customer.PolicyKey;
import com.elasticpath.domain.customer.StoreCustomerAttribute;
import com.elasticpath.domain.store.Store;
import com.elasticpath.importexport.common.adapters.DomainAdapter;
import com.elasticpath.importexport.common.exception.ConfigurationException;
import com.elasticpath.importexport.common.types.JobType;
import com.elasticpath.importexport.exporter.configuration.ExportConfiguration;
import com.elasticpath.importexport.exporter.configuration.search.SearchConfiguration;
import com.elasticpath.importexport.exporter.context.DependencyRegistry;
import com.elasticpath.importexport.exporter.context.ExportContext;
import com.elasticpath.service.customer.AttributePolicyService;
import com.elasticpath.service.customer.StoreCustomerAttributeService;

/**
 * Tests for {@link StoreCustomerAttributeExporter}.
 */
@RunWith(MockitoJUnitRunner.class)
public class StoreCustomerAttributeExporterTest {

	private static final String GUID1 = "guid1";

	private static final String GUID2 = "guid2";

	private static final String GUID3 = "guid3";

	private static final String ATTRIBUTE_KEY1 = "attributeKey1";

	private static final String ATTRIBUTE_KEY2 = "attributeKey2";

	private static final String STORE_CODE = "storeCode";

	private static final String POLICY_GUID = "policyGuid";

	private static final PolicyKey POLICY_KEY = PolicyKey.READ_ONLY;

	@InjectMocks
	private StoreCustomerAttributeExporter storeCustomerAttributeExporter;

	@Mock
	private StoreCustomerAttributeService storeCustomerAttributeService;

	@Mock
	private StoreCustomerAttribute storeCustomerAttribute1;

	@Mock
	private StoreCustomerAttribute storeCustomerAttribute2;

	@Mock
	private AttributePolicyService attributePolicyService;

	@Mock
	private AttributePolicy attributePolicy;

	@Mock
	private DomainAdapter<StoreCustomerAttribute, StoreCustomerAttributeDTO> storeCustomerAttributeAdapter;

	private DependencyRegistry dependencyRegistry;

	private List<StoreCustomerAttribute> storeCustomerAttributes;

	/**
	 * Test initialization.
	 *
	 * @throws ConfigurationException in case of errors
	 */
	@Before
	public void setUp() throws ConfigurationException {
		given(storeCustomerAttribute1.getGuid()).willReturn(GUID1);
		given(storeCustomerAttribute1.getAttributeKey()).willReturn(ATTRIBUTE_KEY1);
		given(storeCustomerAttribute1.getStoreCode()).willReturn(STORE_CODE);
		given(storeCustomerAttribute1.getPolicyKey()).willReturn(POLICY_KEY);

		given(storeCustomerAttribute2.getGuid()).willReturn(GUID2);
		given(storeCustomerAttribute2.getAttributeKey()).willReturn(ATTRIBUTE_KEY2);
		given(storeCustomerAttribute2.getStoreCode()).willReturn(STORE_CODE);
		given(storeCustomerAttribute2.getPolicyKey()).willReturn(POLICY_KEY);

		given(attributePolicy.getGuid()).willReturn(POLICY_GUID);

		storeCustomerAttributes = Arrays.asList(storeCustomerAttribute1, storeCustomerAttribute2);

		when(storeCustomerAttributeService.findAll()).thenReturn(storeCustomerAttributes);

		final ExportContext exportContext = new ExportContext(new ExportConfiguration(), new SearchConfiguration());
		dependencyRegistry = new DependencyRegistry(Arrays.asList(new Class<?>[]{StoreCustomerAttribute.class}));
		exportContext.setDependencyRegistry(dependencyRegistry);

		storeCustomerAttributeExporter.initialize(exportContext);
	}

	/**
	 * All guids found during initialization should be exportable.
	 */
	@Test
	public void testGetListExportableIDsContainsInitializedGuids() {
		final List<String> exportableIDs = storeCustomerAttributeExporter.getListExportableIDs();

		assertThat(exportableIDs).containsOnly(GUID1, GUID2);
	}

	/**
	 * All dependency guids should be exportable.
	 */
	@Test
	public void testGetListExportableIDsContainsDependencyGuids() {
		dependencyRegistry.addGuidDependency(StoreCustomerAttribute.class, GUID3);

		final List<String> exportableIDs = storeCustomerAttributeExporter.getListExportableIDs();

		assertThat(exportableIDs).contains(GUID3);
	}

	/**
	 * Tests dependency addition for related stores.
	 */
	@Test
	public void testAddDependenciesForStores() {
		final DependencyRegistry dependencyRegistry = new DependencyRegistry(
				Arrays.asList(new Class<?>[]{StoreCustomerAttribute.class, Store.class}));

		storeCustomerAttributeExporter.addDependencies(storeCustomerAttributes, dependencyRegistry);

		assertThat(dependencyRegistry.getDependentGuids(Store.class)).containsOnly(STORE_CODE);
	}

	/**
	 * Tests dependency addition for related attributes.
	 */
	@Test
	public void testAddDependenciesForAttributes() {
		final DependencyRegistry dependencyRegistry = new DependencyRegistry(
				Arrays.asList(new Class<?>[]{StoreCustomerAttribute.class, Attribute.class}));

		storeCustomerAttributeExporter.addDependencies(storeCustomerAttributes, dependencyRegistry);

		assertThat(dependencyRegistry.getDependentGuids(Attribute.class)).containsOnly(ATTRIBUTE_KEY1, ATTRIBUTE_KEY2);
	}

	/**
	 * Tests dependency addition for related attribute policies.
	 */
	@Test
	public void testAddDependenciesForAttributePolicies() {
		given(attributePolicyService.findByPolicyKey(POLICY_KEY))
				.willReturn(Collections.singletonList(attributePolicy));

		final DependencyRegistry dependencyRegistry = new DependencyRegistry(
				Arrays.asList(new Class<?>[]{StoreCustomerAttribute.class, AttributePolicy.class}));

		storeCustomerAttributeExporter.addDependencies(storeCustomerAttributes, dependencyRegistry);

		assertThat(dependencyRegistry.getDependentGuids(AttributePolicy.class)).containsOnly(POLICY_GUID);
	}

	/**
	 * Tests loading of attributes.
	 */
	@Test
	public void testFindByIDs() {
		final List<String> guids = Arrays.asList(GUID1, GUID2);
		when(storeCustomerAttributeService.findByGuids(guids))
				.thenReturn(Arrays.asList(storeCustomerAttribute1, storeCustomerAttribute2));

		final List<StoreCustomerAttribute> attributes = storeCustomerAttributeExporter.findByIDs(guids);
		assertThat(attributes).containsOnly(storeCustomerAttribute1, storeCustomerAttribute2);
	}

	/**
	 * Tests the job type.
	 */
	@Test
	public void testGetJobType() {
		assertThat(JobType.STORECUSTOMERATTRIBUTE)
				.as("Incorrect job type returned.")
				.isEqualTo(storeCustomerAttributeExporter.getJobType());
	}

	/**
	 * Tests the dependent classes.
	 */
	@Test
	public void testGetDependantClasses() {
		assertThat(new Class<?>[]{StoreCustomerAttribute.class})
				.as("Incorrect dependent classes returned.")
				.isEqualTo(storeCustomerAttributeExporter.getDependentClasses());
	}

	/**
	 * Tests the dto class.
	 */
	@Test
	public void testGetDtoClass() {
		assertThat(storeCustomerAttributeExporter.getDtoClass()).isEqualTo(StoreCustomerAttributeDTO.class);
	}

	/**
	 * Tests the domain adapter.
	 */
	@Test
	public void testGetDomainAdapter() {
		assertThat(storeCustomerAttributeExporter.getDomainAdapter()).isEqualTo(storeCustomerAttributeAdapter);
	}
}
