/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.update.processor.connectivity.impl.projection.converter.impl;

import static java.util.Locale.ENGLISH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.Sets;
import org.apache.commons.lang3.LocaleUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.catalog.entity.Field;
import com.elasticpath.catalog.entity.FieldOption;
import com.elasticpath.catalog.entity.fieldmetadata.FieldMetadata;
import com.elasticpath.catalog.entity.translation.FieldMetadataTranslation;
import com.elasticpath.catalog.entity.translation.Translation;
import com.elasticpath.catalog.extractor.CatalogTranslationExtractor;
import com.elasticpath.catalog.update.processor.connectivity.impl.projection.extractor.CatalogTranslationExtractorImpl;
import com.elasticpath.catalog.update.processor.connectivity.impl.projection.extractor.adapter.ModifierGroupLdfAdapter;
import com.elasticpath.domain.modifier.ModifierField;
import com.elasticpath.domain.modifier.ModifierFieldLdf;
import com.elasticpath.domain.modifier.ModifierFieldOption;
import com.elasticpath.domain.modifier.ModifierFieldOptionLdf;
import com.elasticpath.domain.modifier.ModifierGroup;
import com.elasticpath.domain.modifier.ModifierGroupLdf;
import com.elasticpath.domain.modifier.ModifierType;
import com.elasticpath.domain.modifier.impl.ModifierFieldImpl;
import com.elasticpath.domain.modifier.impl.ModifierFieldOptionImpl;
import com.elasticpath.domain.modifier.impl.ModifierGroupImpl;
import com.elasticpath.domain.catalog.Catalog;
import com.elasticpath.domain.catalog.impl.CatalogImpl;
import com.elasticpath.domain.store.Store;
import com.elasticpath.service.misc.TimeService;
import com.elasticpath.service.modifier.ModifierService;


/**
 * Tests {@link FieldMetadataToProjectionConverter}.
 */
@RunWith(MockitoJUnitRunner.class)
public class FieldMetadataToProjectionConverterTest {

	private static final int ORDERING_ONE = 1;
	private static final int ORDERING_TWO = 2;
	private static final int ORDERING_THREE = 3;
	private static final String GROUP_DISPLAY_NAME = "Maillot d'Equipe";
	private static final String NAME_FIELD = "NAME";
	private static final String NUMBER_FIELD = "NUMBER";
	private static final String FONT_FIELD = "FONT";
	private final CatalogTranslationExtractor extractor = mock(CatalogTranslationExtractor.class);
	private final Store store = mock(Store.class);

	@Mock
	private TimeService timeService;

	@Mock
	private ModifierService modifierService;


	/**
	 * Ensure that fields in projection are ordered based on the ModifierFieldField.ordering value.
	 */
	@Test
	public void ensureThatFieldsAreOrderedByOrderingValueFromModifierFieldTest() {
		final int seventhOrdering = 7;
		final int ninthOrdering = 9;

		ModifierField field1 = new ModifierFieldImpl();
		field1.setOrdering(ORDERING_TWO);
		field1.setCode("2");

		ModifierField field2 = new ModifierFieldImpl();
		field2.setOrdering(ORDERING_ONE);
		field2.setCode("1");

		ModifierField field3 = new ModifierFieldImpl();
		field3.setOrdering(ORDERING_THREE);
		field3.setCode("3");

		ModifierField field4 = new ModifierFieldImpl();
		field4.setOrdering(ninthOrdering);
		field4.setCode("9");

		ModifierField field5 = new ModifierFieldImpl();
		field5.setOrdering(seventhOrdering);
		field5.setCode("7");

		List<Field> fields = new FieldMetadataToProjectionConverter(extractor, timeService, modifierService)
				.makeSortedFields(Locale.getDefault(), Sets.newHashSet(field1, field2, field3, field4, field5), "en", store);

		assertThat(fields).extracting(Field::getName).containsExactly("1", "2", "3", "7", "9");
	}


	/**
	 * Ensure that fieldsValues in projection are ordered based on the ModifierFieldOption.ordering value.
	 */
	@Test
	public void ensureThatFieldsValuesAreOrderedByOrderingValueFromCartItemModifierOptionTest() {

		ModifierField field = new ModifierFieldImpl();
		field.setFieldType(ModifierType.valueOf(ModifierType.PICK_SINGLE_OPTION_ORDINAL));
		field.setCode("code");

		ModifierFieldOption option1 = new ModifierFieldOptionImpl();
		option1.setOrdering(ORDERING_TWO);
		option1.setValue("2");

		ModifierFieldOption option2 = new ModifierFieldOptionImpl();
		option2.setOrdering(ORDERING_ONE);
		option2.setValue("1");

		ModifierFieldOption option3 = new ModifierFieldOptionImpl();
		option3.setOrdering(ORDERING_THREE);
		option3.setValue("3");

		field.addModifierFieldOption(option1);
		field.addModifierFieldOption(option2);
		field.addModifierFieldOption(option3);

		List<FieldOption> fields = new FieldMetadataToProjectionConverter(extractor, timeService, modifierService)
				.extractOptions(Locale.getDefault(), field,
				"en", store);

		assertThat(fields).extracting(FieldOption::getValue).containsExactly("1", "2", "3");
	}

	/**
	 * Given ModifierGroup with filled Set of ModifierGroupLdf, Set of ModifierField.
	 * Given Store with filled storeCode, defaultLocale, supportedLocales fields.
	 * Then method "convert" converts ModifierGroup to FieldMetadata.
	 */
	@Test
	public void testThatConverterGetModifierGroupWithFilledFieldsAndConvertItToFieldMetadata() {
		final ModifierGroup cartItemModifierGroup = mock(ModifierGroup.class);
		Catalog catalog = new CatalogImpl();
		catalog.setDefaultLocale(LocaleUtils.toLocale("fr"));
		when(modifierService.findCatalogForModifierGroup(cartItemModifierGroup)).thenReturn(catalog);
		final Set<ModifierGroupLdf> cartItemModifierGroupLdfSet = getMockModifierGroupLdf();
		final Set<ModifierField> cartItemModifierFieldsSet = getMockModifierFieldSet();

		when(store.getCode()).thenReturn("storeCode");
		when(store.getDefaultLocale()).thenReturn(LocaleUtils.toLocale("en"));
		when(store.getSupportedLocales()).thenReturn(Arrays.asList(LocaleUtils.toLocale("en"), LocaleUtils.toLocale("fr"), LocaleUtils.toLocale(
				"fr_CA")));
		when(timeService.getCurrentTime()).thenReturn(new Date());

		when(cartItemModifierGroup.getCode()).thenReturn("TEAM_JERSEY");
		when(cartItemModifierGroup.getModifierGroupLdf()).thenReturn(cartItemModifierGroupLdfSet);
		when(cartItemModifierGroup.getModifierFields()).thenReturn(cartItemModifierFieldsSet);

		final CatalogTranslationExtractor projectionExtractor = new CatalogTranslationExtractorImpl();
		final FieldMetadataToProjectionConverter converter =
				new FieldMetadataToProjectionConverter(projectionExtractor, timeService, modifierService);

		final FieldMetadata fieldMetadata = converter.convert(cartItemModifierGroup, store, mock(Catalog.class));
		List<FieldMetadataTranslation> fieldMetadataTranslations = fieldMetadata.getTranslations();

		assertThat(fieldMetadata.getIdentity().getCode()).isEqualTo(cartItemModifierGroup.getCode());
		assertThat(fieldMetadataTranslations)
				.extracting(Translation::getDisplayName)
				.containsAnyOf(GROUP_DISPLAY_NAME);
		assertThat(fieldMetadataTranslations)
				.extracting(FieldMetadataTranslation::getFields)
				.flatExtracting(fields -> fields)
				.extracting(Field::getName)
				.containsAnyOf(NAME_FIELD, NUMBER_FIELD, FONT_FIELD);

	}

	@Test
	public void fieldsShouldNotContainsNullDisplayNameWhenModifierGroupLdfAndModifierFieldLdfHaveDifferentLocales() {
		final ModifierGroupLdf cartItemModifierGroupLdf = mock(ModifierGroupLdf.class);
		when(cartItemModifierGroupLdf.getLocale()).thenReturn("en");
		when(cartItemModifierGroupLdf.getDisplayName()).thenReturn("cartItemModifierGroupLdf");

		final ModifierFieldLdf cartItemModifierFieldLdf = mock(ModifierFieldLdf.class);
		when(cartItemModifierFieldLdf.getLocale()).thenReturn("fr");
		when(cartItemModifierFieldLdf.getDisplayName()).thenReturn("cartItemModifierFieldLdf");

		final ModifierField cartItemModifierField = new ModifierFieldImpl();
		cartItemModifierField.setCode("code");
		cartItemModifierField.addModifierFieldLdf(cartItemModifierFieldLdf);

		final ModifierGroupImpl cartItemModifierGroup = new ModifierGroupImpl();
		Catalog catalog = new CatalogImpl();
		catalog.setDefaultLocale(Locale.getDefault());
		cartItemModifierGroup.addModifierGroupLdf(cartItemModifierGroupLdf);
		cartItemModifierGroup.addModifierField(cartItemModifierField);

		final Store store = mock(Store.class);
		when(store.getDefaultLocale()).thenReturn(ENGLISH);
		when(store.getSupportedLocales()).thenReturn(Collections.singletonList(ENGLISH));

		when(timeService.getCurrentTime()).thenReturn(new Date());
		when(modifierService.findCatalogForModifierGroup(cartItemModifierGroup)).thenReturn(catalog);

		when(extractor.getProjectionTranslations(eq(ENGLISH), anyCollection(), any(ModifierGroupLdfAdapter.class)))
				.thenReturn(Collections.singletonList(new Translation("en", "cartItemModifierGroupLdf")));
		FieldMetadataToProjectionConverter converter =
				new FieldMetadataToProjectionConverter(extractor, timeService, modifierService);

		FieldMetadata fieldMetadata = converter.convert(cartItemModifierGroup, store, mock(Catalog.class));

		List<Field> fields =
				fieldMetadata.getTranslations().stream().flatMap(translation -> translation.getFields().stream()).collect(Collectors.toList());

		assertThat(fields).extracting(Field::getDisplayName).doesNotContainNull();
	}

	private Set<ModifierGroupLdf> getMockModifierGroupLdf() {
		final ModifierGroupLdf cartItemModifierGroupLdfMelliot = mock(ModifierGroupLdf.class);

		when(cartItemModifierGroupLdfMelliot.getDisplayName()).thenReturn(GROUP_DISPLAY_NAME);
		when(cartItemModifierGroupLdfMelliot.getLocale()).thenReturn("fr");

		return new HashSet<>(Collections.singletonList(cartItemModifierGroupLdfMelliot));
	}

	private Set<ModifierField> getMockModifierFieldSet() {
		final ModifierField cartItemModifierFieldName = createModifierField("ShortText", NAME_FIELD,
				"Nom", true, 20);
		final ModifierField cartItemModifierFieldNumber = createModifierField("Integer", NUMBER_FIELD,
				"Numero", true, 99);

		final ModifierField cartItemModifierFieldFont = createModifierFieldWithOptions("PickSingleOption", FONT_FIELD,
				"Police de caractère", false);

		return new HashSet<>(Arrays.asList(cartItemModifierFieldName, cartItemModifierFieldNumber, cartItemModifierFieldFont));
	}

	private ModifierField createModifierField(final String type, final String name, final String displayName,
															  final boolean isRequired,
															  final Integer maxSize) {
		final ModifierField cartItemModifierField = getModifierFieldWithoutSize(type, name, displayName, isRequired);
		when(cartItemModifierField.getMaxSize()).thenReturn(maxSize);

		return cartItemModifierField;
	}

	private ModifierField createModifierFieldWithOptions(final String type, final String name, final String displayName,
																		 final boolean isRequired) {
		final ModifierField cartItemModifierField = getModifierFieldWithoutSize(type, name, displayName, isRequired);
		final ModifierFieldOption cartItemModifierFieldOption = mock(ModifierFieldOption.class);
		final ModifierFieldOptionLdf cartItemModifierFieldOptionLdfCollegiateFr = getModifierFieldOptionLdf("Collégial", "fr");
		final ModifierFieldOptionLdf cartItemModifierFieldOptionLdfCollegiateEn = getModifierFieldOptionLdf("Collegial", "en");

		final Set<ModifierFieldOptionLdf> cartItemModifierFieldOptionLdfs =
				new HashSet<>(Arrays.asList(cartItemModifierFieldOptionLdfCollegiateFr, cartItemModifierFieldOptionLdfCollegiateEn));

		when(cartItemModifierFieldOption.getValue()).thenReturn("COLLEGIATE");
		when(cartItemModifierFieldOption.getModifierFieldOptionsLdf()).thenReturn(cartItemModifierFieldOptionLdfs);

		final Set<ModifierFieldOption> cartItemModifierFieldOptions = new HashSet<>(Collections.singletonList(cartItemModifierFieldOption));

		when(cartItemModifierField.getModifierFieldOptions()).thenReturn(cartItemModifierFieldOptions);

		return cartItemModifierField;
	}

	private ModifierFieldOptionLdf getModifierFieldOptionLdf(final String displayName, final String language) {
		final ModifierFieldOptionLdf cartItemModifierFieldOptionLdfCollegiate = mock(ModifierFieldOptionLdf.class);

		when(cartItemModifierFieldOptionLdfCollegiate.getLocale()).thenReturn(language);
		when(cartItemModifierFieldOptionLdfCollegiate.getDisplayName()).thenReturn(displayName);
		return cartItemModifierFieldOptionLdfCollegiate;
	}

	private ModifierField getModifierFieldWithoutSize(final String type, final String name, final String displayName,
																	  final boolean isRequired) {
		final ModifierField cartItemModifierFieldName = mock(ModifierField.class);
		final ModifierType shortTextType = mock(ModifierType.class);
		final ModifierFieldLdf cartItemModifierFieldLdfFr = mock(ModifierFieldLdf.class);

		when(cartItemModifierFieldLdfFr.getLocale()).thenReturn("fr");
		when(cartItemModifierFieldLdfFr.getDisplayName()).thenReturn(displayName);

		final ModifierFieldLdf cartItemModifierFieldLdfEn = mock(ModifierFieldLdf.class);

		when(cartItemModifierFieldLdfEn.getLocale()).thenReturn("en");
		when(cartItemModifierFieldLdfEn.getDisplayName()).thenReturn(displayName + "_En");

		when(cartItemModifierFieldName.getModifierFieldsLdf()).thenReturn(new HashSet<>(Arrays.asList(cartItemModifierFieldLdfFr,
				cartItemModifierFieldLdfEn)));

		when(shortTextType.getCamelName()).thenReturn(type);

		when(cartItemModifierFieldName.getFieldType()).thenReturn(shortTextType);
		when(cartItemModifierFieldName.getCode()).thenReturn(name);
		when(cartItemModifierFieldName.isRequired()).thenReturn(isRequired);

		return cartItemModifierFieldName;
	}

}

