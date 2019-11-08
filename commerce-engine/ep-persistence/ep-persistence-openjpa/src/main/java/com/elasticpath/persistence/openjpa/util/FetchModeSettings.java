/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.persistence.openjpa.util;

import org.apache.openjpa.persistence.jdbc.FetchMode;

/**
 * The data structure used for storing the old fetch mode settings.
 */
public class FetchModeSettings {

	private FetchMode eagerFetchMode;
	private FetchMode subclassFetchMode;

	public void setEagerFetchMode(final FetchMode eagerFetchMode) {
		this.eagerFetchMode = eagerFetchMode;
	}
	public void setSubclassFetchMode(final FetchMode subclassFetchMode) {
		this.subclassFetchMode = subclassFetchMode;
	}

	public FetchMode getEagerFetchMode() {
		return eagerFetchMode;
	}
	public FetchMode getSubclassFetchMode() {
		return subclassFetchMode;
	}
}
