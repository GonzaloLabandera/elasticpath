/*
 * Copyright (c) Elastic Path Software Inc., 2016
 */

package com.elasticpath.domain.modifier.impl;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Locale;

import org.junit.Test;

import com.elasticpath.domain.modifier.ModifierField;
import com.elasticpath.domain.modifier.ModifierGroup;
import com.elasticpath.domain.modifier.ModifierGroupLdf;
import com.elasticpath.domain.modifier.ModifierType;

/**
 * * Unit tests for {@link ModifierGroupImpl}.
 */
public class ModifierGroupImplTest {

	private static final String ILLEGAL_ARGUMENT_EXCEPTION_EXPECTED = "Should have thrown an IllegalArgumentException";
	private static final String GUID1 = "guid1";

	@Test(expected = UnsupportedOperationException.class)
	public void testUnmodifiableModifierGroupLdf() {
		ModifierGroup modifierGroup = new ModifierGroupImpl();
		modifierGroup.getModifierGroupLdf().add(new ModifierGroupLdfImpl());
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testUnmodifiableModifierField() {
		ModifierGroup modifierGroup = new ModifierGroupImpl();
		modifierGroup.getModifierFields().add(new ModifierFieldImpl());
	}

	@Test
	public void testAddModifierFieldSuccess() {
		ModifierGroup modifierGroup = new ModifierGroupImpl();
		ModifierFieldImpl modifierField = new ModifierFieldImpl();
		modifierField.setFieldType(ModifierType.BOOLEAN);
		modifierField.setOrdering(0);
		modifierField.setGuid(GUID1);
		modifierGroup.addModifierField(modifierField);
	}

	@Test
	public void testAddModifierFieldEmptyCode() {
		try {
			ModifierGroup modifierGroup = new ModifierGroupImpl();
			modifierGroup.addModifierField(new ModifierFieldImpl());
		} catch (IllegalArgumentException ex) {
			assertTrue("Shall validate empty ModifierField.code", ex.getMessage().contains("empty code"));
			return;
		}

		fail(ILLEGAL_ARGUMENT_EXCEPTION_EXPECTED);
	}

	@Test
	public void testAddModifierFieldDuplicatedOrdering() {
		ModifierField modifierField1 = new ModifierFieldImpl();
		modifierField1.setCode("code1");
		modifierField1.setOrdering(0);
		modifierField1.setFieldType(ModifierType.BOOLEAN);
		modifierField1.setGuid(GUID1);

		ModifierField modifierField2 = new ModifierFieldImpl();
		modifierField2.setCode("code2");
		modifierField2.setOrdering(0);
		modifierField2.setFieldType(ModifierType.BOOLEAN);
		modifierField2.setGuid("guid2");

		ModifierGroup modifierGroup = new ModifierGroupImpl();
		modifierGroup.addModifierField(modifierField1);

		try {
			modifierGroup.addModifierField(modifierField2);
		} catch (IllegalArgumentException ex) {
			assertTrue("Shall validate duplicated ModifierField.ordering", ex.getMessage().contains("same ordering"));
			return;
		}

		fail(ILLEGAL_ARGUMENT_EXCEPTION_EXPECTED);
	}

	@Test
	public void testAddModifierGroupLdfEmptyDisplayName() {
		ModifierGroupLdf modifierGroupLdf = new ModifierGroupLdfImpl();
		modifierGroupLdf.setLocale(Locale.US.toString());

		ModifierGroup modifierGroup = new ModifierGroupImpl();

		try {
			modifierGroup.addModifierGroupLdf(modifierGroupLdf);
		} catch (IllegalArgumentException ex) {
			assertTrue("Shall validate empty ModifierGroupLdf.displayName", ex.getMessage().contains("empty display name"));
			return;
		}

		fail(ILLEGAL_ARGUMENT_EXCEPTION_EXPECTED);
	}

	@Test
	public void testAddModifierGroupLdfEmptyLocale() {
		ModifierGroupLdf modifierGroupLdf = new ModifierGroupLdfImpl();
		modifierGroupLdf.setDisplayName("displayName");

		ModifierGroup modifierGroup = new ModifierGroupImpl();

		try {
			modifierGroup.addModifierGroupLdf(modifierGroupLdf);
		} catch (IllegalArgumentException ex) {
			assertTrue("Shall validate empty ModifierGroupLdf.locale", ex.getMessage().contains("empty locale"));
			return;
		}

		fail(ILLEGAL_ARGUMENT_EXCEPTION_EXPECTED);
	}

	@Test
	public void testAddModifierGroupLdfSameLocal() {
		ModifierGroupLdf modifierGroupLdf1 = new ModifierGroupLdfImpl();
		modifierGroupLdf1.setDisplayName("displayName1");
		modifierGroupLdf1.setLocale(Locale.US.toString());

		ModifierGroupLdf modifierGroupLdf2 = new ModifierGroupLdfImpl();
		modifierGroupLdf2.setDisplayName("displayName2");
		modifierGroupLdf2.setLocale(Locale.US.toString());

		ModifierGroup modifierGroup = new ModifierGroupImpl();
		modifierGroup.addModifierGroupLdf(modifierGroupLdf1);

		try {
			modifierGroup.addModifierGroupLdf(modifierGroupLdf2);
		} catch (IllegalArgumentException ex) {
			assertTrue("Shall validate duplicated ModifierGroupLdf.locale", ex.getMessage().contains("same locale"));
			return;
		}

		fail(ILLEGAL_ARGUMENT_EXCEPTION_EXPECTED);
	}

}
