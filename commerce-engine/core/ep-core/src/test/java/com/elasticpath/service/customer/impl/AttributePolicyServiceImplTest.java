/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.service.customer.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.customer.AttributePolicy;
import com.elasticpath.domain.customer.PolicyKey;
import com.elasticpath.persistence.api.PersistenceEngine;

@RunWith(MockitoJUnitRunner.class)
public class AttributePolicyServiceImplTest {

	private static final String GUID = "guid";

	private static final String ATTRIBUTE_KEY = "attributeKey";

	private static final String STORE_CODE = "storeCode";

	private static final PolicyKey POLICY_KEY = PolicyKey.DEFAULT;

	@InjectMocks
	private AttributePolicyServiceImpl attributePolicyService;

	@Mock
	private AttributePolicy attributePolicy;

	@Mock
	private PersistenceEngine persistenceEngine;

	@Test
	public void testFindByGuid() {
		doReturn(Arrays.asList(attributePolicy))
				.when(persistenceEngine).retrieveByNamedQuery("ATTRIBUTE_POLICY_FIND_BY_GUID", GUID);

		assertThat(attributePolicyService.findByGuid(GUID).get()).isEqualTo(attributePolicy);
	}

	@Test
	public void testFindByGuidReturnsNullWhenNotFound() {
		doReturn(Collections.emptyList())
				.when(persistenceEngine).retrieveByNamedQuery("ATTRIBUTE_POLICY_FIND_BY_GUID", GUID);

		assertThat(attributePolicyService.findByGuid(GUID).isPresent()).isFalse();
	}
	
	@Test
	public void testFindByGuids() {
		final List<String> guids = Collections.singletonList(GUID);

		doReturn(Arrays.asList(attributePolicy))
				.when(persistenceEngine).retrieveByNamedQueryWithList("ATTRIBUTE_POLICY_FIND_BY_GUIDS", "list", guids);

		assertThat(attributePolicyService.findByGuids(guids)).containsOnly(attributePolicy);
	}

	@Test
	public void testFindByPolicyKey() {
		doReturn(Arrays.asList(attributePolicy))
				.when(persistenceEngine).retrieveByNamedQuery("ATTRIBUTE_POLICY_FIND_BY_KEY", POLICY_KEY);

		assertThat(attributePolicyService.findByPolicyKey(POLICY_KEY)).containsOnly(attributePolicy);
	}

	@Test
	public void testFindAll() {
		doReturn(Arrays.asList(attributePolicy))
				.when(persistenceEngine).retrieveByNamedQuery("ATTRIBUTE_POLICIES_FIND_ALL");

		assertThat(attributePolicyService.findAll()).containsOnly(attributePolicy);
	}

	@Test
	public void testFindByStoreCodeAndAttributeKey() {
		doReturn(Arrays.asList(attributePolicy))
				.when(persistenceEngine).retrieveByNamedQuery("ATTRIBUTE_POLICIES_FIND_BY_ATTRIBUTE_STORE", STORE_CODE, ATTRIBUTE_KEY);

		assertThat(attributePolicyService.findByStoreCodeAndAttributeKey(STORE_CODE, ATTRIBUTE_KEY))
				.containsOnly(attributePolicy);
	}
}
