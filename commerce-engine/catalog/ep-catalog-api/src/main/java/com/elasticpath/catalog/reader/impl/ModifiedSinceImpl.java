/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.reader.impl;

import java.util.Date;

import com.elasticpath.catalog.reader.ModifiedSince;

/**
 * An implementation of {@link ModifiedSince}.
  */
public class ModifiedSinceImpl implements ModifiedSince {
	private final Date modifiedSince;
	private final Long modifiedSinceOffset;

	/**
	 * Constructor.
	 *
	 * @param modifiedSince       is date threshold for projections which have been created or modified.
	 * @param modifiedSinceOffset is date offset for modifiedSince.
	 */
	public ModifiedSinceImpl(final Date modifiedSince, final Long modifiedSinceOffset) {
		this.modifiedSince = modifiedSince;
		this.modifiedSinceOffset = modifiedSinceOffset;
	}

	/**
	 * Constructor.
	 */
	public ModifiedSinceImpl() {
		this.modifiedSince = null;
		this.modifiedSinceOffset = null;
	}

	@Override
	public Date getModifiedSince() {
		return modifiedSince;
	}

	@Override
	public Long getModifiedSinceOffset() {
		return modifiedSinceOffset;
	}
}
