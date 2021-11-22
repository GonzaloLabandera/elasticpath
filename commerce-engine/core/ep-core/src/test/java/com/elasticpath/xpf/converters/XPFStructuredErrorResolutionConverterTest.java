/*
 * Copyright © 2021 Elastic Path Software Inc. All rights reserved.
 */

package com.elasticpath.xpf.converters;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.base.common.dto.StructuredErrorResolution;
import com.elasticpath.xpf.connectivity.dto.XPFStructuredErrorResolution;

@RunWith(MockitoJUnitRunner.class)
public class XPFStructuredErrorResolutionConverterTest {

	@InjectMocks
	public XPFStructuredErrorResolutionConverter converter;

	@Test
	public void testConvert() {
		StructuredErrorResolution structuredErrorResolution = new StructuredErrorResolution(Object.class, "GUID");

		XPFStructuredErrorResolution expectedXPFStructuredErrorResolution = new XPFStructuredErrorResolution(Object.class, "GUID");

		assertThat(converter.convert(structuredErrorResolution)).isEqualTo(expectedXPFStructuredErrorResolution);
	}

	@Test
	public void testNullConvert() {
		assertThat(converter.convert(null)).isNull();
	}
}
