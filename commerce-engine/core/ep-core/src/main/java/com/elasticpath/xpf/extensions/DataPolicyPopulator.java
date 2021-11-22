/*
 * Copyright © 2021 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.xpf.extensions;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import org.pf4j.Extension;

import com.elasticpath.xpf.XPFExtensionPointEnum;
import com.elasticpath.xpf.annotations.XPFEmbedded;
import com.elasticpath.xpf.connectivity.annontation.XPFAssignment;
import com.elasticpath.xpf.connectivity.context.XPFHttpTagSetContext;
import com.elasticpath.xpf.connectivity.extension.XPFExtensionPointImpl;
import com.elasticpath.xpf.connectivity.extensionpoint.HttpRequestTagSetPopulator;

/**
 * Populator for data policy subject attribute.
 */
@SuppressWarnings("checkstyle:magicnumber")
@Extension
@XPFEmbedded
@XPFAssignment(extensionPoint = XPFExtensionPointEnum.HTTP_TAG_SET_POPULATOR, priority = 1000)
public class DataPolicyPopulator extends XPFExtensionPointImpl implements HttpRequestTagSetPopulator {

	private static final String SUBJECT_ATTRIBUTE_KEY = "DATA_POLICY_SEGMENTS";
	private static final String DATA_POLICY_HEADER = "x-ep-data-policy-segments";

	@Override
	public Map<String, String> collectTagValues(final XPFHttpTagSetContext context) {
		final String dataPolicyHeaderValue = context.getHttpRequest().getHeader(DATA_POLICY_HEADER);

		if (dataPolicyHeaderValue == null) {
			return Collections.emptyMap();
		} else {
			return Collections.singletonMap(SUBJECT_ATTRIBUTE_KEY, Arrays.toString(dataPolicyHeaderValue.split("[\\s,]+")));
		}
	}
}
