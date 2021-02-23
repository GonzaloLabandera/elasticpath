package com.elasticpath.rest.resource.integration.epcommerce.repository.addresses.validator.impl;

import org.junit.Test;

import com.elasticpath.rest.ResourceOperationFailure;

import com.elasticpath.rest.definition.base.AddressEntity;
import com.elasticpath.rest.definitions.validator.constants.ValidationMessages;

/**
 * Contains tests for {@link BaseAddressValidatorImpl}.
 */
public class BaseAddressValidatorTest {
	private static final String NO_VALID_ADDRESS_FIELDS = "No valid address fields specified.";

	private static final String STREET_ADDRESS = "STREET_ADDRESS";
	private static final String EXTENDED_ADDRESS = "EXTENDED_ADDRESS";
	private static final String LOCALITY = "LOCALITY";
	private static final String REGION = "REGION";
	private static final String COUNTRY_NAME = "COUNTRY_NAME";
	private static final String POSTAL_CODE = "POSTAL_CODE";

	private final BaseAddressValidatorImpl validator = new BaseAddressValidatorImpl();


	/**
	 * Test validate given null for the representation argument.
	 */
	@Test
	public void testValidateGivenNull() {
		validator.validate(null)
				.test()
				.assertError(ResourceOperationFailure.badRequestBody(ValidationMessages.MISSING_REQUIRED_REQUEST_BODY));
	}

	/**
	 * Test validation on address entity with no fields populated within the address representation.
	 */
	@Test
	public void testValidationOnAddressEntityWithNoFieldsPopulatedWithinAddressRepresentation() {
		AddressEntity addressDetailsEntity =
				createAddressDetailsEntity(null, null, null, null, null, null);
		testEntityFieldPopulationWithFailure(addressDetailsEntity, NO_VALID_ADDRESS_FIELDS);
	}

	/**
	 * Test validation of street address on address entity within the address representation.
	 */
	@Test
	public void testValidationOfStreetAddressOnAddressEntityWithinAddressRepresentation() {
		AddressEntity addressDetailsEntity = createAddressDetailsEntity(STREET_ADDRESS, null, null, null, null,
				null);
		testEntityFieldPopulationWithSuccess(addressDetailsEntity);
	}

	/**
	 * Test validation of extended address on address entity within the address representation.
	 */
	@Test
	public void testValidationOfExtendedAddressOnAddressEntityWithinAddressRepresentation() {
		AddressEntity addressDetailsEntity = createAddressDetailsEntity(null, EXTENDED_ADDRESS, null, null,
				null, null);
		testEntityFieldPopulationWithSuccess(addressDetailsEntity);
	}

	/**
	 * Test validation of locality on address entity within the address representation.
	 */
	@Test
	public void testValidationOfLocalityOnAddressEntityWithinAddressRepresentation() {
		AddressEntity addressDetailsEntity = createAddressDetailsEntity(null, null, LOCALITY, null, null, null);
		testEntityFieldPopulationWithSuccess(addressDetailsEntity);
	}

	/**
	 * Test validation of region on address entity within the address representation.
	 */
	@Test
	public void testValidationOfRegionOnAddressEntityWithinAddressRepresentation() {
		AddressEntity addressDetailsEntity = createAddressDetailsEntity(null, null, null, REGION, null, null);
		testEntityFieldPopulationWithSuccess(addressDetailsEntity);
	}

	/**
	 * Test validation of country name on address entity within the address representation.
	 */
	@Test
	public void testValidationOfCountryNameOnAddressEntityWithinAddressRepresentation() {
		AddressEntity addressDetailsEntity = createAddressDetailsEntity(null, null, null, null, COUNTRY_NAME,
				null);
		testEntityFieldPopulationWithSuccess(addressDetailsEntity);
	}

	/**
	 * Test validation of postal code on address entity within the address representation.
	 */
	@Test
	public void testValidationOfPostalCodeOnAddressEntityWithinAddressRepresentation() {
		AddressEntity addressDetailsEntity = createAddressDetailsEntity(null, null, null, null, null,
				POSTAL_CODE);
		testEntityFieldPopulationWithSuccess(addressDetailsEntity);
	}

	private void testEntityFieldPopulationWithSuccess(final AddressEntity addressEntity) {
		validator.validate(addressEntity)
				.test()
				.assertNoErrors();
	}

	private void testEntityFieldPopulationWithFailure(final AddressEntity addressEntity,
													  final String failureMessage) {
		validator.validate(addressEntity)
				.test()
				.assertError(ResourceOperationFailure.badRequestBody(failureMessage));
	}

	private AddressEntity createAddressDetailsEntity(final String streetAddress,
													 final String extendedAddress,
													 final String locality,
													 final String region,
													 final String countryName,
													 final String postalCode) {

		return AddressEntity.builder()
				.withStreetAddress(streetAddress)
				.withExtendedAddress(extendedAddress)
				.withLocality(locality)
				.withRegion(region)
				.withCountryName(countryName)
				.withPostalCode(postalCode)
				.build();
	}
}
