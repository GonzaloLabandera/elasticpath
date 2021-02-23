/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.commons.util.impl;

import static com.elasticpath.commons.enums.InvalidCatalogCodeMessageImpl.INVALID_BRAND_CODE_MESSAGE;
import static com.elasticpath.commons.enums.InvalidCatalogCodeMessageImpl.INVALID_CATALOG_CODE_MESSAGE;
import static com.elasticpath.commons.enums.InvalidCatalogCodeMessageImpl.INVALID_CATEGORY_CODE_MESSAGE;
import static com.elasticpath.commons.enums.InvalidCatalogCodeMessageImpl.INVALID_PRODUCT_CODE_MESSAGE;
import static com.elasticpath.commons.enums.InvalidCatalogCodeMessageImpl.INVALID_SKU_CODE_MESSAGE;
import static com.elasticpath.commons.enums.InvalidCatalogCodeMessageImpl.MAX_LENGTH_CODE_MESSAGE;
import static com.elasticpath.commons.enums.InvalidCatalogCodeMessageImpl.NO_SPACES_CODE_MESSAGE;
import static com.elasticpath.commons.enums.InvalidCatalogCodeMessageImpl.VALUE_REQUIRED_CODE_MESSAGE;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.elasticpath.commons.enums.InvalidCatalogCodeMessage;
import com.elasticpath.commons.util.CatalogCodeFormat;
import com.elasticpath.commons.util.CatalogCodeUtil;


/**
 * The default implementation of <code>CatalogCodeUtil</code>.
 */
public class CatalogCodeUtilImpl implements CatalogCodeUtil {

    private static final int DEFAULT_CATALOG_LENGTH = 64;

    private static final String REG_EXPRESSION_CATALOG_CODE = "^[\\p{Alnum}_]+$";
    private static final String REG_EXPRESSION_CATEGORY_CODE = "^[\\p{Alnum}_]+$";
    private static final String REG_EXPRESSION_PRODUCT_CODE = "^[\\p{Alnum}-_.]+$";
    private static final String REG_EXPRESSION_SKU_CODE = "^[\\p{Alnum}-_.]+$";
    private static final String REG_EXPRESSION_BRAND_CODE = "^[\\p{Alnum}_]+$";

    private static final CatalogCodeFormat CATALOG_CODE_FORMAT = new CatalogCodeFormatImpl
            .Builder(REG_EXPRESSION_CATALOG_CODE, INVALID_CATALOG_CODE_MESSAGE)
            .setMaxLength(DEFAULT_CATALOG_LENGTH)
            .setSpacesAllowed(false)
            .build();

    private static final CatalogCodeFormat CATEGORY_CODE_FORMAT = new CatalogCodeFormatImpl
            .Builder(REG_EXPRESSION_CATEGORY_CODE, INVALID_CATEGORY_CODE_MESSAGE)
            .setMaxLength(DEFAULT_CATALOG_LENGTH)
            .setSpacesAllowed(false)
            .build();

    private static final CatalogCodeFormat PRODUCT_CODE_FORMAT = new CatalogCodeFormatImpl
            .Builder(REG_EXPRESSION_PRODUCT_CODE, INVALID_PRODUCT_CODE_MESSAGE)
            .setMaxLength(DEFAULT_CATALOG_LENGTH)
            .setSpacesAllowed(false)
            .build();

    private static final CatalogCodeFormat SKU_CODE_FORMAT = new CatalogCodeFormatImpl
            .Builder(REG_EXPRESSION_SKU_CODE, INVALID_SKU_CODE_MESSAGE)
            .setMaxLength(DEFAULT_CATALOG_LENGTH)
            .setSpacesAllowed(false)
            .build();

    private static final CatalogCodeFormat BRAND_CODE_FORMAT = new CatalogCodeFormatImpl
            .Builder(REG_EXPRESSION_BRAND_CODE, INVALID_BRAND_CODE_MESSAGE)
            .setMaxLength(DEFAULT_CATALOG_LENGTH)
            .setSpacesAllowed(false)
            .build();


    @Override
    public CatalogCodeFormat getCatalogCodeFormat() {
        return CATALOG_CODE_FORMAT;
    }

    @Override
    public CatalogCodeFormat getCategoryCodeFormat() {
        return CATEGORY_CODE_FORMAT;
    }

    @Override
    public CatalogCodeFormat getProductCodeFormat() {
        return PRODUCT_CODE_FORMAT;
    }

    @Override
    public CatalogCodeFormat getSkuCodeFormat() {
        return SKU_CODE_FORMAT;
    }

    @Override
    public CatalogCodeFormat getBrandCodeFormat() {
        return BRAND_CODE_FORMAT;
    }

    @Override
    public List<InvalidCatalogCodeMessage> isValidCatalogCode(final String code) {
        return validateCode(code, getCatalogCodeFormat());
    }

    @Override
    public List<InvalidCatalogCodeMessage> isValidCategoryCode(final String code) {
        return validateCode(code, getCategoryCodeFormat());
    }

    @Override
    public List<InvalidCatalogCodeMessage> isValidProductCode(final String code) {
        return validateCode(code, getProductCodeFormat());
    }

    @Override
    public List<InvalidCatalogCodeMessage> isValidSkuCode(final String code) {
        return validateCode(code, getSkuCodeFormat());
    }

    @Override
    public List<InvalidCatalogCodeMessage> isValidBrandCode(final String code) {
        return validateCode(code, getBrandCodeFormat());
    }

    private List<InvalidCatalogCodeMessage> validateCode(final String code, final CatalogCodeFormat catalogCodeFormat) {
        List<InvalidCatalogCodeMessage> messages = new ArrayList<>();
        if (StringUtils.isBlank(code)) {
            messages.add(VALUE_REQUIRED_CODE_MESSAGE);
            return messages;
        }
        if (code.length() > catalogCodeFormat.getMaxLength()) {
            InvalidCatalogCodeMessage message = MAX_LENGTH_CODE_MESSAGE;
            message.addParameter(String.valueOf(catalogCodeFormat.getMaxLength()));
            messages.add(message);
        }
        if (!catalogCodeFormat.isSpacesAllowed() && code.contains(" ")) {
            messages.add(NO_SPACES_CODE_MESSAGE);
        }
        if (!code.matches(catalogCodeFormat.getRegex())) {
            messages.add(catalogCodeFormat.getInvalidCatalogCodeMessage());
        }
        return messages;
    }

}
