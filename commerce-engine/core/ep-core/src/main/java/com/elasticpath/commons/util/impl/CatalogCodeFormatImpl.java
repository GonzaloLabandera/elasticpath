/**
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.commons.util.impl;

import com.elasticpath.commons.enums.InvalidCatalogCodeMessage;
import com.elasticpath.commons.util.CatalogCodeFormat;

/**
 * The default implementation of <code>CatalogCodeFormat</code>.
 */
public class CatalogCodeFormatImpl implements CatalogCodeFormat {

	private static final int DEFAULT_MAX_LENGTH = 255;
	private static final boolean DEFAULT_SPACES_ALLOWED = false;

	private final boolean spacesAllowed;
	private final int maxLength;
	private final String regex;
	private final InvalidCatalogCodeMessage invalidCatalogCodeMessage;

	/**
	 * Constructor.
	 *
	 * @param builder the {@link CatalogCodeFormatImpl} builder.
	 */
	protected CatalogCodeFormatImpl(final Builder builder) {
		invalidCatalogCodeMessage = builder.invalidCatalogCodeMessage;
		regex = builder.regex;
		maxLength = builder.maxLength;
		spacesAllowed = builder.spacesAllowed;
	}

	/**
	 * @return a int value that defines the Max Lenght accepted in this Catalog Code Format
	 */
	public int getMaxLength() {
		return maxLength;
	}

	/**
	 * @return <code>true</code> if the format is set to use spaces. Otherwise, <code>false</code>
	 */
	public boolean isSpacesAllowed() {
		return spacesAllowed;
	}

	/**
	 * @return a regular expression that defines the string accepted in this Catalog Code Format
	 */
	public String getRegex() {
		return regex;
	}

	/**
	 * @return <code>invalidCatalogCodeMessage</code> which defines the message object to be used in case of errors
	 */
	public InvalidCatalogCodeMessage getInvalidCatalogCodeMessage() {
		return invalidCatalogCodeMessage;
	}

	/**
	 * Builder to build {@link CatalogCodeFormat}s.
	 */
	public static class Builder {

		private final String regex;
		private final InvalidCatalogCodeMessage invalidCatalogCodeMessage;

		private int maxLength = DEFAULT_MAX_LENGTH;
		private boolean spacesAllowed = DEFAULT_SPACES_ALLOWED;

		/**
		 * Constructor of Builder to build {@link CatalogCodeFormat}s.
		 *
		 * @param regexBase mandatory parameter that defines the Regular Expression that validates the Catalog Code format
		 * @param invalidCatalogCodeMessage mandatory parameter that gives the <code>InvalidCatalogCodeMessage</code> to be used in case of errors
		 */
		public Builder(final String regexBase, final InvalidCatalogCodeMessage invalidCatalogCodeMessage) {
			regex = regexBase;
			this.invalidCatalogCodeMessage = invalidCatalogCodeMessage;
		}

		/**
		 * Defines the Code Format Max Lenght value.
		 * <p/>
		 * If it is not defined it will use the <code>DEFAULT_MAX_LENGTH_255<code/>
		 *
		 * @param maxLength Defines the Code Format Max Lenght value.
		 * @return Builder
		 */
		public Builder setMaxLength(final int maxLength) {
			this.maxLength = maxLength;
			return this;
		}

		/**
		 *  Defines if Code Format will allow Spaces.
		 *  <p/>
		 *  Default value is <code>DEFAULT_NO_SPACES_TRUE</code>
		 *
		 * @param spacesAllowed Set <code>true</code> if the Code Format should allow Spaces. Otherwise, <code>false</code>
		 * @return Builder
		 */
		public Builder setSpacesAllowed(final boolean spacesAllowed) {
			this.spacesAllowed = spacesAllowed;
			return this;
		}

		/**
		 * @return Builder
		 */
		public CatalogCodeFormatImpl build() {
			return new CatalogCodeFormatImpl(this);
		}

	}

}
