/**
 * Copyright (c) Elastic Path Software Inc., 2015
 */
package com.elasticpath.ql.parser;

/**
 * An enumeration of the available query types.
 */
public enum EPQueryType {

	/** A product query type. */
	PRODUCT("Product", TargetLanguage.LUCENE),

	/** A category query type. */
	CATEGORY("Category", TargetLanguage.LUCENE),

	/** A catalog query type. */
	CATALOG("Catalog", TargetLanguage.JPQL),

	/** A promotion query type. */
	PROMOTION("Promotion", TargetLanguage.LUCENE),

	/** A price list query type. */
	PRICELIST("PriceList", TargetLanguage.JPQL),

	/** A configuration query type. */
	CONFIGURATION("Configuration", TargetLanguage.SQL),

	/** A price list assignment query type. */
	PRICELISTASSIGNMENT("PriceListAssignment", TargetLanguage.JPQL),

	/** A dynamic content query type. */
	DYNAMICCONTENT("DynamicContent", TargetLanguage.JPQL), DYNAMICCONTENTDELIVERY("DynamicContentDelivery", TargetLanguage.JPQL),

	/** Query type warehouse. */
	WAREHOUSE("Warehouse", TargetLanguage.JPQL),

	/** Query type for Stores. */
	STORE("Store", TargetLanguage.JPQL),

	/** Query types for taxes. */
	TAXCODE("TaxCode", TargetLanguage.JPQL),
	
	/** Query types for taxes. */
	TAXJURISDICTION("TaxJurisdiction", TargetLanguage.JPQL),

	/** Query type for customers. */
	CUSTOMER("Customer", TargetLanguage.SQL),

	/** Query type for customer profile attributes. */
	CUSTOMERPROFILEATTRIBUTE("CustomerProfileAttribute", TargetLanguage.JPQL),

	/** Query type for UserRoles. */
	USER_ROLE("UserRole", TargetLanguage.JPQL),
	
	/** Query type for CM users. */
	CMUSER("CmUser", TargetLanguage.JPQL),
	
	/** Query type for Gift Certificates. */
	GIFTCERTIFICATE("GiftCertificate", TargetLanguage.JPQL),
	
	/** Query type for Content Space. */
	CONTENTSPACE("ContentSpace", TargetLanguage.JPQL),
	
	/** Query type for ShippingServiceLevels. */
	SHIPPINGSERVICELEVEL("ShippingServiceLevel", TargetLanguage.JPQL),
	
	/** Query type for ShippingRegion. */
	SHIPPINGREGION("ShippingRegion", TargetLanguage.JPQL),
	
	/** Query type for TagCondition. */
	SAVEDCONDITION("SavedCondition", TargetLanguage.JPQL),
	
	/** Query type for Import Job. */
	CMIMPORTJOB("CmImportJob", TargetLanguage.JPQL),

	/** Query type for Facet Job. */
	FACET("Facet", TargetLanguage.JPQL),
	
	/** Query type for SortAttribute Job. */
	SORTATTRIBUTE("SortAttribute", TargetLanguage.JPQL),

	/** Query type for TagGroup Job. */
	TAGGROUP("TagGroup", TargetLanguage.JPQL);


	/** The identifier of the type - useful for naming. */
	private String typeName;

	private TargetLanguage targetLanguage;

	/**
	 * Create an EPQueryType with the specified identifier.
	 * 
	 * @param typeName the identifier for the type.
	 * @param language the target language for given query type
	 */
	EPQueryType(final String typeName, final TargetLanguage language) {
		this.typeName = typeName;
		this.targetLanguage = language;
	}

	/**
	 * Return a string identifier for this type.
	 * 
	 * @return an identifier for this query type.
	 */
	public String getTypeName() {
		return typeName;
	}

	/**
	 * Gets the target language for this query type.
	 * 
	 * @return the target language
	 */
	public TargetLanguage getTargetLanguage() {
		return targetLanguage;
	}

	/**
	 * Attempts to find the {@link EPQueryType} with the given type name. Returns null if the type is not found.
	 * 
	 * @param typeName the type name
	 * @return a {@link EPQueryType} with the given name
	 */
	public static EPQueryType findFromName(final String typeName) {
		for (EPQueryType type : EPQueryType.values()) {
			if (typeName.equals(type.getTypeName())) {
				return type;
			}
		}
		return null;
	}

}
