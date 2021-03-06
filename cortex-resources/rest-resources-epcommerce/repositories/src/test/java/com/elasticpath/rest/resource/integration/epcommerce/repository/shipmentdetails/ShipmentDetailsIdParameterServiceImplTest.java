/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.shipmentdetails;

import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.rest.ResourceOperationFailure;
import com.elasticpath.rest.identity.Subject;
import com.elasticpath.rest.identity.attribute.AccountSharedIdSubjectAttribute;
import com.elasticpath.rest.identity.attribute.SubjectAttribute;
import com.elasticpath.rest.identity.type.ImmutableSubject;
import com.elasticpath.rest.resource.ResourceOperationContext;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.CartOrderRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.impl.CartOrderRepositoryImpl;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.CustomerRepository;

/**
 * Test for {@link ShipmentDetailsIdParameterServiceImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ShipmentDetailsIdParameterServiceImplTest {

	private static final String SCOPE = "scope";
	private static final String USER_ID = "userId";
	private static final int NUM_OF_IDS = 2;
	private static final String SHARED_ID = "sharedId";
	private static final String ACCOUNT_GUID = "accountGuid";


	@InjectMocks
	private ShipmentDetailsIdParameterServiceImpl shipmentDetailsIdParameterService;

	@Mock
	private CartOrderRepository cartOrderRepository;

	@Mock
	private ResourceOperationContext resourceOperationContext;

	@Mock
	private CustomerRepository customerRepository;

	@Test
	public void verifyFindShipmentDetailsIdsReturnsShipmentDetailsIdIdentifierPart() {
		List<String> cartIds = new ArrayList<>(NUM_OF_IDS);
		for (int i = 0; i < NUM_OF_IDS; i++) {
			String cartId = String.valueOf(i);
			cartIds.add(cartId);
		}

		when(cartOrderRepository.findCartOrderGuidsByCustomer(SCOPE, USER_ID)).thenReturn(Observable.fromIterable(cartIds));

		shipmentDetailsIdParameterService.findShipmentDetailsIds(SCOPE, USER_ID)
				.test()
				.assertNoErrors()
				.assertValueAt(0, shipmentDetailsId -> shipmentDetailsId.getValue().get(ShipmentDetailsConstants.ORDER_ID).equals("0"))
				.assertValueAt(1, shipmentDetailsId -> shipmentDetailsId.getValue().get(ShipmentDetailsConstants.ORDER_ID).equals("1"));
	}

	@Test
	public void verifyFindShipmentDetailsIdReturnsEmptyWhenCartOrderIdsNotFound() {
		String errorMsg = String.format(CartOrderRepositoryImpl.NO_CART_ORDERS_FOR_CUSTOMER, USER_ID, SCOPE);
		when(cartOrderRepository.findCartOrderGuidsByCustomer(SCOPE, USER_ID))
				.thenReturn(Observable.error(ResourceOperationFailure.notFound(errorMsg)));

		shipmentDetailsIdParameterService.findShipmentDetailsIds(SCOPE, USER_ID)
				.test()
				.assertNoErrors()
				.assertNoValues();
	}

	@Test
	public void verifyFindShipmentDetailsIdMakesSearchForAccountCartOrder() {
		String errorMsg = String.format(CartOrderRepositoryImpl.NO_CART_ORDERS_FOR_CUSTOMER, USER_ID, SCOPE);
		when(cartOrderRepository.findCartOrderGuidsByAccount(SCOPE, ACCOUNT_GUID))
				.thenReturn(Observable.error(ResourceOperationFailure.notFound(errorMsg)));
		SubjectAttribute attribute = new AccountSharedIdSubjectAttribute("key", SHARED_ID);
		Subject subject = new ImmutableSubject(emptyList(), singleton(attribute));
		when(resourceOperationContext.getSubject()).thenReturn(subject);
		when(customerRepository.getAccountGuid(subject)).thenReturn(ACCOUNT_GUID);

		shipmentDetailsIdParameterService.findShipmentDetailsIds(SCOPE, USER_ID)
				.test()
				.assertNoErrors()
				.assertNoValues();
	}

}
