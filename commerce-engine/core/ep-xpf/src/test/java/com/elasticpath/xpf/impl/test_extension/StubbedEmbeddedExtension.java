/**
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.xpf.impl.test_extension;

import org.pf4j.Extension;
import org.pf4j.ExtensionPoint;

import com.elasticpath.xpf.annotations.XPFEmbedded;
import com.elasticpath.xpf.connectivity.extension.XPFExtensionPointImpl;

/**
 * Test embedded extension.
 */
@Extension
@XPFEmbedded
public class StubbedEmbeddedExtension extends XPFExtensionPointImpl implements ExtensionPoint {

}