/**
 * Copyright (c) Elastic Path Software Inc., 2013
 */
package com.elasticpath.tools.epcoretool.client;

/**
 * The Interface CliParser.
 */
public interface CliParser {

	/**
	 * Execute.
	 *
	 * @param param the param
	 */
	void execute(String param);

	/**
	 * Help.
	 *
	 * @return the string
	 */
	String help();
}
