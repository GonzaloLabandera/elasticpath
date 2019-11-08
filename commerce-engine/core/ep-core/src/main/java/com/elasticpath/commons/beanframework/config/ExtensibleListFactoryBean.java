/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.commons.beanframework.config;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.config.ListFactoryBean;

/**
 * Factory class to build extensible lists.
 */
public class ExtensibleListFactoryBean extends ListFactoryBean {

	private List<?> removeList = Collections.emptyList();

	private String valueType;


	/**
	 * Getter for the class name that objects in the list must be assignable to.
	 *
	 * @return the String value representation of the class name
	 */
	public String getValueType() {
		return valueType;
	}

	/**
	 * Setter for the class name that objects in the list must be assignable to.
	 *
	 * @param valueType the String value representation for the class name
	 */
	public void setValueType(final String valueType) {
		this.valueType = valueType;
	}


	/**
	 * Setter for list of objects to be removed.
	 *
	 * @param removeList list of objects to be removed
	 */
	public void setRemoveList(final List<?> removeList) {
		Objects.requireNonNull(removeList, "List of elements for removal cannot be null");
		this.removeList = removeList;
	}

	/**
	 * Method to create a list.
	 *
	 * @return array list with specified values, without the objects in the remove list
	 */
	@Override
	protected List<Object> createInstance() {
		@SuppressWarnings("unchecked")
		List<Object> sourceList = super.createInstance();
		sourceList.removeAll(removeList);
		return sourceList;
	}

}
