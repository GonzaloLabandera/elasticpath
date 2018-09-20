/**
 * Copyright (c) Elastic Path Software Inc., 2013
 */
package com.elasticpath.common.dto.customer.transformer;

import org.apache.commons.lang.StringUtils;

import com.elasticpath.common.dto.customer.CreditCardDTO;
import com.elasticpath.common.dto.customer.builder.CreditCardDTOBuilder;
import com.elasticpath.commons.exception.EpInvalidValueBindException;
import com.elasticpath.commons.util.security.CreditCardEncrypter;
import com.elasticpath.domain.customer.CustomerCreditCard;
import com.elasticpath.domain.customer.impl.AbstractCustomerCreditCardBuilder;

/**
 * Transforms {@link CustomerCreditCard}s to {@link CreditCardDTO}s.
 */
public class CreditCardDTOTransformer {
	private AbstractCustomerCreditCardBuilder customerCreditCardBuilder;
	private CreditCardDTOBuilder creditCardDTOBuilder;
	private CreditCardEncrypter creditCardEncrypter;

	/**
	 * Transforms {@link CreditCardDTO} to {@link CustomerCreditCard}.
	 *
	 * @param dto the {@link CreditCardDTO} to transform
	 * @return transformed {@link CreditCardDTO}
	 */
	public CustomerCreditCard transformToDomain(final CreditCardDTO dto) {
		if (StringUtils.isEmpty(dto.getCardNumber())) {
			throw new EpInvalidValueBindException("Credit card number field is empty.");
		}

		String encryptedCardNumber = getCreditCardEncrypter().encrypt(dto.getCardNumber());
		return getCustomerCreditCardBuilder()
				.withCardType(dto.getCardType())
				.withExpiryYear(dto.getExpiryYear())
				.withExpiryMonth(dto.getExpiryMonth())
				.withGuid(dto.getGuid())
				.withCardHolderName(dto.getCardHolderName())
				.withStartYear(dto.getStartYear())
				.withStartMonth(dto.getStartMonth())
				.withIssueNumber(dto.getIssueNumber())
				.withCardNumber(encryptedCardNumber)
				.build();
		}
		
	/**
	 * Transforms {@link CustomerCreditCard} to {@link CreditCardDTO}.
	 *
	 * @param domainEntity the {@link CustomerCreditCard} to transform
	 * @return transformed {@link CustomerCreditCard}
	 */
	public CreditCardDTO transformToDto(final CustomerCreditCard domainEntity) {
		return getCreditCardDTOBuilder() 
				.withCardType(domainEntity.getCardType())
				.withExpiryYear(domainEntity.getExpiryYear())
				.withExpiryMonth(domainEntity.getExpiryMonth())
				.withGuid(domainEntity.getGuid())
				.withCardHolderName(domainEntity.getCardHolderName())
				.withCardNumber(domainEntity.getCardNumber())
				.withStartYear(domainEntity.getStartYear())
				.withStartMonth(domainEntity.getStartMonth())
				.withIssueNumber(domainEntity.getIssueNumber())
				.build();
	}

	public void setCustomerCreditCardBuilder(final AbstractCustomerCreditCardBuilder customerCreditCardBuilder) {
		this.customerCreditCardBuilder = customerCreditCardBuilder;
	}

	protected AbstractCustomerCreditCardBuilder getCustomerCreditCardBuilder() {
		return this.customerCreditCardBuilder;
	}

	public void setCreditCardDTOBuilder(final CreditCardDTOBuilder creditCardDTOBuilder) {
		this.creditCardDTOBuilder = creditCardDTOBuilder;
	}

	protected CreditCardDTOBuilder getCreditCardDTOBuilder() {
		return this.creditCardDTOBuilder;
	}

	public void setCreditCardEncrypter(final CreditCardEncrypter creditCardEncrypter) {
		this.creditCardEncrypter = creditCardEncrypter;
	}

	protected CreditCardEncrypter getCreditCardEncrypter() {
		return this.creditCardEncrypter;
	}
}
