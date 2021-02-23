/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.commons.util;

import java.util.List;

import com.elasticpath.commons.enums.InvalidCatalogCodeMessage;

/**
 * Provides catalog code utility methods used throughout the application.
 */
public interface CatalogCodeUtil {

    /**
     * @return a Code Format that defines the Product Code validation.
     */
    CatalogCodeFormat getProductCodeFormat();

    /**
     * @return a Code Format that defines the Catalog Code validation.
     */
    CatalogCodeFormat getCatalogCodeFormat();

    /**
     * @return a Code Format that defines the Category Code validation.
     */
    CatalogCodeFormat getCategoryCodeFormat();

    /**
     * @return a Code Format that defines the SKU code validation.
     */
    CatalogCodeFormat getSkuCodeFormat();

    /**
     * @return a Code Format that defines the brand code validation.
     */
    CatalogCodeFormat getBrandCodeFormat();

    /**
     * Checks whether the given code is a valid brand code, matching it against its respective CatalogCodeFormat implementation.
     *
     * @param code the code to be validated
     * @return a List of <code>InvalidCatalogCodeMessage</code> with reasons for the invalid given brand code. Otherwise, a empty List.
     */
    List<InvalidCatalogCodeMessage> isValidBrandCode(String code);

    /**
     * Checks whether the given code is a valid product code, matching it against its respective CatalogCodeFormat implementation.
     *
     * @param code the code to be validated
     * @return a List of <code>InvalidCatalogCodeMessage</code> with messages for the invalid given product code. Otherwise, a empty List.
     */
    List<InvalidCatalogCodeMessage> isValidProductCode(String code);

    /**
     * Checks whether the given code is a valid sku code, matching it against its respective CatalogCodeFormat implementation.
     *
     * @param code the code to be validated
     * @return a List of <code>InvalidCatalogCodeMessage</code> with messages for the invalid given sku code. Otherwise, a empty List.
     */
    List<InvalidCatalogCodeMessage> isValidSkuCode(String code);

    /**
     * Checks whether the given code is a valid catalog code, matching it against its respective CatalogCodeFormat implementation.
     *
     * @param code the code to be validated
     * @return a List of <code>InvalidCatalogCodeMessage</code> with messages for the invalid given catalog code. Otherwise, a empty List.
     */
    List<InvalidCatalogCodeMessage> isValidCatalogCode(String code);

    /**
     * Checks whether the given code is a valid categrory code, matching it against its respective CatalogCodeFormat implementation.
     *
     * @param code the code to be validated
     * @return a List of <code>InvalidCatalogCodeMessage</code> with messages for the invalid given categrory code. Otherwise, a empty List.
     */
    List<InvalidCatalogCodeMessage> isValidCategoryCode(String code);

}
