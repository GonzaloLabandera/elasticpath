/**
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.commons.enums;

import java.util.List;

/**
 * The expected actions of whoever implements a representation of Invalid Catalog Code Messages.
 */
public interface InvalidCatalogCodeMessage {

	/**
	 * The message code defined in <code>CoreMessages</code> to map bundles with the error messages.
	 *
	 * @return messageCode to be find in CoreMessages
	 */
	String getMessageCode();

	/**
	 * The list of parameters to be replaced in its respective error message.
	 *
	 * @return list of <code>String</code> containing the parameters for error messages
	 */
	List<String> getParameters();

	/**
	 * Use this to add parameters to be replaced in its respective error message.
	 *
	 * @param parameter to add a parameter for the error message.
	 */
	void addParameter(String parameter);

}
