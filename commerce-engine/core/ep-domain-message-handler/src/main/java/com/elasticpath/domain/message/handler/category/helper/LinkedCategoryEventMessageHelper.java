/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.domain.message.handler.category.helper;

import java.util.List;

import com.elasticpath.messaging.EventMessage;

/**
 * Represents an interface for linked category helper.
 */
public interface LinkedCategoryEventMessageHelper {

	/**
	 * Extracts a category code from eventMessage.
	 *
	 * @param eventMessage source eventMessage.
	 * @return category code.
	 */
	String getUnlinkedCategoryCode(EventMessage eventMessage);

	/**
	 * Extracts a list of category stores from eventMessage.
	 *
	 * @param eventMessage source eventMessage.
	 * @return a list of category stores.
	 */
	List<String> getUnlinkedCategoryStores(EventMessage eventMessage);

}
