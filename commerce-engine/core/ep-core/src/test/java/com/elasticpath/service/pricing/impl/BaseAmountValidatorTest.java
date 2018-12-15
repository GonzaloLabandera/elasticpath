/*
 * Copyright (c) Elastic Path Software Inc., 2013
 */
package com.elasticpath.service.pricing.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.Before;
import org.junit.Test;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

import com.elasticpath.common.dto.pricing.BaseAmountDTO;
import com.elasticpath.domain.pricing.BaseAmount;
import com.elasticpath.domain.pricing.impl.BaseAmountImpl;


/** */
public class BaseAmountValidatorTest {
	
	private static final int DIGIT_6 = 6;

	private static final int DIGIT_5 = 5;

	private static final String QUANTITY = "quantity";

	private static final String LIST_VALUE = "listValue";

	private static final String OBJECT_TYPE = "objectType";

	private static final String SALE_VALUE = "saleValue";

	private static final String PRODUCT = "PRODUCT";

	private static final String OBJ_GUID = "objGuid";

	private static final String GUID = "guid";

	private static final String DESC_GUID = "descGuid";

	private static final String BASE_AMOUNT = "BaseAmount";
	
	private BaseAmountValidatorImpl validator = new BaseAmountValidatorImpl();

	/** */
	@Before 
	public void setUp() {
		validator = new BaseAmountValidatorImpl();
		validator.setErrorInvalidGuid("validator.baseAmount.invalidGuid");
		validator.setErrorInvalidObjectGuid("validator.baseAmount.invalidObjectGuid");
		validator.setErrorInvalidObjectType("validator.baseAmount.invalidObjectType");
		validator.setErrorInvalidPriceListDescriptor("validator.baseAmount.invalidPriceListDescriptorGuid");
		validator.setErrorInvalidQuantity("validator.baseAmount.invalidQuantity");
		validator.setErrorInvalidListPrice("validator.baseAmount.invalidListPrice");
		validator.setErrorInvalidSalePrice("validator.baseAmount.invalidSalePrice");
		validator.setErrorSalePriceIsMoreThenListPrice("validator.baseAmount.salePriceIsMoreThenListPrice");
	}
	/** */
	@Test
	public void testSupports() {
		assertThat(validator.supports(BaseAmount.class)).isTrue();
	}
	/** */
	@Test
	public void testDoesNotSupport() {
		assertThat(validator.supports(BaseAmountDTO.class)).isFalse();
	}
	/** */
	@Test
	public void testValidObject() {
		BaseAmount amount = createBaseAmount();
		Errors errors = new BeanPropertyBindingResult(amount, BASE_AMOUNT); 
		
		validator.validate(amount, errors);
		assertThat(errors.hasErrors()).isFalse();
	}
	/** */
	@Test
	public void testZeroQuantity() {
		BaseAmount amount = createBaseAmountWithQuantity(BigDecimal.ZERO);
		Errors errors = new BeanPropertyBindingResult(amount, BASE_AMOUNT); 
		
		validator.validate(amount, errors);
		assertThat(errors.hasErrors()).isTrue();
		assertThat(errors.getErrorCount()).isEqualTo(1);
		assertThat(errors.getFieldError(QUANTITY).getCode()).isEqualTo("validator.baseAmount.invalidQuantity");
		assertThat(errors.getFieldError(QUANTITY).getRejectedValue()).isEqualTo(BigDecimal.ZERO);
	}
	
	/** */
	@Test
	public void testNullQuantity() {
		BaseAmount amount = createBaseAmountWithQuantity(null);
		Errors errors = new BeanPropertyBindingResult(amount, BASE_AMOUNT); 
		
		validator.validate(amount, errors);
		assertThat(errors.getErrorCount()).isEqualTo(1);
		assertThat(errors.getFieldError(QUANTITY).getCode()).isEqualTo("validator.baseAmount.invalidQuantity");
		assertThat(errors.getFieldError(QUANTITY).getRejectedValue()).isNull();
	}

	/** */
	@Test
	public void testZeroListPrice() {
		BaseAmount amount = createBaseAmountWithListPrice(new BigDecimal(-1));
		Errors errors = new BeanPropertyBindingResult(amount, BASE_AMOUNT); 
		
		validator.validate(amount, errors);
		assertThat(errors.hasErrors()).isTrue();
		assertThat(errors.getErrorCount()).isEqualTo(1);
		assertThat(errors.getFieldError(LIST_VALUE).getCode()).isEqualTo("validator.baseAmount.invalidListPrice");
		assertThat(errors.getFieldError(LIST_VALUE).getRejectedValue()).isEqualTo(new BigDecimal(-1));
	}

	/** */
	@Test
	public void testInvalidObjectGuid() {
		BaseAmount amount = createBaseAmountWithObjectGuid(null);
		Errors errors = new BeanPropertyBindingResult(amount, BASE_AMOUNT); 
		
		validator.validate(amount, errors);
		assertThat(errors.hasErrors()).isTrue();
		assertThat(errors.getErrorCount()).isEqualTo(1);
		assertThat(errors.getFieldError("objectGuid").getCode()).isEqualTo("validator.baseAmount.invalidObjectGuid");
		assertThat(errors.getFieldError("objectGuid").getRejectedValue()).isNull();
	}
	
	/** */
	@Test
	public void testNullObjectType() {
		BaseAmount amount = createBaseAmountWithObjectType(null);
		Errors errors = new BeanPropertyBindingResult(amount, BASE_AMOUNT); 
		
		validator.validate(amount, errors);
		assertThat(errors.hasErrors()).isTrue();
		assertThat(errors.getErrorCount()).isEqualTo(1);
		assertThat(errors.getFieldError(OBJECT_TYPE).getCode()).isEqualTo("validator.baseAmount.invalidObjectType");
		assertThat(errors.getFieldError(OBJECT_TYPE).getRejectedValue()).isNull();
	}
	
	/** */
	@Test
	public void testInvalidObjectType() {
		BaseAmount amount = createBaseAmountWithObjectType("NOT_PRODUCT_OR_SKU");
		Errors errors = new BeanPropertyBindingResult(amount, BASE_AMOUNT); 
		
		validator.validate(amount, errors);
		assertThat(errors.hasErrors()).isTrue();
		assertThat(errors.getErrorCount()).isEqualTo(1);
		assertThat(errors.getFieldError(OBJECT_TYPE).getCode()).isEqualTo("validator.baseAmount.invalidObjectType");
		assertThat(errors.getFieldError(OBJECT_TYPE).getRejectedValue()).isEqualTo("NOT_PRODUCT_OR_SKU");
	}

	/** */
	@Test
	public void testNullListPrice() {
		BaseAmount amount = createBaseAmountWithListPrice(null);
		Errors errors = new BeanPropertyBindingResult(amount, BASE_AMOUNT); 
		
		validator.validate(amount, errors);
		assertThat(errors.hasErrors()).isTrue();
		assertThat(errors.getErrorCount()).isEqualTo(1);
		assertThat(errors.getFieldError(LIST_VALUE).getCode()).isEqualTo("validator.baseAmount.invalidListPrice");
		assertThat(errors.getFieldError(LIST_VALUE).getRejectedValue()).isNull();
	}
	/** */
	@Test
	public void testNegativeSalePrice() {
		BaseAmount amount = createBaseAmountWithSalePrice(new BigDecimal(-1));
		Errors errors = new BeanPropertyBindingResult(amount, BASE_AMOUNT); 
		
		validator.validate(amount, errors);
		assertThat(errors.hasErrors()).isTrue();
		assertThat(errors.getErrorCount()).isEqualTo(1);
		assertThat(errors.getFieldError(SALE_VALUE).getCode()).isEqualTo("validator.baseAmount.invalidSalePrice");
		assertThat(errors.getFieldError(SALE_VALUE).getRejectedValue()).isEqualTo(new BigDecimal(-1));
	}
	/** */
	@Test
	public void testNullSalePrice() {
		BaseAmount amount = createBaseAmountWithSalePrice(null);
		Errors errors = new BeanPropertyBindingResult(amount, BASE_AMOUNT); 
		
		validator.validate(amount, errors);
		assertThat(errors.hasErrors()).isFalse();
	}
	/** */
	@Test
	public void testListPriceLowerThanSalePrice() {
		BaseAmount amount = createBaseAmountWithListPriceAndSalePrice(new BigDecimal(DIGIT_5), new BigDecimal(DIGIT_6));
		Errors errors = new BeanPropertyBindingResult(amount, BASE_AMOUNT); 
		
		validator.validate(amount, errors);
		assertThat(errors.hasErrors()).isTrue();
		assertThat(errors.getErrorCount()).isEqualTo(1);
		assertThat(errors.getFieldError(SALE_VALUE).getCode()).isEqualTo("validator.baseAmount.salePriceIsMoreThenListPrice");
		assertThat(errors.getFieldError(SALE_VALUE).getRejectedValue()).isEqualTo(new BigDecimal(DIGIT_6));
	}

	private BaseAmount createBaseAmountWithObjectType(final String objectType) {
		return new BaseAmountImpl(GUID, OBJ_GUID, objectType, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, DESC_GUID);
	}
	private BaseAmount createBaseAmountWithObjectGuid(final String objectGuid) {
		return new BaseAmountImpl(GUID, objectGuid, PRODUCT, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, DESC_GUID);
	}
	private BaseAmount createBaseAmountWithQuantity(final BigDecimal quantity) {
		return new BaseAmountImpl(GUID, OBJ_GUID, PRODUCT, quantity, BigDecimal.ONE, BigDecimal.ONE, DESC_GUID);
	}
	private BaseAmount createBaseAmountWithListPrice(final BigDecimal listPrice) {
		return new BaseAmountImpl(GUID, OBJ_GUID, PRODUCT, BigDecimal.ONE, listPrice, BigDecimal.ONE, DESC_GUID);
	}
	private BaseAmount createBaseAmountWithSalePrice(final BigDecimal salePrice) {
		return new BaseAmountImpl(GUID, OBJ_GUID, PRODUCT, BigDecimal.ONE, BigDecimal.ONE, salePrice, DESC_GUID);
	}
	private BaseAmount createBaseAmountWithListPriceAndSalePrice(final BigDecimal listPrice, final BigDecimal salePrice) {
		return new BaseAmountImpl(GUID, OBJ_GUID, PRODUCT, BigDecimal.ONE, listPrice, salePrice, DESC_GUID);
	}
	
	private BaseAmountImpl createBaseAmount() {
		return new BaseAmountImpl(GUID, OBJ_GUID, PRODUCT, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, DESC_GUID);
	}
}
