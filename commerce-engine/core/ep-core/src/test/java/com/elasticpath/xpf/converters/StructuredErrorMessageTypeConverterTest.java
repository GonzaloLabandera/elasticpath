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
public class StructuredErrorMessageTypeConverterTest {

	@InjectMocks
	public StructuredErrorMessageTypeConverter converter;

	@Test
	public void testConvert() {
		assertThat(converter.convert(XPFStructuredErrorMessageType.ERROR)).isEqualTo(StructuredErrorMessageType.ERROR);
		assertThat(converter.convert(XPFStructuredErrorMessageType.INFORMATION)).isEqualTo(StructuredErrorMessageType.INFORMATION);
		assertThat(converter.convert(XPFStructuredErrorMessageType.NEEDINFO)).isEqualTo(StructuredErrorMessageType.NEEDINFO);
		assertThat(converter.convert(XPFStructuredErrorMessageType.WARNING)).isEqualTo(StructuredErrorMessageType.WARNING);
		assertThat(converter.convert(XPFStructuredErrorMessageType.PROMOTION)).isEqualTo(StructuredErrorMessageType.PROMOTION);
	}
}
