/**
 * Copyright (c) Elastic Path Software Inc., 2014
 */
package com.elasticpath.importexport.common.adapters.settings;

import org.apache.commons.lang3.StringEscapeUtils;

import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.importexport.common.adapters.AbstractDomainAdapterImpl;
import com.elasticpath.importexport.common.dto.settings.DefinedValueDTO;
import com.elasticpath.settings.domain.SettingValue;

/**
 * The implementation of <code>DomainAdapter</code> interface. It is responsible for data transformation between 
 * <code>SettingValue</code> and <code>DefinedValueDTO</code> objects.
 */
public class SettingValueAdapter extends AbstractDomainAdapterImpl<SettingValue, DefinedValueDTO> {

	@Override
	public void populateDTO(final SettingValue source, final DefinedValueDTO target) {
		target.setContext(source.getContext());
		target.setValue(StringEscapeUtils.escapeXml(source.getValue()));
	}

	@Override
	public void populateDomain(final DefinedValueDTO source, final SettingValue target) {
		target.setContext(source.getContext());
		target.setValue(StringEscapeUtils.unescapeXml(source.getValue()));
	}

	@Override
	public DefinedValueDTO createDtoObject() {
		return new DefinedValueDTO();
	}
	
	@Override
	public SettingValue createDomainObject() {
		return getBeanFactory().getPrototypeBean(ContextIdNames.SETTING_VALUE, SettingValue.class);
	}
}
