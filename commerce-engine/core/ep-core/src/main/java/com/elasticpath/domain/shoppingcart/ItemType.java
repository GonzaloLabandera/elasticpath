/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.domain.shoppingcart;

import java.util.Collection;

import com.elasticpath.commons.util.extenum.AbstractExtensibleEnum;

/**
 * Defines the possible cart item types.
 * 
 * A cart item can be a:
 * 
 * 1. simple SKU (no children)
 * 2. composite SKU (a bundle) with bundle constituents
 * 3. composite SKU with dependent items
 * 4. dependent item
 * 5. bundle constituent
 */
public class ItemType extends AbstractExtensibleEnum<ItemType> {
	private static final long serialVersionUID = 1L;

	/** SIMPLE ordinal. */
	public static final int SIMPLE_ORDINAL = 0;

	/**
	 * A cart item is a simple SKU.
	 */
	public static final ItemType SIMPLE = new ItemType(SIMPLE_ORDINAL, "SIMPLE");

	/** BUNDLE ordinal. */
	public static final int BUNDLE_ORDINAL = 1;

	/**
	 * A cart item is a bundle.
	 */
	public static final ItemType BUNDLE = new ItemType(BUNDLE_ORDINAL, "BUNDLE");

	/** SKU_WITH_DEPENDENTS ordinal. */
	public static final int SKU_WITH_DEPENDENTS_ORDINAL = 2;

	/**
	 * A cart item with dependent items.
	 */
	public static final ItemType SKU_WITH_DEPENDENTS = new ItemType(SKU_WITH_DEPENDENTS_ORDINAL, "SKU_WITH_DEPENDENTS");


	/** DEPENDENT ordinal. */
	public static final int DEPENDENT_ORDINAL = 3;

	/**
	 * Dependent item.
	 */
	public static final ItemType DEPENDENT = new ItemType(DEPENDENT_ORDINAL, "DEPENDENT");

	/** BUNDLE_CONSTITUENT ordinal. */
	public static final int BUNDLE_CONSTITUENT_ORDINAL = 4;

	/**
	 * Bundle constituent.
	 */
	public static final ItemType BUNDLE_CONSTITUENT = new ItemType(BUNDLE_CONSTITUENT_ORDINAL, "BUNDLE_CONSTITUENT");

	private final int index;

	/**
	 * Instantiates a new root item type.
	 *
	 * @param ordinal the ordinal
	 * @param name the name
	 */
	public ItemType(final int ordinal, final String name) {
		super(ordinal, name, ItemType.class);
		this.index = ordinal;
	}

	/**
	 * Returns the index of the root type.
	 *
	 * @return the numeric index of the root type
	 */
	public int getIndex() {
		return this.index;
	}

	@Override
	protected Class<ItemType> getEnumType() {
		return ItemType.class;
	}

	/**
	 * Return the values.
	 *
	 * @return the collection of values
	 */
	public static Collection<ItemType> values() {
		return values(ItemType.class);
	}

	/**
	 * Find the enum value with the specified name.
	 * @param name the name
	 * @return the enum value
	 */
	public static ItemType valueOf(final String name) {
		return valueOf(name, ItemType.class);
	}

	/**
	 * Used during deserialization from db.
	 *
	 * @param ordinal the db ordinal.
	 * @return the {@ItemType} corresponding to db ordinal.
	 */
	public static ItemType fromOrdinal(final int ordinal) {
		return valueOf(ordinal, ItemType.class);
	}

}
