/**
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.commons.util.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class CatalogCodeUtilImplTest {

    private CatalogCodeUtilImpl catalogCodeUtil;

    /**
     * Prepare for tests.
     *
     * @throws Exception in case of failure.
     */
    @Before
    public void setUp() throws Exception {
        catalogCodeUtil = new CatalogCodeUtilImpl();
    }

    /**
     * Test method for 'com.elasticpath.commons.util.impl.CatalogCodeUtilImpl.isValidBrandCode(String)'.
     */
    @Test
    @SuppressWarnings({"PMD.AvoidDuplicateLiterals"})
    public void testIsValidBrandCode() {
        assertTrue(catalogCodeUtil.isValidBrandCode("aaaBBB112").isEmpty());
        assertTrue(catalogCodeUtil.isValidBrandCode("aaa_BBB112").isEmpty());

        assertFalse(catalogCodeUtil.isValidBrandCode("aaa-BBB112").isEmpty());
        assertFalse(catalogCodeUtil.isValidBrandCode("good.brand.code").isEmpty());
        assertFalse(catalogCodeUtil.isValidBrandCode(null).isEmpty());
        assertFalse(catalogCodeUtil.isValidBrandCode("").isEmpty());
        assertFalse(catalogCodeUtil.isValidBrandCode(" aaa BBB112").isEmpty());
        assertFalse(catalogCodeUtil.isValidBrandCode("/aaaBBB112").isEmpty());
        assertFalse(catalogCodeUtil.isValidBrandCode("#aaaBBB112").isEmpty());
        assertFalse(catalogCodeUtil.isValidBrandCode("aaaBBB,112").isEmpty());
        assertFalse(catalogCodeUtil.isValidBrandCode("aaaBBB(112)").isEmpty());
    }

    /**
     * Test method for 'com.elasticpath.commons.util.impl.CatalogCodeUtilImpl.isValidProductCode(String)'.
     */
    @Test
    @SuppressWarnings({"PMD.AvoidDuplicateLiterals"})
    public void testIsValidProductCode() {
        assertTrue(catalogCodeUtil.isValidProductCode("aaaBBB112").isEmpty());
        assertTrue(catalogCodeUtil.isValidProductCode("aaa_BBB112").isEmpty());
        assertTrue(catalogCodeUtil.isValidProductCode("aaa-BBB112").isEmpty());
        assertTrue(catalogCodeUtil.isValidProductCode("good.prod.code").isEmpty());

        assertFalse(catalogCodeUtil.isValidProductCode(null).isEmpty());
        assertFalse(catalogCodeUtil.isValidProductCode("").isEmpty());
        assertFalse(catalogCodeUtil.isValidProductCode(" aaa BBB112").isEmpty());
        assertFalse(catalogCodeUtil.isValidProductCode("/aaaBBB112").isEmpty());
        assertFalse(catalogCodeUtil.isValidProductCode("#aaaBBB112").isEmpty());
        assertFalse(catalogCodeUtil.isValidProductCode("aaaBBB,112").isEmpty());
        assertFalse(catalogCodeUtil.isValidProductCode("aaaBBB(112)").isEmpty());
    }

    /**
     * Test method for 'com.elasticpath.commons.util.impl.CatalogCodeUtilImpl.isValidSkuCode(String)'.
     */
    @Test
    @SuppressWarnings({"PMD.AvoidDuplicateLiterals"})
    public void testIsValidSkuCode() {
        assertTrue(catalogCodeUtil.isValidSkuCode("aaaBBB112").isEmpty());
        assertTrue(catalogCodeUtil.isValidSkuCode("aaa_BBB112").isEmpty());
        assertTrue(catalogCodeUtil.isValidSkuCode("aaa-BBB112").isEmpty());
        assertTrue(catalogCodeUtil.isValidSkuCode("good.sku.code").isEmpty());

        assertFalse(catalogCodeUtil.isValidSkuCode(null).isEmpty());
        assertFalse(catalogCodeUtil.isValidSkuCode("").isEmpty());
        assertFalse(catalogCodeUtil.isValidSkuCode(" aaa BBB112").isEmpty());
        assertFalse(catalogCodeUtil.isValidSkuCode("/aaaBBB112").isEmpty());
        assertFalse(catalogCodeUtil.isValidSkuCode("#aaaBBB112").isEmpty());
        assertFalse(catalogCodeUtil.isValidSkuCode("aaaBBB,112").isEmpty());
        assertFalse(catalogCodeUtil.isValidSkuCode("aaaBBB(112)").isEmpty());
        assertFalse(catalogCodeUtil.isValidSkuCode("aaa-BBB112.aaa-BBB112.aaa-BBB112.aaa-BBB112.aaa-BBB112.aaa-BBB112.aaa-BBB112").isEmpty());
    }

    /**
     * Test method for 'com.elasticpath.commons.util.impl.CatalogCodeUtilImpl.isValidCatalogCode(String)'.
     */
    @Test
    @SuppressWarnings({"PMD.AvoidDuplicateLiterals"})
    public void testIsValidCatalogCode() {
        assertTrue(catalogCodeUtil.isValidCatalogCode("aaaBBB112").isEmpty());
        assertTrue(catalogCodeUtil.isValidCatalogCode("aaa_BBB112").isEmpty());

        assertFalse(catalogCodeUtil.isValidCatalogCode("aaa-BBB112").isEmpty());
        assertFalse(catalogCodeUtil.isValidCatalogCode("good.catalog.code").isEmpty());
        assertFalse(catalogCodeUtil.isValidCatalogCode(null).isEmpty());
        assertFalse(catalogCodeUtil.isValidCatalogCode("").isEmpty());
        assertFalse(catalogCodeUtil.isValidCatalogCode(" aaa BBB112").isEmpty());
        assertFalse(catalogCodeUtil.isValidCatalogCode("/aaaBBB112").isEmpty());
        assertFalse(catalogCodeUtil.isValidCatalogCode("#aaaBBB112").isEmpty());
        assertFalse(catalogCodeUtil.isValidCatalogCode("aaaBBB,112").isEmpty());
        assertFalse(catalogCodeUtil.isValidCatalogCode("aaaBBB(112)").isEmpty());
    }

    /**
     * Test method for 'com.elasticpath.commons.util.impl.CatalogCodeUtilImpl.isValidCategoryCode(String)'.
     */
    @Test
    @SuppressWarnings({"PMD.AvoidDuplicateLiterals"})
    public void testIsValidCategoryCode() {
        assertTrue(catalogCodeUtil.isValidCategoryCode("aaaBBB112").isEmpty());
        assertTrue(catalogCodeUtil.isValidCategoryCode("aaa_BBB112").isEmpty());

        assertFalse(catalogCodeUtil.isValidCategoryCode("aaa-BBB112").isEmpty());
        assertFalse(catalogCodeUtil.isValidCategoryCode("good.category.code").isEmpty());
        assertFalse(catalogCodeUtil.isValidCategoryCode(null).isEmpty());
        assertFalse(catalogCodeUtil.isValidCategoryCode("").isEmpty());
        assertFalse(catalogCodeUtil.isValidCategoryCode(" aaa BBB112").isEmpty());
        assertFalse(catalogCodeUtil.isValidCategoryCode("/aaaBBB112").isEmpty());
        assertFalse(catalogCodeUtil.isValidCategoryCode("#aaaBBB112").isEmpty());
        assertFalse(catalogCodeUtil.isValidCategoryCode("aaaBBB,112").isEmpty());
        assertFalse(catalogCodeUtil.isValidCategoryCode("aaaBBB(112)").isEmpty());
    }

}