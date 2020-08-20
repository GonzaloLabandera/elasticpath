/**
 * Copyright (c) Elastic Path Software Inc., 2016
 */
package com.elasticpath.importexport.common.types;

import java.util.HashMap;
import java.util.Map;

import com.elasticpath.ql.parser.EPQueryType;

/**
 * Job Type determines type of export/import operations. Each Job Type reflects type of complex object to be processed: Product for example. So
 * appropriate classes can be used for execution of the job according to its type.
 */
public enum JobType {

	/** Job Type for Product. */
	PRODUCT("products", EPQueryType.PRODUCT),

	/** Job Type for ProductBundle. */
	PRODUCTBUNDLE("bundles", null),

	/** Job Type for Modifier Groups. */
	MODIFIERGROUP("modifiergroups", null),

	/** Job type for Modifier Group Filters. **/
	MODIFIERGROUPFILTER("modifiergroupfilters", null),

	/** Job Type for Catalog. */
	CATALOG("catalogs", EPQueryType.CATALOG),

	/** Job Type for Category. */
	CATEGORY("categories", EPQueryType.CATEGORY),

	/** Job Type for Promotion. */
	PROMOTION("promotions", EPQueryType.PROMOTION),

	/** Job Type for Coupon Sets. */
	COUPONSET("coupon_sets", null),

	/** Job Type for Condition Rules. */
	CONDITIONRULE("condition_rules", null),

	/** Job type for Assets. */
	ASSETS("assets", null),

	/** Job type for Inventory. */
	INVENTORY("inventory", null),

	/** Job type for Pricing. */
	PRICING("pricing", null),

	/** Job type for ProductAssociations. */
	PRODUCTASSOCIATION("productassociations", null),

	/** Job type for ProductCategoryAssociations. */
	PRODUCTCATEGORYASSOCIATION("productcategoryassociations", null),

	/** Job Type for Price List Descriptor. */
	PRICELISTDESCRIPTOR("price_lists", EPQueryType.PRICELIST),

	/** Job Type for Base Amount. */
	BASEAMOUNT("amounts", null),

	/** Job type for SystemConfiguration. */
	SYSTEMCONFIGURATION("system_configuration", EPQueryType.CONFIGURATION),

	/** Job type for Dynamic Content. */
	DYNAMICCONTENT("dynamic_contents", EPQueryType.DYNAMICCONTENT),

	/** Job type for Dynamic Content Delivery. */
	DYNAMICCONTENTDELIVERY("dynamic_content_deliveries", EPQueryType.DYNAMICCONTENTDELIVERY),

	/** Job type for PLAs. */
	PRICELISTASSIGNMENT("price_list_assignments", EPQueryType.PRICELISTASSIGNMENT),

	/** Payment providers. */
	PAYMENTPROVIDER("payment_providers", null),

	/** Warehouses. */
	WAREHOUSE("warehouses", EPQueryType.WAREHOUSE),

	/** Job type for tax codes. */
	TAXCODE("tax_codes", EPQueryType.TAXCODE),

	/** Job type for tax jurisdictions. */
	TAXJURISDICTION("tax_jurisdictions", EPQueryType.TAXJURISDICTION),

	/** Job type for customer profile attributes. */
	CUSTOMERPROFILEATTRIBUTE("customerprofile_attributes", EPQueryType.CUSTOMERPROFILEATTRIBUTE),

	/** Stores. */
	STORE("stores", EPQueryType.STORE),

	/** Store Associations. */
	STOREASSOCIATION("store_associations", null),

	/** Customers. */
	CUSTOMER("customers", EPQueryType.CUSTOMER),

	/** Customer Groups. */
	CUSTOMERGROUP("customer_groups", null),

	/** User Roles. */
	USERROLE("user_roles", EPQueryType.USER_ROLE),

	/** Commerce Manager Users. */
	CMUSER("cmusers", EPQueryType.CMUSER),

	/** Gift certificates. */
	GIFTCERTIFICATE("gift_certificates", EPQueryType.GIFTCERTIFICATE),

	/** Job type for Content Spaces. */
	CONTENTSPACE("contentspaces", EPQueryType.CONTENTSPACE),

	/** Job type for Shipping Service Level. */
	SHIPPINGSERVICELEVEL("shipping_service_levels", EPQueryType.SHIPPINGSERVICELEVEL),

	/** Job type for Shipping Regions. */
	SHIPPINGREGION("shipping_regions", EPQueryType.SHIPPINGREGION),

	/** Job type for Saved Conditions. */
	SAVEDCONDITION("saved_conditions", EPQueryType.SAVEDCONDITION),

	/** Job Type for ImportJob. */
	CMIMPORTJOB("cmimportjobs", EPQueryType.CMIMPORTJOB),

	/** Data Policies. */
	DATAPOLICY("data_policies", null),

	/** Customer Consents. */
	CUSTOMERCONSENT("customer_consents", null),

	/** Facets. */
	FACET("facets", null),

	/** Sort attributes. */
	SORTATTRIBUTE("sort_attributes", EPQueryType.SORTATTRIBUTE),

	/** Store customer attributes. */
	STORECUSTOMERATTRIBUTE("store_customer_attributes", null),

	/** Attribute policies. */
	ATTRIBUTEPOLICY("attribute_policies", null),

	/** Job Type for TagGroup. */
	TAGGROUP("tag_groups", EPQueryType.TAGGROUP),

	/** Job Type for UserAccountAssociation. */
	USERACCOUNTASSOCIATION("useraccountassociations", null);

	/*
	 * The map of Job Types available in the system. XML tag name is the key and JobType is the value.
	 */
	private static Map<String, JobType> jobTypeMap = new HashMap<>();

	/*
	 * Fill the map with Job Types registered in this enumeration.
	 */
	static {
		for (JobType jobType : JobType.values()) {
			jobTypeMap.put(jobType.getTagName(), jobType);
		}
	}

	/**
	 * String representation of job type name is used as tag name in XML files produced/processed by jobs.
	 */
	private final String tagName;

	/**
	 * Query type associated to this job type.
	 */
	private final EPQueryType epQueryType;

	/**
	 * Private Constructor.
	 *
	 * @param tagName XML tag name
	 * @param epQueryType associated query type
	 */
	JobType(final String tagName, final EPQueryType epQueryType) {
		this.tagName = tagName;
		this.epQueryType = epQueryType;
	}

	/**
	 * Gets the name of XML tag representing Job Type.
	 *
	 * @return the tag name
	 */
	public String getTagName() {
		return tagName;
	}

	/**
	 * Associated EPQL query type.
	 *
	 * @return EP query type
	 */
	public EPQueryType getEpQueryType() {
		return epQueryType;
	}

	/**
	 * Finds JobType in the map by its XML tag name.
	 *
	 * @param tagName XML tag name
	 * @return appropriate JobType if exists
	 */
	public static JobType getJobTypeByTag(final String tagName) {
		return jobTypeMap.get(tagName);
	}

}
