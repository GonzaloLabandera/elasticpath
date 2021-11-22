/*
 * Copyright (c) Elastic Path Software Inc., 2012
 */
package com.elasticpath.common.dto.customer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.unitils.reflectionassert.ReflectionAssert.assertReflectionEquals;

import java.util.Collections;
import java.util.Currency;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.unitils.reflectionassert.ReflectionComparatorMode;

import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.common.dto.AddressDTO;
import com.elasticpath.common.dto.customer.builder.CustomerDTOBuilder;
import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.commons.util.Utility;
import com.elasticpath.commons.util.impl.UtilityImpl;
import com.elasticpath.domain.attribute.AttributeType;
import com.elasticpath.domain.attribute.CustomerProfileValue;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.CustomerAddress;
import com.elasticpath.domain.customer.CustomerAuthentication;
import com.elasticpath.domain.customer.CustomerGroup;
import com.elasticpath.domain.customer.CustomerType;
import com.elasticpath.domain.customer.impl.CustomerAddressImpl;
import com.elasticpath.domain.customer.impl.CustomerAuthenticationImpl;
import com.elasticpath.domain.customer.impl.CustomerGroupImpl;
import com.elasticpath.domain.customer.impl.CustomerImpl;
import com.elasticpath.domain.misc.LocalizedProperties;
import com.elasticpath.domain.misc.RandomGuid;
import com.elasticpath.domain.misc.impl.LocalizedPropertiesImpl;
import com.elasticpath.domain.misc.impl.RandomGuidImpl;
import com.elasticpath.service.customer.AccountTreeService;
import com.elasticpath.service.customer.AddressService;
import com.elasticpath.service.customer.CustomerGroupService;
import com.elasticpath.service.customer.CustomerService;
import com.elasticpath.service.customer.impl.AddressServiceImpl;
import com.elasticpath.test.BeanFactoryExpectationsFactory;
import com.elasticpath.test.factory.CustomerAddressBuilder;
import com.elasticpath.test.factory.CustomerBuilder;
import com.elasticpath.test.factory.TestCustomerProfileFactory;

/**
 * Test {@link CustomerDtoAssembler} functionality.
 */
@SuppressWarnings({ "PMD.TooManyStaticImports", "PMD.TooManyMethods" })
public class CustomerDtoAssemblerTest {

	private static final String EXPECTED_DTO_SHOULD_EQUAL_ACTUAL = "The assembled customer DTO should be equal to the expected customer DTO.";

	private static final String EXPECTED_DOMAIN_OBJECT_SHOULD_EQUAL_ACTUAL =
			"The assembled customer domain object should be equal to the expected customer domain object.";

	private static final String DEFAULT_CUSTOMER_GROUP_GUID = "DEFAULT_CUSTOMER_GROUP_GUID";

	private static final String CUSTOMER_GROUP_GUID = "CUSTOMER_GROUP_GUID";

	private static final String SALT = "SALT";

	private static final String COMPANY_NAME = "COMPANY";

	private static final String EMAIL_ADDRESS = "customer@elasticpath.com";

	private static final String SHARED_ID = "SHARED_ID";

	private static final String USER_NAME = "USER_NAME";

	private static final String STORE_CODE = "STORE_CODE";

	private static final String PASSWORD = "PASSWORD";

	private static final String CUSTOMER_GUID = "CUSTOMER_GUID";

	private static final Currency PREFERRED_CURRENCY = Currency.getInstance("CAD");

	private static final Locale PREFERRED_LOCALE = Locale.CANADA_FRENCH;

	private static final String FAX_NUMBER = "604-555-5555";

	private static final String PHONE_NUMBER = "604-555-1234";

	private static final String LAST_NAME = "LAST";

	private static final String FIRST_NAME = "FIRST";

	private static final long CUSTOMER_UID = 123L;

	private static final String PARENT_GUID = "ParentGuid";

	private static final String SOME_ROOT_GUID = "someRootGuid";

	private static final Date DATE_OF_BIRTH = new Date();

	@Rule
	public final JUnitRuleMockery context = new JUnitRuleMockery() {
		{
			setImposteriser(ClassImposteriser.INSTANCE);
		}
	};

	private BeanFactoryExpectationsFactory expectationsFactory;

	private CustomerDtoAssembler customerDtoAssembler;

	private CustomerGroupService customerGroupService;

	private Date creationDate;

	private Date lastEditDate;

	private CustomerAddress address;

	private CustomerGroup defaultCustomerGroup;

	private CustomerGroup customerGroup;

	private CustomerService customerService;

	private AccountTreeService accountTreeService;

	private AddressService addressService;

	private final Utility utility = new UtilityImpl() {
		private static final long serialVersionUID = 1L;

		@Override
		protected String getDefaultDateFormatPattern() {
			return "EEE MMM dd HH:mm:ss z yyyy";
		}
	};

	/**
	 * Test setup.
	 */
	@Before
	public void setUp() {
		BeanFactory beanFactory = context.mock(BeanFactory.class);
		expectationsFactory = new BeanFactoryExpectationsFactory(context, beanFactory);


		creationDate = new Date();
		lastEditDate = creationDate;
		address = createAddress();
		defaultCustomerGroup = createDefaultCustomerGroup();
		customerGroup = createCustomerGroup();
		expectationsFactory.allowingBeanFactoryGetPrototypeBean(ContextIdNames.RANDOM_GUID, RandomGuid.class, RandomGuidImpl.class);
		expectationsFactory.allowingBeanFactoryGetSingletonBean(ContextIdNames.UTILITY, Utility.class, utility);
		expectationsFactory.allowingBeanFactoryGetPrototypeBean(ContextIdNames.LOCALIZED_PROPERTIES, LocalizedProperties.class, 
				LocalizedPropertiesImpl.class);
		
		expectationsFactory.allowingBeanFactoryGetPrototypeBean(ContextIdNames.CUSTOMER_ADDRESS, CustomerAddress.class, CustomerAddressImpl.class);
		expectationsFactory.allowingBeanFactoryGetPrototypeBean(ContextIdNames.CUSTOMER_AUTHENTICATION, CustomerAuthentication.class,
				CustomerAuthenticationImpl.class);
		expectationsFactory.allowingBeanFactoryGetPrototypeBean(ContextIdNames.ADDRESS_SERVICE, AddressService.class,
				AddressServiceImpl.class);

		customerService = context.mock(CustomerService.class);
		accountTreeService = context.mock(AccountTreeService.class);

		customerDtoAssembler = new CustomerDtoAssembler();
		customerDtoAssembler.setBeanFactory(beanFactory);

		customerGroupService = context.mock(CustomerGroupService.class);
		addressService = context.mock(AddressService.class);

		customerDtoAssembler.setCustomerGroupService(customerGroupService);
		customerDtoAssembler.setCustomerService(customerService);
		customerDtoAssembler.setAccountTreeService(accountTreeService);
		customerDtoAssembler.setAddressService(addressService);
	}

	/**
	 * Tear down.
	 */
	@After
	public void tearDown() {
		expectationsFactory.close();
	}

	/**
	 * Test customer DTO assembly from domain object.
	 */
	@Test
	public void testCustomerAssembleDtoFromDomainObject() {
		context.checking(new Expectations() {
			{
				allowing(addressService).findByCustomer(0L);
				will(returnValue(Collections.singletonList(address)));
			}
		});
		CustomerDTO expectedCustomerDTO = createTestCustomerDtoBuilder()
				.build();

		Customer customer = createCustomer();


		CustomerDTO customerDTO = new CustomerDTO();
		customerDtoAssembler.assembleDto(customer, customerDTO);

		assertReflectionEquals(EXPECTED_DTO_SHOULD_EQUAL_ACTUAL, expectedCustomerDTO, customerDTO, ReflectionComparatorMode.LENIENT_ORDER);
	}

	/**
	 * Test customer DTO assembly from domain object with parent guid.
	 */
	@Test
	public void testCustomerAssembleDtoFromDomainObjectWithParentGuid() {
		context.checking(new Expectations() {
			{
				allowing(addressService).findByCustomer(0L);
				will(returnValue(Collections.singletonList(address)));
			}
		});
		CustomerDTO expectedCustomerDTO = createCustomerDTOWithParent(PARENT_GUID, CustomerType.ACCOUNT);

		Customer customer = createAccountWithAllFields(PARENT_GUID);

		CustomerDTO customerDTO = new CustomerDTO();
		customerDtoAssembler.assembleDto(customer, customerDTO);

		assertReflectionEquals(EXPECTED_DTO_SHOULD_EQUAL_ACTUAL, expectedCustomerDTO, customerDTO, ReflectionComparatorMode.LENIENT_ORDER);
	}

	/**
	 * Test customer DTO assembly does not set preferred billing address when null on the domain object being translated.
	 */
	@Test
	public void testCustomerAssembleDtoDoesNotSetPreferredBillingAddressWhenNull() {
		context.checking(new Expectations() {
			{
				allowing(addressService).findByCustomer(0L);
				will(returnValue(Collections.singletonList(address)));
			}
		});

		CustomerDTO expectedCustomerDto = createTestCustomerDtoBuilder().build();
		expectedCustomerDto.setPreferredBillingAddressGuid(null);

		Customer customer = createCustomer();
		customer.setPreferredBillingAddress(null);

		CustomerDTO customerDto = new CustomerDTO();

		customerDtoAssembler.assembleDto(customer, customerDto);

		assertReflectionEquals(EXPECTED_DTO_SHOULD_EQUAL_ACTUAL, expectedCustomerDto, customerDto, ReflectionComparatorMode.LENIENT_ORDER);
		assertNull("The preferred billing address guid should be null.", customerDto.getPreferredBillingAddressGuid());
	}

	/**
	 * Test customer DTO assembly does not set preferred shipping address when null on the domain object being translated.
	 */
	@Test
	public void testCustomerAssembleDtoDoesNotSetPreferredShippingingAddressWhenNull() {
		context.checking(new Expectations() {
			{
				allowing(addressService).findByCustomer(0L);
				will(returnValue(Collections.singletonList(address)));
			}
		});

		CustomerDTO expectedCustomerDto = createTestCustomerDtoBuilder().build();
		expectedCustomerDto.setPreferredShippingAddressGuid(null);

		Customer customer = createCustomer();
		customer.setPreferredShippingAddress(null);

		CustomerDTO customerDto = new CustomerDTO();

		customerDtoAssembler.assembleDto(customer, customerDto);

		assertReflectionEquals(EXPECTED_DTO_SHOULD_EQUAL_ACTUAL, expectedCustomerDto, customerDto, ReflectionComparatorMode.LENIENT_ORDER);
		assertNull("The preferred shipping address guid should be null.", customerDto.getPreferredShippingAddressGuid());
	}

	/**
	 * Test customer domain assembly from DTO.
	 */
	@Test
	public void testCustomerDomainAssemblyFromDto() {
		shouldFindDefaultCustomerGroupByName();
		shouldFindCustomerGroupsByGuid();
		Customer expectedCustomer = createCustomer();

		CustomerDTO customerDto = createTestCustomerDtoBuilder().build();

		Customer customer = CustomerBuilder.newCustomer().build();

		customerDtoAssembler.assembleDomain(customerDto, customer);

		assertReflectionEquals(EXPECTED_DOMAIN_OBJECT_SHOULD_EQUAL_ACTUAL, expectedCustomer, customer, ReflectionComparatorMode.LENIENT_DATES);
		assertFalse("First time buyer field must be false", customer.isFirstTimeBuyer());
	}

	/**
	 * Test customer domain with parent assembly from DTO.
	 */
	@Test
	public void testCustomerDomainWithParentAssemblyFromDto() {
		shouldFindDefaultCustomerGroupByName();
		shouldFindCustomerGroupsByGuid();

		final Customer parent = createAccount(null);

		shouldFindCustomerTypeByGuid(parent.getCustomerType());
		shouldFindRootCustomerGuid(parent, SOME_ROOT_GUID);

		Customer expectedCustomer = createAccountWithAllFields(PARENT_GUID);

		CustomerDTO customerDto = createCustomerDTOWithParent(PARENT_GUID, CustomerType.ACCOUNT);

		Customer customer = CustomerBuilder.newCustomer().build();

		customerDtoAssembler.assembleDomain(customerDto, customer);

		assertReflectionEquals(EXPECTED_DOMAIN_OBJECT_SHOULD_EQUAL_ACTUAL, expectedCustomer, customer, ReflectionComparatorMode.LENIENT_DATES);
	}

	/**
	 * Test that assemble domain method throws exception if parent does not exist in database.
	 */
	@Test(expected = EpServiceException.class)
	public void testThatAssembleDomainMethodThrowsExceptionIfParentDoesNotExistInDb() {
		shouldFindDefaultCustomerGroupByName();
		shouldFindCustomerGroupsByGuid();
		shouldFindCustomerTypeByGuid(null);

		CustomerDTO customerDto = createCustomerDTOWithParent(PARENT_GUID, CustomerType.ACCOUNT);
		Customer customer = CustomerBuilder.newCustomer().build();

		customerDtoAssembler.assembleDomain(customerDto, customer);
	}

	/**
	 * Test that assembleDomain method throws exception if target is persisted and parentGuid of the source is different.
	 */
	@Test(expected = EpServiceException.class)
	public void testThatAssembleDomainMethodThrowsExceptionIfTargetIsPersistedAndParentGuidOfTheSourceIsDifferent() {
		shouldFindDefaultCustomerGroupByName();
		shouldFindCustomerGroupsByGuid();

		CustomerDTO customerDto = createCustomerDTOWithParent(PARENT_GUID, CustomerType.ACCOUNT);
		customerDto.setGuid(SOME_ROOT_GUID);

		Customer customer = CustomerBuilder.newCustomer().build();
		customer.setUidPk(CUSTOMER_UID);

		customerDtoAssembler.assembleDomain(customerDto, customer);
	}

	/**
	 * Test that assembleDomain method throws exception if parent is not Account.
	 */
	@Test(expected = EpServiceException.class)
	public void testThatAssembleDomainMethodThrowsExceptionIfParentIsNotAccount() {
		shouldFindDefaultCustomerGroupByName();
		shouldFindCustomerGroupsByGuid();

		final Customer parent = createCustomer();

		shouldFindCustomerTypeByGuid(parent.getCustomerType());

		CustomerDTO customerDto = createCustomerDTOWithParent(PARENT_GUID, CustomerType.ACCOUNT);

		Customer customer = CustomerBuilder.newCustomer().build();

		customerDtoAssembler.assembleDomain(customerDto, customer);
	}

	/**
	 * Test that assembleDomain method throws exception if CustomerDto is not an Account.
	 */
	@Test(expected = EpServiceException.class)
	public void testThatAssembleDomainMethodThrowsExceptionIfCustomerDtoIsNotAnAccount() {
		shouldFindDefaultCustomerGroupByName();
		shouldFindCustomerGroupsByGuid();

		CustomerDTO customerDto = createCustomerDTOWithParent(PARENT_GUID, CustomerType.SINGLE_SESSION_USER);

		Customer customer = CustomerBuilder.newCustomer().build();

		customerDtoAssembler.assembleDomain(customerDto, customer);
	}

	private void shouldFindRootCustomerGuid(final Customer parent, final String rootGuid) {
		context.checking(new Expectations() {
			{
				allowing(accountTreeService).findAncestorGuids(parent.getGuid());
				will(returnValue(Collections.singletonList(rootGuid)));
			}
		});
	}

	/**
	 * Test customer assemble domain does not create duplicate addresses when assembling the domain object.
	 */
	@Test
	public void testCustomerAssembleDomainDoesNotCreateDuplicateAddressesWhenAssemblingDomainObject() {
		shouldFindDefaultCustomerGroupByName();
		shouldFindCustomerGroupsByGuid();

		Customer expectedCustomer = createCustomer();

		CustomerDTO customerDto = createTestCustomerDtoBuilder()
				.withAddresses(createAddressDto(address))
				.build();

		Customer customer = CustomerBuilder.newCustomer().build();

		customerDtoAssembler.assembleDomain(customerDto, customer);

		assertReflectionEquals(EXPECTED_DOMAIN_OBJECT_SHOULD_EQUAL_ACTUAL, expectedCustomer, customer, ReflectionComparatorMode.LENIENT_DATES);
		assertEquals("The domain object should have no duplicate addresses", 1, customer.getTransientAddresses().size());
	}

	/**
	 * Make sure default customer group is always included when assembling domain.
	 */
	@Test
	public void testCustomerAssembleDomainCreateImplicitDefaultCustomerGroup() {
		shouldFindDefaultCustomerGroupByName();

		final CustomerDTO customerDto = createTestCustomerDtoBuilder()
				.withGroups(new String[0])
				.build();

		final Customer customer = CustomerBuilder.newCustomer().build();

		customerDtoAssembler.assembleDomain(customerDto, customer);

		assertEquals("The domain object should have the default customer group assigned to it", 1, customer.getCustomerGroups().size());
		assertSame("The customer group assigned should be the default customer group",
				defaultCustomerGroup, customer.getCustomerGroups().get(0));
	}

	/**
	 * Make sure default customer group is not duplicated when assembling domain.
	 */
	@Test
	public void testCustomerAssembleDomainDefaultCustomerGroupNotDuplicated() {
		shouldFindDefaultCustomerGroupByName();
		shouldFindDefaultCustomerGroupsByGuid();

		final CustomerDTO customerDto = createTestCustomerDtoBuilder()
				.withGroups(defaultCustomerGroup.getGuid())
				.build();

		final Customer customer = CustomerBuilder.newCustomer().build();

		customerDtoAssembler.assembleDomain(customerDto, customer);

		assertEquals("The domain object should only have 1 instance of the default customer group assigned to it",
				1, customer.getCustomerGroups().size());
		assertSame("The customer group assigned should be the default customer group",
				defaultCustomerGroup, customer.getCustomerGroups().get(0));
	}

	/**
	 * Test customer assemble domain does not create duplicate customer groups when assembling the domain object.
	 */
	@Test
	public void testCustomerAssembleDomainDoesNotCreateDuplicateCustomerGroupsWhenAssemblingDomainObject() {
		shouldFindDefaultCustomerGroupByName();
		shouldFindCustomerGroupsByGuid();

		Customer expectedCustomer = createCustomer();

		CustomerDTO customerDto = createTestCustomerDtoBuilder()
				.withGroups(CUSTOMER_GROUP_GUID, customerGroup.getGuid())
				.build();

		Customer customer = CustomerBuilder.newCustomer().build();

		customerDtoAssembler.assembleDomain(customerDto, customer);

		assertReflectionEquals(EXPECTED_DOMAIN_OBJECT_SHOULD_EQUAL_ACTUAL, expectedCustomer, customer, ReflectionComparatorMode.LENIENT_DATES);
		assertEquals("The domain object should have exactly 2 customer groups", 2, customer.getCustomerGroups().size());
		assertNotEquals("The domain object should have no duplicate customer groups",
				customer.getCustomerGroups().get(0).getGuid(), customer.getCustomerGroups().get(1).getGuid());
	}

	/**
	 * Test customer dto assembler uses emptying filter if no card filter set.
	 */
	@Test
	public void testCustomerDtoAssemblerUsesEmptyingFilterIfNoCardFilterSet() {
		context.checking(new Expectations() {
			{
				allowing(addressService).findByCustomer(0L);
				will(returnValue(Collections.emptyList()));
			}
		});
		Customer customer = CustomerBuilder.newCustomer().build();
		CustomerDTO customerDto = new CustomerDTO();

		customerDtoAssembler.assembleDto(customer, customerDto);
	}

	@Test
	public void testCustomerDtoAssemblerAssemblesCustomerProfileValuesWithCreationDate() {
		context.checking(new Expectations() {
			{
				allowing(addressService).findByCustomer(0L);
				will(returnValue(Collections.emptyList()));
			}
		});
		Customer customer = CustomerBuilder.newCustomer().build();
		customer.setPreferredShippingAddress(null);

		Date creationDate = new Date();
		customer.getProfileValueMap()
				.values()
				.forEach(value -> value.setCreationDate(creationDate));

		CustomerDTO customerDto = new CustomerDTO();

		customerDtoAssembler.assembleDto(customer, customerDto);

		List<Date> dtoCreationDates = customerDto.getProfileValues().stream()
				.map(AttributeValueDTO::getCreationDate)
				.collect(Collectors.toList());

		assertThat(dtoCreationDates)
				.allMatch(date -> date.equals(creationDate));
	}

	@Test
	public void testCustomerDtoAssemblerAssemblesCustomerProfileValuesDtoWithCreationDate() {
		shouldFindDefaultCustomerGroupByName();
		shouldFindDefaultCustomerGroupsByGuid();
		shouldFindCustomerGroupsByGuid();

		CustomerDTO customerDTO = createTestCustomerDtoBuilder().build();

		Date creationDate = new Date();
		customerDTO.getProfileValues()
				.forEach(value -> value.setCreationDate(creationDate));

		Customer customer = CustomerBuilder.newCustomer().build();

		customerDtoAssembler.assembleDomain(customerDTO, customer);

		List<Date> domainCreationDates = customer.getProfileValueMap().values()
				.stream()
				.map(CustomerProfileValue::getCreationDate)
				.collect(Collectors.toList());

		assertThat(domainCreationDates)
				.allMatch(date -> date.equals(creationDate));
	}

	/**
	 * Test that assembleDomain method throws exception if target is persisted and customer type of the source is different.
	 */
	@Test(expected = EpServiceException.class)
	public void testThatAssembleDomainMethodThrowsExceptionIfTargetIsPersistedAndCustomerTypeOfTheSourceIsDifferent() {
		shouldFindDefaultCustomerGroupByName();
		shouldFindCustomerGroupsByGuid();

		CustomerDTO customerDto = createTestCustomerDtoBuilder()
				.withCustomerType(CustomerType.ACCOUNT.getName())
				.build();

		Customer customer = CustomerBuilder.newCustomer().build();
		customer.setCustomerType(CustomerType.REGISTERED_USER);
		customer.setUidPk(CUSTOMER_UID);


		customerDtoAssembler.assembleDomain(customerDto, customer);
	}

	private void shouldFindDefaultCustomerGroupByName() {
		context.checking(new Expectations() {
			{
				allowing(customerGroupService).findByGroupName(CustomerGroup.DEFAULT_GROUP_NAME);
				will(returnValue(defaultCustomerGroup));
			}
		});
	}

	private void shouldFindDefaultCustomerGroupsByGuid() {
		context.checking(new Expectations() {
			{
				allowing(customerGroupService).findByGuid(DEFAULT_CUSTOMER_GROUP_GUID);
				will(returnValue(defaultCustomerGroup));
			}
		});
	}

	private void shouldFindCustomerTypeByGuid(final CustomerType customerType) {
		context.checking(new Expectations() {
			{
				allowing(customerService).getCustomerTypeByGuid(PARENT_GUID);
				will(returnValue(customerType));
			}
		});
	}

	private void shouldFindCustomerGroupsByGuid() {
		shouldFindDefaultCustomerGroupsByGuid();
		context.checking(new Expectations() {
			{
				allowing(customerGroupService).findByGuid(CUSTOMER_GROUP_GUID);
				will(returnValue(customerGroup));
			}
		});
	}

	private Customer createCustomer() {
		return CustomerBuilder.newCustomer()
			.withGuid(CUSTOMER_GUID)
			.withCreationDate(creationDate)
			.withLastEditDate(lastEditDate)
			.withAddedAddress(address)
			.withPreferredBillingAddress(address)
			.withPreferredShippingAddress(address)
			.withPassword(PASSWORD, SALT)
			.withStatus(Customer.STATUS_ACTIVE)
			.withStoreCode(STORE_CODE)
			.withSharedId(SHARED_ID)
			.withUsername(USER_NAME)
			.withAddedCustomerGroup(defaultCustomerGroup)
			.withAddedCustomerGroup(customerGroup)
			.withEmail(EMAIL_ADDRESS)
			.withPreferredLocale(PREFERRED_LOCALE)
			.withPreferredCurrency(PREFERRED_CURRENCY)
			.withFirstName(FIRST_NAME)
			.withLastName(LAST_NAME)
			.withCustomerType(CustomerType.REGISTERED_USER)
			.withDateOfBirth(DATE_OF_BIRTH)
			.withPhoneNumber(PHONE_NUMBER)
			.withCompany(COMPANY_NAME)
			.withToBeNotified(true)
			.withHtmlEmailPreferred(true)
			.withFaxNumber(FAX_NUMBER)
			.withFirstTimeBuyer(false)
			.withCustomerType(CustomerType.SINGLE_SESSION_USER)
			.build();
	}

	private Customer createAccountWithAllFields(final String parentGuid) {
		final Customer customer = createCustomer();
		customer.setParentGuid(parentGuid);
		customer.setCustomerType(CustomerType.ACCOUNT);
		customer.setCustomerProfileAttributes(new TestCustomerProfileFactory().getProfile());
		return customer;
	}

	private Customer createAccount(final String parentGuid) {
		final Customer account = CustomerBuilder.newCustomer().build();
		account.setCustomerType(CustomerType.ACCOUNT);
		account.setParentGuid(parentGuid);
		account.setCustomerProfileAttributes(new TestCustomerProfileFactory().getProfile());
		return account;
	}

	private CustomerGroup createDefaultCustomerGroup() {
		CustomerGroup customerGroup = new CustomerGroupImpl();

		customerGroup.setGuid(DEFAULT_CUSTOMER_GROUP_GUID);
		customerGroup.setName(CustomerGroup.DEFAULT_GROUP_NAME);

		return customerGroup;
	}

	private CustomerGroup createCustomerGroup() {
		CustomerGroup customerGroup = new CustomerGroupImpl();

		customerGroup.setGuid(CUSTOMER_GROUP_GUID);
		customerGroup.setName("CUSTOMER_GROUP_NAME");

		return customerGroup;
	}

	private CustomerAddress createAddress() {
		return CustomerAddressBuilder.newCustomerAddress()
				.withGuid("CUSTOMER_ADDRESS_GUID")
				.withFirstName(FIRST_NAME)
				.withLastName(LAST_NAME)
				.withStreet1("STREET 1")
				.withStreet2("STREET 2")
				.withCity("CITY")
				.withSubCountry("SUBCOUNTRY")
				.withCountry("COUNTRY")
				.withZipOrPostalCode("ZIPCODE")
				.withCommercialAddress(false)
				.withOrganization(COMPANY_NAME)
				.withFaxNumber(FAX_NUMBER)
				.withPhoneNumber(PHONE_NUMBER)
				.build();
	}

	private CustomerDTOBuilder createTestCustomerDtoBuilder() {
		HashSet<AttributeValueDTO> attributeValueDTOs = new HashSet<>();
		attributeValueDTOs.add(createAttributeValueDto(CustomerImpl.ATT_KEY_CP_FIRST_NAME, AttributeType.SHORT_TEXT.toString(), FIRST_NAME));
		attributeValueDTOs.add(createAttributeValueDto(CustomerImpl.ATT_KEY_CP_LAST_NAME, AttributeType.SHORT_TEXT.toString(), LAST_NAME));
		attributeValueDTOs.add(createAttributeValueDto(CustomerImpl.ATT_KEY_CP_EMAIL, AttributeType.SHORT_TEXT.toString(), EMAIL_ADDRESS));
		attributeValueDTOs.add(createAttributeValueDto(CustomerImpl.ATT_KEY_CP_PREF_LOCALE,
				AttributeType.SHORT_TEXT.toString(),
				PREFERRED_LOCALE.toString()));
		attributeValueDTOs.add(createAttributeValueDto(CustomerImpl.ATT_KEY_CP_PREF_CURR,
				AttributeType.SHORT_TEXT.toString(),
				PREFERRED_CURRENCY.toString()));
		attributeValueDTOs.add(createAttributeValueDto(CustomerImpl.ATT_KEY_CP_PHONE, AttributeType.SHORT_TEXT.toString(), PHONE_NUMBER));
		attributeValueDTOs.add(createAttributeValueDto(CustomerImpl.ATT_KEY_CP_FAX, AttributeType.SHORT_TEXT.toString(), FAX_NUMBER));
		attributeValueDTOs.add(createAttributeValueDto(CustomerImpl.ATT_KEY_CP_COMPANY, AttributeType.SHORT_TEXT.toString(), COMPANY_NAME));
		attributeValueDTOs.add(createAttributeValueDto(CustomerImpl.ATT_KEY_CP_DOB,
				AttributeType.DATE.toString(),
				DATE_OF_BIRTH.toString()));
		attributeValueDTOs.add(createAttributeValueDto(CustomerImpl.ATT_KEY_CP_HTML_EMAIL, AttributeType.BOOLEAN.toString(), "true"));
		attributeValueDTOs.add(createAttributeValueDto(CustomerImpl.ATT_KEY_CP_BE_NOTIFIED, AttributeType.BOOLEAN.toString(), "true"));

		return new CustomerDTOBuilder()
				.withGuid(CUSTOMER_GUID)
				.withCreationDate(creationDate)
				.withLastEditDate(lastEditDate)
				.withProfileValues(attributeValueDTOs)
				.withPreferredShippingAddressGuid(address.getGuid())
				.withPreferredBillingAddressGuid(address.getGuid())
				.withAddresses(createAddressDto(address))
				.withPassword(PASSWORD)
				.withSalt(SALT)
				.withStatus(Customer.STATUS_ACTIVE)
				.withStoreCode(STORE_CODE)
				.withSharedId(SHARED_ID)
				.withUsername(USER_NAME)
				.withCustomerType(CustomerType.SINGLE_SESSION_USER.getName())
				.withGroups(defaultCustomerGroup.getGuid(), customerGroup.getGuid());
	}

	private CustomerDTO createCustomerDTOWithParent(final String parentGuid, final CustomerType customerType) {
		return createTestCustomerDtoBuilder()
				.withCustomerType(customerType.getName())
				.withParentGuid(parentGuid)
				.build();
	}

	private AddressDTO createAddressDto(final CustomerAddress address) {
		AddressDTO addressDto = new AddressDTO();

		addressDto.setGuid(address.getGuid());
		addressDto.setFirstName(address.getFirstName());
		addressDto.setLastName(address.getLastName());
		addressDto.setStreet1(address.getStreet1());
		addressDto.setStreet2(address.getStreet2());
		addressDto.setCity(address.getCity());
		addressDto.setSubCountry(address.getSubCountry());
		addressDto.setCountry(address.getCountry());
		addressDto.setZipOrPostalCode(address.getZipOrPostalCode());
		addressDto.setCommercialAddress(address.isCommercialAddress());
		addressDto.setOrganization(address.getOrganization());
		addressDto.setPhoneNumber(address.getPhoneNumber());
		addressDto.setFaxNumber(address.getFaxNumber());

		return addressDto;
	}

	private AttributeValueDTO createAttributeValueDto(final String key, final String type, final String value) {
		AttributeValueDTO attributeValueDTO = new AttributeValueDTO();

		attributeValueDTO.setKey(key);
		attributeValueDTO.setType(type);
		attributeValueDTO.setValue(value);

		return attributeValueDTO;
	}

}
