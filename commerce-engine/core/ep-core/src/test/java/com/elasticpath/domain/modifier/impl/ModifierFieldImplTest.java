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

import com.elasticpath.domain.modifier.ModifierField;
import com.elasticpath.domain.modifier.ModifierFieldLdf;
import com.elasticpath.domain.modifier.ModifierFieldOption;

/**
 * Unit tests for {@link ModifierFieldImpl}.
 */
public class ModifierFieldImplTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	private static final String GUID1 = "guid1";

	/** Verify that the field options collection is immutable. */
	@Test(expected = UnsupportedOperationException.class)
	public void testUnmodifiableModifierFieldOption() {
		ModifierField cartItemModifierField = new ModifierFieldImpl();
		cartItemModifierField.getModifierFieldOptions().add(new ModifierFieldOptionImpl());
	}

	/** Verify that the field LDF collection is immutable. */
	@Test(expected = UnsupportedOperationException.class)
	public void testUnmodifiableModifierFieldLdf() {
		ModifierField cartItemModifierField = new ModifierFieldImpl();
		cartItemModifierField.getModifierFieldsLdf().add(new ModifierFieldLdfImpl());
	}

	/** Test adding a field LDF via the add method is successful. */
	@Test
	public void testAddModifierFieldLdfSuccess() {
		ModifierFieldLdf cartItemModifierFieldLdf = new ModifierFieldLdfImpl();
		cartItemModifierFieldLdf.setLocale(Locale.US.toString());
		cartItemModifierFieldLdf.setDisplayName(GUID1);

		ModifierField cartItemModifierField = new ModifierFieldImpl();
		cartItemModifierField.addModifierFieldLdf(cartItemModifierFieldLdf);
		assertThat(cartItemModifierField.getModifierFieldsLdf(), contains(cartItemModifierFieldLdf));
	}

	/** Verify the thrown exception message when display name is empty. */
	@Test
	public void testAddModifierFieldLdfEmptyDisplayName() {
		ModifierFieldLdf cartItemModifierFieldLdf = new ModifierFieldLdfImpl();
		cartItemModifierFieldLdf.setLocale(Locale.US.toString());

		ModifierField cartItemModifierField = new ModifierFieldImpl();

		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage(containsString("empty display name"));
		cartItemModifierField.addModifierFieldLdf(cartItemModifierFieldLdf);
	}

	/** Verify the thrown exception message when locale is empty. */
	@Test
	public void testAddModifierFieldLdfEmptyLocale() {
		ModifierFieldLdf cartItemModifierGroupLdf = new ModifierFieldLdfImpl();
		cartItemModifierGroupLdf.setDisplayName("displayName");

		ModifierField cartItemModifierField = new ModifierFieldImpl();

		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage(containsString("empty locale"));
		cartItemModifierField.addModifierFieldLdf(cartItemModifierGroupLdf);
	}

	/** Verify the thrown exception message when there's a duplicate locale. */
	@Test
	public void testAddModifierFieldLdfSameLocal() {
		ModifierFieldLdf cartItemModifierGroupLdf1 = new ModifierFieldLdfImpl();
		cartItemModifierGroupLdf1.setDisplayName("displayName1");
		cartItemModifierGroupLdf1.setLocale(Locale.US.toString());

		ModifierFieldLdf cartItemModifierGroupLdf2 = new ModifierFieldLdfImpl();
		cartItemModifierGroupLdf2.setDisplayName("displayName2");
		cartItemModifierGroupLdf2.setLocale(Locale.US.toString());

		ModifierField cartItemModifierField = new ModifierFieldImpl();
		cartItemModifierField.addModifierFieldLdf(cartItemModifierGroupLdf1);

		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage(containsString("same locale"));
		cartItemModifierField.addModifierFieldLdf(cartItemModifierGroupLdf2);
	}

	/** Verify that adding a field option via the add method is successful. */
	@Test
	public void testAddModifierFieldOptionSuccess() {
		ModifierFieldOption cartItemModifierFieldOption = new ModifierFieldOptionImpl();
		cartItemModifierFieldOption.setOrdering(0);
		cartItemModifierFieldOption.setValue("value");

		ModifierField cartItemModifierField = new ModifierFieldImpl();
		cartItemModifierField.addModifierFieldOption(cartItemModifierFieldOption);
		assertThat(cartItemModifierField.getModifierFieldOptions(), contains(cartItemModifierFieldOption));
	}

	/** Verify the thrown exception message when a field option value is empty. */
	@Test
	public void testAddModifierFieldOptionEmptyValue() {
		ModifierFieldOption cartItemModifierFieldOption = new ModifierFieldOptionImpl();
		cartItemModifierFieldOption.setOrdering(0);

		ModifierField cartItemModifierField = new ModifierFieldImpl();

		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage(containsString("empty modifierFieldOption.value"));
		cartItemModifierField.addModifierFieldOption(cartItemModifierFieldOption);
	}

	/** Verify the thrown exception message when the field option ordering is duplicated. */
	@Test
	public void testAddModifierFieldOptionDuplicateOrdering() {
		ModifierFieldOption cartItemModifierFieldOption1 = new ModifierFieldOptionImpl();
		cartItemModifierFieldOption1.setValue("value1");
		cartItemModifierFieldOption1.setOrdering(0);

		ModifierFieldOption cartItemModifierFieldOption2 = new ModifierFieldOptionImpl();
		cartItemModifierFieldOption2.setValue("value2");
		cartItemModifierFieldOption2.setOrdering(0);

		ModifierField cartItemModifierField = new ModifierFieldImpl();
		cartItemModifierField.addModifierFieldOption(cartItemModifierFieldOption1);

		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage(containsString("same ordering"));
		cartItemModifierField.addModifierFieldOption(cartItemModifierFieldOption2);
	}

}
