/**
 * Copyright (c) Elastic Path Software Inc., 2012
 */
package com.elasticpath.common.dto.customer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.unitils.reflectionassert.ReflectionAssert.assertReflectionEquals;

import java.util.Arrays;
import java.util.Currency;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;

import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.unitils.reflectionassert.ReflectionComparatorMode;

import com.elasticpath.common.dto.AddressDTO;
import com.elasticpath.common.dto.assembler.customer.BuiltinFilters;
import com.elasticpath.common.dto.customer.builder.CreditCardDTOBuilder;
import com.elasticpath.common.dto.customer.builder.CustomerDTOBuilder;
import com.elasticpath.common.dto.customer.builder.LegacyCreditCardDTOBuilder;
import com.elasticpath.common.dto.customer.transformer.CreditCardDTOTransformer;
import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.commons.util.Utility;
import com.elasticpath.commons.util.impl.UtilityImpl;
import com.elasticpath.domain.attribute.AttributeType;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.CustomerAddress;
import com.elasticpath.domain.customer.CustomerAuthentication;
import com.elasticpath.domain.customer.CustomerCreditCard;
import com.elasticpath.domain.customer.CustomerGroup;
import com.elasticpath.domain.customer.impl.CustomerAddressImpl;
import com.elasticpath.domain.customer.impl.CustomerAuthenticationImpl;
import com.elasticpath.domain.customer.impl.CustomerCreditCardImpl;
import com.elasticpath.domain.customer.impl.CustomerGroupImpl;
import com.elasticpath.domain.customer.impl.CustomerImpl;
import com.elasticpath.domain.misc.impl.RandomGuidImpl;
import com.elasticpath.service.customer.CustomerGroupService;
import com.elasticpath.test.BeanFactoryExpectationsFactory;
import com.elasticpath.test.factory.CustomerAddressBuilder;
import com.elasticpath.test.factory.CustomerBuilder;
import com.elasticpath.test.factory.CustomerCreditCardBuilder;

/**
 * Test {@link CustomerDtoAssembler} functionality.
 */
@SuppressWarnings({ "PMD.TooManyStaticImports", "PMD.TooManyMethods" })
public class CustomerDtoAssemblerTest {

	private static final String EXPECTED_DTO_SHOULD_EQUAL_ACTUAL = "The assembled customer DTO should be equal to the expected customer DTO.";

	private static final String EXPECTED_DOMAIN_OBJECT_SHOULD_EQUAL_ACTUAL =
			"The assembled customer domain object should be equal to the expected customer domain object.";

	private static final String CREDIT_CARD_TYPE_VISA = "VISA";

	private static final String CREDIT_CARD_GUID = "CREDIT_CARD_GUID";

	private static final String DEFAULT_CUSTOMER_GROUP_GUID = "DEFAULT_CUSTOMER_GROUP_GUID";

	private static final String CUSTOMER_GROUP_GUID = "CUSTOMER_GROUP_GUID";

	private static final String SALT = "SALT";

	private static final String COMPANY_NAME = "COMPANY";

	private static final char GENDER = 'M';

	private static final String EMAIL_ADDRESS = "customer@elasticpath.com";

	private static final String USER_ID = "USER_ID";

	private static final String STORE_CODE = "STORE_CODE";

	private static final String PASSWORD = "PASSWORD";

	private static final String CUSTOMER_GUID = "CUSTOMER_GUID";

	private static final Currency PREFERRED_CURRENCY = Currency.getInstance("CAD");

	private static final Locale PREFERRED_LOCALE = Locale.CANADA_FRENCH;

	private static final String FAX_NUMBER = "604-555-5555";

	private static final String PHONE_NUMBER = "604-555-1234";

	private static final String LAST_NAME = "LAST";

	private static final String FIRST_NAME = "FIRST";

	private static final Date DATE_OF_BIRTH = new Date();

	@Rule
	public final JUnitRuleMockery context = new JUnitRuleMockery() {
		{
			setImposteriser(ClassImposteriser.INSTANCE);
		}
	};

	private BeanFactory beanFactory;

	private BeanFactoryExpectationsFactory expectationsFactory;

	private CustomerDtoAssembler customerDtoAssembler;

	private CustomerGroupService customerGroupService;

	private Date creationDate;

	private Date lastEditDate;

	private CustomerAddress address;

	private CustomerGroup defaultCustomerGroup;

	private CustomerGroup customerGroup;

	private CreditCardDTOTransformer creditCardDTOTransformer;

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
		beanFactory = context.mock(BeanFactory.class);
		expectationsFactory = new BeanFactoryExpectationsFactory(context, beanFactory);

		creditCardDTOTransformer = context.mock(CreditCardDTOTransformer.class);

		expectationsFactory.allowingBeanFactoryGetBean(ContextIdNames.RANDOM_GUID, RandomGuidImpl.class);
		expectationsFactory.allowingBeanFactoryGetBean(ContextIdNames.UTILITY, utility);

		expectationsFactory.allowingBeanFactoryGetBean(ContextIdNames.CUSTOMER_ADDRESS, CustomerAddressImpl.class);
		expectationsFactory.allowingBeanFactoryGetBean(ContextIdNames.CUSTOMER_CREDIT_CARD, CustomerCreditCardImpl.class);
		expectationsFactory.allowingBeanFactoryGetBean(ContextIdNames.CUSTOMER_AUTHENTICATION, CustomerAuthenticationImpl.class);

		customerDtoAssembler = new CustomerDtoAssembler();
		customerDtoAssembler.setBeanFactory(beanFactory);
		customerDtoAssembler.setCardFilter(BuiltinFilters.STATIC);

		customerGroupService = context.mock(CustomerGroupService.class);
		customerDtoAssembler.setCustomerGroupService(customerGroupService);

		customerDtoAssembler.setCreditCardDTOTransformer(creditCardDTOTransformer);

		creationDate = new Date();
		lastEditDate = creationDate;
		address = createAddress();
		defaultCustomerGroup = createDefaultCustomerGroup();
		customerGroup = createCustomerGroup();
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
		CustomerCreditCard testCreditCard = createCreditCard(CREDIT_CARD_GUID, CREDIT_CARD_TYPE_VISA);

		CreditCardDTO expectedCreditCardDto = createTestCreditCardDtoFromCreditCard(testCreditCard);
		CustomerDTO expectedCustomerDTO = createTestCustomerDtoBuilder()
				.withPaymentMethods(expectedCreditCardDto)
				.withDefaultPaymentMethod(expectedCreditCardDto)
				.build();

		shouldTransformCreditCardToDto(testCreditCard, expectedCreditCardDto);

		Customer customerWithCreditCards = createCustomerWithCreditCards(testCreditCard);
		customerWithCreditCards.getPaymentMethods().setDefault(testCreditCard);

		CustomerDTO customerDTO = new CustomerDTO();
		customerDtoAssembler.assembleDto(customerWithCreditCards, customerDTO);

		assertReflectionEquals(EXPECTED_DTO_SHOULD_EQUAL_ACTUAL, expectedCustomerDTO, customerDTO, ReflectionComparatorMode.LENIENT_ORDER);
	}

	/**
	 * Test customer DTO assembly does not set preferred billing address when null on the domain object being translated.
	 */
	@Test
	public void testCustomerAssembleDtoDoesNotSetPreferredBillingAddressWhenNull() {
		CustomerDTO expectedCustomerDto = createTestCustomerDtoBuilder().build();
		expectedCustomerDto.setPreferredBillingAddressGuid(null);

		Customer customer = createCustomerWithCreditCards();
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
		CustomerDTO expectedCustomerDto = createTestCustomerDtoBuilder().build();
		expectedCustomerDto.setPreferredShippingAddressGuid(null);

		Customer customer = createCustomerWithCreditCards();
		customer.setPreferredShippingAddress(null);

		CustomerDTO customerDto = new CustomerDTO();

		customerDtoAssembler.assembleDto(customer, customerDto);

		assertReflectionEquals(EXPECTED_DTO_SHOULD_EQUAL_ACTUAL, expectedCustomerDto, customerDto, ReflectionComparatorMode.LENIENT_ORDER);
		assertNull("The preferred shipping address guid should be null.", customerDto.getPreferredShippingAddressGuid());
	}

	/**
	 * Test customer DTO assembly does not use credit card when card type not populated with a recognized card type on the domain object being
	 * translated.
	 */
	@Test
	public void testCustomerAssembleDtoDoesNotUseCreditCardWhenCardTypeNotPopulatedWithRecognizedCardType() {
		CustomerCreditCard creditCard = createCreditCard(CREDIT_CARD_GUID, CREDIT_CARD_TYPE_VISA);
		CreditCardDTO expectedCreditCard = createTestCreditCardDtoFromCreditCard(creditCard);

		CustomerDTO expectedCustomerDto = createTestCustomerDtoBuilder()
				.withPaymentMethods(expectedCreditCard)
				.withDefaultPaymentMethod(expectedCreditCard)
				.build();

		Customer customer = createCustomerWithCreditCards(creditCard);
		customer.getCreditCards().get(0).setCardType("UNKNOWN_CARD_TYPE");

		shouldTransformCreditCardToDto(creditCard, expectedCreditCard);
		CustomerDTO customerDto = new CustomerDTO();

		customerDtoAssembler.assembleDto(customer, customerDto);

		assertReflectionEquals(EXPECTED_DTO_SHOULD_EQUAL_ACTUAL, expectedCustomerDto, customerDto, ReflectionComparatorMode.LENIENT_ORDER);
	}

	/**
	 * Test customer domain assembly from DTO.
	 */
	@Test
	public void testCustomerDomainAssemblyFromDto() {
		shouldFindDefaultCustomerGroupByName();
		shouldFindCustomerGroupsByGuid();
		CustomerCreditCard expectedCreditCard = createCreditCard(CREDIT_CARD_GUID, CREDIT_CARD_TYPE_VISA);
		Customer expectedCustomer = createCustomerWithCreditCards(expectedCreditCard);

		CreditCardDTO creditCardDTO = createTestCreditCardDtoFromCreditCard(expectedCreditCard);

		CustomerDTO customerDto = createTestCustomerDtoBuilder()
				.withPaymentMethods(creditCardDTO)
				.build();

		Customer customer = CustomerBuilder.newCustomer().build();

		shouldTransformCreditCardDtoToDomain(creditCardDTO, expectedCreditCard);
		customerDtoAssembler.assembleDomain(customerDto, customer);

		assertReflectionEquals(EXPECTED_DOMAIN_OBJECT_SHOULD_EQUAL_ACTUAL, expectedCustomer, customer, ReflectionComparatorMode.LENIENT_DATES);
		assertFalse("First time buyer field must be false", customer.isFirstTimeBuyer());
	}

	/**
	 * Test customer assemble domain does not create duplicate addresses when assembling the domain object.
	 */
	@Test
	public void testCustomerAssembleDomainDoesNotCreateDuplicateAddressesWhenAssemblingDomainObject() {
		shouldFindDefaultCustomerGroupByName();
		shouldFindCustomerGroupsByGuid();

		Customer expectedCustomer = createCustomerWithCreditCards();

		CustomerDTO customerDto = createTestCustomerDtoBuilder()
				.withAddresses(createAddressDto(address))
				.build();

		Customer customer = CustomerBuilder.newCustomer().build();

		customerDtoAssembler.assembleDomain(customerDto, customer);

		assertReflectionEquals(EXPECTED_DOMAIN_OBJECT_SHOULD_EQUAL_ACTUAL, expectedCustomer, customer, ReflectionComparatorMode.LENIENT_DATES);
		assertEquals("The domain object should have no duplicate addresses", 1, customer.getAddresses().size());
	}

	/**
	 * Test customer assemble domain creates multiple credit cards when assembling domain object.
	 */
	@Test
	public void testCustomerAssembleDomainCreatesMultipleCreditCardsWhenAssemblingDomainObject() {
		shouldFindDefaultCustomerGroupByName();
		shouldFindCustomerGroupsByGuid();

		CustomerCreditCard expectedCreditCard = createCreditCard(CREDIT_CARD_GUID, CREDIT_CARD_TYPE_VISA);
		CustomerCreditCard expectedAlternateCreditCard = createCreditCard("ALTERNATE_CARD_GUID", "MASTERCARD");

		Customer expectedCustomer = createCustomerWithCreditCards(expectedCreditCard, expectedAlternateCreditCard);
		expectedCustomer.getPaymentMethods().setDefault(expectedCreditCard);

		CreditCardDTO creditCardDto = createTestCreditCardDtoFromCreditCard(expectedCreditCard);
		CreditCardDTO alternativeCreditCardDto = createTestCreditCardDtoFromCreditCard(expectedAlternateCreditCard);
		CustomerDTO customerDto = createTestCustomerDtoBuilder()
				.withPaymentMethods(creditCardDto, alternativeCreditCardDto)
				.build();

		Customer customer = CustomerBuilder.newCustomer().build();

		shouldTransformCreditCardDtoToDomain(creditCardDto, expectedCreditCard);
		shouldTransformCreditCardDtoToDomain(alternativeCreditCardDto, expectedAlternateCreditCard);

		customerDtoAssembler.assembleDomain(customerDto, customer);

		assertReflectionEquals(EXPECTED_DOMAIN_OBJECT_SHOULD_EQUAL_ACTUAL, expectedCustomer, customer, ReflectionComparatorMode.LENIENT_DATES);
		assertEquals("The domain object should have two credit cards.", 2, customer.getCreditCards().size());
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

		Customer expectedCustomer = createCustomerWithCreditCards();

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
		Customer customer = CustomerBuilder.newCustomer().build();
		CustomerDTO customerDto = new CustomerDTO();

		customerDtoAssembler.setCardFilter(null);
		customerDtoAssembler.assembleDto(customer, customerDto);

		assertEquals("The card filter should use the EMPTYING built in filter if none set.",
				BuiltinFilters.EMPTYING,
				customerDtoAssembler.getCardFilter());
	}

	/**
	 * Test legacy credit cards are stil assembled correctly on a {@link CustomerDTO}.
	 */
	@Test
	public void testCustomerDtoAssemblerAssemblesLegacyCreditCardDtos() {
		shouldFindDefaultCustomerGroupByName();
		shouldFindCustomerGroupsByGuid();

		CustomerCreditCard expectedCreditCard = createCreditCard(CREDIT_CARD_GUID, CREDIT_CARD_TYPE_VISA);
		Customer expectedCustomer = createCustomerWithCreditCards(expectedCreditCard);

		LegacyCreditCardDTO legacyCreditCardDto = new LegacyCreditCardDTOBuilder()
				.withGuid(CREDIT_CARD_GUID)
				.withCardType(CREDIT_CARD_TYPE_VISA)
				.build();

		CustomerDTO customerDto = createTestCustomerDtoBuilder()
				.withCreditCards(legacyCreditCardDto)
				.build();

		shouldTransformCreditCardDtoToDomain(legacyCreditCardDto, expectedCreditCard);

		Customer customer = CustomerBuilder.newCustomer().build();
		customerDtoAssembler.assembleDomain(customerDto, customer);

		assertReflectionEquals(EXPECTED_DOMAIN_OBJECT_SHOULD_EQUAL_ACTUAL, expectedCustomer, customer, ReflectionComparatorMode.LENIENT_DATES);
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

	private void shouldFindCustomerGroupsByGuid() {
		shouldFindDefaultCustomerGroupsByGuid();
		context.checking(new Expectations() {
			{
				allowing(customerGroupService).findByGuid(CUSTOMER_GROUP_GUID);
				will(returnValue(customerGroup));
			}
		});
	}

	private void shouldTransformCreditCardToDto(final CustomerCreditCard creditCard, final CreditCardDTO creditCardDTO) {
		context.checking(new Expectations() {
			{
				allowing(creditCardDTOTransformer).transformToDto(creditCard);
				will(returnValue(creditCardDTO));
			}
		});
	}

	private void shouldTransformCreditCardDtoToDomain(final CreditCardDTO creditCardDTO, final CustomerCreditCard creditCard) {
		context.checking(new Expectations() {
			{
				allowing(creditCardDTOTransformer).transformToDomain(with(creditCardDTO));
				will(returnValue(creditCard));
			}
		});
	}

	private Customer createCustomerWithCreditCards(final CustomerCreditCard... creditCards) {
		return CustomerBuilder.newCustomer()
			.withGuid(CUSTOMER_GUID)
			.withCreationDate(creationDate)
			.withLastEditDate(lastEditDate)
			.withAddedAddress(address)
			.withPreferredBillingAddress(address)
			.withPreferredShippingAddress(address)
			.withCustomerAuthentication(createCustomerAuthentication())
			.withPassword(PASSWORD)
			.withStatus(Customer.STATUS_ACTIVE)
			.withStoreCode(STORE_CODE)
			.withUserId(USER_ID)
			.withAddedCustomerGroup(defaultCustomerGroup)
			.withAddedCustomerGroup(customerGroup)
			.withEmail(EMAIL_ADDRESS)
			.withPreferredLocale(PREFERRED_LOCALE)
			.withPreferredCurrency(PREFERRED_CURRENCY)
			.withFirstName(FIRST_NAME)
			.withLastName(LAST_NAME)
			.withAnonymous(false)
			.withDateOfBirth(DATE_OF_BIRTH)
			.withPhoneNumber(PHONE_NUMBER)
			.withGender(GENDER)
			.withCompany(COMPANY_NAME)
			.withToBeNotified(true)
			.withHtmlEmailPreferred(true)
			.withFaxNumber(FAX_NUMBER)
			.withCreditCards(Arrays.asList(creditCards))
			.withFirstTimeBuyer(false)
			.build();
	}

	private CustomerAuthentication createCustomerAuthentication() {
		CustomerAuthentication customerAuthentication = new CustomerAuthenticationImpl();

		customerAuthentication.setSalt(SALT);

		return customerAuthentication;
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

	private CustomerCreditCard createCreditCard(final String cardGuid, final String cardType) {
		return CustomerCreditCardBuilder.newCustomerCreditCard()
				.withGuid(cardGuid)
				.withCardType(cardType)
				.build();
	}

	private CreditCardDTO createTestCreditCardDtoFromCreditCard(final CustomerCreditCard creditCard) {
		return new CreditCardDTOBuilder()
				.withGuid(creditCard.getGuid())
				.withCardType(creditCard.getCardType())
				.build();
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
		attributeValueDTOs.add(createAttributeValueDto(CustomerImpl.ATT_KEY_CP_ANONYMOUS_CUST, AttributeType.BOOLEAN.toString(), "false"));
		attributeValueDTOs.add(createAttributeValueDto(CustomerImpl.ATT_KEY_CP_PHONE, AttributeType.SHORT_TEXT.toString(), PHONE_NUMBER));
		attributeValueDTOs.add(createAttributeValueDto(CustomerImpl.ATT_KEY_CP_FAX, AttributeType.SHORT_TEXT.toString(), FAX_NUMBER));
		attributeValueDTOs.add(createAttributeValueDto(CustomerImpl.ATT_KEY_CP_GENDER,
				AttributeType.SHORT_TEXT.toString(),
				String.valueOf(GENDER)));
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
				.withUserId(USER_ID)
				.withGroups(defaultCustomerGroup.getGuid(), customerGroup.getGuid());
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
