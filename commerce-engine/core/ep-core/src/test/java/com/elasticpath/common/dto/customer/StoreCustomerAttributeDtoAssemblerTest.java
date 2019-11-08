/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.common.dto.customer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import java.util.Collections;
import java.util.Map;

import com.google.common.collect.Maps;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.domain.attribute.Attribute;
import com.elasticpath.domain.customer.AttributePolicy;
import com.elasticpath.domain.customer.PolicyKey;
import com.elasticpath.domain.customer.StoreCustomerAttribute;
import com.elasticpath.domain.customer.impl.StoreCustomerAttributeImpl;
import com.elasticpath.domain.store.Store;
import com.elasticpath.service.attribute.AttributeService;
import com.elasticpath.service.customer.AttributePolicyService;
import com.elasticpath.service.customer.CustomerProfileAttributeService;
import com.elasticpath.service.store.StoreService;

/**
 * Tests {@link StoreCustomerAttributeDtoAssembler}.
 */
@RunWith(MockitoJUnitRunner.class)
public class StoreCustomerAttributeDtoAssemblerTest {

	private static final String GUID = "guid";

	private static final String STORE_CODE = "storeCode";

	private static final String ATTRIBUTE_KEY = "attributeKey";

	private static final PolicyKey POLICY_KEY = PolicyKey.READ_ONLY;

	@InjectMocks
	private StoreCustomerAttributeDtoAssembler storeCustomerAttributeDtoAssembler;

	@Mock
	private AttributeService attributeService;

	@Mock
	private StoreService storeService;

	@Mock
	private AttributePolicyService attributePolicyService;

	@Mock
	private Attribute attribute;

	@Mock
	private Store store;

	@Mock
	private AttributePolicy attributePolicy;

	@Mock
	private CustomerProfileAttributeService customerProfileAttributeService;

	private final Map<String, PolicyKey> predefinedProfileAttributePolicies = Maps.newHashMap();

	/**
	 * Test initialization.
	 */
	@Before
	public void setUp() {
		given(attributeService.findByKey(ATTRIBUTE_KEY)).willReturn(attribute);

		given(storeService.findStoreWithCode(STORE_CODE)).willReturn(store);

		given(attribute.getKey()).willReturn(ATTRIBUTE_KEY);

		given(store.getCode()).willReturn(STORE_CODE);

		given(attributePolicyService.findByPolicyKey(POLICY_KEY))
				.willReturn(Collections.singletonList(attributePolicy));

		given(customerProfileAttributeService.getPredefinedProfileAttributePolicies())
				.willReturn(predefinedProfileAttributePolicies);
	}

	/**
	 * Tests assembly of DTO from domain.
	 */
	@Test
	public void testAssembleDto() {
		final StoreCustomerAttribute domain = new StoreCustomerAttributeImpl();
		domain.setGuid(GUID);
		domain.setStoreCode(STORE_CODE);
		domain.setAttributeKey(ATTRIBUTE_KEY);
		domain.setPolicyKey(POLICY_KEY);

		final StoreCustomerAttributeDTO dto = new StoreCustomerAttributeDTO();

		storeCustomerAttributeDtoAssembler.assembleDto(domain, dto);

		assertThat(dto.getGuid()).isEqualTo(domain.getGuid());
		assertThat(dto.getStoreCode()).isEqualTo(domain.getStoreCode());
		assertThat(dto.getAttributeKey()).isEqualTo(domain.getAttributeKey());
		assertThat(dto.getPolicyKey()).isEqualTo(domain.getPolicyKey().getName());
	}

	/**
	 * Tests assembly of domain from DTO.
	 */
	@Test
	public void testAssembleDomain() {
		final StoreCustomerAttributeDTO dto = createStoreCustomerAttributeDTO();
		final StoreCustomerAttribute domain = new StoreCustomerAttributeImpl();

		storeCustomerAttributeDtoAssembler.assembleDomain(dto, domain);

		assertThat(domain.getGuid()).isEqualTo(dto.getGuid());
		assertThat(domain.getStoreCode()).isEqualTo(dto.getStoreCode());
		assertThat(domain.getAttributeKey()).isEqualTo(dto.getAttributeKey());
		assertThat(domain.getPolicyKey()).isEqualTo(PolicyKey.valueOf(dto.getPolicyKey()));
	}

	/**
	 * Tests domain assembly store validation.
	 */
	@Test
	public void testAssembleDomainStoreValidationFailure() {
		given(storeService.findStoreWithCode(STORE_CODE)).willReturn(null);

		final StoreCustomerAttributeDTO dto = createStoreCustomerAttributeDTO();
		final StoreCustomerAttribute domain = new StoreCustomerAttributeImpl();

		assertThatThrownBy(() -> storeCustomerAttributeDtoAssembler.assembleDomain(dto, domain))
				.isInstanceOf(EpServiceException.class)
				.hasMessage("Store with code storeCode not found.");
	}

	/**
	 * Tests domain assembly attribute validation.
	 */
	@Test
	public void testAssembleDomainAttributeValidationFailure() {
		given(attributeService.findByKey(ATTRIBUTE_KEY)).willReturn(null);

		final StoreCustomerAttributeDTO dto = createStoreCustomerAttributeDTO();
		final StoreCustomerAttribute domain = new StoreCustomerAttributeImpl();

		assertThatThrownBy(() -> storeCustomerAttributeDtoAssembler.assembleDomain(dto, domain))
				.isInstanceOf(EpServiceException.class)
				.hasMessage("Attribute with key attributeKey not found.");
	}

	/**
	 * Tests domain assembly attribute validation against predefined policies.
	 */
	@Test
	public void testAssembleDomainAttributeValidationPredefinedPolicy() {
		// given
		predefinedProfileAttributePolicies.put(ATTRIBUTE_KEY, PolicyKey.READ_ONLY);

		final StoreCustomerAttributeDTO dto = createStoreCustomerAttributeDTO();
		final StoreCustomerAttribute domain = new StoreCustomerAttributeImpl();

		assertThatThrownBy(() -> storeCustomerAttributeDtoAssembler.assembleDomain(dto, domain))
				.isInstanceOf(EpServiceException.class)
				.hasMessage("Attribute with key attributeKey is predefined with a system policy.");
	}

	/**
	 * Tests domain assembly policy key validation.
	 */
	@Test
	public void testAssembleDomainPolicyKeyValidationFailure() {
		given(attributePolicyService.findByPolicyKey(POLICY_KEY)).willReturn(Collections.emptyList());

		final StoreCustomerAttributeDTO dto = createStoreCustomerAttributeDTO();
		final StoreCustomerAttribute domain = new StoreCustomerAttributeImpl();

		assertThatThrownBy(() -> storeCustomerAttributeDtoAssembler.assembleDomain(dto, domain))
				.isInstanceOf(EpServiceException.class)
				.hasMessage("Policy with key READ_ONLY not found.");
	}

	private StoreCustomerAttributeDTO createStoreCustomerAttributeDTO() {
		final StoreCustomerAttributeDTO dto = new StoreCustomerAttributeDTO();
		dto.setGuid(GUID);
		dto.setStoreCode(STORE_CODE);
		dto.setAttributeKey(ATTRIBUTE_KEY);
		dto.setPolicyKey(POLICY_KEY.getName());
		return dto;
	}
}
