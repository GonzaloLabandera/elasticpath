/**
 * Copyright (c) Elastic Path Software Inc., 2015
 */
package com.elasticpath.domain.dataimport.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.commons.constants.GlobalConstants;
import com.elasticpath.commons.exception.EpDateBindException;
import com.elasticpath.commons.exception.EpInvalidValueBindException;
import com.elasticpath.commons.exception.EpNonNullBindException;
import com.elasticpath.commons.exception.EpUnsupportedOperationException;
import com.elasticpath.commons.util.Utility;
import com.elasticpath.commons.util.impl.ConverterUtils;
import com.elasticpath.commons.util.impl.UtilityImpl;
import com.elasticpath.domain.EpDomainException;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.CustomerAuthentication;
import com.elasticpath.domain.customer.impl.CustomerImpl;
import com.elasticpath.domain.dataimport.ImportField;
import com.elasticpath.service.attribute.AttributeService;
import com.elasticpath.service.dataimport.ImportGuidHelper;
import com.elasticpath.test.factory.CustomerBuilder;
import com.elasticpath.test.factory.TestCustomerProfileFactory;
import com.elasticpath.validation.service.ValidatorUtils;

/**
 * Test <code>ImportDataTypeCustomerImpl</code>.
 */
@SuppressWarnings({ "PMD.TooManyMethods" })
@RunWith(MockitoJUnitRunner.class)
public class ImportDataTypeCustomerImplTest {
	private static final String TEST_GUID = "aaa";

	private static final String TEST_SHARED_ID = "bbb";

	private static final String TEST_USERNAME = "ccc";

	private static final String NON_ALPHANUMERIC = "This.Is.A.Test;";

	private static final String DATE_FORMAT = "EEE MMM dd HH:mm:ss z yyyy";

	// Required Import Fields
	private static final String IMPORT_FIELD_GUID = "guid";

	private static final String IMPORT_FIELD_SHARED_ID = "sharedId";

	private static final String IMPORT_CUSTOMER_TYPE = "customerType";

	// Optional Import Fields
	private static final String IMPORT_FIELD_PASSWORD = "password";

	private static final String IMPORT_FIELD_USERNAME = "username";

	private static final String IMPORT_FIELD_STATUS = "status";

	private static final String IMPORT_FIELD_CREATIONDATE = "creationDate";

	private static final String IMPORT_FIELD_PARENT_GUID = "parentGuid";

	private static final String[] REQUIRED_FIELDS = new String[] { IMPORT_FIELD_GUID, IMPORT_FIELD_SHARED_ID, IMPORT_CUSTOMER_TYPE,
			CustomerImpl.ATT_KEY_CP_EMAIL, CustomerImpl.ATT_KEY_CP_FIRST_NAME, CustomerImpl.ATT_KEY_CP_LAST_NAME,
			CustomerImpl.ATT_KEY_CP_HTML_EMAIL, CustomerImpl.ATT_KEY_AP_NAME };

	private static final String[] OPTIONAL_FIELDS = new String[] {
			IMPORT_FIELD_STATUS, IMPORT_FIELD_PASSWORD, IMPORT_FIELD_CREATIONDATE, IMPORT_FIELD_USERNAME, IMPORT_FIELD_PARENT_GUID,
			CustomerImpl.ATT_KEY_CP_COMPANY, CustomerImpl.ATT_KEY_CP_DOB, CustomerImpl.ATT_KEY_CP_FAX, CustomerImpl.ATT_KEY_CP_PHONE,
			CustomerImpl.ATT_KEY_CP_PREF_CURR, CustomerImpl.ATT_KEY_CP_PREF_LOCALE, CustomerImpl.ATT_KEY_CP_BE_NOTIFIED,
			CustomerImpl.ATT_KEY_CP_BUSINESS_NUMBER, CustomerImpl.ATT_KEY_CP_TAX_EXEMPTION_ID, CustomerImpl.ATT_KEY_AP_BUSINESS_NUMBER,
			CustomerImpl.ATT_KEY_AP_PHONE, CustomerImpl.ATT_KEY_AP_FAX, CustomerImpl.ATT_KEY_AP_TAX_EXEMPTION_ID };

	private ImportDataTypeCustomerImpl customerImportType;

	private Customer customer;

	@Mock
	private ImportGuidHelper importGuidHelper;

	private String validDateString;

	private final Utility utility = new UtilityImpl() {
		private static final long serialVersionUID = 1L;

		@Override
		protected String getDefaultDateFormatPattern() {
			return DATE_FORMAT;
		}
	};

	@Mock
	private BeanFactory beanFactory;

	@Mock
	private AttributeService attributeService;

	@Mock
	private ValidatorUtils validatorUtils;

	@Mock
	private CustomerAuthentication customerAuthentication;

	/** Test initialization. */
	@Before
	public void setUp() {

		when(beanFactory.getSingletonBean(ContextIdNames.ATTRIBUTE_SERVICE, AttributeService.class)).thenReturn(attributeService);
		when(beanFactory.getSingletonBean(ContextIdNames.VALIDATOR_UTILS, ValidatorUtils.class)).thenReturn(validatorUtils);
		when(beanFactory.getSingletonBean(ContextIdNames.UTILITY, Utility.class)).thenReturn(utility);
		when(beanFactory.getPrototypeBean(ContextIdNames.CUSTOMER_AUTHENTICATION, CustomerAuthentication.class)).thenReturn(customerAuthentication);

		when(attributeService.getCustomerProfileAttributesMap()).thenReturn(new TestCustomerProfileFactory().getProfile());

		customerImportType = new ImportDataTypeCustomerImpl() {
			private static final long serialVersionUID = 740L;

			@Override
			protected <T> T getSingletonBean(final String beanName, final Class<T> clazz) {
				return beanFactory.getSingletonBean(beanName, clazz);
			}
		};

		customerImportType.init(null);

		customer = createCustomer();

		validDateString = ConverterUtils.date2String(new Date(), DATE_FORMAT, Locale.getDefault());
	}

	/**
	 * Assert that {@link ImportDataTypeCustomerImpl#init(Object)} will be successful if given a null argument.
	 */
	@Test
	public void testSuccessfulInit() {
		customerImportType.init(null);
	}

	/**
	 * Assert that {@link ImportDataTypeCustomerImpl#init(Object) will fail if not given null argument.
	 */
	@Test(expected = EpDomainException.class)
	public void testUnsuccessfulInit() {
		customerImportType.init(new Object());
	}

	/**
	 * Assert the successful path on {@link ImportDataTypeCustomerImpl#getRequiredImportFields()}.
	 */
	@Test
	public void testGetRequiredImportFields() {
		List<String> requiredFields = Arrays.asList(REQUIRED_FIELDS);
		List<ImportField> retrievedRequiredImportFields = customerImportType.getRequiredImportFields();
		assertThat(retrievedRequiredImportFields).hasSize(REQUIRED_FIELDS.length);

		for (ImportField importField : retrievedRequiredImportFields) {
			assertThat(importField.isRequired()).isTrue();
			assertThat(requiredFields).contains(importField.getName());
		}
	}

	/**
	 * Assert the successful path on {@link ImportDataTypeCustomerImpl#getOptionalImportFields()}.
	 */
	@Test
	public void testGetOptionalImportFields() {
		List<String> optionalFields = Arrays.asList(OPTIONAL_FIELDS);
		List<ImportField> retrievedOptionalImportFields = customerImportType.getOptionalImportFields();
		assertThat(retrievedOptionalImportFields).hasSize(OPTIONAL_FIELDS.length);

		for (ImportField importField : retrievedOptionalImportFields) {
			assertThat(importField.isRequired()).isFalse();
			assertThat(optionalFields)
				.as("%s should not be an optional field", importField.getName())
				.contains(importField.getName());
		}
	}

	/**
	 * Assert the successful path on {@link ImportDataTypeCustomerImpl#getImportField}.
	 */
	@Test
	public void testGetImportFieldOfCustomerProfiles() {

		final String firstName = "firstname";
		customer.getCustomerProfile().setStringProfileValue(CustomerImpl.ATT_KEY_CP_FIRST_NAME, firstName);

		ImportField importField = customerImportType.getImportField(ImportDataTypeProductImpl.PREFIX_OF_FIELD_NAME
				+ CustomerImpl.ATT_KEY_CP_FIRST_NAME);
		assertThat(importField.getStringValue(customer)).isEqualTo(firstName);

		final String newFirstName = "firstname2";

		importField.setStringValue(customer, newFirstName, importGuidHelper);
		assertThat(customer.getCustomerProfile().getStringProfileValue(CustomerImpl.ATT_KEY_CP_FIRST_NAME)).isEqualTo(newFirstName);
	}

	/**
	 * Verify get import field of customer GUID.
	 */
	@Test
	public void testGetImportFieldOfCustomerGuid() {
		ImportField importField = customerImportType.getImportField(ImportDataTypeCustomerImpl.PREFIX_OF_FIELD_NAME + IMPORT_FIELD_GUID);
		importField.setStringValue(customer, TEST_GUID, importGuidHelper);
		assertThat(customer.getGuid()).isEqualTo(TEST_GUID);
	}

	/**
	 * Test get import field of customer guid will fail with null.
	 */
	@Test(expected = EpNonNullBindException.class)
	public void testGetImportFieldOfCustomerGuidWillFailWithNull() {
		ImportField importField = customerImportType.getImportField(ImportDataTypeCustomerImpl.PREFIX_OF_FIELD_NAME + IMPORT_FIELD_GUID);
		importField.setStringValue(customer, GlobalConstants.NULL_VALUE, importGuidHelper);
	}

	/**
	 * Test get import field of shared id.
	 */
	@Test
	public void testGetImportFieldOfSharedId() {
		ImportField importField = customerImportType.getImportField(ImportDataTypeCustomerImpl.PREFIX_OF_FIELD_NAME + IMPORT_FIELD_SHARED_ID);
		importField.setStringValue(customer, TEST_SHARED_ID, importGuidHelper);
		assertThat(importField.getStringValue(customer)).isEqualTo(TEST_SHARED_ID);
	}

	/**
	 * Test get import field of user id fail null.
	 */
	@Test
	public void testGetImportFieldOfSharedIdNull() {
		ImportField importField = customerImportType.getImportField(ImportDataTypeCustomerImpl.PREFIX_OF_FIELD_NAME + IMPORT_FIELD_SHARED_ID);
		importField.setStringValue(customer, GlobalConstants.NULL_VALUE, importGuidHelper);
		assertTrue(StringUtils.isNotEmpty(importField.getStringValue(customer)));
	}

	/**
	 * Test get import field of user name.
	 */
	@Test
	public void testGetImportFieldOfUsernameNonNull() {
		ImportField importField = customerImportType.getImportField(ImportDataTypeCustomerImpl.PREFIX_OF_FIELD_NAME + IMPORT_FIELD_USERNAME);
		assertThat(importField.getStringValue(customer)).isEqualTo(GlobalConstants.NULL_VALUE);

		//when(customer.getCustomerAuthentication()).thenReturn(customerAuthentication);
		when(customerAuthentication.getUsername()).thenReturn(TEST_USERNAME);
		importField.setStringValue(customer, TEST_USERNAME, importGuidHelper);
		assertThat(importField.getStringValue(customer)).isEqualTo(TEST_USERNAME);
	}

	/**
	 * Test get import field of user name.
	 */
	@Test
	public void testGetImportFieldOfUsernameNull() {
		ImportField importField = customerImportType.getImportField(ImportDataTypeCustomerImpl.PREFIX_OF_FIELD_NAME + IMPORT_FIELD_USERNAME);
		assertThat(importField.getStringValue(customer)).isEqualTo(GlobalConstants.NULL_VALUE);

		importField.setStringValue(customer, GlobalConstants.NULL_VALUE, importGuidHelper);
		assertThat(importField.getStringValue(customer)).isEqualTo(GlobalConstants.NULL_VALUE);
	}

	/**
	 * Test get import field of password non null.
	 */
	@Test
	public void testGetImportFieldOfPasswordNonNull() {
		ImportField importField = customerImportType.getImportField(ImportDataTypeCustomerImpl.PREFIX_OF_FIELD_NAME + IMPORT_FIELD_PASSWORD);
		assertThat(importField.getStringValue(customer)).isEqualTo(GlobalConstants.NULL_VALUE);

		final String password = "eightcha";
		when(customerAuthentication.getClearTextPassword()).thenReturn(password);
		importField.setStringValue(customer, password, importGuidHelper);
		assertThat(importField.getStringValue(customer)).isEqualTo(password);
	}

	/**
	 * Test get import field of password null.
	 */
	@Test
	public void testGetImportFieldOfPasswordNull() {
		ImportField importField = customerImportType.getImportField(ImportDataTypeCustomerImpl.PREFIX_OF_FIELD_NAME + IMPORT_FIELD_PASSWORD);
		assertThat(importField.getStringValue(customer)).isEqualTo(GlobalConstants.NULL_VALUE);

		importField.setStringValue(customer, GlobalConstants.NULL_VALUE, importGuidHelper);
		assertThat(importField.getStringValue(customer)).isEqualTo(GlobalConstants.NULL_VALUE);
	}

	/**
	 * Test get import field of password too short.
	 */
	@Test(expected = EpInvalidValueBindException.class)
	public void testGetImportFieldOfPasswordTooShort() {
		ImportField importField = customerImportType.getImportField(ImportDataTypeCustomerImpl.PREFIX_OF_FIELD_NAME + IMPORT_FIELD_PASSWORD);
		importField.setStringValue(customer, "sevench", importGuidHelper);
	}

	/**
	 * Test get import field of status enabled.
	 */
	@Test
	public void testGetImportFieldOfStatusEnabled() {
		ImportField importField = customerImportType.getImportField(ImportDataTypeCustomerImpl.PREFIX_OF_FIELD_NAME + IMPORT_FIELD_STATUS);
		assertThat(importField.getStringValue(customer)).isEqualTo(String.valueOf(Customer.STATUS_ACTIVE));
		importField.setStringValue(customer, "1", importGuidHelper);
		assertThat(importField.getStringValue(customer))
			.as(IMPORT_FIELD_STATUS)
			.isEqualTo(String.valueOf(Customer.STATUS_ACTIVE));
	}

	/**
	 * Test get import field of status disabled.
	 */
	@Test
	public void testGetImportFieldOfStatusDisabled() {
		ImportField importField = customerImportType.getImportField(ImportDataTypeCustomerImpl.PREFIX_OF_FIELD_NAME + IMPORT_FIELD_STATUS);
		assertThat(importField.getStringValue(customer)).isEqualTo(String.valueOf(Customer.STATUS_ACTIVE));
		importField.setStringValue(customer, "2", importGuidHelper);
		assertThat(importField.getStringValue(customer))
			.as(IMPORT_FIELD_STATUS)
			.isEqualTo(String.valueOf(Customer.STATUS_DISABLED));
	}

	/**
	 * Test get import field of status pending.
	 */
	@Test
	public void testGetImportFieldOfStatusPending() {
		ImportField importField = customerImportType.getImportField(ImportDataTypeCustomerImpl.PREFIX_OF_FIELD_NAME + IMPORT_FIELD_STATUS);
		assertThat(importField.getStringValue(customer)).isEqualTo(String.valueOf(Customer.STATUS_ACTIVE));
		importField.setStringValue(customer, "3", importGuidHelper);
		assertThat(importField.getStringValue(customer))
			.as(IMPORT_FIELD_STATUS)
			.isEqualTo(String.valueOf(Customer.STATUS_PENDING_APPROVAL));
	}

	/**
	 * Test get import field of status invalid.
	 */
	@Test(expected = EpInvalidValueBindException.class)
	public void testGetImportFieldOfStatusInvalid() {
		ImportField importField = customerImportType.getImportField(ImportDataTypeCustomerImpl.PREFIX_OF_FIELD_NAME + IMPORT_FIELD_STATUS);
		assertThat(importField.getStringValue(customer)).isEqualTo(String.valueOf(Customer.STATUS_ACTIVE));
		importField.setStringValue(customer, "0", importGuidHelper);
	}

	/**
	 * Test get import field of status null.
	 */
	@Test
	public void testGetImportFieldOfStatusNull() {
		ImportField importField = customerImportType.getImportField(ImportDataTypeCustomerImpl.PREFIX_OF_FIELD_NAME + IMPORT_FIELD_STATUS);
		assertThat(importField.getStringValue(customer)).isEqualTo(String.valueOf(Customer.STATUS_ACTIVE));
		assertThat(importField.getStringValue(customer))
			.as(IMPORT_FIELD_STATUS)
			.isEqualTo(String.valueOf(Customer.STATUS_ACTIVE));
	}

	/**
	 * Test get import field of status fail alphanumeric.
	 */
	@Test(expected = EpInvalidValueBindException.class)
	public void testGetImportFieldOfStatusFailAlphanumeric() {
		ImportField importField = customerImportType.getImportField(ImportDataTypeCustomerImpl.PREFIX_OF_FIELD_NAME + IMPORT_FIELD_STATUS);
		importField.setStringValue(customer, NON_ALPHANUMERIC, importGuidHelper);
	}

	/**
	 * Test get import field of creation date non null.
	 */
	@Test
	public void testGetImportFieldOfCreationDateNonNull() {
		ImportField importField = customerImportType.getImportField(ImportDataTypeCustomerImpl.PREFIX_OF_FIELD_NAME + IMPORT_FIELD_CREATIONDATE);

		// Check that the importField initially returns a value of today's date
		final Date creationDate = customer.getCreationDate();
		assertThat(ConverterUtils.string2Date(importField.getStringValue(customer), DATE_FORMAT,
				Locale.getDefault()).toString()).isEqualTo(creationDate.toString());

		importField.setStringValue(customer, validDateString, importGuidHelper);
		assertThat(importField.getStringValue(customer))
			.as(IMPORT_FIELD_CREATIONDATE + " was not set or retrieved properly.")
			.isEqualTo(validDateString);
	}

	/**
	 * Test get import field of creation date null.
	 */
	@Test
	public void testGetImportFieldOfCreationDateNull() {
		ImportField importField = customerImportType.getImportField(ImportDataTypeCustomerImpl.PREFIX_OF_FIELD_NAME + IMPORT_FIELD_CREATIONDATE);
		importField.setStringValue(customer, null, importGuidHelper);

		// Should be automatically assigned to creation date, but we don't have that to the millisecond
		final Date creationDate = customer.getCreationDate();
		assertThat(ConverterUtils.string2Date(importField.getStringValue(customer), DATE_FORMAT, Locale.getDefault()).toString())
			.isEqualTo(creationDate.toString());
	}

	/**
	 * Test get import field of creation date fail invalid date.
	 */
	@Test(expected = EpDateBindException.class)
	public void testGetImportFieldOfCreationDateFailInvalidDate() {
		ImportField importField = customerImportType.getImportField(ImportDataTypeCustomerImpl.PREFIX_OF_FIELD_NAME + IMPORT_FIELD_CREATIONDATE);
		importField.setStringValue(customer, NON_ALPHANUMERIC, importGuidHelper);
	}

	/**
	 * Test is entity import.
	 */
	@Test
	public void testIsEntityImport() {
		assertThat(customerImportType.isEntityImport()).isTrue();
		assertThat(customerImportType.isValueObjectImport()).isFalse();
	}

	/**
	 * Test create value object failure.
	 */
	@Test(expected = EpUnsupportedOperationException.class)
	public void testCreateValueObjectFailure() {
		customerImportType.createValueObject();
	}

	/**
	 * Test get import job runner bean name.
	 */
	@Test
	public void testGetImportJobRunnerBeanName() {
		assertThat(customerImportType.getImportJobRunnerBeanName()).isEqualTo("importJobRunnerCustomer");
	}

	private Customer createCustomer() {
		return CustomerBuilder.newCustomer(beanFactory).build();
	}
}
