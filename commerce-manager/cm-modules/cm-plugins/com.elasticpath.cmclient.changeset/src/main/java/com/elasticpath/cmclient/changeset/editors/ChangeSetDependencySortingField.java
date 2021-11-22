/**
 * Copyright (c) Elastic Path Software Inc., 2016
 */
package com.elasticpath.cmclient.changeset.editors;


import com.elasticpath.commons.pagination.SortingField;
import com.elasticpath.commons.util.extenum.AbstractExtensibleEnum;

/**
 * The change set dependency sorting field.
 */
public class ChangeSetDependencySortingField extends AbstractExtensibleEnum<ChangeSetDependencySortingField> implements SortingField {
	private static final long serialVersionUID = -3828916616704571530L;

	/**
	 * Source object name sorting field.
	 */
	public static final SortingField SOURCE_OBJECT_NAME = new ChangeSetDependencySortingField(0, "sourceObjectName"); //$NON-NLS-1$
	
	/**
	 * Source object type sorting field.
	 */
	public static final SortingField SOURCE_OBJECT_TYPE = new ChangeSetDependencySortingField(1, "sourceObjectType"); //$NON-NLS-1$
	
	/**
	 * Dependency object name sorting field.
	 */
	public static final SortingField DEPENDENCY_OBJECT_NAME = new ChangeSetDependencySortingField(2, "dependencyObjectName"); //$NON-NLS-1$
	
	/**
	 * Dependency object type sorting field.
	 */
	public static final SortingField DEPENDENCY_OBJECT_TYPE = new ChangeSetDependencySortingField(3, "dependencyObjectType"); //$NON-NLS-1$
	
	/**
	 * Change set name sorting field.
	 */
	public static final SortingField CHANGE_SET_NAME = new ChangeSetDependencySortingField(4, "changeSetName"); //$NON-NLS-1$

	/**
	 * Create an enum value for a enum type. Name is case-insensitive and will be stored in upper-case.
	 *
	 * @param ordinal the ordinal value
	 * @param name    the name value (this will be converted to upper-case).
	 */
	protected ChangeSetDependencySortingField(final int ordinal, final String name) {
		super(ordinal, name, ChangeSetDependencySortingField.class);
	}

	@Override
	protected Class getEnumType() {
		return ChangeSetDependencySortingField.class;
	}
}
