/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.common.dto.customer;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.customer.AttributePolicy;
import com.elasticpath.domain.customer.PolicyKey;
import com.elasticpath.domain.customer.PolicyPermission;
import com.elasticpath.domain.customer.impl.AttributePolicyImpl;

/**
 * Tests {@link AttributePolicyDtoAssembler}.
 */
@RunWith(MockitoJUnitRunner.class)
public class AttributePolicyDtoAssemblerTest {

	private static final String GUID = "guid";

	private static final PolicyKey POLICY_KEY = PolicyKey.DEFAULT;

	private static final PolicyPermission POLICY_PERMISSION = PolicyPermission.EDIT;

	@InjectMocks
	private AttributePolicyDtoAssembler attributePolicyDtoAssembler;

	/**
	 * Tests assembly of DTO from domain.
	 */
	@Test
	public void testAssembleDto() {
		final AttributePolicy domain = new AttributePolicyImpl();
		domain.setGuid(GUID);
		domain.setPolicyKey(POLICY_KEY);
		domain.setPolicyPermission(POLICY_PERMISSION);

		final AttributePolicyDTO dto = new AttributePolicyDTO();

		attributePolicyDtoAssembler.assembleDto(domain, dto);

		assertThat(dto.getGuid()).isEqualTo(domain.getGuid());
		assertThat(dto.getPolicyKey()).isEqualTo(domain.getPolicyKey().getName());
		assertThat(dto.getPolicyPermission()).isEqualTo(domain.getPolicyPermission().getName());
	}

	/**
	 * Tests assembly of domain from DTO.
	 */
	@Test
	public void testAssembleDomain() {
		final AttributePolicyDTO dto = new AttributePolicyDTO();
		dto.setGuid(GUID);
		dto.setPolicyKey(POLICY_KEY.getName());
		dto.setPolicyPermission(POLICY_PERMISSION.getName());

		final AttributePolicy domain = new AttributePolicyImpl();

		attributePolicyDtoAssembler.assembleDomain(dto, domain);

		assertThat(domain.getGuid()).isEqualTo(dto.getGuid());
		assertThat(domain.getPolicyKey()).isEqualTo(PolicyKey.valueOf(dto.getPolicyKey()));
		assertThat(domain.getPolicyPermission()).isEqualTo(PolicyPermission.valueOf(dto.getPolicyPermission()));
	}
}
