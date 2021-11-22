package com.elasticpath.xpf.impl.test_extension;

import org.pf4j.Extension;
import org.pf4j.ExtensionPoint;

import com.elasticpath.xpf.connectivity.extension.XPFExtensionPointImpl;

/**
 * Test extension.
 */
@Extension
public class SomeExtensionClass extends XPFExtensionPointImpl implements SomeExtensionPointInterface {

}

/**
 * Test extension point.
 */
interface SomeExtensionPointInterface extends ExtensionPoint {

}
