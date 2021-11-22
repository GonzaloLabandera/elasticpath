package com.elasticpath.xpf.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.xpf.XPFExtensionSelector;

@RunWith(MockitoJUnitRunner.class)
public class XPFExtensionSelectorByStoreCodeTest {

	@Test
	public void testDifferentStoreCodesNotMatches() {
		XPFExtensionSelector firstSelector = new XPFExtensionSelectorByStoreCode("mobee");
		XPFExtensionSelector secondSelector = new XPFExtensionSelectorByStoreCode("kobee");

		assertFalse(firstSelector.matches(secondSelector));
		assertFalse(secondSelector.matches(firstSelector));
	}

	@Test
	public void testInsensitiveMatchOnStoreCodes() {
		XPFExtensionSelector firstSelector = new XPFExtensionSelectorByStoreCode("MoBeE");
		XPFExtensionSelector secondSelector = new XPFExtensionSelectorByStoreCode("mObEe");

		assertTrue(firstSelector.matches(secondSelector));
		assertTrue(secondSelector.matches(firstSelector));
	}
}
