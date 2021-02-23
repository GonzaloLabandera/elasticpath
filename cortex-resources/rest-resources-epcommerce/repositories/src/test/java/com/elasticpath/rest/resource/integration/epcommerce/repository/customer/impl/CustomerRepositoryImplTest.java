/*
 * Copyright © 2013 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.customer.impl;

import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import io.reactivex.Completable;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.base.exception.structured.EpValidationException;
import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.CustomerAddress;
import com.elasticpath.domain.customer.CustomerSession;
import com.elasticpath.domain.customer.CustomerType;
import com.elasticpath.domain.customer.UserAccountAssociation;
import com.elasticpath.domain.customer.impl.CustomerImpl;
import com.elasticpath.domain.shopper.Shopper;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.persistence.api.EpPersistenceException;
import com.elasticpath.rest.ResourceOperationFailure;
import com.elasticpath.rest.ResourceStatus;
import com.elasticpath.rest.advise.ImmutableMessage;
import com.elasticpath.rest.chain.BrokenChainException;
import com.elasticpath.rest.command.ExecutionResult;
import com.elasticpath.rest.command.ExecutionResultFactory;
import com.elasticpath.rest.identity.Subject;
import com.elasticpath.rest.identity.attribute.AccountSharedIdSubjectAttribute;
import com.elasticpath.rest.identity.attribute.SubjectAttribute;
import com.elasticpath.rest.identity.type.ImmutableSubject;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.CustomerSessionRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.dto.CustomerDTO;
import com.elasticpath.rest.resource.integration.epcommerce.repository.transform.ExceptionTransformer;
import com.elasticpath.rest.resource.integration.epcommerce.repository.transform.impl.ReactiveAdapterImpl;
import com.elasticpath.service.customer.AccountTreeService;
import com.elasticpath.service.customer.CustomerService;
import com.elasticpath.service.customer.CustomerSessionService;
import com.elasticpath.service.customer.UserAccountAssociationService;
import com.elasticpath.service.shoppingcart.ShoppingCartService;
import com.elasticpath.settings.SettingsReader;
import com.elasticpath.settings.domain.SettingValue;

/**
 * Test class for {@link CustomerRepositoryImpl}.
 */
@SuppressWarnings("PMD.TooManyMethods")
@RunWith(MockitoJUnitRunner.class)
public class CustomerRepositoryImplTest {

	private static final String ACCOUNT_ROLE_FIELD_SETTING_PATH = "COMMERCE/SYSTEM/JWT/singleSessionUserRole";
	private static final String THE_RESULT_SHOULD_BE_SUCCESSFUL = "The result should be successful";
	private static final String THE_RESULT_SHOULD_BE_A_FAILURE = "The result should be a failure";
	private static final String SHARED_ID = "sharedId";
	private static final String ACCOUNT_SHARED_ID = "sharedId";
	private static final String SCOPE = "scope";
	private static final String CUSTOMER_GUID = "customer_guid";
	private static final String EP_PERSISTENCE_EXCEPTION = "ep persistence exception";
	private static final String ACCOUNT_GUID = "accountGuid";
	private static final int PAGE_START_INDEX = 1;
	private static final int PAGE_SIZE = 5;

	@Mock
	private CustomerService mockCustomerService;
	@InjectMocks
	private ReactiveAdapterImpl reactiveAdapter;
	@Mock
	private Customer mockCustomer;
	@Mock(answer = Answers.RETURNS_DEEP_STUBS)
	private CustomerSession mockCustomerSession;
	@Mock
	private CustomerSessionRepository mockCustomerSessionRepository;
	@Mock
	private ShoppingCartService mockShoppingCartService;
	@Mock
	private ShoppingCart mockShoppingCart;
	@Mock
	private CustomerSessionService mockCustomerSessionService;
	@Mock
	private Shopper mockShopper;
	@Mock
	private CustomerAddress mockAddress;
	@Mock
	private ExceptionTransformer exceptionTransformer;
	@Mock
	private BeanFactory coreBeanFactory;
	@Mock
	private SettingsReader settingsReader;
	@Mock
	private UserAccountAssociationService userAccountAssociationService;
	@Mock
	private AccountTreeService accountTreeService;

	private CustomerRepositoryImpl customerRepository;

	@Before
	public void setUp() {
		customerRepository = new CustomerRepositoryImpl(mockCustomerService, accountTreeService, mockCustomerSessionRepository,
				mockCustomerSessionService,
				mockShoppingCartService, coreBeanFactory, reactiveAdapter, settingsReader,
				userAccountAssociationService);
		when(mockCustomerService.findCustomerGuidBySharedId(SHARED_ID)).thenReturn(ACCOUNT_GUID);
	}

	@Test
	public void testFindCustomerBySharedIdWithAppropriateParameters() {
		Customer expectedCustomer = new CustomerImpl();

		when(mockCustomerSessionRepository.findCustomerSessionBySharedId(SCOPE, SHARED_ID))
				.thenReturn(ExecutionResultFactory.createReadOK(mockCustomerSession));
		when(mockCustomerSession.getShopper().getCustomer()).thenReturn(expectedCustomer);

		ExecutionResult<Customer> result = customerRepository.findCustomerBySharedId(SCOPE, SHARED_ID);

		assertTrue(THE_RESULT_SHOULD_BE_SUCCESSFUL, result.isSuccessful());
		assertEquals("The returned customer should be the same as expected", expectedCustomer, result.getData());
	}

	@Test
	public void testFindCustomerBySharedIdWhenCustomerNotFound() {
		when(mockCustomerSessionRepository.findCustomerSessionBySharedId(SCOPE, SHARED_ID))
				.thenReturn(ExecutionResultFactory.createReadOK(mockCustomerSession));
		when(mockCustomerSession.getShopper().getCustomer()).thenReturn(null);

		ExecutionResult<Customer> result = customerRepository.findCustomerBySharedId(SCOPE, SHARED_ID);

		assertTrue(THE_RESULT_SHOULD_BE_A_FAILURE, result.isFailure());
		assertEquals("The status returned should be NOT_FOUND", ResourceStatus.NOT_FOUND, result.getResourceStatus());
	}

	@Test
	public void testFindCustomerBySharedIdWhenExceptionThrown() {
		when(mockCustomerSessionRepository.findCustomerSessionBySharedId(SCOPE, SHARED_ID))
				.thenThrow(new EpPersistenceException(EP_PERSISTENCE_EXCEPTION));

		ExecutionResult<Customer> result = customerRepository.findCustomerBySharedId(SCOPE, SHARED_ID);
		assertServerError(result);

	}

	@Test
	public void testFindCustomerByGuidWithValidGuid() {
		Customer expectedCustomer = new CustomerImpl();

		when(mockCustomerSessionRepository.findCustomerSessionByGuidAndStoreCode(CUSTOMER_GUID, SCOPE))
				.thenReturn(ExecutionResultFactory.createReadOK(mockCustomerSession));
		when(mockCustomerSession.getShopper().getCustomer()).thenReturn(expectedCustomer);

		ExecutionResult<Customer> result = customerRepository.findCustomerByGuidAndStoreCode(CUSTOMER_GUID, SCOPE);

		assertTrue("The result should be a success", result.isSuccessful());
		assertEquals("The customer returned should be the same as expected", expectedCustomer, result.getData());
	}

	@Test
	public void testFindCustomerByGuidWithCustomerNotFound() {
		when(mockCustomerSessionRepository.findCustomerSessionByGuidAndStoreCode(CUSTOMER_GUID, SCOPE))
				.thenReturn(ExecutionResultFactory.createReadOK(mockCustomerSession));
		when(mockCustomerSession.getShopper().getCustomer()).thenReturn(null);

		ExecutionResult<Customer> result = customerRepository.findCustomerByGuidAndStoreCode(CUSTOMER_GUID, SCOPE);

		assertTrue("The result should be a failure", result.isFailure());
		assertEquals("The resource status should be NOT_FOUND", ResourceStatus.NOT_FOUND, result.getResourceStatus());
	}

	@Test
	public void testFindCustomerByGuidWhenExceptionThrown() {
		when(mockCustomerSessionRepository.findCustomerSessionByGuidAndStoreCode(CUSTOMER_GUID, SCOPE))
				.thenThrow(new EpPersistenceException(EP_PERSISTENCE_EXCEPTION));

		ExecutionResult<Customer> result = customerRepository.findCustomerByGuidAndStoreCode(CUSTOMER_GUID, SCOPE);
		assertServerError(result);
	}

	@Test
	public void testUpdateCustomerWithExistingSharedId() {

		EpValidationException epValidationException = mock(EpValidationException.class);
		when(exceptionTransformer.getResourceOperationFailure(epValidationException)).thenReturn(ResourceOperationFailure.stateFailure("error"));
		when(mockCustomerService.update(mockCustomer)).thenThrow(epValidationException);

		customerRepository.updateCustomer(mockCustomer)
				.test()
				.assertError(ResourceOperationFailure.stateFailure("error"))
				.assertNoValues()
				.assertNotComplete();
	}

	@Test
	public void testUpdateCustomerWithExceptionThrown() {
		Customer customer = new CustomerImpl();
		when(mockCustomerService.update(customer)).thenThrow(new EpPersistenceException(""));

		customerRepository.updateCustomer(customer)
				.test()
				.assertError(EpPersistenceException.class)
				.assertNoValues();
	}

	private void assertServerError(final ExecutionResult result) {
		assertTrue(THE_RESULT_SHOULD_BE_A_FAILURE, result.isFailure());
		assertEquals("The resource status returned should be a SERVER_ERROR", ResourceStatus.SERVER_ERROR, result.getResourceStatus());
	}

	@Test
	public void testUpdateCustomer() {
		when(mockCustomerService.update(mockCustomer)).thenReturn(mockCustomer);
		when(mockCustomer.getGuid()).thenReturn(CUSTOMER_GUID);
		when(mockCustomerSessionRepository.invalidateCustomerSessionByGuid(CUSTOMER_GUID)).thenReturn(Completable.complete());

		customerRepository.updateCustomer(mockCustomer)
				.test()
				.assertNoErrors()
				.assertComplete();
	}

	@Test
	public void mergeCustomerShouldReturnErrorWhenAMergeCustomerFails() throws Exception {
		ExecutionResult<Object> executionResult = customerRepository.mergeCustomer(mockCustomerSession, mockCustomer, SCOPE);
		assertServerError(executionResult);
	}

	@Test
	public void mergeCustomerShouldReturnSuccess() throws Exception {
		when(mockShoppingCartService.findOrCreateDefaultCartByCustomerSession(mockCustomerSession)).thenReturn(mockShoppingCart);
		when(mockCustomerSession.getShopper()).thenReturn(mockShopper);
		when(mockShoppingCart.getShopper()).thenReturn(mockShopper);

		ExecutionResult<Object> executionResult = customerRepository.mergeCustomer(mockCustomerSession, mockCustomer, SCOPE);

		assertEquals("The resultStatus shoud be OK", ResourceStatus.UPDATE_OK, executionResult.getResourceStatus());
	}


	@Test
	public void testAddUnauthenticatedUser() {
		when(mockCustomerService.addByAuthenticate(mockCustomer, false)).thenReturn(mockCustomer);

		customerRepository.addUnauthenticatedUser(mockCustomer);

		verify(mockCustomerService, atLeastOnce()).addByAuthenticate(mockCustomer, false);
	}

	@Test
	public void testAddAddress() throws Exception {
		when(mockCustomerService.addOrUpdateAddress(mockCustomer, mockAddress)).thenReturn(mockCustomer);

		customerRepository.addAddress(mockCustomer, mockAddress)
				.test()
				.assertNoErrors()
				.assertValue(mockCustomer);
	}

	@Test
	public void testUpdateInCache() throws Exception {
		when(mockCustomerService.update(mockCustomer)).thenReturn(mockCustomer);

		customerRepository.update(mockCustomer)
				.test()
				.assertNoErrors()
				.assertValue(mockCustomer);
	}

	@Test
	public void testIsFirstTimeBuyer() throws Exception {
		Customer customer = mock(Customer.class);
		when(customer.isFirstTimeBuyer()).thenReturn(true);

		assertTrue(customerRepository.isFirstTimeBuyer(customer));
	}

	@Test
	public void testIsNotFirstTimeBuyer() throws Exception {
		Customer customer = mock(Customer.class);
		when(customer.isFirstTimeBuyer()).thenReturn(false);

		assertFalse(customerRepository.isFirstTimeBuyer(customer));
	}

	@Test
	public void testThatFindOrCreateUserReturnsExistingUser() {
		Customer expectedCustomer = new CustomerImpl();
		expectedCustomer.setCustomerType(CustomerType.SINGLE_SESSION_USER);

		when(mockCustomerSessionRepository.findCustomerSessionBySharedId(SCOPE, SHARED_ID))
				.thenReturn(ExecutionResultFactory.createReadOK(mockCustomerSession));
		when(mockCustomerSession.getShopper().getCustomer()).thenReturn(expectedCustomer);

		Customer resultCustomer = customerRepository.findOrCreateUser(null, SCOPE, SHARED_ID, null).getData();

		verify(mockCustomerSessionRepository).findCustomerSessionBySharedId(SCOPE, SHARED_ID);
		verify(mockCustomerSession.getShopper()).getCustomer();
		assertEquals("The returned customer should be the same as expected", expectedCustomer, resultCustomer);
	}

	@Test(expected = BrokenChainException.class)
	public void testThatFindOrCreateUserThrowsExceptionIfAccountSharedIdIsNull() {
		when(mockCustomerSessionRepository.findCustomerSessionBySharedId(SCOPE, SHARED_ID))
				.thenReturn(ExecutionResultFactory.createReadOK(mockCustomerSession));
		when(mockCustomerSession.getShopper().getCustomer()).thenReturn(null);

		customerRepository.findOrCreateUser(null, SCOPE, SHARED_ID, null);

		verify(mockCustomerSessionRepository, times(2)).findCustomerSessionBySharedId(SCOPE, SHARED_ID);
		verify(mockCustomerSession.getShopper(), times(2)).getCustomer();
	}

	@Test
	public void testThatFindOrCreateUserThrowsExceptionIfAccountDoesNotExist() {
		when(mockCustomerSessionRepository.findCustomerSessionBySharedId(SCOPE, SHARED_ID))
				.thenReturn(ExecutionResultFactory.createReadOK(mockCustomerSession));
		when(mockCustomerSession.getShopper().getCustomer()).thenReturn(null);
		when(mockCustomerService.findBySharedId(ACCOUNT_SHARED_ID))
				.thenReturn(null);

		assertThatThrownBy(() -> customerRepository.findOrCreateUser(null, SCOPE, SHARED_ID, ACCOUNT_SHARED_ID))
				.isInstanceOf(BrokenChainException.class)
				.extracting(exception -> ((BrokenChainException) exception).getBrokenResult())
				.extracting(result -> ((ExecutionResult) result).getStructuredErrorMessages().get(0))
				.extracting(message -> ((ImmutableMessage) message).getId())
				.isEqualTo("authentication.account.not.found");

		verify(mockCustomerSessionRepository, times(2)).findCustomerSessionBySharedId(SCOPE, SHARED_ID);
		verify(mockCustomerSession.getShopper(), times(2)).getCustomer();
		verify(mockCustomerService, times(2)).findBySharedId(ACCOUNT_SHARED_ID);
	}

	@Test(expected = BrokenChainException.class)
	public void testThatFindOrCreateUserThrowsExceptionIfThereIsFailureDuringAuthenticationOfTheUser() {
		Customer account = new CustomerImpl();
		Customer authenticatedUser = mock(Customer.class);
		CustomerDTO customerDTO = mockCustomerDTO();

		when(mockCustomerSessionRepository.findCustomerSessionBySharedId(SCOPE, SHARED_ID))
				.thenReturn(ExecutionResultFactory.createReadOK(mockCustomerSession));
		when(mockCustomerSession.getShopper().getCustomer()).thenReturn(null);
		when(mockCustomerService.findBySharedId(ACCOUNT_SHARED_ID))
				.thenReturn(account);
		when(coreBeanFactory.getPrototypeBean(ContextIdNames.CUSTOMER, Customer.class)).thenReturn(authenticatedUser);
		when(mockCustomerService.addByAuthenticate(authenticatedUser, false)).thenThrow(new RuntimeException());

		customerRepository.findOrCreateUser(customerDTO, SCOPE, SHARED_ID, ACCOUNT_SHARED_ID);

		verify(mockCustomerSessionRepository, times(2)).findCustomerSessionBySharedId(SCOPE, SHARED_ID);
		verify(mockCustomerSession.getShopper(), times(2)).getCustomer();
		verify(mockCustomerService, times(2)).findBySharedId(ACCOUNT_SHARED_ID);
		verify(coreBeanFactory, times(2)).getPrototypeBean(ContextIdNames.CUSTOMER, Customer.class);
		verify(mockCustomerService).addByAuthenticate(authenticatedUser, false);
	}

	@Test
	public void testThatFindOrCreateUserCreatesUserByValuesFromUserDTOAndAssociateItWithAccount() {
		Customer account = new CustomerImpl();
		Customer expectedUser = mock(Customer.class);
		CustomerDTO customerDTO = mockCustomerDTO();
		SettingValue settingValue = mock(SettingValue.class);
		when(settingValue.getValue()).thenReturn("BUYER");
		UserAccountAssociation userAccountAssociation = mock(UserAccountAssociation.class);

		when(mockCustomerSessionRepository.findCustomerSessionBySharedId(SCOPE, SHARED_ID))
				.thenReturn(ExecutionResultFactory.createReadOK(mockCustomerSession));
		when(mockCustomerSession.getShopper().getCustomer()).thenReturn(null);
		when(mockCustomerService.findBySharedId(ACCOUNT_SHARED_ID))
				.thenReturn(account);
		when(coreBeanFactory.getPrototypeBean(ContextIdNames.CUSTOMER, Customer.class)).thenReturn(expectedUser);
		when(mockCustomerService.addByAuthenticate(expectedUser, false)).thenReturn(expectedUser);
		when(settingsReader.getSettingValue(ACCOUNT_ROLE_FIELD_SETTING_PATH, SCOPE)).thenReturn(settingValue);
		when(userAccountAssociationService.associateUserToAccount(expectedUser, account, "BUYER"))
				.thenReturn(userAccountAssociation);

		final Customer resultCustomer = customerRepository.findOrCreateUser(customerDTO, SCOPE, SHARED_ID, ACCOUNT_SHARED_ID).getData();

		verify(mockCustomerSessionRepository).findCustomerSessionBySharedId(SCOPE, SHARED_ID);
		verify(mockCustomerSession.getShopper()).getCustomer();
		verify(mockCustomerService).findBySharedId(ACCOUNT_SHARED_ID);
		verify(coreBeanFactory).getPrototypeBean(ContextIdNames.CUSTOMER, Customer.class);
		verify(mockCustomerService).addByAuthenticate(expectedUser, false);
		verify(settingsReader).getSettingValue(ACCOUNT_ROLE_FIELD_SETTING_PATH, SCOPE);
		verify(userAccountAssociationService).associateUserToAccount(expectedUser, account, "BUYER");
		assertEquals("The returned customer should be the same as expected", expectedUser, resultCustomer);
	}

	private CustomerDTO mockCustomerDTO() {
		CustomerDTO customerDTO = mock(CustomerDTO.class);
		when(customerDTO.getStoreCode()).thenReturn(SCOPE);
		when(customerDTO.getSharedId()).thenReturn(SHARED_ID);
		when(customerDTO.getEmail()).thenReturn("email");
		when(customerDTO.getFirstName()).thenReturn("firstName");
		when(customerDTO.getLastName()).thenReturn("lastName");
		when(customerDTO.getUsername()).thenReturn("username");
		when(customerDTO.getUserCompany()).thenReturn("user company");

		return customerDTO;
	}

	@Test
	public void testGetAccountGuid() {
		final SubjectAttribute attribute = new AccountSharedIdSubjectAttribute("key", SHARED_ID);
		final Subject subject = new ImmutableSubject(emptyList(), singleton(attribute));

		final String resultGuid = customerRepository.getAccountGuid(subject);
		assertEquals("The returned customer GUID should be the same as expected", ACCOUNT_GUID, resultGuid);
	}

	@Test
	public void testGetCustomerGuidReturnsAccountGuid() {
		final SubjectAttribute attribute = new AccountSharedIdSubjectAttribute("key", SHARED_ID);
		final Subject subject = new ImmutableSubject(emptyList(), singleton(attribute));

		final String resultGuid = customerRepository.getCustomerGuid(CUSTOMER_GUID, subject);
		assertEquals("The returned GUID should be account GUID", ACCOUNT_GUID, resultGuid);
	}

	@Test
	public void testGetCustomerGuidReturnsUserGuidWhenNoSharedIdInSubject() {
		final Subject subject = new ImmutableSubject(emptyList(), emptyList());

		final String resultGuid = customerRepository.getCustomerGuid(CUSTOMER_GUID, subject);
		assertEquals("The returned GUID should be user GUID", CUSTOMER_GUID, resultGuid);
	}

	@Test
	public void testFindChildren() {
		when(mockCustomerService.findByGuid(ACCOUNT_GUID)).thenReturn(mockCustomer);
		when(accountTreeService.fetchSubtree(mockCustomer)).thenReturn(Collections.singletonList(CUSTOMER_GUID));

		final List<String> childAccounts = customerRepository.findDescendants(ACCOUNT_GUID);
		assertEquals("The returned child account guids be the same as expected", Collections.singletonList(CUSTOMER_GUID), childAccounts);
	}

	@Test
	public void testFindChildrenPaginated() {
		when(accountTreeService.fetchChildAccountGuidsPaginated(ACCOUNT_GUID, PAGE_START_INDEX, PAGE_SIZE))
				.thenReturn(Collections.singletonList(CUSTOMER_GUID));

		final List<String> childAccounts = customerRepository.findPaginatedChildren(ACCOUNT_GUID, PAGE_START_INDEX, PAGE_SIZE);
		assertEquals("The returned paginated child account guids be the same as expected", Collections.singletonList(CUSTOMER_GUID), childAccounts);
	}
}
