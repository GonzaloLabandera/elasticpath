/**
 * Copyright (c) Elastic Path Software Inc., 2013
 */
package com.elasticpath.importexport.example;

import com.elasticpath.persistence.api.AbstractPersistableImpl;

/**
 * Example {@link Persistable} class for extensibility testing.
 */
public class ExamplePersistence extends AbstractPersistableImpl {
	private static final long serialVersionUID = 1L;
	private long uidPk;
	private String name;

	@Override
	public long getUidPk() {
		return uidPk;
	}

	@Override
	public void setUidPk(final long uidPk) {
		this.uidPk = uidPk;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}
}