/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.cucumber.definitions;

/**
 * Data policy name is randomly generated and there might be a need to share its instance, hence the purpose of this class is to hold the value.
 */
public final class DataPolicyNameHolder {

	private static String name;

	private DataPolicyNameHolder() {
		// this is an utility class
	}

	/**
	 * Set the new data policy name.
	 *
	 * @param newName new policy name
	 */
	public static void setName(final String newName) {
		name = newName;
	}

	/**
	 * Getter.
	 *
	 * @return recent data policy name
	 */
	public static String getName() {
		return name;
	}
}