/*
 * Copyright © 2021 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.test.integration.extension;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import org.springframework.beans.factory.annotation.Autowired;

import com.elasticpath.test.integration.BasicSpringContextTest;
import com.elasticpath.test.integration.DirtiesDatabase;
import com.elasticpath.xpf.XPFExtensionLookup;
import com.elasticpath.xpf.XPFExtensionPointEnum;
import com.elasticpath.xpf.XPFExtensionResolver;
import com.elasticpath.xpf.connectivity.extensionpoint.SystemInformation;
import com.elasticpath.xpf.impl.XPFExtensionSelectorAny;
import com.elasticpath.xpf.impl.XPFExtensionSelectorByStoreCode;
import com.elasticpath.xpf.impl.XPFPluginManager;
import com.elasticpath.xpf.itests.example.TestExtensionPoint;
import com.elasticpath.xpf.itests.example.TestExtensionPointWithNoExtensions;
import com.elasticpath.xpf.util.XPFUtils;

/**
 * Note that this test will initialize XPF/PF4J and a real Spring Context.
 */
public class XPFITest extends BasicSpringContextTest {

	@Autowired
	private XPFExtensionLookup xpfExtensionLookup;

	@Autowired
	private XPFExtensionResolver xpfInMemoryExtensionPointResolver;

	@Autowired
	private List<TestExtensionPoint> embeddedTestExtensions;

	@Autowired
	private XPFPluginManager pluginManager;

	private static final String EXTENSION_CLASS_NAME_1 = "com.elasticpath.xpf.itests.example.impl.TestSystemInformation1Impl";
	private static final String EXTENSION_CLASS_NAME_2 = "com.elasticpath.xpf.itests.example.impl.TestSystemInformation2Impl";
	private static final String EXTENSION_CLASS_NAME_3 = "com.elasticpath.xpf.itests.example.impl.TestSystemInformation3Impl";

	private static final String STORE_1 = "mobee";
	private static final String STORE_2 = "kobee";

	private static final int PRIORITY_TOP = 0;
	private static final int PRIORITY_LOWER = 50;

	private static final XPFExtensionPointEnum EXTENSION_POINT_ENUM = XPFExtensionPointEnum.SYSTEM_INFORMATION;

	@Before
	public void setUp(){
		pluginManager.stopPlugins();
	}

	/**
	 * This test verifies that the embedded plugins were registered in the EP spring context.
	 * 
	 * Embedded extensions are added as beans to the EP Spring Context, so autowiring a list of
	 * TestExtensionPoints should give us a list of all Embedded implementations, but exclude
	 * TestExtension, which is an Extension but not an Embedded Extension.
	 */
	@Test
	@DirtiesDatabase
	public void verifyExtensionsRegisteredInSpringContext() {
		assertEquals("Only @XPFEmbedded extensions should be found in the spring context", 2, embeddedTestExtensions.size());
	}

	@Test
	@DirtiesDatabase
	public void testOnlyMatchingExtensionsAreFound() {
		List<TestExtensionPointWithNoExtensions> extensions =
				xpfExtensionLookup.getMultipleExtensions(TestExtensionPointWithNoExtensions.class, XPFExtensionPointEnum.SYSTEM_INFORMATION,
						new XPFExtensionSelectorAny());
		assertEquals(0, extensions.size());
	}

	/**
	 * Test that all embedded extensions are returned when the “any” selector is used.
	 */
	@Test
	@DirtiesDatabase
	@SuppressWarnings("checkstyle:magicnumber")
	public void testAllExtensionsAreReturnedWithAnySelector() {
		List<SystemInformation> systemMetrics = xpfExtensionLookup.getMultipleExtensions(SystemInformation.class,
				XPFExtensionPointEnum.SYSTEM_INFORMATION, new XPFExtensionSelectorAny());
		assertEquals(6, systemMetrics.size());
	}

	/**
	 * Test that all embedded extensions are returned when the “store” selector is used.
	 */
	@Test
	@DirtiesDatabase
	@SuppressWarnings("checkstyle:magicnumber")
	public void testAllExtensionsAreReturnedWithStoreSelector() {
		List<SystemInformation> systemMetrics = xpfExtensionLookup.getMultipleExtensions(SystemInformation.class,
				XPFExtensionPointEnum.SYSTEM_INFORMATION, new XPFExtensionSelectorByStoreCode(STORE_1));
		assertEquals(6, systemMetrics.size());
	}

	/**
	 * Test global extension exclusions.
	 */
	@Test
	@DirtiesDatabase
	public void testGlobalExtensionExclusions() {
		xpfInMemoryExtensionPointResolver.removeExtensionFromSelector(EXTENSION_CLASS_NAME_1, null, EXTENSION_POINT_ENUM,
				new XPFExtensionSelectorAny());

		List<SystemInformation> systemMetrics = xpfExtensionLookup.getMultipleExtensions(SystemInformation.class,
				XPFExtensionPointEnum.SYSTEM_INFORMATION, new XPFExtensionSelectorByStoreCode(STORE_1));

		assertFalse(systemMetrics.stream().anyMatch(extPoint -> extPoint.getClass().getName().equals(EXTENSION_CLASS_NAME_1)));

		systemMetrics = xpfExtensionLookup.getMultipleExtensions(SystemInformation.class,
				XPFExtensionPointEnum.SYSTEM_INFORMATION, new XPFExtensionSelectorByStoreCode(STORE_2));

		assertFalse(systemMetrics.stream().anyMatch(extPoint -> extPoint.getClass().getName().equals(EXTENSION_CLASS_NAME_1)));
	}

	/**
	 * Test store-specific extension exclusions.
	 */
	@Ignore("specific selector removes XPFExtensionSelectorAny selector for current logic")
	@Test
	@DirtiesDatabase
	public void testStoreSpecificExtensionExclusions() {
		xpfInMemoryExtensionPointResolver.assignExtensionToSelector(EXTENSION_CLASS_NAME_1, null, EXTENSION_POINT_ENUM,
				new XPFExtensionSelectorAny(), PRIORITY_TOP);

		xpfInMemoryExtensionPointResolver.removeExtensionFromSelector(EXTENSION_CLASS_NAME_1, null, EXTENSION_POINT_ENUM,
				new XPFExtensionSelectorByStoreCode(STORE_1));

		List<SystemInformation> systemMetrics = xpfExtensionLookup.getMultipleExtensions(SystemInformation.class,
				XPFExtensionPointEnum.SYSTEM_INFORMATION, new XPFExtensionSelectorByStoreCode(STORE_1));

		assertFalse(systemMetrics.stream().anyMatch(extPoint -> extPoint.getClass().getName().equals(EXTENSION_CLASS_NAME_1)));

		systemMetrics = xpfExtensionLookup.getMultipleExtensions(SystemInformation.class,
				XPFExtensionPointEnum.SYSTEM_INFORMATION, new XPFExtensionSelectorByStoreCode(STORE_2));

		assertTrue(systemMetrics.stream().anyMatch(extPoint -> extPoint.getClass().getName().equals(EXTENSION_CLASS_NAME_1)));
	}

	/**
	 * Test global priority updates.
	 */
	@Test
	@DirtiesDatabase
	public void testGlobalPriorityUpdates() {
		xpfInMemoryExtensionPointResolver.assignExtensionToSelector(EXTENSION_CLASS_NAME_1, null, EXTENSION_POINT_ENUM,
				new XPFExtensionSelectorAny(), PRIORITY_TOP);

		SystemInformation systemMetric = xpfExtensionLookup.getSingleExtension(SystemInformation.class,
				XPFExtensionPointEnum.SYSTEM_INFORMATION, new XPFExtensionSelectorByStoreCode(STORE_1));

		assertEquals(EXTENSION_CLASS_NAME_1, XPFUtils.getProxiedExtensionClass(systemMetric).getName());
	}

	/**
	 * Test store-specific priority updates.
	 */
	@Test
	@DirtiesDatabase
	public void testStoreSpecificPriorityUpdates() {
		xpfInMemoryExtensionPointResolver.assignExtensionToSelector(EXTENSION_CLASS_NAME_1, null, EXTENSION_POINT_ENUM,
				new XPFExtensionSelectorByStoreCode(STORE_1), PRIORITY_LOWER);
		xpfInMemoryExtensionPointResolver.assignExtensionToSelector(EXTENSION_CLASS_NAME_2, null, EXTENSION_POINT_ENUM,
				new XPFExtensionSelectorByStoreCode(STORE_2), PRIORITY_TOP);

		SystemInformation systemMetric = xpfExtensionLookup.getSingleExtension(SystemInformation.class,
				XPFExtensionPointEnum.SYSTEM_INFORMATION, new XPFExtensionSelectorByStoreCode(STORE_1));

		assertEquals(EXTENSION_CLASS_NAME_1, XPFUtils.getProxiedExtensionClass(systemMetric).getName());
	}

	@Test
	@DirtiesDatabase
	public void testGlobalRemovalAndStoreSpecificAddition() {
		xpfInMemoryExtensionPointResolver.removeExtensionFromSelector(EXTENSION_CLASS_NAME_1, null, EXTENSION_POINT_ENUM,
				new XPFExtensionSelectorAny());
		xpfInMemoryExtensionPointResolver.assignExtensionToSelector(EXTENSION_CLASS_NAME_1, null, EXTENSION_POINT_ENUM,
				new XPFExtensionSelectorByStoreCode(STORE_1), PRIORITY_TOP);

		List<SystemInformation> systemMetrics = xpfExtensionLookup.getMultipleExtensions(SystemInformation.class,
				XPFExtensionPointEnum.SYSTEM_INFORMATION, new XPFExtensionSelectorByStoreCode(STORE_1));

		assertTrue(systemMetrics.stream().anyMatch(extPoint -> XPFUtils.getProxiedExtensionClass(extPoint).getName().equals(EXTENSION_CLASS_NAME_1)));

		systemMetrics = xpfExtensionLookup.getMultipleExtensions(SystemInformation.class, XPFExtensionPointEnum.SYSTEM_INFORMATION,
				new XPFExtensionSelectorByStoreCode(STORE_2));

		assertFalse(systemMetrics.stream().anyMatch(extPoint -> extPoint.getClass().getName().equals(EXTENSION_CLASS_NAME_1)));
	}
}
