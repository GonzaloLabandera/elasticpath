/**
 * Copyright (c) Elastic Path Software Inc., 2013
 */
package com.elasticpath.importexport.common.adapters.paymentprovider;

import com.elasticpath.common.dto.paymentprovider.PaymentProviderDTO;
import com.elasticpath.importexport.common.adapters.AbstractDomainAdapterImpl;
import com.elasticpath.importexport.common.assembler.paymentprovider.PaymentProviderDTOAssembler;
import com.elasticpath.importexport.common.dto.paymentprovider.PaymentProviderConfigDomainProxy;

/**
 * Domain adapter for PaymentProviders. Similar to a DtoAssemblerDelegating one, however has some special handling as described in
 * {@link PaymentProviderDTOAssembler}.
 */
public class PaymentProviderAdapter extends AbstractDomainAdapterImpl<PaymentProviderConfigDomainProxy, PaymentProviderDTO> {

	private PaymentProviderDTOAssembler assembler;

	@Override
	public void populateDTO(final PaymentProviderConfigDomainProxy source, final PaymentProviderDTO target) {
		assembler.assembleDto(source, target);
	}

	@Override
	public PaymentProviderDTO createDtoObject() {
		return assembler.getDtoInstance();
	}

	@Override
	public PaymentProviderConfigDomainProxy createDomainObject() {
		return assembler.getDomainInstance();
	}

	public void setAssembler(final PaymentProviderDTOAssembler assembler) {
		this.assembler = assembler;
	}

	@Override
	public PaymentProviderConfigDomainProxy buildDomain(final PaymentProviderDTO source, final PaymentProviderConfigDomainProxy target) {
		return assembler.assembleDomain(source);
	}

	/**
	 * This method is not supported for payment gateways. Use buildDomain above.
	 * 
	 * @param source ignored: this method throws {@code UnsupportedOperationException}
	 * @param target ignored: this method throws {@code UnsupportedOperationException}
	 */
	@Override
	public void populateDomain(final PaymentProviderDTO source, final PaymentProviderConfigDomainProxy target) {
		/* This method is pass by reference, so we can't change the reference to 'target' without returning it */
		throw new UnsupportedOperationException("If we are populating a non persisted payment provider, we must create one through a factory.");
	}

}
