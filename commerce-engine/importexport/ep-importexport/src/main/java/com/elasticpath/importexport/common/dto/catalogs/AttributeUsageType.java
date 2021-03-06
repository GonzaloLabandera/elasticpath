/**
 * Copyright (c) Elastic Path Software Inc., 2013
 */
package com.elasticpath.importexport.common.dto.catalogs;

import com.elasticpath.domain.attribute.AttributeUsage;
import com.elasticpath.domain.attribute.impl.AttributeUsageImpl;

/**
 * The AttributeUsageType.
 */
public enum AttributeUsageType {

	/**
	 * Category Usage Type.
	 */
	Category {

		@Override
		public AttributeUsage usage() {
			return AttributeUsageImpl.CATEGORY_USAGE;
		}
	},

	/**
	 * Product Usage Type.
	 */
	Product {

		@Override
		public AttributeUsage usage() {
			return AttributeUsageImpl.PRODUCT_USAGE;
		}
	},

	/**
	 * SKU Usage Type.
	 */
	SKU {
		
		@Override
		public AttributeUsage usage() {
			return AttributeUsageImpl.SKU_USAGE;
		}
	},

	/**
	 * User Profile Usage Type.
	 */
	UserProfile {

		@Override
		public AttributeUsage usage() {
			return AttributeUsageImpl.USER_PROFILE_USAGE;
		}
	},
	
	/**
	 * Account Profile Usage Type.
	 */
	AccountProfile {

		@Override
		public AttributeUsage usage() {
			return AttributeUsageImpl.ACCOUNT_PROFILE_USAGE;
		}
	};

	/**
	 * Returns AttributeUsage for current type. 
	 * 
	 * @return AttributeUsage
	 */
	public abstract AttributeUsage usage();
	
	/**
	 * Converts AttributeUsage to AttributeUsageType.
	 *
	 * @param usage the AttributeUsage
	 * @return AttributeUsageType
	 */
	public static AttributeUsageType valueOf(final AttributeUsage usage) {
		return valueOf(usage.toString());
	}
}
