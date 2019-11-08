/*
 * Copyright (c) Elastic Path Software Inc., 2016
 */

package com.elasticpath.domain.modifier.impl;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

import java.util.Locale;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.elasticpath.domain.modifier.ModifierFieldOption;
import com.elasticpath.domain.modifier.ModifierFieldOptionLdf;

/**
 * * Unit tests for {@link ModifierFieldOptionImpl}.
 */
public class ModifierFieldOptionImplTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	/** Verify the field options LDF collection is immutable. */
	@Test(expected = UnsupportedOperationException.class)
	public void testUnmodifiableModifierFieldOptionLdf() {
		ModifierFieldOption modifierFieldOption = new ModifierFieldOptionImpl();
		modifierFieldOption.getModifierFieldOptionsLdf().add(new ModifierFieldOptionLdfImpl());
	}

	/** Verify that adding a field option LDF via the add method is successful. */
	@Test
	public void testAddModifierGroupLdfSuccess() {
		ModifierFieldOptionLdf modifierFieldOptionLdf = new ModifierFieldOptionLdfImpl();
		modifierFieldOptionLdf.setLocale(Locale.US.toString());
		modifierFieldOptionLdf.setDisplayName("displayName");

		ModifierFieldOption modifierFieldOption = new ModifierFieldOptionImpl();
		modifierFieldOption.addModifierFieldOptionLdf(modifierFieldOptionLdf);
		assertThat(modifierFieldOption.getModifierFieldOptionsLdf(), contains(modifierFieldOptionLdf));
	}

	/** Verify the thrown exception message when the display name is empty. */
	@Test
	public void testAddModifierGroupLdfEmptyDisplayName() {
		ModifierFieldOptionLdf modifierFieldOptionLdf = new ModifierFieldOptionLdfImpl();
		modifierFieldOptionLdf.setLocale(Locale.US.toString());

		ModifierFieldOption modifierFieldOption = new ModifierFieldOptionImpl();

		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage(containsString("empty display name"));
		modifierFieldOption.addModifierFieldOptionLdf(modifierFieldOptionLdf);
	}

	/** Verify the thrown exception message when the locale is empty. */
	@Test
	public void testAddModifierGroupLdfEmptyLocale() {
		ModifierFieldOptionLdf modifierFieldOptionLdf = new ModifierFieldOptionLdfImpl();
		modifierFieldOptionLdf.setDisplayName("displayName");

		ModifierFieldOption modifierFieldOption = new ModifierFieldOptionImpl();

		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage(containsString("empty locale"));
		modifierFieldOption.addModifierFieldOptionLdf(modifierFieldOptionLdf);
	}

	/** Verify the thrown exception message when the locale is duplicated. */
	@Test
	public void testAddModifierGroupLdfSameLocal() {
		ModifierFieldOptionLdf modifierFieldOptionLdf1 = new ModifierFieldOptionLdfImpl();
		modifierFieldOptionLdf1.setDisplayName("displayName1");
		modifierFieldOptionLdf1.setLocale(Locale.US.toString());

		ModifierFieldOptionLdf modifierFieldOptionLdf2 = new ModifierFieldOptionLdfImpl();
		modifierFieldOptionLdf2.setDisplayName("displayName2");
		modifierFieldOptionLdf2.setLocale(Locale.US.toString());

		ModifierFieldOption modifierFieldOption = new ModifierFieldOptionImpl();
		modifierFieldOption.addModifierFieldOptionLdf(modifierFieldOptionLdf1);

		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage(containsString("same locale"));
		modifierFieldOption.addModifierFieldOptionLdf(modifierFieldOptionLdf2);
	}

}
