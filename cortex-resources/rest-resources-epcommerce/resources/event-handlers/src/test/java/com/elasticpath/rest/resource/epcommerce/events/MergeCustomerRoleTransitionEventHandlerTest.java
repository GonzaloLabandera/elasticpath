/**
 * Copyright © 2016 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.epcommerce.events;

import static com.elasticpath.rest.resource.integration.epcommerce.common.authentication.AuthenticationConstants.PUBLIC_ROLENAME;
import static com.elasticpath.rest.resource.integration.epcommerce.common.authentication.AuthenticationConstants.REGISTERED_ROLE;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.reactivex.Single;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.shopper.Shopper;
import com.elasticpath.rest.command.ExecutionResult;
import com.elasticpath.rest.command.ExecutionResultFactory;
import com.elasticpath.rest.relos.rs.events.RoleTransitionEvent;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.CustomerRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.ShopperRepository;
import com.elasticpath.service.shoppingcart.ShoppingCartService;

@RunWith(MockitoJUnitRunner.class)
public class MergeCustomerRoleTransitionEventHandlerTest {

	private static final String SCOPE = "scope";

	private static final String OLD_USER_GUID = "oldUserGuid";
	private static final String NEW_USER_GUID = "newUserGuid";

	@Mock
	private CustomerRepository customerRepository;

	@Mock
	private ShopperRepository shopperRepository;

	@Mock
	private ShoppingCartService shoppingCartService;

	@Mock
	private RoleTransitionEvent mockRoleTransitionEvent;

	@Mock
	private Shopper mockAnonymousShopper;

	@Mock
	private Customer mockRegisteredCustomer;

	@InjectMocks
	private MergeCustomerRoleTransitionEventHandler classToTest;

	/**
	 * Test two different roles. Should return nothing
	 * @throws Exception
	 */
	@Test
	public void testOldRoleNotSameAsNewRoleShouldNotMerge() throws Exception {
		when(mockRoleTransitionEvent.getOldRole()).thenReturn("nonExistentRole");

		classToTest.handleEvent(SCOPE, mockRoleTransitionEvent);

		verify(mockRoleTransitionEvent, never()).getOldUserGuid();
		verify(mockRoleTransitionEvent, never()).getNewUserGuid();
	}

	@Test
	public void shouldMergeAnonymousToRegisteredCustomerSession() throws Exception {
		mockRoleTransition();
		mockGUIDTransition();

		ExecutionResult<Object> mergedResultOk = ExecutionResultFactory.createUpdateOK();

		when(customerRepository.findCustomerByGuidAndStoreCode(NEW_USER_GUID, SCOPE))
				.thenReturn(ExecutionResultFactory.createReadOK(mockRegisteredCustomer));
		when(shopperRepository.findOrCreateShopper(OLD_USER_GUID, SCOPE))
				.thenReturn(Single.just(mockAnonymousShopper));
		when(shoppingCartService.findOrCreateDefaultCartByShopper(mockAnonymousShopper))
				.thenReturn(null);
		when(customerRepository.mergeCustomer(mockAnonymousShopper, mockRegisteredCustomer, SCOPE))
				.thenReturn(mergedResultOk);

		classToTest.handleEvent(SCOPE, mockRoleTransitionEvent);

		verify(mockRoleTransitionEvent).getNewUserGuid();
		verify(mockRoleTransitionEvent).getOldUserGuid();
		verify(customerRepository).findCustomerByGuidAndStoreCode(NEW_USER_GUID, SCOPE);
		verify(customerRepository).mergeCustomer(mockAnonymousShopper, mockRegisteredCustomer, SCOPE);
	}

	private void mockGUIDTransition() {
		ExecutionResult<Customer> recipientCustomer = ExecutionResultFactory.createReadOK(mockRegisteredCustomer);
		when(mockRoleTransitionEvent.getOldUserGuid()).thenReturn(OLD_USER_GUID);
		when(mockRoleTransitionEvent.getNewUserGuid()).thenReturn(NEW_USER_GUID);
		when(customerRepository.findCustomerByGuidAndStoreCode(NEW_USER_GUID, SCOPE)).thenReturn(recipientCustomer);
	}

	private void mockRoleTransition() {
		when(mockRoleTransitionEvent.getOldRole()).thenReturn(PUBLIC_ROLENAME);
		when(mockRoleTransitionEvent.getNewRole()).thenReturn(REGISTERED_ROLE);
	}
}
