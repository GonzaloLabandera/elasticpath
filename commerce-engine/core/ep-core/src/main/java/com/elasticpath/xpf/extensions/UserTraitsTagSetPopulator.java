/**
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.xpf.extensions;

import java.util.Map;

import org.pf4j.Extension;

import com.elasticpath.xpf.XPFExtensionPointEnum;
import com.elasticpath.xpf.annotations.XPFEmbedded;
import com.elasticpath.xpf.connectivity.annontation.XPFAssignment;
import com.elasticpath.xpf.connectivity.context.XPFHttpTagSetContext;
import com.elasticpath.xpf.connectivity.extension.XPFExtensionPointImpl;
import com.elasticpath.xpf.connectivity.extensionpoint.HttpRequestTagSetPopulator;

/**
 * Populator for user trait subject attributes.
 */
@SuppressWarnings("checkstyle:magicnumber")
@Extension
@XPFEmbedded
@XPFAssignment(extensionPoint = XPFExtensionPointEnum.HTTP_TAG_SET_POPULATOR, priority = 1900)
public class UserTraitsTagSetPopulator extends XPFExtensionPointImpl implements HttpRequestTagSetPopulator {

	@Override
	public Map<String, String> collectTagValues(final XPFHttpTagSetContext context) {
		return context.getUserTraitValues();
	}

}
