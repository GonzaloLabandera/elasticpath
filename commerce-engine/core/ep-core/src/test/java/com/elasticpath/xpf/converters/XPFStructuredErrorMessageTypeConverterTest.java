/*
 * Copyright © 2021 Elastic Path Software Inc. All rights reserved.
 */

package com.elasticpath.xpf.converters;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.base.common.dto.StructuredErrorMessageType;
import com.elasticpath.xpf.connectivity.dto.XPFStructuredErrorMessageType;

@RunWith(MockitoJUnitRunner.class)
public class XPFStructuredErrorMessageTypeConverterTest {

	@InjectMocks
	public XPFStructuredErrorMessageTypeConverter converter;

	@Test
	public void testConvert() {
		assertThat(converter.convert(StructuredErrorMessageType.ERROR)).isEqualTo(XPFStructuredErrorMessageType.ERROR);
		assertThat(converter.convert(StructuredErrorMessageType.INFORMATION)).isEqualTo(XPFStructuredErrorMessageType.INFORMATION);
		assertThat(converter.convert(StructuredErrorMessageType.NEEDINFO)).isEqualTo(XPFStructuredErrorMessageType.NEEDINFO);
		assertThat(converter.convert(StructuredErrorMessageType.WARNING)).isEqualTo(XPFStructuredErrorMessageType.WARNING);
		assertThat(converter.convert(StructuredErrorMessageType.PROMOTION)).isEqualTo(XPFStructuredErrorMessageType.PROMOTION);
	}
}
