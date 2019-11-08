/*
 * Copyright (c) Elastic Path Software Inc., 2016
 */

package com.elasticpath.test.integration.modifier;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;

import com.elasticpath.domain.modifier.ModifierField;
import com.elasticpath.domain.modifier.ModifierFieldLdf;
import com.elasticpath.domain.modifier.ModifierFieldOption;
import com.elasticpath.domain.modifier.ModifierFieldOptionLdf;
import com.elasticpath.domain.modifier.ModifierGroup;
import com.elasticpath.domain.modifier.ModifierGroupLdf;
import com.elasticpath.domain.modifier.ModifierType;
import com.elasticpath.domain.modifier.impl.ModifierFieldImpl;
import com.elasticpath.domain.modifier.impl.ModifierFieldLdfImpl;
import com.elasticpath.domain.modifier.impl.ModifierFieldOptionImpl;
import com.elasticpath.domain.modifier.impl.ModifierFieldOptionLdfImpl;
import com.elasticpath.domain.modifier.impl.ModifierGroupImpl;
import com.elasticpath.domain.modifier.impl.ModifierGroupLdfImpl;
import com.elasticpath.domain.catalog.Catalog;
import com.elasticpath.domain.catalog.ProductType;
import com.elasticpath.domain.catalog.impl.ProductTypeImpl;
import com.elasticpath.domain.factory.TestGuidUtility;
import com.elasticpath.domain.tax.TaxCode;
import com.elasticpath.service.modifier.ModifierService;
import com.elasticpath.service.catalog.ProductTypeService;
import com.elasticpath.test.db.DbTestCase;
import com.elasticpath.test.util.Utils;

/**
 * Test that the modifier is persisted correctly.
 */
public abstract class AbstractModifierTest extends DbTestCase {

	private static final int ZERO = 0;
	private static final int FIRST = 1;
	private static final int SECOND = 2;
	private static final int THIRD = 3;
	private static final int FORTH = 4;
	private static final int FIFTH = 5;

	@Autowired
	private ModifierService modifierService;

	@Autowired
	private ProductTypeService productTypeService;

	protected ProductType persistSimpleProductTypeWithModifierGroup() {
		Catalog catalog = createPersistedCatalog();
		TaxCode taxCode = createPersistedTaxCode("taxCode");
		final ProductType productType = createProductType(catalog, taxCode);

		// Create ModifierGroup
		ModifierGroup modifierGroup = new ModifierGroupImpl();
		modifierGroup.setCode(TestGuidUtility.getGuid());

		//Add ModifierGroupLdf to ModifierGroup
		ModifierGroupLdf modifierGroupLdf = createModifierGroupLdf(Locale.US, "ModifierGroupLdf1-ENGLISH");
		modifierGroup.addModifierGroupLdf(modifierGroupLdf);
		modifierGroupLdf = createModifierGroupLdf(Locale.FRENCH, "ModifierGroupLdf1-French");
		modifierGroup.addModifierGroupLdf(modifierGroupLdf);
		modifierGroupLdf = createModifierGroupLdf(Locale.GERMAN, "ModifierGroupLdf2-GERMAN");
		modifierGroup.addModifierGroupLdf(modifierGroupLdf);

		//Add ModifierField to ModifierGroup
		ModifierField modifierField = createModifierField("code1", ModifierType.SHORT_TEXT, false, 0);
		modifierGroup.addModifierField(modifierField);
		modifierField = createModifierField("code2", ModifierType.INTEGER, false, FIRST);
		modifierGroup.addModifierField(modifierField);
		modifierField = createModifierField("code3", ModifierType.BOOLEAN, false, SECOND);
		modifierGroup.addModifierField(modifierField);

		ModifierField modifierFieldWithOption;
		modifierFieldWithOption = createModifierFieldWithOption("code4",
				ModifierType.SHORT_TEXT,
				false, THIRD,
				"optionValue1");
		modifierGroup.addModifierField(modifierFieldWithOption);
		modifierFieldWithOption = createModifierFieldWithOption("code5", ModifierType.INTEGER, false, FORTH, "optionValue2");
		modifierGroup.addModifierField(modifierFieldWithOption);
		modifierFieldWithOption = createModifierFieldWithOption("code6", ModifierType.BOOLEAN, false, FIFTH, "optionValue3");
		modifierGroup.addModifierField(modifierFieldWithOption);

		// Persist ModifierGroup and dependent objects
		modifierGroup = modifierService.add(modifierGroup);

		//Add ModifierGroup to ExtProductType
		productType.getModifierGroups().add(modifierGroup);

		// Persist ExtProductType
		productTypeService.add(productType);

		return productType;
	}

	protected ModifierField createModifierFieldWithOption(final String code, final ModifierType attributeType,
			final boolean required,
			final int ordering, final String optionValue) {
		ModifierField modifierField = createModifierField(code, attributeType, required, ordering);

		ModifierFieldOption modifierFieldOption = createModifierFieldOption(optionValue + "1", ZERO);
		modifierField.addModifierFieldOption(modifierFieldOption);
		modifierFieldOption = createModifierFieldOption(optionValue + "2", FIRST);
		modifierField.addModifierFieldOption(modifierFieldOption);
		modifierFieldOption = createModifierFieldOption(optionValue + "3", SECOND);
		modifierField.addModifierFieldOption(modifierFieldOption);

		return modifierField;
	}

	protected ModifierFieldOption createModifierFieldOption(final String value, final int ordering) {
		ModifierFieldOption modifierFieldOption = new ModifierFieldOptionImpl();
		modifierFieldOption.setOrdering(ordering);
		modifierFieldOption.setValue(value);

		ModifierFieldOptionLdf modifierFieldOptionLdf = createModifierFieldOptionLdf(Locale.US, "displayName-ENGLISH");
		modifierFieldOption.addModifierFieldOptionLdf(modifierFieldOptionLdf);
		modifierFieldOptionLdf = createModifierFieldOptionLdf(Locale.FRENCH, "displayName-FRENCH");
		modifierFieldOption.addModifierFieldOptionLdf(modifierFieldOptionLdf);

		return modifierFieldOption;
	}

	protected ModifierFieldOptionLdf createModifierFieldOptionLdf(final Locale locale, final String displayName) {
		ModifierFieldOptionLdf modifierFieldOptionLdf = new ModifierFieldOptionLdfImpl();
		modifierFieldOptionLdf.setDisplayName(displayName);
		modifierFieldOptionLdf.setLocale(locale.toString());

		return modifierFieldOptionLdf;
	}

	protected ModifierField createModifierField(final String code, final ModifierType attributeType, final boolean required,
			final int ordering) {
		ModifierField modifierField = new ModifierFieldImpl();

		modifierField.setCode(code);
		modifierField.setFieldType(attributeType);
		modifierField.setOrdering(ordering);
		modifierField.setRequired(required);

		ModifierFieldLdf modifierFieldLdf;
		modifierFieldLdf = createModifierFieldLdf(Locale.US, "ModifierFieldLdf1-ENGLISH");
		modifierField.addModifierFieldLdf(modifierFieldLdf);
		modifierFieldLdf = createModifierFieldLdf(Locale.FRENCH, "ModifierFieldLdf1-FRENCH");
		modifierField.addModifierFieldLdf(modifierFieldLdf);
		modifierFieldLdf = createModifierFieldLdf(Locale.GERMAN, "ModifierFieldLdf21-GERMAN");
		modifierField.addModifierFieldLdf(modifierFieldLdf);

		return modifierField;
	}

	protected ModifierFieldLdf createModifierFieldLdf(final Locale locale, final String displayName) {
		ModifierFieldLdf modifierFieldLdf = new ModifierFieldLdfImpl();
		modifierFieldLdf.setLocale(locale.toString());
		modifierFieldLdf.setDisplayName(displayName);

		return modifierFieldLdf;
	}

	protected ModifierGroupLdf createModifierGroupLdf(final Locale locale, final String displayName) {
		ModifierGroupLdf modifierGroupLdf = new ModifierGroupLdfImpl();
		modifierGroupLdf.setDisplayName(displayName);
		modifierGroupLdf.setLocale(locale.toString());
		return modifierGroupLdf;
	}

	protected ProductType createProductType(final Catalog catalog, final TaxCode taxCode) {
		final ProductType productType = new ProductTypeImpl();
		productType.setName(Utils.uniqueCode("productName"));
		productType.initialize();
		productType.setCatalog(catalog);
		productType.setTaxCode(taxCode);

		return productType;
	}

}
