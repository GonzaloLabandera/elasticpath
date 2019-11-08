/*
 * Copyright (c) Elastic Path Software Inc., 2016
 */
package com.elasticpath.domain.modifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.google.common.collect.ImmutableSet;

import com.elasticpath.commons.util.extenum.AbstractExtensibleEnum;
import com.elasticpath.validation.defs.AbstractConstraintDef;
import com.elasticpath.validation.defs.BooleanDef;
import com.elasticpath.validation.defs.DecimalDef;
import com.elasticpath.validation.defs.EpEmailDef;
import com.elasticpath.validation.defs.ISO8601DateDef;
import com.elasticpath.validation.defs.ISO8601DateTimeDef;
import com.elasticpath.validation.defs.IntegerDef;
import com.elasticpath.validation.defs.MultiOptionDef;
import com.elasticpath.validation.defs.SingleOptionDef;


/**
 * Cart Item Modifier Type Enum.
 */
public class ModifierType extends AbstractExtensibleEnum<ModifierType> implements Comparable<ModifierType> {

	private static final long serialVersionUID = 1L;

	/**
	 * ordinal static for Short Text type.
	 */
	public static final int SHORT_TEXT_ORDINAL = 1;

	/**
	 * ordinal static for Decimal type.
	 */
	public static final int DECIMAL_ORDINAL = 2;

	/**
	 * ordinal static for Integer type.
	 */
	public static final int INTEGER_ORDINAL = 3;

	/**
	 * ordinal static for Boolean type.
	 */
	public static final int BOOLEAN_ORDINAL = 4;

	/**
	 * ordinal static for DATE type.
	 */
	public static final int DATE_ORDINAL = 5;

	/**
	 * ordinal static for Date Time type.
	 */
	public static final int DATE_TIME_ORDINAL = 6;

	/**
	 * ordinal static for Pick Single Option type.
	 */
	public static final int PICK_SINGLE_OPTION_ORDINAL = 7;

	/**
	 * ordinal static for Pick Multi Option type.
	 */
	public static final int PICK_MULTI_OPTION_ORDINAL = 8;

	/**
	 * ordinal static for Email type.
	 */
	public static final int EMAIL_ORDINAL = 9;

	/**
	 * SHORT TEXT Cart item modifier type.
	 */
	public static final ModifierType SHORT_TEXT = new ModifierType(SHORT_TEXT_ORDINAL, "SHORT_TEXT", "ShortText");

	/**
	 * DECIMAL Cart item modifier type.
	 */
	public static final ModifierType DECIMAL = new ModifierType(DECIMAL_ORDINAL, "DECIMAL", "Decimal", new DecimalDef());

	/**
	 * INTEGER Cart item modifier type.
	 */
	public static final ModifierType INTEGER = new ModifierType(INTEGER_ORDINAL, "INTEGER", "Integer", new IntegerDef());

	/**
	 * BOOLEAN Cart item modifier type.
	 */
	public static final ModifierType BOOLEAN = new ModifierType(BOOLEAN_ORDINAL, "BOOLEAN", "Boolean", new BooleanDef());

	/**
	 * DATE Cart item modifier type.
	 */
	public static final ModifierType DATE = new ModifierType(DATE_ORDINAL, "DATE", "Date", new ISO8601DateDef());

	/**
	 * DATE_TIME Cart item modifier type.
	 */
	public static final ModifierType DATE_TIME = new ModifierType(DATE_TIME_ORDINAL, "DATE_TIME", "DateTime",
			new ISO8601DateTimeDef());

	/**
	 * PICK_SINGLE_OPTION Cart item modifier type.
	 */
	public static final ModifierType PICK_SINGLE_OPTION = new ModifierType(PICK_SINGLE_OPTION_ORDINAL, "PICK_SINGLE_OPTION",
			"PickSingleOption", new SingleOptionDef());

	/**
	 * PICK_MULTI_OPTION Cart item modifier type.
	 */
	public static final ModifierType PICK_MULTI_OPTION = new ModifierType(PICK_MULTI_OPTION_ORDINAL, "PICK_MULTI_OPTION",
			"PickMultiOption", new MultiOptionDef());

	/**
	 * EMAIL Cart item modifier type.
	 */
	public static final ModifierType EMAIL = new ModifierType(EMAIL_ORDINAL, "EMAIL", "Email", new EpEmailDef());

	private final String camelName;

	@SuppressWarnings("PMD.AssignmentToNonFinalStatic")
	private static List<ModifierType> modifierTypes;

	private final AbstractConstraintDef<?>[] constraintDef;

	/**
	 * Create a new enum value.
	 *
	 * @param ordinal       the unique ordinal value
	 * @param name          the named value for this extensible enum
	 * @param camelName     the camel case name for this extensible enum
	 * @param constraintDef the optional array of constraint definitions
	 */
	@SuppressWarnings("squid:S3010")
	protected ModifierType(final int ordinal,
			final String name,
			final String camelName,
			final AbstractConstraintDef<?>... constraintDef) {

		super(ordinal, name, ModifierType.class);
		this.camelName = camelName;
		if (ordinal == 0) {
			throw new IllegalArgumentException("We should not accept zero as a valid code for ModifierType");
		}

		if (modifierTypes == null) {
			modifierTypes = new ArrayList<>();
		}

		modifierTypes.add(this);
		this.constraintDef = constraintDef;
	}

	@Override
	protected Class<ModifierType> getEnumType() {
		return ModifierType.class;
	}

	/**
	 * Get the camel case name.
	 *
	 * @return the camel case name
	 */
	public String getCamelName() {
		return camelName;
	}

	/**
	 * Get the constraint definitions.
	 *
	 * @return optional constraint definitions.
	 */
	@SuppressWarnings("squid:S1452")
	public Optional<AbstractConstraintDef<?>[]> getConstraintDefs() {
		return Optional.ofNullable(constraintDef);
	}

	/**
	 * Find the enum value with the specified ordinal.
	 *
	 * @param ordinal the ordinal
	 * @return the enum value
	 */
	public static ModifierType valueOf(final int ordinal) {
		return valueOf(ordinal, ModifierType.class);
	}

	/**
	 * Find the enum value with the specified camel case name.
	 *
	 * @param camelCaseName the camel case name
	 * @return the enum value
	 */
	public static ModifierType valueOfCamelCase(final String camelCaseName) {
		for (ModifierType modifierType : modifierTypes) {
			if (modifierType.getCamelName().equals(camelCaseName)) {
				return modifierType;
			}
		}
		throw new IllegalArgumentException("Cannot find ModifierType with camel case name: " + camelCaseName);
	}

	/**
	 * Find the enum value with the specified ordinal.
	 *
	 * @param type the type
	 * @return the enum value
	 */
	public static ModifierType valueOf(final String type) {
		return valueOf(type, ModifierType.class);
	}

	/**
	 * Find all enum values for a particular enum type.
	 *
	 * @return the enum values
	 */
	public static Collection<ModifierType> values() {
		return values(ModifierType.class);
	}

	@Override
	@SuppressWarnings("squid:S1210")
	public int compareTo(final ModifierType other) {
		final int ordinal1 = getOrdinal();
		final int ordinal2 = other.getOrdinal();
		if (ordinal1 < ordinal2) {
			return -1;
		}
		if (ordinal1 > ordinal2) {
			return 1;
		}
		return 0;
	}

	/**
	 * Indicates the modifier is a pick type modifier.
	 *
	 * @return true if the modifier is a pick type
	 */
	public boolean isPickType() {
		return ImmutableSet.of(PICK_SINGLE_OPTION_ORDINAL, PICK_MULTI_OPTION_ORDINAL).contains(getOrdinal());
	}

}
