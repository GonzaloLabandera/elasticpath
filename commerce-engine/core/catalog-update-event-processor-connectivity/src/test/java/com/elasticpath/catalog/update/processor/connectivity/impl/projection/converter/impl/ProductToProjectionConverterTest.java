/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.update.processor.connectivity.impl.projection.converter.impl;

import static com.elasticpath.catalog.entity.constants.OfferPropertiesNames.PROPERTY_BUNDLE_PRICING;
import static com.elasticpath.catalog.entity.constants.OfferPropertiesNames.PROPERTY_ITEM_TYPE;
import static com.elasticpath.catalog.entity.constants.OfferPropertiesNames.PROPERTY_MINIMUM_ORDER_QUANTITY;
import static com.elasticpath.catalog.entity.constants.OfferPropertiesNames.PROPERTY_NOT_SOLD_SEPARATELY;
import static com.elasticpath.catalog.entity.constants.OfferPropertiesNames.PROPERTY_OFFER_TYPE;
import static com.elasticpath.catalog.entity.constants.OfferPropertiesNames.PROPERTY_TAX_CODE;
import static com.elasticpath.catalog.entity.constants.ProjectionIdentityTypeNames.OFFER_IDENTITY_TYPE;
import static com.elasticpath.domain.catalog.ProductAssociationType.ACCESSORY;
import static com.elasticpath.domain.catalog.ProductAssociationType.CROSS_SELL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.catalog.entity.AvailabilityRules;
import com.elasticpath.catalog.entity.NameIdentity;
import com.elasticpath.catalog.entity.Property;
import com.elasticpath.catalog.entity.TranslatedName;
import com.elasticpath.catalog.entity.attribute.Attribute;
import com.elasticpath.catalog.entity.attribute.AttributeReaderCapability;
import com.elasticpath.catalog.entity.brand.Brand;
import com.elasticpath.catalog.entity.brand.BrandReaderCapability;
import com.elasticpath.catalog.entity.category.Category;
import com.elasticpath.catalog.entity.category.CategoryReaderCapability;
import com.elasticpath.catalog.entity.offer.Association;
import com.elasticpath.catalog.entity.offer.AssociationValue;
import com.elasticpath.catalog.entity.offer.BundlePricing;
import com.elasticpath.catalog.entity.offer.Item;
import com.elasticpath.catalog.entity.offer.ItemType;
import com.elasticpath.catalog.entity.offer.Offer;
import com.elasticpath.catalog.entity.offer.OfferCategories;
import com.elasticpath.catalog.entity.offer.SelectionType;
import com.elasticpath.catalog.entity.option.Option;
import com.elasticpath.catalog.entity.option.OptionReaderCapability;
import com.elasticpath.catalog.entity.translation.AttributeTranslation;
import com.elasticpath.catalog.entity.translation.OfferTranslation;
import com.elasticpath.catalog.entity.translation.OptionTranslation;
import com.elasticpath.catalog.entity.translation.Translation;
import com.elasticpath.catalog.entity.translation.TranslationUnit;
import com.elasticpath.catalog.extractor.CatalogTranslationExtractor;
import com.elasticpath.catalog.spi.CatalogProjectionPlugin;
import com.elasticpath.catalog.spi.CatalogProjectionPluginProvider;
import com.elasticpath.commons.constants.ImportConstants;
import com.elasticpath.domain.attribute.AttributeMultiValueType;
import com.elasticpath.domain.attribute.AttributeType;
import com.elasticpath.domain.attribute.AttributeValue;
import com.elasticpath.domain.attribute.AttributeValueGroup;
import com.elasticpath.domain.modifier.ModifierGroup;
import com.elasticpath.domain.catalog.AvailabilityCriteria;
import com.elasticpath.domain.catalog.BundleConstituent;
import com.elasticpath.domain.catalog.Catalog;
import com.elasticpath.domain.catalog.ConstituentItem;
import com.elasticpath.domain.catalog.LocaleDependantFields;
import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.catalog.ProductAssociation;
import com.elasticpath.domain.catalog.ProductAssociationType;
import com.elasticpath.domain.catalog.ProductBundle;
import com.elasticpath.domain.catalog.ProductCategory;
import com.elasticpath.domain.catalog.ProductCharacteristics;
import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.domain.catalog.ProductType;
import com.elasticpath.domain.catalog.SelectionRule;
import com.elasticpath.domain.catalog.impl.CategoryImpl;
import com.elasticpath.domain.catalog.impl.ProductBundleImpl;
import com.elasticpath.domain.catalog.impl.ProductImpl;
import com.elasticpath.domain.catalog.impl.ProductTypeImpl;
import com.elasticpath.domain.catalog.impl.SelectionRuleImpl;
import com.elasticpath.domain.catalogview.StoreProduct;
import com.elasticpath.domain.catalogview.StoreProductSku;
import com.elasticpath.domain.catalogview.impl.StoreProductImpl;
import com.elasticpath.domain.skuconfiguration.SkuOption;
import com.elasticpath.domain.skuconfiguration.SkuOptionValue;
import com.elasticpath.domain.store.Store;
import com.elasticpath.domain.tax.TaxCode;
import com.elasticpath.service.catalog.BundleIdentifier;
import com.elasticpath.service.catalog.ProductAssociationService;
import com.elasticpath.service.catalog.ProductCharacteristicsService;
import com.elasticpath.service.catalogview.StoreProductService;
import com.elasticpath.service.misc.TimeService;
import com.elasticpath.service.tax.TaxCodeRetriever;
import com.elasticpath.settings.SettingsReader;
import com.elasticpath.settings.domain.SettingValue;

/**
 * Test for {@link SkuOptionToProjectionConverter}.
 */
@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings({"PMD.TooManyMethods", "PMD.ExcessiveImports", "PMD.ExcessiveClassLength", "PMD.ExcessiveClassLength", "PMD"
		+ ".CouplingBetweenObjects"})
public class ProductToProjectionConverterTest {

	private static final String PRODUCT_SKU_CODE = "productSkuCode";
	private static final String CATEGORY_CODE = "categoryCode";
	private static final String STORE_CODE = "storeCode";
	private static final String STORE_PRODUCT_CODE = "storeProductCode";
	private static final String PRODUCT_TYPE = "ProductType";
	private static final boolean NOT_SOLD_SEPARATELY_VALUE = true;
	private static final int MIN_ORDER_QTY = 3;
	private static final String TAX_CODE = "TaxCode";
	private static final String ALWAYS = "ALWAYS";
	private static final String HAS_STOCK = "HAS_STOCK";
	private static final String PRE_ORDER = "PRE_ORDER";
	private static final String BACK_ORDER = "BACK_ORDER";
	private static final String CATALOG = "catalog";
	private static final String PRODUCT_1 = "product1";
	private static final String PRODUCT_2 = "product2";
	private static final String PRODUCT_3 = "product3";
	private static final String ATTRIBUTE_KEY = "attributeKey";
	private static final String ATTRIBUTE_SKU_KEY = "attributeSkuKey";
	private static final String OPTION_KEY = "optionKey";
	private static final String BRAND_CODE = "brandCode";
	private static final ZonedDateTime CURRENT_DATE = ZonedDateTime.now();
	private static final Integer FEATURED_VALUE = 1;
	private static final String DETAILS_VALUE = "details_value";
	private static final String DETAILS_VALUE_WITH_COMMA = "details, with, comma";
	private static final String DETAILS_MULTI_VALUE = DETAILS_VALUE + ImportConstants.SHORT_TEXT_MULTI_VALUE_SEPARATOR_CHAR + "\""
			+ DETAILS_VALUE_WITH_COMMA + "\"";
	private static final String DETAILS_ITEM_VALUE = "details_item_value";
	private static Optional<AttributeReaderCapability> attributeReaderCapability = Optional.ofNullable(mock(AttributeReaderCapability.class));
	private static Optional<OptionReaderCapability> optionReaderCapability = Optional.ofNullable(mock(OptionReaderCapability.class));
	private static Optional<BrandReaderCapability> brandReaderCapability = Optional.ofNullable(mock(BrandReaderCapability.class));
	private static Optional<CategoryReaderCapability> categoryReaderCapability = Optional.ofNullable(mock(CategoryReaderCapability.class));
	private static final int BUNDLE_CONSISTENT_QUANTITY = 5;

	@Mock
	private TimeService timeService;
	@Mock
	private StoreProductService storeProductService;

	@Mock
	private ProductCharacteristicsService productCharacteristicsService;

	@Mock
	private TaxCodeRetriever taxCodeRetriever;

	@Mock
	private ProductAssociationService productAssociationService;

	@Mock
	private BundleIdentifier bundleIdentifier;

	@Mock
	private SettingsReader settingsReader;

	@Mock
	private CatalogTranslationExtractor translationExtractor;

	@Mock
	private CatalogProjectionPluginProvider provider;

	@Mock
	private CatalogProjectionPlugin plugin;

	@Before
	public void setup() {
		final ProductCharacteristics characteristics = mock(ProductCharacteristics.class);
		when(characteristics.isBundle()).thenReturn(false);
		when(timeService.getCurrentTime()).thenReturn(new Date());
		when(productCharacteristicsService.getProductCharacteristics(any(Product.class))).thenReturn(characteristics);
		when(productCharacteristicsService.getProductCharacteristics(any(ProductSku.class))).thenReturn(characteristics);
		when(taxCodeRetriever.getEffectiveTaxCode(any(ProductSku.class))).thenReturn(mock(TaxCode.class));
		when(provider.getCatalogProjectionPlugin()).thenReturn(plugin);
		when(plugin.getReaderCapability(AttributeReaderCapability.class)).thenReturn(attributeReaderCapability);
		when(plugin.getReaderCapability(OptionReaderCapability.class)).thenReturn(optionReaderCapability);
		when(plugin.getReaderCapability(BrandReaderCapability.class)).thenReturn(brandReaderCapability);
		when(plugin.getReaderCapability(CategoryReaderCapability.class)).thenReturn(categoryReaderCapability);
	}

	@Test
	public void testThatConverterGetStoreProductAndStoreWithFilledFieldAndConvertItToOfferProjectionWithBundlePricingProperty() {
		final Optional<StoreProductSku> storeProductSku = mockStoreProductSku();
		final StoreProduct storeProduct = mockStoreProduct(storeProductSku.get());
		final Store store = mockStore();
		final ProductCharacteristics productCharacteristics = mock(ProductCharacteristics.class);
		final TaxCode taxCode = mockTaxCode();

		when(productCharacteristics.isBundle()).thenReturn(true);
		when(productCharacteristics.isCalculatedBundle()).thenReturn(true);
		when(storeProductService.getProductForStore(storeProduct, store)).thenReturn(storeProduct);
		when(productCharacteristicsService.getProductCharacteristics(storeProduct)).thenReturn(productCharacteristics);
		when(taxCodeRetriever.getEffectiveTaxCode(storeProductSku.get())).thenReturn(taxCode);
		when(storeProductSku.get().canSyndicate()).thenReturn(true);
		when(storeProductService.getProductSkuForStore(storeProductSku.get(), store)).thenReturn(storeProductSku);
		when(bundleIdentifier.asProductBundle(storeProduct)).thenReturn(new ProductBundleImpl());

		final ProductToProjectionConverter converter = new ProductToProjectionConverter(storeProductService, timeService,
				productCharacteristicsService, taxCodeRetriever, productAssociationService, bundleIdentifier, settingsReader,
				translationExtractor, provider);

		final Offer offer = converter.convert(storeProduct, store, mockCatalog());

		assertThat(offer.getIdentity().getType()).isEqualTo(OFFER_IDENTITY_TYPE);
		assertThat(offer.getIdentity().getCode()).isEqualTo(STORE_PRODUCT_CODE);
		assertThat(offer.getIdentity().getStore()).isEqualTo(STORE_CODE);
		assertThat(offer.getModifiedDateTime()).isNotNull();
		assertThat(offer.getItems()).extracting(Item::getItemCode).containsExactly(PRODUCT_SKU_CODE);
		assertThat(offer.getProperties()).extracting(Property::getName)
				.containsExactly(PROPERTY_OFFER_TYPE, PROPERTY_NOT_SOLD_SEPARATELY, PROPERTY_MINIMUM_ORDER_QUANTITY, PROPERTY_BUNDLE_PRICING);
		assertThat(offer.getProperties()).extracting(Property::getValue)
				.containsExactly(PRODUCT_TYPE, String.valueOf(NOT_SOLD_SEPARATELY_VALUE), String.valueOf(MIN_ORDER_QTY),
						BundlePricing.CALCULATED.toString());
	}

	@Test
	public void testThatConverterGetStoreProductAndStoreWithFilledFieldAndConvertItToOfferProjectionWithoutBundlePricingProperty() {
		final ProductSku productSku = mockProductSku();
		final StoreProduct storeProduct = mockStoreProduct(productSku);
		final Store store = mockStore();

		when(storeProductService.getProductForStore(storeProduct, store)).thenReturn(storeProduct);
		final ProductToProjectionConverter converter = new ProductToProjectionConverter(storeProductService, timeService,
				productCharacteristicsService, taxCodeRetriever, productAssociationService, bundleIdentifier, settingsReader,
				translationExtractor, provider);

		final Offer offer = converter.convert(storeProduct, store, mockCatalog());

		assertThat(offer.getIdentity().getType()).isEqualTo(OFFER_IDENTITY_TYPE);
		assertThat(offer.getIdentity().getCode()).isEqualTo(STORE_PRODUCT_CODE);
		assertThat(offer.getIdentity().getStore()).isEqualTo(STORE_CODE);
		assertThat(offer.getModifiedDateTime()).isNotNull();
		assertThat(offer.getProperties()).extracting(Property::getName)
				.containsExactly(PROPERTY_OFFER_TYPE, PROPERTY_NOT_SOLD_SEPARATELY, PROPERTY_MINIMUM_ORDER_QUANTITY);
		assertThat(offer.getProperties()).extracting(Property::getValue)
				.containsExactly(PRODUCT_TYPE, String.valueOf(NOT_SOLD_SEPARATELY_VALUE), String.valueOf(MIN_ORDER_QTY));

	}

	@Test
	public void testThatConverterGetStoreProductAndStoreWithFilledFieldAndConvertItToOfferProjection() {
		final Optional<StoreProductSku> storeProductSku = mockStoreProductSku();
		final StoreProduct storeProduct = mockStoreProduct(storeProductSku.get());
		final Store store = mockStore();
		when(storeProductService.getProductForStore(storeProduct, store)).thenReturn(storeProduct);
		final TaxCode taxCode = mockTaxCode();
		when(taxCodeRetriever.getEffectiveTaxCode(storeProductSku.get())).thenReturn(taxCode);
		when(storeProductSku.get().canSyndicate()).thenReturn(true);
		when(storeProductService.getProductSkuForStore(storeProductSku.get(), store)).thenReturn(storeProductSku);

		final ProductToProjectionConverter converter = new ProductToProjectionConverter(storeProductService, timeService,
				productCharacteristicsService, taxCodeRetriever, productAssociationService, bundleIdentifier, settingsReader,
				translationExtractor, provider);

		final Offer offer = converter.convert(storeProduct, store, mockCatalog());

		assertThat(offer.getIdentity().getType()).isEqualTo(OFFER_IDENTITY_TYPE);
		assertThat(offer.getIdentity().getCode()).isEqualTo(STORE_PRODUCT_CODE);
		assertThat(offer.getIdentity().getStore()).isEqualTo(STORE_CODE);
		assertThat(offer.getModifiedDateTime()).isNotNull();
		assertThat(offer.getItems()).extracting(Item::getItemCode).containsExactly(PRODUCT_SKU_CODE);
		assertThat(offer.getItems()).flatExtracting(Item::getProperties)
				.extracting(Property::getName)
				.containsExactly(PROPERTY_ITEM_TYPE, PROPERTY_TAX_CODE);
		assertThat(offer.getItems()).flatExtracting(Item::getProperties)
				.extracting(Property::getValue)
				.containsExactly(ItemType.DIGITAL.toString(), TAX_CODE);
		assertThat(offer.getProperties()).extracting(Property::getName)
				.containsExactly(PROPERTY_OFFER_TYPE, PROPERTY_NOT_SOLD_SEPARATELY, PROPERTY_MINIMUM_ORDER_QUANTITY);
		assertThat(offer.getProperties()).extracting(Property::getValue)
				.containsExactly(PRODUCT_TYPE, String.valueOf(NOT_SOLD_SEPARATELY_VALUE), String.valueOf(MIN_ORDER_QTY));

	}

	@Test
	public void convertShouldCallGetCurrentTimeProductCharacteristicsService() {
		final int getProductCharacteristicsCalls = 3;

		final Optional<StoreProductSku> storeProductSku = mockStoreProductSku();
		final StoreProduct storeProduct = mockStoreProduct(storeProductSku.get());
		final Store store = mockStore();
		final ProductCharacteristics productCharacteristics = mock(ProductCharacteristics.class);
		final TaxCode taxCode = mockTaxCode();

		when(storeProductService.getProductForStore(storeProduct, store)).thenReturn(storeProduct);
		when(storeProductService.getProductSkuForStore(storeProductSku.get(), store)).thenReturn(storeProductSku);
		when(productCharacteristics.isBundle()).thenReturn(true);
		when(productCharacteristics.isCalculatedBundle()).thenReturn(true);
		when(storeProductSku.get().canSyndicate()).thenReturn(true);
		when(productCharacteristicsService.getProductCharacteristics(storeProduct)).thenReturn(productCharacteristics);
		when(taxCodeRetriever.getEffectiveTaxCode(storeProductSku.get())).thenReturn(taxCode);
		when(bundleIdentifier.asProductBundle(storeProduct)).thenReturn(new ProductBundleImpl());

		final ProductToProjectionConverter converter = new ProductToProjectionConverter(storeProductService, timeService,
				productCharacteristicsService, taxCodeRetriever, productAssociationService, bundleIdentifier, settingsReader,
				translationExtractor, provider);

		converter.convert(storeProduct, store, mockCatalog());

		verify(timeService).getCurrentTime();
		verify(productCharacteristicsService, times(getProductCharacteristicsCalls)).getProductCharacteristics(storeProduct);
	}

	@Test
	public void syndicatedProductShouldBeConvertedToNotDeletedOffer() {
		final StoreProduct storeProduct = mockStoreProduct();
		final Store store = mockStore();
		when(storeProductService.getProductForStore(storeProduct, store)).thenReturn(storeProduct);
		final ProductCharacteristics productCharacteristics = mock(ProductCharacteristics.class);
		when(productCharacteristics.isBundle()).thenReturn(true);
		when(productCharacteristicsService.getProductCharacteristics(storeProduct)).thenReturn(productCharacteristics);
		when(storeProductService.getProductForStore(storeProduct, store)).thenReturn(storeProduct);
		when(bundleIdentifier.asProductBundle(storeProduct)).thenReturn(new ProductBundleImpl());
		final ProductToProjectionConverter converter = new ProductToProjectionConverter(storeProductService, timeService,
				productCharacteristicsService, taxCodeRetriever, productAssociationService, bundleIdentifier, settingsReader,
				translationExtractor, provider);

		final Offer offer = converter.convert(storeProduct, store, mockCatalog());

		assertFalse(offer.isDeleted());
	}

	@Test
	public void itemsShouldContainsExactlyProductSkuCode() {
		Optional<StoreProductSku> productSku = mockStoreProductSku();
		final StoreProduct storeProduct = mockStoreProduct(productSku.get());
		final Store store = mockStore();
		final ProductCharacteristics productCharacteristics = mock(ProductCharacteristics.class);
		when(productCharacteristics.isBundle()).thenReturn(true);
		when(productCharacteristicsService.getProductCharacteristics(storeProduct)).thenReturn(productCharacteristics);
		when(storeProductService.getProductForStore(storeProduct, store)).thenReturn(storeProduct);
		when(productSku.get().canSyndicate()).thenReturn(true);
		when(storeProductService.getProductSkuForStore(productSku.get(), store)).thenReturn(productSku);
		final TaxCode taxCode = mockTaxCode();
		when(taxCodeRetriever.getEffectiveTaxCode(productSku.get())).thenReturn(taxCode);
		when(bundleIdentifier.asProductBundle(storeProduct)).thenReturn(new ProductBundleImpl());

		final ProductToProjectionConverter converter = new ProductToProjectionConverter(storeProductService, timeService,
				productCharacteristicsService, taxCodeRetriever, productAssociationService, bundleIdentifier, settingsReader,
				translationExtractor, provider);
		final Offer offer = converter.convert(storeProduct, store, mockCatalog());

		assertThat(offer.getItems()).extracting(Item::getItemCode).containsExactly(PRODUCT_SKU_CODE);
	}

	@Test
	public void notSyndicatedProductShouldBeConvertedToDeletedOffer() {
		final StoreProduct storeProduct = mockStoreProduct();
		when(storeProduct.canSyndicate()).thenReturn(false);
		final Store store = mockStore();
		when(storeProductService.getProductForStore(storeProduct, store)).thenReturn(storeProduct);
		when(storeProductService.getProductForStore(storeProduct, store)).thenReturn(storeProduct);

		final ProductToProjectionConverter converter = new ProductToProjectionConverter(storeProductService, timeService,
				productCharacteristicsService, taxCodeRetriever, productAssociationService, bundleIdentifier, settingsReader,
				translationExtractor, provider);
		final Offer offer = converter.convert(storeProduct, store, mockCatalog());

		assertTrue(offer.isDeleted());
	}

	@Test
	public void notSyndicatedProductShouldContainNullItems() {
		final StoreProduct storeProduct = mockStoreProduct();
		when(storeProduct.canSyndicate()).thenReturn(false);
		final Store store = mockStore();
		when(storeProductService.getProductForStore(storeProduct, store)).thenReturn(storeProduct);

		final ProductToProjectionConverter converter = new ProductToProjectionConverter(storeProductService, timeService,
				productCharacteristicsService, taxCodeRetriever, productAssociationService, bundleIdentifier, settingsReader,
				translationExtractor, provider);
		final Offer offer = converter.convert(storeProduct, store, mockCatalog());

		assertThat(offer.getItems()).isNull();
	}

	@Test
	public void notSyndicatedSkuShouldBeSkippedForOffer() {
		final StoreProduct storeProduct = mockStoreProduct();
		final Store store = mockStore();
		when(storeProductService.getProductForStore(storeProduct, store)).thenReturn(storeProduct);
		final ProductCharacteristics productCharacteristics = mock(ProductCharacteristics.class);
		when(productCharacteristics.isBundle()).thenReturn(true);
		when(productCharacteristicsService.getProductCharacteristics(storeProduct)).thenReturn(productCharacteristics);
		when(storeProductService.getProductForStore(storeProduct, store)).thenReturn(storeProduct);
		when(bundleIdentifier.asProductBundle(storeProduct)).thenReturn(new ProductBundleImpl());

		final ProductToProjectionConverter converter = new ProductToProjectionConverter(storeProductService, timeService,
				productCharacteristicsService, taxCodeRetriever, productAssociationService, bundleIdentifier, settingsReader,
				translationExtractor, provider);
		final Offer offer = converter.convert(storeProduct, store, mockCatalog());

		assertThat(offer.getItems()).isEmpty();
	}


	@Test
	public void canDiscoverShouldReturnEmptySetWhenProductIsNotHiddenAndIsNotSoldSeparately() {
		final StoreProduct storeProduct = mockStoreProduct();
		when(storeProduct.isHidden()).thenReturn(false);
		when(storeProduct.isNotSoldSeparately()).thenReturn(true);

		final Store store = mockStore();
		when(storeProductService.getProductForStore(storeProduct, store)).thenReturn(storeProduct);
		final Catalog catalog = mockCatalog();

		final ProductToProjectionConverter converter = new ProductToProjectionConverter(storeProductService, timeService,
				productCharacteristicsService, taxCodeRetriever, productAssociationService, bundleIdentifier, settingsReader,
				translationExtractor, provider);

		final Offer offer = converter.convert(storeProduct, store, catalog);

		assertThat(offer.getAvailabilityRules().getCanDiscover()).isEmpty();
	}

	@Test
	public void canViewShouldReturnAlwaysWhenProductIsNotHiddenAndIsNotSoldSeparately() {
		final StoreProduct storeProduct = mockStoreProduct();
		when(storeProduct.isHidden()).thenReturn(false);
		when(storeProduct.isNotSoldSeparately()).thenReturn(true);

		final Store store = mockStore();
		when(storeProductService.getProductForStore(storeProduct, store)).thenReturn(storeProduct);
		final Catalog catalog = mockCatalog();

		final ProductToProjectionConverter converter = new ProductToProjectionConverter(storeProductService, timeService,
				productCharacteristicsService, taxCodeRetriever, productAssociationService, bundleIdentifier, settingsReader,
				translationExtractor, provider);

		final Offer offer = converter.convert(storeProduct, store, catalog);

		assertThat(offer.getAvailabilityRules().getCanView()).containsExactly(ALWAYS);
	}

	@Test
	public void canAddToCartShouldReturnEmptySetWhenProductIsNotHiddenAndIsNotSoldSeparately() {
		final StoreProduct storeProduct = mockStoreProduct();
		when(storeProduct.isHidden()).thenReturn(false);
		when(storeProduct.isNotSoldSeparately()).thenReturn(true);

		final Store store = mockStore();
		when(storeProductService.getProductForStore(storeProduct, store)).thenReturn(storeProduct);
		final Catalog catalog = mockCatalog();

		final ProductToProjectionConverter converter = new ProductToProjectionConverter(storeProductService, timeService,
				productCharacteristicsService, taxCodeRetriever, productAssociationService, bundleIdentifier, settingsReader,
				translationExtractor, provider);

		final Offer offer = converter.convert(storeProduct, store, catalog);

		assertThat(offer.getAvailabilityRules().getCanAddToCart()).isEmpty();
	}

	@Test
	public void canDiscoverShouldReturnAlwaysWhenProductIsNotHiddenAndIsSoldSeparatelyAndAvailableCriteriaIsAlwaysAvailable() {
		final StoreProduct storeProduct = mockStoreProduct();
		when(storeProduct.isHidden()).thenReturn(false);
		when(storeProduct.isNotSoldSeparately()).thenReturn(false);
		when(storeProduct.getAvailabilityCriteria()).thenReturn(AvailabilityCriteria.ALWAYS_AVAILABLE);
		final Store store = mockStore();
		when(storeProductService.getProductForStore(storeProduct, store)).thenReturn(storeProduct);
		final Catalog catalog = mockCatalog();

		final ProductToProjectionConverter converter = new ProductToProjectionConverter(storeProductService, timeService,
				productCharacteristicsService, taxCodeRetriever, productAssociationService, bundleIdentifier, settingsReader,
				translationExtractor, provider);

		final Offer offer = converter.convert(storeProduct, store, catalog);
		verify(storeProductService).getProductForStore(storeProduct, store);

		assertThat(offer.getAvailabilityRules().getCanDiscover()).containsExactly(ALWAYS);
	}

	@Test
	public void canViewShouldReturnAlwaysWhenProductIsNotHiddenAndIsSoldSeparatelyAndAvailableCriteriaIsAlwaysAvailable() {
		final StoreProduct storeProduct = mockStoreProduct();
		when(storeProduct.isHidden()).thenReturn(false);
		when(storeProduct.isNotSoldSeparately()).thenReturn(false);
		when(storeProduct.getAvailabilityCriteria()).thenReturn(AvailabilityCriteria.ALWAYS_AVAILABLE);

		final Store store = mockStore();
		when(storeProductService.getProductForStore(storeProduct, store)).thenReturn(storeProduct);
		final Catalog catalog = mockCatalog();

		final ProductToProjectionConverter converter = new ProductToProjectionConverter(storeProductService, timeService,
				productCharacteristicsService, taxCodeRetriever, productAssociationService, bundleIdentifier, settingsReader,
				translationExtractor, provider);

		final Offer offer = converter.convert(storeProduct, store, catalog);

		assertThat(offer.getAvailabilityRules().getCanView()).containsExactly(ALWAYS);
	}

	@Test
	public void canAddToCartShouldReturnAlwaysWhenProductIsNotHiddenAndIsSoldSeparatelyAndAvailableCriteriaIsAlwaysAvailable() {
		final StoreProduct storeProduct = mockStoreProduct();
		when(storeProduct.isHidden()).thenReturn(false);
		when(storeProduct.isNotSoldSeparately()).thenReturn(false);
		when(storeProduct.getAvailabilityCriteria()).thenReturn(AvailabilityCriteria.ALWAYS_AVAILABLE);

		final Store store = mockStore();
		when(storeProductService.getProductForStore(storeProduct, store)).thenReturn(storeProduct);
		final Catalog catalog = mockCatalog();

		final ProductToProjectionConverter converter = new ProductToProjectionConverter(storeProductService, timeService,
				productCharacteristicsService, taxCodeRetriever, productAssociationService, bundleIdentifier, settingsReader,
				translationExtractor, provider);

		final Offer offer = converter.convert(storeProduct, store, catalog);

		assertThat(offer.getAvailabilityRules().getCanAddToCart()).containsExactly(ALWAYS);
	}

	@Test
	public void canDiscoverShouldReturnHasStockWhenProductIsNotHiddenAndIsSoldSeparatelyAndAvailableCriteriaIsAvailableWhenInStock() {
		final StoreProduct storeProduct = mockStoreProduct();
		when(storeProduct.isHidden()).thenReturn(false);
		when(storeProduct.isNotSoldSeparately()).thenReturn(false);
		when(storeProduct.getAvailabilityCriteria()).thenReturn(AvailabilityCriteria.AVAILABLE_WHEN_IN_STOCK);

		final Store store = mockStore();
		when(storeProductService.getProductForStore(storeProduct, store)).thenReturn(storeProduct);
		final Catalog catalog = mockCatalog();

		final ProductToProjectionConverter converter = new ProductToProjectionConverter(storeProductService, timeService,
				productCharacteristicsService, taxCodeRetriever, productAssociationService, bundleIdentifier, settingsReader,
				translationExtractor, provider);

		final Offer offer = converter.convert(storeProduct, store, catalog);

		assertThat(offer.getAvailabilityRules().getCanDiscover()).containsExactly(HAS_STOCK);
	}

	@Test
	public void canViewShouldReturnAlwaysWhenProductIsNotHiddenAndIsSoldSeparatelyAndAvailableCriteriaIsAvailableWhenInStock() {
		final StoreProduct storeProduct = mockStoreProduct();
		when(storeProduct.isHidden()).thenReturn(false);
		when(storeProduct.isNotSoldSeparately()).thenReturn(false);
		when(storeProduct.getAvailabilityCriteria()).thenReturn(AvailabilityCriteria.AVAILABLE_WHEN_IN_STOCK);

		final Store store = mockStore();
		when(storeProductService.getProductForStore(storeProduct, store)).thenReturn(storeProduct);
		final Catalog catalog = mockCatalog();

		final ProductToProjectionConverter converter = new ProductToProjectionConverter(storeProductService, timeService,
				productCharacteristicsService, taxCodeRetriever, productAssociationService, bundleIdentifier, settingsReader,
				translationExtractor, provider);

		final Offer offer = converter.convert(storeProduct, store, catalog);

		assertThat(offer.getAvailabilityRules().getCanView()).containsExactly(ALWAYS);
	}

	@Test
	public void canAddToCartShouldReturnHasStockWhenProductIsNotHiddenAndIsSoldSeparatelyAndAvailableCriteriaIsAvailableWhenInStock() {
		final StoreProduct storeProduct = mockStoreProduct();
		when(storeProduct.isHidden()).thenReturn(false);
		when(storeProduct.isNotSoldSeparately()).thenReturn(false);
		when(storeProduct.getAvailabilityCriteria()).thenReturn(AvailabilityCriteria.AVAILABLE_WHEN_IN_STOCK);

		final Store store = mockStore();
		when(storeProductService.getProductForStore(storeProduct, store)).thenReturn(storeProduct);
		final Catalog catalog = mockCatalog();

		final ProductToProjectionConverter converter = new ProductToProjectionConverter(storeProductService, timeService,
				productCharacteristicsService, taxCodeRetriever, productAssociationService, bundleIdentifier, settingsReader,
				translationExtractor, provider);

		final Offer offer = converter.convert(storeProduct, store, catalog);

		assertThat(offer.getAvailabilityRules().getCanAddToCart()).containsExactly(HAS_STOCK);
	}

	@Test
	public void canDiscoverShouldReturnPreOrderAndHasStockWhenProductIsNotHiddenAndIsSoldSeparatelyAndAvailableCriteriaIsAvailableForPreOrder() {
		final StoreProduct storeProduct = mockStoreProduct();
		when(storeProduct.isHidden()).thenReturn(false);
		when(storeProduct.isNotSoldSeparately()).thenReturn(false);
		when(storeProduct.getAvailabilityCriteria()).thenReturn(AvailabilityCriteria.AVAILABLE_FOR_PRE_ORDER);

		final Store store = mockStore();
		when(storeProductService.getProductForStore(storeProduct, store)).thenReturn(storeProduct);
		final Catalog catalog = mockCatalog();

		final ProductToProjectionConverter converter = new ProductToProjectionConverter(storeProductService, timeService,
				productCharacteristicsService, taxCodeRetriever, productAssociationService, bundleIdentifier, settingsReader,
				translationExtractor, provider);

		final Offer offer = converter.convert(storeProduct, store, catalog);

		assertThat(offer.getAvailabilityRules().getCanDiscover()).containsOnlyOnce(PRE_ORDER, HAS_STOCK);
	}

	@Test
	public void canViewShouldReturnAlwaysWhenProductIsNotHiddenAndIsSoldSeparatelyAndAvailableCriteriaIsAvailableForPreOrder() {
		final StoreProduct storeProduct = mockStoreProduct();
		when(storeProduct.isHidden()).thenReturn(false);
		when(storeProduct.isNotSoldSeparately()).thenReturn(false);
		when(storeProduct.getAvailabilityCriteria()).thenReturn(AvailabilityCriteria.AVAILABLE_FOR_PRE_ORDER);

		final Store store = mockStore();
		when(storeProductService.getProductForStore(storeProduct, store)).thenReturn(storeProduct);
		final Catalog catalog = mockCatalog();

		final ProductToProjectionConverter converter = new ProductToProjectionConverter(storeProductService, timeService,
				productCharacteristicsService, taxCodeRetriever, productAssociationService, bundleIdentifier, settingsReader,
				translationExtractor, provider);

		final Offer offer = converter.convert(storeProduct, store, catalog);

		assertThat(offer.getAvailabilityRules().getCanView()).containsExactly(ALWAYS);
	}

	@Test
	public void canAddToCartShouldReturnPreOrderAndHasStockWhenProductIsNotHiddenAndIsSoldSeparatelyAndAvailableCriteriaIsAvailableForPreOrder() {
		final StoreProduct storeProduct = mockStoreProduct();
		when(storeProduct.isHidden()).thenReturn(false);
		when(storeProduct.isNotSoldSeparately()).thenReturn(false);
		when(storeProduct.getAvailabilityCriteria()).thenReturn(AvailabilityCriteria.AVAILABLE_FOR_PRE_ORDER);

		final Store store = mockStore();
		when(storeProductService.getProductForStore(storeProduct, store)).thenReturn(storeProduct);
		final Catalog catalog = mockCatalog();

		final ProductToProjectionConverter converter = new ProductToProjectionConverter(storeProductService, timeService,
				productCharacteristicsService, taxCodeRetriever, productAssociationService, bundleIdentifier, settingsReader,
				translationExtractor, provider);

		final Offer offer = converter.convert(storeProduct, store, catalog);

		assertThat(offer.getAvailabilityRules().getCanAddToCart()).containsOnlyOnce(PRE_ORDER, HAS_STOCK);
	}

	@Test
	public void canDiscoverShouldReturnHasStockAndBackOrderWhenProductIsNotHiddenAndIsSoldSeparatelyAndAvailableCriteriaIsAvailableForBackOrder() {
		final StoreProduct storeProduct = mockStoreProduct();
		when(storeProduct.isHidden()).thenReturn(false);
		when(storeProduct.isNotSoldSeparately()).thenReturn(false);
		when(storeProduct.getAvailabilityCriteria()).thenReturn(AvailabilityCriteria.AVAILABLE_FOR_BACK_ORDER);

		final Store store = mockStore();
		when(storeProductService.getProductForStore(storeProduct, store)).thenReturn(storeProduct);
		final Catalog catalog = mockCatalog();

		final ProductToProjectionConverter converter = new ProductToProjectionConverter(storeProductService, timeService,
				productCharacteristicsService, taxCodeRetriever, productAssociationService, bundleIdentifier, settingsReader,
				translationExtractor, provider);

		final Offer offer = converter.convert(storeProduct, store, catalog);

		assertThat(offer.getAvailabilityRules().getCanDiscover()).containsExactly(HAS_STOCK, BACK_ORDER);
	}

	@Test
	public void canViewShouldReturnAlwaysWhenProductIsNotHiddenAndIsSoldSeparatelyAndAvailableCriteriaIsAvailableForBackOrder() {
		final StoreProduct storeProduct = mockStoreProduct();
		when(storeProduct.isHidden()).thenReturn(false);
		when(storeProduct.isNotSoldSeparately()).thenReturn(false);
		when(storeProduct.getAvailabilityCriteria()).thenReturn(AvailabilityCriteria.AVAILABLE_FOR_BACK_ORDER);

		final Store store = mockStore();
		when(storeProductService.getProductForStore(storeProduct, store)).thenReturn(storeProduct);
		final Catalog catalog = mockCatalog();

		final ProductToProjectionConverter converter = new ProductToProjectionConverter(storeProductService, timeService,
				productCharacteristicsService, taxCodeRetriever, productAssociationService, bundleIdentifier, settingsReader,
				translationExtractor, provider);

		final Offer offer = converter.convert(storeProduct, store, catalog);

		assertThat(offer.getAvailabilityRules().getCanView()).containsExactly(ALWAYS);
	}

	@Test
	public void canAddToCartShouldReturnHasStockAndBackOrderWhenProductIsNotHiddenAndIsSoldSeparatelyAndAvailableCriteriaIsAvailableForBackOrder() {
		final StoreProduct storeProduct = mockStoreProduct();
		when(storeProduct.isHidden()).thenReturn(false);
		when(storeProduct.isNotSoldSeparately()).thenReturn(false);
		when(storeProduct.getAvailabilityCriteria()).thenReturn(AvailabilityCriteria.AVAILABLE_FOR_BACK_ORDER);

		final Store store = mockStore();
		when(storeProductService.getProductForStore(storeProduct, store)).thenReturn(storeProduct);
		final Catalog catalog = mockCatalog();

		final ProductToProjectionConverter converter = new ProductToProjectionConverter(storeProductService, timeService,
				productCharacteristicsService, taxCodeRetriever, productAssociationService, bundleIdentifier, settingsReader,
				translationExtractor, provider);

		final Offer offer = converter.convert(storeProduct, store, catalog);

		assertThat(offer.getAvailabilityRules().getCanAddToCart()).containsExactly(HAS_STOCK, BACK_ORDER);
	}

	@Test
	public void associationsSizeShouldBeTwoWhenTwoDifferentProductAssociationTypesAreReturnedByProductAssociationService() {
		final ProductAssociation productAssociation1 = mockCanSyndicateAssociation(CROSS_SELL, PRODUCT_1, 1);
		final ProductAssociation productAssociation2 = mockCanSyndicateAssociation(ACCESSORY, PRODUCT_2, 1);
		when(productAssociationService.getAssociations(STORE_PRODUCT_CODE, CATALOG, true))
				.thenReturn(new HashSet<>(Arrays.asList(productAssociation1, productAssociation2)));

		final StoreProduct storeProduct = mockStoreProduct();
		final Store store = mockStore();

		when(storeProductService.getProductForStore(storeProduct, store)).thenReturn(storeProduct);

		final ProductToProjectionConverter converter = new ProductToProjectionConverter(storeProductService, timeService,
				productCharacteristicsService, taxCodeRetriever, productAssociationService, bundleIdentifier, settingsReader,
				translationExtractor, provider);

		final Offer offer = converter.convert(storeProduct, store, mockCatalog());

		assertThat(offer.getAssociations()).hasSize(2);
	}

	@Test
	public void associationsListSizeShouldBeTwoWhenTwoDifferentProductsExistForSpecifiedProductAssociationType() {
		final ProductAssociation productAssociation1 = mockCanSyndicateAssociation(CROSS_SELL, PRODUCT_1, 1);
		final ProductAssociation productAssociation2 = mockCanSyndicateAssociation(CROSS_SELL, PRODUCT_2, 2);
		final ProductAssociation productAssociation3 = mockCanSyndicateAssociation(ACCESSORY, PRODUCT_3, 1);
		when(productAssociationService.getAssociations(STORE_PRODUCT_CODE, CATALOG, true))
				.thenReturn(new HashSet<>(Arrays.asList(productAssociation1, productAssociation2, productAssociation3)));

		final StoreProduct storeProduct = mockStoreProduct();
		final Store store = mockStore();

		when(storeProductService.getProductForStore(storeProduct, store)).thenReturn(storeProduct);

		final ProductToProjectionConverter converter = new ProductToProjectionConverter(storeProductService, timeService,
				productCharacteristicsService, taxCodeRetriever, productAssociationService, bundleIdentifier, settingsReader,
				translationExtractor, provider);

		final Offer offer = converter.convert(storeProduct, store, mockCatalog());

		assertThat(offer.getAssociations()).filteredOn(association -> association.getType().equals("crosssell"))
				.flatExtracting(Association::getList).hasSize(2);
	}

	@Test
	public void associationsListShouldContainsOffersSortedExactlyByOrderingWithinProductAssociationType() {
		final ProductAssociation productAssociation1 = mockCanSyndicateAssociation(CROSS_SELL, PRODUCT_1, 1);
		final ProductAssociation productAssociation2 = mockCanSyndicateAssociation(CROSS_SELL, PRODUCT_2, 2);
		when(productAssociationService.getAssociations(STORE_PRODUCT_CODE, CATALOG, true))
				.thenReturn(new HashSet<>(Arrays.asList(productAssociation2, productAssociation1)));

		final StoreProduct storeProduct = mockStoreProduct();
		final Store store = mockStore();

		when(storeProductService.getProductForStore(storeProduct, store)).thenReturn(storeProduct);

		final ProductToProjectionConverter converter = new ProductToProjectionConverter(storeProductService, timeService,
				productCharacteristicsService, taxCodeRetriever, productAssociationService, bundleIdentifier, settingsReader,
				translationExtractor, provider);

		final Offer offer = converter.convert(storeProduct, store, mockCatalog());

		assertThat(offer.getAssociations().get(0).getList()).extracting(AssociationValue::getOffer).containsExactly(PRODUCT_1, PRODUCT_2);
	}

	@Test
	public void associationsListSizeShouldBeOneWhenThereIsOnlyOneValidProductAssociationType() {
		final ProductAssociation inValidProductAssociation = mockNotCanSyndicateAssociation(CROSS_SELL, PRODUCT_1, 1);
		final ProductAssociation validProductAssociation = mockCanSyndicateAssociation(CROSS_SELL, PRODUCT_2, 2);
		when(productAssociationService.getAssociations(STORE_PRODUCT_CODE, CATALOG, true))
				.thenReturn(new HashSet<>(Arrays.asList(inValidProductAssociation, validProductAssociation)));

		final StoreProduct storeProduct = mockStoreProduct();
		final Store store = mockStore();

		when(storeProductService.getProductForStore(storeProduct, store)).thenReturn(storeProduct);

		final ProductToProjectionConverter converter = new ProductToProjectionConverter(storeProductService, timeService,
				productCharacteristicsService, taxCodeRetriever, productAssociationService, bundleIdentifier, settingsReader,
				translationExtractor, provider);

		final Offer offer = converter.convert(storeProduct, store, mockCatalog());

		assertThat(offer.getAssociations()).hasSize(1);
	}

	@Test
	public void associationsListShouldContainsProductCodeFromCanSyndicateAssociationOnly() {
		final ProductAssociation inValidProductAssociation = mockNotCanSyndicateAssociation(CROSS_SELL, PRODUCT_1, 1);
		final ProductAssociation validProductAssociation = mockCanSyndicateAssociation(CROSS_SELL, PRODUCT_2, 2);
		when(productAssociationService.getAssociations(STORE_PRODUCT_CODE, CATALOG, true))
				.thenReturn(new HashSet<>(Arrays.asList(inValidProductAssociation, validProductAssociation)));

		final StoreProduct storeProduct = mockStoreProduct();
		final Store store = mockStore();

		when(storeProductService.getProductForStore(storeProduct, store)).thenReturn(storeProduct);

		final ProductToProjectionConverter converter = new ProductToProjectionConverter(storeProductService, timeService,
				productCharacteristicsService, taxCodeRetriever, productAssociationService, bundleIdentifier, settingsReader,
				translationExtractor, provider);

		final Offer offer = converter.convert(storeProduct, store, mockCatalog());

		assertThat(offer.getAssociations().get(0).getList()).extracting(AssociationValue::getOffer)
				.containsExactly(validProductAssociation.getTargetProduct().getCode());
	}

	@Test
	public void associationsListShouldNotContainsProductCodeFromNotCanSyndicateAssociation() {
		final ProductAssociation inValidProductAssociation = mockNotCanSyndicateAssociation(CROSS_SELL, PRODUCT_1, 1);
		final ProductAssociation validProductAssociation = mockCanSyndicateAssociation(CROSS_SELL, PRODUCT_2, 2);
		when(productAssociationService.getAssociations(STORE_PRODUCT_CODE, CATALOG, true))
				.thenReturn(new HashSet<>(Arrays.asList(inValidProductAssociation, validProductAssociation)));

		final StoreProduct storeProduct = mockStoreProduct();
		final Store store = mockStore();

		when(storeProductService.getProductForStore(storeProduct, store)).thenReturn(storeProduct);

		final ProductToProjectionConverter converter = new ProductToProjectionConverter(storeProductService, timeService,
				productCharacteristicsService, taxCodeRetriever, productAssociationService, bundleIdentifier, settingsReader,
				translationExtractor, provider);

		final Offer offer = converter.convert(storeProduct, store, mockCatalog());

		assertThat(offer.getAssociations().get(0).getList()).extracting(AssociationValue::getOffer)
				.doesNotContain(inValidProductAssociation.getTargetProduct().getCode());
	}

	@Test
	public void associationsListShouldNotContainsDateTimeWhenTheyAreNullInProductAssociation() {
		final ProductAssociation productAssociation = mockCanSyndicateAssociation(CROSS_SELL, PRODUCT_1, 1);
		when(productAssociation.getStartDate()).thenReturn(null);
		when(productAssociation.getEndDate()).thenReturn(null);

		when(productAssociationService.getAssociations(STORE_PRODUCT_CODE, CATALOG, true))
				.thenReturn(new HashSet<>(Collections.singletonList(productAssociation)));

		final StoreProduct storeProduct = mockStoreProduct();
		final Store store = mockStore();

		when(storeProductService.getProductForStore(storeProduct, store)).thenReturn(storeProduct);

		final ProductToProjectionConverter converter = new ProductToProjectionConverter(storeProductService, timeService,
				productCharacteristicsService, taxCodeRetriever, productAssociationService, bundleIdentifier, settingsReader,
				translationExtractor, provider);

		final Offer offer = converter.convert(storeProduct, store, mockCatalog());

		assertThat(offer.getAssociations().get(0).getList()).extracting(AssociationValue::getEnableDateTime).containsOnlyNulls();
		assertThat(offer.getAssociations().get(0).getList()).extracting(AssociationValue::getDisableDateTime).containsOnlyNulls();
	}

	@Test
	public void associationsTypeShouldBeInLowerCase() {
		final ProductAssociation productAssociation = mockCanSyndicateAssociation(CROSS_SELL, PRODUCT_1, 1);
		when(productAssociationService.getAssociations(STORE_PRODUCT_CODE, CATALOG, true))
				.thenReturn(new HashSet<>(Collections.singletonList(productAssociation)));

		final StoreProduct storeProduct = mockStoreProduct();
		final Store store = mockStore();

		when(storeProductService.getProductForStore(storeProduct, store)).thenReturn(storeProduct);

		final ProductToProjectionConverter converter = new ProductToProjectionConverter(storeProductService, timeService,
				productCharacteristicsService, taxCodeRetriever, productAssociationService, bundleIdentifier, settingsReader,
				translationExtractor, provider);

		final Offer offer = converter.convert(storeProduct, store, mockCatalog());

		assertThat(offer.getAssociations().get(0).getType()).isLowerCase();
	}

	@Test
	public void notBundleSingleSkuOfferShouldBeSelectionRulesNoneTypeWithQuantityZero() {
		final StoreProduct storeProduct = mockStoreProduct();
		final ProductType productType = new ProductTypeImpl();
		productType.setMultiSku(false);
		final Store store = mockStore();
		when(storeProduct.getProductType()).thenReturn(productType);
		when(storeProductService.getProductForStore(storeProduct, store)).thenReturn(storeProduct);

		final ProductToProjectionConverter converter = new ProductToProjectionConverter(storeProductService, timeService,
				productCharacteristicsService, taxCodeRetriever, productAssociationService, bundleIdentifier, settingsReader,
				translationExtractor, provider);

		final Offer offer = converter.convert(storeProduct, store, mockCatalog());

		assertThat(offer.getSelectionRules().getQuantity()).isEqualTo(0);
		assertThat(offer.getSelectionRules().getSelectionType()).isEqualTo(SelectionType.NONE);
	}

	@Test
	public void notBundleMultiSkuOfferShouldBeSelectionRulesItemTypeWithQuantityOne() {
		final StoreProduct storeProduct = mockStoreProduct();
		final ProductType productType = new ProductTypeImpl();
		productType.setMultiSku(true);
		final Store store = mockStore();
		when(storeProduct.getProductType()).thenReturn(productType);
		when(storeProductService.getProductForStore(storeProduct, store)).thenReturn(storeProduct);

		final ProductToProjectionConverter converter = new ProductToProjectionConverter(storeProductService, timeService,
				productCharacteristicsService, taxCodeRetriever, productAssociationService, bundleIdentifier, settingsReader,
				translationExtractor, provider);

		final Offer offer = converter.convert(storeProduct, store, mockCatalog());

		assertThat(offer.getSelectionRules().getQuantity()).isEqualTo(1);
		assertThat(offer.getSelectionRules().getSelectionType()).isEqualTo(SelectionType.ITEM);
	}

	@Test
	public void dynamicBundleOfferShouldBeSelectionRulesComponentTypeWithQuantitySelectionRuleGetParameter() {
		final StoreProduct storeProduct = mockBundleProduct();
		final int quantity = 5;
		final SelectionRule selectionRule = new SelectionRuleImpl();
		selectionRule.setParameter(quantity);
		final ProductBundle productBundle = (ProductBundle) storeProduct.getWrappedProduct();
		productBundle.setSelectionRule(selectionRule);
		final Store store = mockStore();
		when(storeProductService.getProductForStore(storeProduct, store)).thenReturn(storeProduct);
		when(productCharacteristicsService.getProductCharacteristics(storeProduct).isDynamicBundle()).thenReturn(true);
		final ProductToProjectionConverter converter = new ProductToProjectionConverter(storeProductService, timeService,
				productCharacteristicsService, taxCodeRetriever, productAssociationService, bundleIdentifier, settingsReader,
				translationExtractor, provider);

		final Offer offer = converter.convert(storeProduct, store, mockCatalog());

		assertThat(offer.getSelectionRules().getQuantity()).isEqualTo(quantity);
		assertThat(offer.getSelectionRules().getSelectionType()).isEqualTo(SelectionType.COMPONENT);
	}

	@Test
	public void selectionTypeShouldBeNoneAndQuantityShouldBe0WhenProductBundleSelectionRuleIsNull() {
		final StoreProduct storeProduct = mockBundleProduct();

		final ProductBundle productBundle = (ProductBundle) storeProduct.getWrappedProduct();
		productBundle.setSelectionRule(null);

		final Store store = mockStore();

		when(storeProductService.getProductForStore(storeProduct, store)).thenReturn(storeProduct);
		when(productCharacteristicsService.getProductCharacteristics(storeProduct).isDynamicBundle()).thenReturn(true);
		when(bundleIdentifier.asProductBundle(storeProduct)).thenReturn(productBundle);

		final ProductToProjectionConverter converter = new ProductToProjectionConverter(storeProductService, timeService,
				productCharacteristicsService, taxCodeRetriever, productAssociationService, bundleIdentifier, settingsReader,
				translationExtractor, provider);

		final Offer offer = converter.convert(storeProduct, store, mockCatalog());

		assertThat(offer.getSelectionRules().getSelectionType()).isEqualTo(SelectionType.NONE);
		assertThat(offer.getSelectionRules().getQuantity()).isEqualTo(0);
	}

	@Test
	public void brandNameAndDisplayNameShouldBeNullInTranslationUnitWhenBrandIsNull() {
		final StoreProduct storeProduct = mockStoreProduct();
		when(storeProduct.getBrand()).thenReturn(null);

		final Store store = mockStore();

		when(storeProductService.getProductForStore(storeProduct, store)).thenReturn(storeProduct);
		when(translationExtractor.getProjectionTranslations(any(), any(), any())).thenReturn(Collections.singletonList(new Translation("en",
				"product")));

		final ProductToProjectionConverter converter = new ProductToProjectionConverter(storeProductService, timeService,
				productCharacteristicsService, taxCodeRetriever, productAssociationService, bundleIdentifier, settingsReader,
				translationExtractor, provider);

		final Offer offer = converter.convert(storeProduct, store, mockCatalog());

		assertThat(offer.getTranslations()).extracting(OfferTranslation::getBrand).extracting(TranslationUnit::getName).containsOnlyNulls();
		assertThat(offer.getTranslations()).extracting(OfferTranslation::getBrand).extracting(TranslationUnit::getDisplayName).containsOnlyNulls();
	}

	@Test
	public void fixedBundleOfferShouldBeSelectionRulesNoneTypeWithQuantityZero() {
		final StoreProduct storeProduct = mockBundleProduct();
		final Store store = mockStore();
		when(storeProductService.getProductForStore(storeProduct, store)).thenReturn(storeProduct);

		final ProductToProjectionConverter converter = new ProductToProjectionConverter(storeProductService, timeService,
				productCharacteristicsService, taxCodeRetriever, productAssociationService, bundleIdentifier, settingsReader,
				translationExtractor, provider);

		final Offer offer = converter.convert(storeProduct, store, mockCatalog());

		assertThat(offer.getSelectionRules().getQuantity()).isEqualTo(0);
		assertThat(offer.getSelectionRules().getSelectionType()).isEqualTo(SelectionType.NONE);
	}

	@Test
	public void noneBundleOfferShouldHaveEmptyComponents() {
		final StoreProduct storeProduct = mockStoreProduct();
		final ProductType productType = new ProductTypeImpl();
		productType.setMultiSku(false);
		final Store store = mockStore();
		when(storeProduct.getProductType()).thenReturn(productType);
		when(storeProductService.getProductForStore(storeProduct, store)).thenReturn(storeProduct);

		final ProductToProjectionConverter converter = new ProductToProjectionConverter(storeProductService, timeService,
				productCharacteristicsService, taxCodeRetriever, productAssociationService, bundleIdentifier, settingsReader,
				translationExtractor, provider);

		final Offer offer = converter.convert(storeProduct, store, mockCatalog());

		assertThat(offer.getComponents().getList()).isEmpty();
	}

	@Test
	public void bundleOfferNoneSkuComponentShouldBeWithoutItemAndWithStoreCode() {
		final String offerCode = "offerCode";
		final StoreProduct storeProduct = mockBundleProduct(offerCode);
		final Store store = mockStore();
		when(storeProductService.getProductForStore(storeProduct, store)).thenReturn(storeProduct);
		when(((ProductBundle) storeProduct.getWrappedProduct()).getConstituents().get(0).getConstituent().isProductSku()).thenReturn(false);
		final ProductToProjectionConverter converter = new ProductToProjectionConverter(storeProductService, timeService,
				productCharacteristicsService, taxCodeRetriever, productAssociationService, bundleIdentifier, settingsReader,
				translationExtractor, provider);

		final Offer offer = converter.convert(storeProduct, store, mockCatalog());

		assertThat(offer.getComponents().getList().get(0).getItem()).isNull();
		assertThat(offer.getComponents().getList().get(0).getOffer()).isEqualTo(offerCode);
		assertThat(offer.getComponents().getList().get(0).getQuantity()).isEqualTo(BUNDLE_CONSISTENT_QUANTITY);
	}

	@Test
	public void bundleOfferSkuComponentShouldBeWithItemAndWithStoreCode() {
		final String itemCode = "itemCode";
		final StoreProduct storeProduct = mockBundleProduct(itemCode);
		final Store store = mockStore();
		when(storeProductService.getProductForStore(storeProduct, store)).thenReturn(storeProduct);

		when(((ProductBundle) storeProduct.getWrappedProduct()).getConstituents().get(0).getConstituent().isProductSku()).thenReturn(true);


		final ProductToProjectionConverter converter = new ProductToProjectionConverter(storeProductService, timeService,
				productCharacteristicsService, taxCodeRetriever, productAssociationService, bundleIdentifier, settingsReader,
				translationExtractor, provider);

		final Offer offer = converter.convert(storeProduct, store, mockCatalog());

		assertThat(offer.getComponents().getList().get(0).getItem()).isEqualTo(itemCode);
		assertThat(offer.getComponents().getList().get(0).getOffer()).isEqualTo("offerCode");
		assertThat(offer.getComponents().getList().get(0).getQuantity()).isEqualTo(BUNDLE_CONSISTENT_QUANTITY);
	}

	@Test
	public void testThatOfferContainsFormFields() {
		final String cartItemModifierGroupCode = "groupCode";
		final StoreProduct storeProduct = mockBundleProduct();
		final Store store = mockStore();
		final ProductType productType = mock(ProductType.class);
		final ModifierGroup cartItemModifierGroup = mock(ModifierGroup.class);
		when(storeProductService.getProductForStore(storeProduct, store)).thenReturn(storeProduct);
		when(storeProduct.getProductType()).thenReturn(productType);
		when(productType.getModifierGroups()).thenReturn(Collections.singleton(cartItemModifierGroup));
		when(cartItemModifierGroup.getCode()).thenReturn(cartItemModifierGroupCode);

		final ProductToProjectionConverter converter = new ProductToProjectionConverter(storeProductService, timeService,
				productCharacteristicsService, taxCodeRetriever, productAssociationService, bundleIdentifier, settingsReader,
				translationExtractor, provider);

		final Offer offer = converter.convert(storeProduct, store, mockCatalog());

		assertThat(offer.getFormFields().get(0)).isEqualTo(cartItemModifierGroupCode);
		assertThat(offer.getFormFields().size()).isEqualTo(1);
	}

	@Test
	public void testThatOfferContainsShippingPropertiesInPhysicalItem() {
		final BigDecimal weight = BigDecimal.ONE;
		final BigDecimal width = new BigDecimal(2);
		final BigDecimal length = new BigDecimal(3);
		final BigDecimal height = new BigDecimal(4);
		final String unitsWeight = "kg";
		final String unitsLength = "m";
		final SettingValue weightDefinition = mock(SettingValue.class);
		final SettingValue lengthDefinition = mock(SettingValue.class);


		final StoreProduct storeProduct = mockStoreProduct();
		final Store store = mockStore();
		final ProductSku productSku = mock(ProductSku.class);
		Map<String, ProductSku> skus = new HashMap<>();
		skus.put(StringUtils.EMPTY, productSku);
		final Optional<StoreProductSku> storeProductSku = mockStoreProductSku();

		when(storeProductService.getProductForStore(storeProduct, store)).thenReturn(storeProduct);
		when(storeProduct.getProductSkus()).thenReturn(skus);
		when(storeProductService.getProductSkuForStore(productSku, store)).thenReturn(storeProductSku);
		when(storeProductSku.get().canSyndicate()).thenReturn(true);
		when(storeProductSku.get().isShippable()).thenReturn(true);
		when(storeProductSku.get().getWeight()).thenReturn(weight);
		when(storeProductSku.get().getWidth()).thenReturn(width);
		when(storeProductSku.get().getLength()).thenReturn(length);
		when(storeProductSku.get().getHeight()).thenReturn(height);
		when(weightDefinition.getValue()).thenReturn(unitsWeight);
		when(lengthDefinition.getValue()).thenReturn(unitsLength);
		when(settingsReader.getSettingValue("COMMERCE/SYSTEM/UNITS/weight")).thenReturn(weightDefinition);
		when(settingsReader.getSettingValue("COMMERCE/SYSTEM/UNITS/length")).thenReturn(lengthDefinition);

		final ProductToProjectionConverter converter = new ProductToProjectionConverter(storeProductService, timeService,
				productCharacteristicsService, taxCodeRetriever, productAssociationService, bundleIdentifier, settingsReader,
				translationExtractor, provider);

		final Offer offer = converter.convert(storeProduct, store, mockCatalog());

		assertThat(offer.getItems().get(0).getShippingProperties().getHeight()).isEqualTo(height);
		assertThat(offer.getItems().get(0).getShippingProperties().getLength()).isEqualTo(length);
		assertThat(offer.getItems().get(0).getShippingProperties().getWeight()).isEqualTo(weight);
		assertThat(offer.getItems().get(0).getShippingProperties().getWidth()).isEqualTo(width);
		assertThat(offer.getItems().get(0).getShippingProperties().getUnitsLength()).isEqualTo(unitsLength);
		assertThat(offer.getItems().get(0).getShippingProperties().getUnitsWeight()).isEqualTo(unitsWeight);
	}

	@Test
	public void testThatOfferContainsNoShippingPropertiesInNotPhysicalItem() {
		final StoreProduct storeProduct = mockStoreProduct();
		final Store store = mockStore();
		final ProductSku productSku = mock(ProductSku.class);
		Map<String, ProductSku> skus = new HashMap<>();
		skus.put(StringUtils.EMPTY, productSku);
		final Optional<StoreProductSku> storeProductSku = mockStoreProductSku();

		when(storeProductService.getProductForStore(storeProduct, store)).thenReturn(storeProduct);
		when(storeProduct.getProductSkus()).thenReturn(skus);
		when(storeProductService.getProductSkuForStore(productSku, store)).thenReturn(storeProductSku);
		when(storeProductSku.get().canSyndicate()).thenReturn(true);
		when(storeProductSku.get().isShippable()).thenReturn(false);

		final ProductToProjectionConverter converter = new ProductToProjectionConverter(storeProductService, timeService,
				productCharacteristicsService, taxCodeRetriever, productAssociationService, bundleIdentifier, settingsReader,
				translationExtractor, provider);

		final Offer offer = converter.convert(storeProduct, store, mockCatalog());

		assertThat(offer.getItems().get(0).getShippingProperties()).isNull();
	}

	@Test
	public void testThatOfferContainsNoShippingPropertiesInBundleProduct() {
		final StoreProduct storeProduct = mockStoreProduct();
		final Store store = mockStore();
		final ProductSku productSku = mock(ProductSku.class);
		Map<String, ProductSku> skus = new HashMap<>();
		skus.put(StringUtils.EMPTY, productSku);
		final Optional<StoreProductSku> storeProductSku = mockStoreProductSku();
		final ProductCharacteristics productCharacteristics = mock(ProductCharacteristics.class);

		when(storeProductService.getProductForStore(storeProduct, store)).thenReturn(storeProduct);
		when(storeProduct.getProductSkus()).thenReturn(skus);
		when(storeProductService.getProductSkuForStore(productSku, store)).thenReturn(storeProductSku);
		when(storeProductSku.get().canSyndicate()).thenReturn(true);
		when(storeProductSku.get().isShippable()).thenReturn(true);
		when(productCharacteristics.isBundle()).thenReturn(true);
		when(productCharacteristicsService.getProductCharacteristics(storeProductSku.get())).thenReturn(productCharacteristics);

		final ProductToProjectionConverter converter = new ProductToProjectionConverter(storeProductService, timeService,
				productCharacteristicsService, taxCodeRetriever, productAssociationService, bundleIdentifier, settingsReader,
				translationExtractor, provider);

		final Offer offer = converter.convert(storeProduct, store, mockCatalog());

		assertThat(offer.getItems().get(0).getShippingProperties()).isNull();
	}

	@Test
	public void testThatOfferContainsTranslations() {
		final StoreProduct storeProduct = mockStoreProduct();
		final Store store = mockStore();
		final Attribute attribute = mockAttribute(ATTRIBUTE_KEY);
		final Option option = mockOption();
		final Optional<Brand> brand = Optional.ofNullable(mockBrand());

		when(storeProductService.getProductForStore(storeProduct, store)).thenReturn(storeProduct);
		when(translationExtractor.getProjectionTranslations(any(), any(), any())).thenReturn(Collections.singletonList(new Translation("en",
				"productDisplayName")));
		when(attributeReaderCapability.get().findAllWithCodes(store.getCode(), Arrays.asList(ATTRIBUTE_KEY, ATTRIBUTE_KEY)))
				.thenReturn(Collections.singletonList(attribute));
		when(optionReaderCapability.get().findAllWithCodes(store.getCode(), Collections.singletonList(OPTION_KEY)))
				.thenReturn(Collections.singletonList(option));
		when(brandReaderCapability.get().get(store.getCode(), BRAND_CODE)).thenReturn(brand);

		final ProductToProjectionConverter converter = new ProductToProjectionConverter(storeProductService, timeService,
				productCharacteristicsService, taxCodeRetriever, productAssociationService, bundleIdentifier, settingsReader,
				translationExtractor, provider);

		final Offer offer = converter.convert(storeProduct, store, mockCatalog());

		assertThat(offer.getTranslations().get(0).getLanguage()).isEqualTo("en");
		assertThat(offer.getTranslations().get(0).getDisplayName()).isEqualTo("productDisplayName");
		assertThat(offer.getTranslations().get(0).getBrand().getName()).isEqualTo("brandCode");
		assertThat(offer.getTranslations().get(0).getBrand().getDisplayName()).isEqualTo("brandDisplayName");
		assertThat(offer.getTranslations().get(0).getOptions().get(0).getName()).isEqualTo("optionKey");
		assertThat(offer.getTranslations().get(0).getOptions().get(0).getDisplayName()).isEqualTo("optionDisplayName");
		assertThat(offer.getTranslations().get(0).getDetails().get(0).getDisplayName()).isEqualTo(ATTRIBUTE_KEY + Locale.ENGLISH.toString());
		assertThat(offer.getTranslations().get(0).getDetails().get(0).getName()).isEqualTo(ATTRIBUTE_KEY);
		assertThat(offer.getTranslations().get(0).getDetails().get(0).getValues().get(0)).isEqualTo(DETAILS_VALUE);
		assertThat(offer.getTranslations().get(0).getDetails().get(0).getDisplayValues().get(0)).isEqualTo(DETAILS_VALUE);
	}

	@Test
	public void testThatOfferContainsDefaultStoreTranslationsWhenAppropriateTranslationIsAbsent() {
		final StoreProduct storeProduct = mockStoreProduct();
		final Store store = mockStore();
		final Attribute attribute = mockAttribute(ATTRIBUTE_KEY);
		final Option option = mockOption();
		final Optional<Brand> brand = Optional.ofNullable(mockBrand());

		when(store.getSupportedLocales()).thenReturn(Arrays.asList(Locale.ENGLISH, Locale.FRENCH));
		when(storeProductService.getProductForStore(storeProduct, store)).thenReturn(storeProduct);
		when(translationExtractor.getProjectionTranslations(any(), any(), any())).thenReturn(Arrays.asList(new Translation("en",
				"productDisplayName"), new Translation("fr", "productDisplayName_fr")));
		when(attributeReaderCapability.get().findAllWithCodes(store.getCode(), Arrays.asList(ATTRIBUTE_KEY, ATTRIBUTE_KEY)))
				.thenReturn(Collections.singletonList(attribute));
		when(optionReaderCapability.get().findAllWithCodes(store.getCode(), Collections.singletonList(OPTION_KEY)))
				.thenReturn(Collections.singletonList(option));
		when(brandReaderCapability.get().get(store.getCode(), BRAND_CODE)).thenReturn(brand);

		final ProductToProjectionConverter converter = new ProductToProjectionConverter(storeProductService, timeService,
				productCharacteristicsService, taxCodeRetriever, productAssociationService, bundleIdentifier, settingsReader,
				translationExtractor, provider);

		final Offer offer = converter.convert(storeProduct, store, mockCatalog());
		final OfferTranslation frenchTranslation = offer.getTranslations()
				.stream()
				.filter(translation -> translation.getDisplayName().equals("productDisplayName_fr"))
				.findFirst()
				.orElse(null);
		assertThat(frenchTranslation).isNotNull();
		assertThat(frenchTranslation.getDetails().get(0).getDisplayName()).isEqualTo(ATTRIBUTE_KEY + Locale.ENGLISH.toString());
		assertThat(frenchTranslation.getDetails().get(0).getDisplayValues().get(0)).isEqualTo(DETAILS_VALUE);
	}

	@Test
	public void testThatOfferContainsDefaultCatalogTranslationsWhenAppropriateTranslationIsAbsentAndStoreDefaultTranslationIsAbsent() {
		final StoreProduct storeProduct = mockStoreProduct();
		final Store store = mockStore();
		final Attribute attribute = mockAttribute(ATTRIBUTE_KEY);
		final Option option = mockOption();
		final Optional<Brand> brand = Optional.ofNullable(mockBrand());
		final Catalog catalog = mockCatalog();

		when(catalog.getDefaultLocale()).thenReturn(Locale.ENGLISH);
		when(store.getDefaultLocale()).thenReturn(Locale.FRENCH);
		when(store.getSupportedLocales()).thenReturn(Arrays.asList(Locale.FRENCH));
		when(storeProductService.getProductForStore(storeProduct, store)).thenReturn(storeProduct);
		when(translationExtractor.getProjectionTranslations(any(), any(), any())).thenReturn(Arrays.asList(new Translation("en",
				"productDisplayName"), new Translation("fr", "productDisplayName_fr")));
		when(attributeReaderCapability.get().findAllWithCodes(store.getCode(), Arrays.asList(ATTRIBUTE_KEY, ATTRIBUTE_KEY)))
				.thenReturn(Collections.singletonList(attribute));
		when(optionReaderCapability.get().findAllWithCodes(store.getCode(), Collections.singletonList(OPTION_KEY)))
				.thenReturn(Collections.singletonList(option));
		when(brandReaderCapability.get().get(store.getCode(), BRAND_CODE)).thenReturn(brand);

		final ProductToProjectionConverter converter = new ProductToProjectionConverter(storeProductService, timeService,
				productCharacteristicsService, taxCodeRetriever, productAssociationService, bundleIdentifier, settingsReader,
				translationExtractor, provider);

		final Offer offer = converter.convert(storeProduct, store, catalog);
		final OfferTranslation frenchTranslation = offer.getTranslations()
				.stream()
				.filter(translation -> translation.getDisplayName().equals("productDisplayName_fr"))
				.findFirst()
				.orElse(null);

		assertThat(frenchTranslation).isNotNull();
		assertThat(frenchTranslation.getDetails().get(0).getDisplayName()).isEqualTo(ATTRIBUTE_KEY + Locale.ENGLISH.toString());
		assertThat(frenchTranslation.getDetails().get(0).getDisplayValues().get(0)).isEqualTo(DETAILS_VALUE);
	}

	@Test
	public void testThatOfferContainsAnyTranslationsWhenAppropriateTranslationAndStoreDefaultAndCatalogDefaultAreAbsent() {
		final StoreProduct storeProduct = mockStoreProduct();
		final Store store = mockStore();
		final Attribute attribute = mockAttribute(ATTRIBUTE_KEY);
		final Option option = mockOption();
		final Optional<Brand> brand = Optional.ofNullable(mockBrand());
		final Catalog catalog = mockCatalog();

		when(catalog.getDefaultLocale()).thenReturn(Locale.CHINA);
		when(store.getDefaultLocale()).thenReturn(Locale.FRENCH);
		when(store.getSupportedLocales()).thenReturn(Arrays.asList(Locale.FRENCH));
		when(storeProductService.getProductForStore(storeProduct, store)).thenReturn(storeProduct);
		when(translationExtractor.getProjectionTranslations(any(), any(), any())).thenReturn(Collections.singletonList(new Translation("fr",
				"productDisplayName_fr")));
		when(attributeReaderCapability.get().findAllWithCodes(store.getCode(), Arrays.asList(ATTRIBUTE_KEY, ATTRIBUTE_KEY)))
				.thenReturn(Collections.singletonList(attribute));
		when(optionReaderCapability.get().findAllWithCodes(store.getCode(), Collections.singletonList(OPTION_KEY)))
				.thenReturn(Collections.singletonList(option));
		when(brandReaderCapability.get().get(store.getCode(), BRAND_CODE)).thenReturn(brand);

		final ProductToProjectionConverter converter = new ProductToProjectionConverter(storeProductService, timeService,
				productCharacteristicsService, taxCodeRetriever, productAssociationService, bundleIdentifier, settingsReader,
				translationExtractor, provider);

		final Offer offer = converter.convert(storeProduct, store, catalog);
		final OfferTranslation frenchTranslation = offer.getTranslations()
				.stream()
				.filter(translation -> translation.getDisplayName().equals("productDisplayName_fr"))
				.findFirst()
				.orElse(null);

		assertThat(frenchTranslation).isNotNull();
		assertThat(frenchTranslation.getDetails().get(0).getDisplayName()).isEqualTo(ATTRIBUTE_KEY + Locale.ENGLISH.toString());
		assertThat(frenchTranslation.getDetails().get(0).getDisplayValues().get(0)).isEqualTo(DETAILS_VALUE);
	}

	@Test
	public void testThatOfferDetailsTranslationsParsedMultiValueProperly() {
		final StoreProduct storeProduct = mockStoreProduct();
		final Store store = mockStore();
		final Attribute attribute = mockAttribute(ATTRIBUTE_KEY);
		final Option option = mockOption();
		final Optional<Brand> brand = Optional.ofNullable(mockBrand());

		when(storeProduct.getAttributeValueMap().values().stream().findFirst().get().getAttribute().isMultiValueEnabled()).thenReturn(true);
		when(storeProduct.getAttributeValueMap().values().stream().findFirst().get().getStringValue()).thenReturn(DETAILS_MULTI_VALUE);

		when(store.getSupportedLocales()).thenReturn(Arrays.asList(Locale.ENGLISH, Locale.FRENCH));
		when(storeProductService.getProductForStore(storeProduct, store)).thenReturn(storeProduct);
		when(translationExtractor.getProjectionTranslations(any(), any(), any())).thenReturn(Arrays.asList(new Translation("en",
				"productDisplayName"), new Translation("fr", "productDisplayName_fr")));
		when(attributeReaderCapability.get().findAllWithCodes(store.getCode(), Arrays.asList(ATTRIBUTE_KEY, ATTRIBUTE_KEY)))
				.thenReturn(Collections.singletonList(attribute));
		when(optionReaderCapability.get().findAllWithCodes(store.getCode(), Collections.singletonList(OPTION_KEY)))
				.thenReturn(Collections.singletonList(option));
		when(brandReaderCapability.get().get(store.getCode(), BRAND_CODE)).thenReturn(brand);

		final ProductToProjectionConverter converter = new ProductToProjectionConverter(storeProductService, timeService,
				productCharacteristicsService, taxCodeRetriever, productAssociationService, bundleIdentifier, settingsReader,
				translationExtractor, provider);

		final Offer offer = converter.convert(storeProduct, store, mockCatalog());
		final OfferTranslation frenchTranslation = offer.getTranslations()
				.stream()
				.filter(translation -> translation.getDisplayName().equals("productDisplayName_fr"))
				.findFirst()
				.orElse(null);
		assertThat(frenchTranslation).isNotNull();
		assertThat(frenchTranslation.getDetails().get(0).getDisplayName()).isEqualTo(ATTRIBUTE_KEY + Locale.ENGLISH.toString());
		assertThat(frenchTranslation.getDetails().get(0).getDisplayValues().get(0)).isEqualTo(DETAILS_VALUE);
		assertThat(frenchTranslation.getDetails().get(0).getDisplayValues().get(1)).isEqualTo(DETAILS_VALUE_WITH_COMMA);
	}

	@Test
	public void testThatOfferItemContainsTranslations() {
		final StoreProduct storeProduct = mockStoreProduct();
		final Store store = mockStore();
		final Attribute offerAttribute = mockAttribute(ATTRIBUTE_KEY);
		final Attribute skuAttribute = mockAttribute(ATTRIBUTE_SKU_KEY);

		final Option option = mockOption();
		final Optional<Brand> brand = Optional.ofNullable(mockBrand());

		final ProductSku productSku = mock(ProductSku.class);
		Map<String, ProductSku> skus = new HashMap<>();
		skus.put(StringUtils.EMPTY, productSku);
		final Optional<StoreProductSku> storeProductSku = mockStoreProductSku();

		when(storeProductService.getProductForStore(storeProduct, store)).thenReturn(storeProduct);
		when(translationExtractor.getProjectionTranslations(any(), any(), any())).thenReturn(Collections.singletonList(new Translation("en",
				"productDisplayName")));
		when(attributeReaderCapability.get().findAllWithCodes(store.getCode(), Arrays.asList(ATTRIBUTE_KEY, ATTRIBUTE_KEY, ATTRIBUTE_SKU_KEY)))
				.thenReturn(Arrays.asList(offerAttribute, skuAttribute));
		when(optionReaderCapability.get().findAllWithCodes(store.getCode(), Collections.singletonList(OPTION_KEY)))
				.thenReturn(Collections.singletonList(option));
		when(brandReaderCapability.get().get(store.getCode(), BRAND_CODE)).thenReturn(brand);

		when(storeProductService.getProductForStore(storeProduct, store)).thenReturn(storeProduct);
		when(storeProduct.getProductSkus()).thenReturn(skus);
		when(storeProduct.getStoreProductSkus()).thenReturn(new HashSet<>(Collections.singletonList(storeProductSku.orElse(null))));
		when(storeProductService.getProductSkuForStore(productSku, store)).thenReturn(storeProductSku);
		when(storeProductSku.get().canSyndicate()).thenReturn(true);

		final ProductToProjectionConverter converter = new ProductToProjectionConverter(storeProductService, timeService,
				productCharacteristicsService, taxCodeRetriever, productAssociationService, bundleIdentifier, settingsReader,
				translationExtractor, provider);

		final Offer offer = converter.convert(storeProduct, store, mockCatalog());

		assertThat(offer.getItems().get(0).getTranslations().get(0).getLanguage()).isEqualTo("en");
		assertThat(offer.getItems().get(0).getTranslations().get(0).getOptions().get(0).getName()).isEqualTo("optionKey");
		assertThat(offer.getItems().get(0).getTranslations().get(0).getOptions().get(0).getDisplayName()).isEqualTo("optionDisplayName");
		assertThat(offer.getItems().get(0).getTranslations().get(0).getOptions().get(0).getValue()).isEqualTo("optionValueName");
		assertThat(offer.getItems().get(0).getTranslations().get(0).getOptions().get(0).getDisplayValue()).isEqualTo("optionDisplayValue");
		assertThat(offer.getItems().get(0).getTranslations().get(0).getDetails().get(0).getName()).isEqualTo(ATTRIBUTE_SKU_KEY);
		assertThat(offer.getItems().get(0).getTranslations().get(0).getDetails().get(0).getDisplayName()).isEqualTo(ATTRIBUTE_SKU_KEY
				+ Locale.ENGLISH.toString());
		assertThat(offer.getItems().get(0).getTranslations().get(0).getDetails().get(0).getValues().get(0)).isEqualTo(DETAILS_ITEM_VALUE);
		assertThat(offer.getItems().get(0).getTranslations().get(0).getDetails().get(0).getDisplayValues().get(0)).isEqualTo(DETAILS_ITEM_VALUE);
	}

	@Test
	public void testThatOfferContainsNotDefaultSettingValue() {
		final SettingValue weightDefinition = mock(SettingValue.class);
		final SettingValue lengthDefinition = mock(SettingValue.class);

		final StoreProduct storeProduct = mockStoreProduct();
		final Store store = mockStore();
		final ProductSku productSku = mock(ProductSku.class);
		Map<String, ProductSku> skus = new HashMap<>();
		skus.put(StringUtils.EMPTY, productSku);
		final Optional<StoreProductSku> storeProductSku = mockStoreProductSku();

		when(storeProductService.getProductForStore(storeProduct, store)).thenReturn(storeProduct);
		when(storeProduct.getProductSkus()).thenReturn(skus);
		when(storeProductService.getProductSkuForStore(productSku, store)).thenReturn(storeProductSku);
		when(storeProductSku.get().canSyndicate()).thenReturn(true);
		when(storeProductSku.get().isShippable()).thenReturn(true);

		when(settingsReader.getSettingValue("COMMERCE/SYSTEM/UNITS/weight")).thenReturn(weightDefinition);
		when(settingsReader.getSettingValue("COMMERCE/SYSTEM/UNITS/length")).thenReturn(lengthDefinition);

		final ProductToProjectionConverter converter = new ProductToProjectionConverter(storeProductService, timeService,
				productCharacteristicsService, taxCodeRetriever, productAssociationService, bundleIdentifier, settingsReader,
				translationExtractor, provider);

		converter.convert(storeProduct, store, mockCatalog());

		verify(weightDefinition).getValue();
		verify(lengthDefinition).getValue();
	}

	@Test
	public void testThatOfferContainsCategory() {
		final StoreProduct storeProduct = mockStoreProduct();
		final Store store = mockStore();

		when(storeProductService.getProductForStore(storeProduct, store)).thenReturn(storeProduct);

		final ProductToProjectionConverter converter = new ProductToProjectionConverter(storeProductService, timeService,
				productCharacteristicsService, taxCodeRetriever, productAssociationService, bundleIdentifier, settingsReader,
				translationExtractor, provider);

		final Offer offer = converter.convert(storeProduct, store, mockCatalog());

		final OfferCategories categories = offer.getCategories().iterator().next();
		assertThat(categories.getCode()).isEqualTo(CATEGORY_CODE);
		assertThat(categories.getDisableDateTime()).isEqualTo(CURRENT_DATE);
		assertThat(categories.getEnableDateTime()).isEqualTo(CURRENT_DATE);
		assertThat(categories.getFeatured()).isEqualTo(FEATURED_VALUE);
		assertThat(categories.getPath()).containsExactly("path");
		assertThat(categories.isDefaultCategory()).isTrue();
	}

	@Test
	public void testThatOfferIsDeletedIfAllCategoryWereDeleted() {
		final StoreProduct storeProduct = mockStoreProduct();
		final Store store = mockStore();

		final List<Category> category = Collections.singletonList(mockCategory());
		when(category.get(0).isDeleted()).thenReturn(true);
		when(categoryReaderCapability.get().findAllWithCodes(anyString(), anyList())).thenReturn(category);
		final ProductToProjectionConverter converter = new ProductToProjectionConverter(storeProductService, timeService,
				productCharacteristicsService, taxCodeRetriever, productAssociationService, bundleIdentifier, settingsReader,
				translationExtractor, provider);

		final Offer offer = converter.convert(storeProduct, store, mockCatalog());

		assertThat(offer.isDeleted()).isEqualTo(true);
	}

	@Test
	public void testThatOfferIsNotDeletedIfOnlyOneCategoryWasDeleted() {
		final StoreProduct storeProduct = mockStoreProduct();
		final Store store = mockStore();
		final Category deletedCategory = mockCategory();
		final Category category = mockCategory();
		final List<Category> categoryList = Arrays.asList(deletedCategory, category);

		when(deletedCategory.isDeleted()).thenReturn(true);
		when(storeProductService.getProductForStore(storeProduct, store)).thenReturn(storeProduct);
		when(categoryReaderCapability.get().findAllWithCodes(anyString(), anyList())).thenReturn(categoryList);

		final ProductToProjectionConverter converter = new ProductToProjectionConverter(storeProductService, timeService,
				productCharacteristicsService, taxCodeRetriever, productAssociationService, bundleIdentifier, settingsReader,
				translationExtractor, provider);
		final Offer offer = converter.convert(storeProduct, store, mockCatalog());

		assertThat(offer.isDeleted()).isEqualTo(false);
	}

	@Test
	public void testThatOfferIsNotDeletedIfAllCategoryAreNotDeleted() {
		final StoreProduct storeProduct = mockStoreProduct();
		final Store store = mockStore();
		final List<Category> category = Collections.singletonList(mockCategory());
		when(categoryReaderCapability.get().findAllWithCodes(anyString(), anyList())).thenReturn(category);
		when(storeProductService.getProductForStore(storeProduct, store)).thenReturn(storeProduct);

		final ProductToProjectionConverter converter = new ProductToProjectionConverter(storeProductService, timeService,
				productCharacteristicsService, taxCodeRetriever, productAssociationService, bundleIdentifier, settingsReader,
				translationExtractor, provider);

		final Offer offer = converter.convert(storeProduct, store, mockCatalog());

		assertThat(offer.isDeleted()).isEqualTo(false);
	}

	@Test
	public void testThatOfferIsDeletedIfProductCanNotSyndicate() {
		final StoreProduct storeProduct = mockStoreProduct();
		final Store store = mockStore();
		final List<Category> category = Collections.singletonList(mockCategory());

		when(categoryReaderCapability.get().findAllWithCodes(anyString(), anyList())).thenReturn(category);
		when(storeProductService.getProductForStore(storeProduct, store)).thenReturn(storeProduct);
		when(storeProduct.canSyndicate()).thenReturn(false);

		final ProductToProjectionConverter converter = new ProductToProjectionConverter(storeProductService, timeService,
				productCharacteristicsService, taxCodeRetriever, productAssociationService, bundleIdentifier, settingsReader,
				translationExtractor, provider);
		final Offer offer = converter.convert(storeProduct, store, mockCatalog());

		assertThat(offer.isDeleted()).isEqualTo(true);
	}

	@Test
	public void testThatBundleOfferContainsBundleItem() {
		final ProductSku productSku = mockProductSku();
		final StoreProduct storeProduct = mockStoreProduct(productSku);
		final Store store = mockStore();
		final Optional<StoreProductSku> storeProductSku = mockStoreProductSku();

		when(storeProduct.getProductType().isMultiSku()).thenReturn(false);
		when(storeProductService.getProductForStore(storeProduct, store)).thenReturn(storeProduct);
		when(storeProductService.getProductSkuForStore(productSku, store)).thenReturn(storeProductSku);
		when(storeProductSku.get().canSyndicate()).thenReturn(true);

		final ProductCharacteristics characteristics = mock(ProductCharacteristics.class);
		when(characteristics.isBundle()).thenReturn(true);
		when(productCharacteristicsService.getProductCharacteristics(any(ProductSku.class))).thenReturn(characteristics);


		final ProductToProjectionConverter converter = new ProductToProjectionConverter(storeProductService, timeService,
				productCharacteristicsService, taxCodeRetriever, productAssociationService, bundleIdentifier, settingsReader,
				translationExtractor, provider);

		final Offer offer = converter.convert(storeProduct, store, mockCatalog());
		assertThat(offer.getItems()).flatExtracting(Item::getProperties).extracting(Property::getValue).contains("BUNDLE");
	}

	@Test
	public void testThatBundleProductItemDoesNotContainTaxCode() {
		final Optional<StoreProductSku> storeProductSku = mockStoreProductSku();
		final StoreProduct storeProduct = mockStoreProduct(storeProductSku.get());
		final Store store = mockStore();

		when(storeProductService.getProductForStore(storeProduct, store)).thenReturn(storeProduct);
		when(storeProductSku.get().canSyndicate()).thenReturn(true);
		when(storeProductService.getProductSkuForStore(storeProductSku.get(), store)).thenReturn(storeProductSku);
		final ProductCharacteristics characteristics = mock(ProductCharacteristics.class);
		when(characteristics.isBundle()).thenReturn(true);
		when(productCharacteristicsService.getProductCharacteristics(any(ProductSku.class))).thenReturn(characteristics);

		final ProductToProjectionConverter converter = new ProductToProjectionConverter(storeProductService, timeService,
				productCharacteristicsService, taxCodeRetriever, productAssociationService, bundleIdentifier, settingsReader,
				translationExtractor, provider);

		final Offer offer = converter.convert(storeProduct, store, mockCatalog());

		assertThat(offer.getItems()).flatExtracting(Item::getProperties)
				.extracting(Property::getValue)
				.doesNotContain(TAX_CODE);
	}


	private ProductAssociation mockCanSyndicateAssociation(final ProductAssociationType productAssociationType, final String productCode,
														   final int ordering) {
		return mockProductAssociation(productAssociationType, productCode, ordering, true, ZonedDateTime.now(), ZonedDateTime.now().plusDays(1L));
	}

	private ProductAssociation mockNotCanSyndicateAssociation(final ProductAssociationType productAssociationType, final String productCode,
															  final int ordering) {
		return mockProductAssociation(productAssociationType, productCode, ordering, false, ZonedDateTime.now(), ZonedDateTime.now().plusDays(1L));
	}

	private ProductAssociation mockProductAssociation(final ProductAssociationType productAssociationType, final String productCode,
													  final int ordering, final boolean canSyndicate, final ZonedDateTime startDate,
													  final ZonedDateTime endDate) {
		final Product product = mock(Product.class);
		when(product.getCode()).thenReturn(productCode);

		final ProductAssociation productAssociation = mock(ProductAssociation.class);
		when(productAssociation.getAssociationType()).thenReturn(productAssociationType);
		when(productAssociation.getTargetProduct()).thenReturn(product);
		when(productAssociation.getOrdering()).thenReturn(ordering);
		when(productAssociation.canSyndicate()).thenReturn(canSyndicate);
		when(productAssociation.getStartDate()).thenReturn(Date.from(startDate.toInstant()));
		when(productAssociation.getEndDate()).thenReturn(Date.from(endDate.toInstant()));

		return productAssociation;
	}

	private StoreProduct mockBundleProduct() {
		return mockBundleProduct(StringUtils.EMPTY);
	}

	private StoreProduct mockBundleProduct(final String constituentItemCode) {
		ProductType productType = new ProductTypeImpl();
		productType.setName("name");
		final ProductBundleImpl productBundle = new ProductBundleImpl();

		final StoreProduct storeProduct = mock(StoreProductImpl.class);
		when(storeProduct.canSyndicate()).thenReturn(true);
		when(storeProduct.getProductType()).thenReturn(productType);
		when(storeProduct.getExpectedReleaseDate()).thenReturn(new Date());

		final ConstituentItem constituentItem = mock(ConstituentItem.class);
		when(constituentItem.getCode()).thenReturn(constituentItemCode);
		Product product = mock(ProductImpl.class);
		when(product.getCode()).thenReturn("offerCode");
		when(constituentItem.getProduct()).thenReturn(product);

		when(constituentItem.getProduct()).thenReturn(product);

		final BundleConstituent bundleConstituent = mock(BundleConstituent.class);
		when(bundleConstituent.getConstituent()).thenReturn(constituentItem);
		when(bundleConstituent.getQuantity()).thenReturn(BUNDLE_CONSISTENT_QUANTITY);

		productBundle.addConstituent(bundleConstituent);
		when(bundleIdentifier.asProductBundle(storeProduct)).thenReturn(productBundle);

		final ProductCharacteristics productCharacteristics = mock(ProductCharacteristics.class);
		when(productCharacteristics.isBundle()).thenReturn(true);
		when(productCharacteristicsService.getProductCharacteristics(storeProduct)).thenReturn(productCharacteristics);
		when(bundleIdentifier.asProductBundle(storeProduct)).thenReturn(productBundle);

		final LocaleDependantFields localeDependantFields = mock(LocaleDependantFields.class);
		when(localeDependantFields.getDisplayName()).thenReturn("product_display_name");
		when(storeProduct.getLocaleDependantFields(Locale.ENGLISH)).thenReturn(localeDependantFields);
		when(storeProduct.getWrappedProduct()).thenReturn(productBundle);

		final com.elasticpath.domain.catalog.Brand brand = mock(com.elasticpath.domain.catalog.Brand.class);
		when(storeProduct.getBrand()).thenReturn(brand);


		return storeProduct;
	}

	private StoreProduct mockStoreProduct() {
		final ProductSku productSku = mockProductSku();
		when(productSku.getSkuCode()).thenReturn(PRODUCT_SKU_CODE);

		return mockStoreProduct(productSku);
	}

	private StoreProduct mockStoreProduct(final ProductSku productSku) {
		when(productSku.getSkuCode()).thenReturn(PRODUCT_SKU_CODE);
		final StoreProduct storeProduct = mock(StoreProductImpl.class);
		final ProductType productType = mock(ProductType.class);
		final LocaleDependantFields localeDependantFields = mock(LocaleDependantFields.class);
		final com.elasticpath.domain.attribute.Attribute attribute = mock(com.elasticpath.domain.attribute.Attribute.class);
		final AttributeValue attributeValueEn = mock(AttributeValue.class);
		final AttributeValue attributeValueCh = mock(AttributeValue.class);
		final AttributeValue attributeValueEmpty = mock(AttributeValue.class);
		final SkuOption sku = mock(SkuOption.class);
		final com.elasticpath.domain.catalog.Brand brand = mock(com.elasticpath.domain.catalog.Brand.class);
		final Set<com.elasticpath.domain.catalog.Category> categories = mockCategories();
		final ProductCategory productCategory = mockProductCategory();
		final List<Category> categoryList = Collections.singletonList(mockCategory());

		when(categoryReaderCapability.get().findAllWithCodes(anyString(), anyList())).thenReturn(categoryList);
		when(productType.getName()).thenReturn(PRODUCT_TYPE);
		when(storeProduct.getCategories()).thenReturn(categories);
		when(storeProduct.getProductCategory(any())).thenReturn(productCategory);
		when(storeProduct.getCode()).thenReturn(STORE_PRODUCT_CODE);
		when(storeProduct.getProductType()).thenReturn(productType);
		when(storeProduct.isNotSoldSeparately()).thenReturn(NOT_SOLD_SEPARATELY_VALUE);
		when(storeProduct.getMinOrderQty()).thenReturn(MIN_ORDER_QTY);
		when(storeProduct.getProductSkus()).thenReturn(Collections.singletonMap(PRODUCT_SKU_CODE, productSku));
		when(storeProduct.getStartDate()).thenReturn(new Date());
		when(storeProduct.getEndDate()).thenReturn(new Date());
		when(storeProduct.getExpectedReleaseDate()).thenReturn(new Date());
		when(storeProduct.isHidden()).thenReturn(true);
		when(storeProduct.getDiscoverRules()).thenCallRealMethod();
		when(storeProduct.getViewRules()).thenCallRealMethod();
		when(storeProduct.getAddToCartRules()).thenCallRealMethod();
		when(storeProduct.getLocaleDependantFields(Locale.ENGLISH)).thenReturn(localeDependantFields);
		when(localeDependantFields.getDisplayName()).thenReturn("product_display_name");
		when(storeProduct.canSyndicate()).thenReturn(true);
		when(attributeValueEn.getAttribute()).thenReturn(attribute);
		when(attributeValueEn.getStringValue()).thenReturn(DETAILS_VALUE);
		when(attributeValueEn.getValue()).thenReturn(DETAILS_VALUE);
		when(attributeValueEn.getAttributeType()).thenReturn(AttributeType.SHORT_TEXT);


		when(attributeValueCh.getAttribute()).thenReturn(attribute);

		when(attributeValueEmpty.getAttribute()).thenReturn(attribute);

		when(attribute.getKey()).thenReturn(ATTRIBUTE_KEY);
		when(attribute.getMultiValueType()).thenReturn(AttributeMultiValueType.RFC_4180);
		Map<String, AttributeValue> valueMap = new HashMap<>();
		valueMap.put("a1", attributeValueEn);
		valueMap.put("a2", attributeValueCh);

		AttributeValueGroup attributeValueGroup = mock(AttributeValueGroup.class);
		when(attributeValueGroup.getAttributeValue(ATTRIBUTE_KEY, Locale.ENGLISH)).thenReturn(attributeValueEn);

		when(storeProduct.getAttributeValueMap()).thenReturn(valueMap);
		when(storeProduct.getFullAttributeValues(Locale.ENGLISH)).thenReturn(Collections.singletonList(attributeValueEn));
		when(storeProduct.getFullAttributeValues(Locale.FRENCH)).thenReturn(Collections.singletonList(attributeValueEmpty));
		when(storeProduct.getAttributeValueGroup()).thenReturn(attributeValueGroup);

		when(sku.getOptionKey()).thenReturn(OPTION_KEY);
		when(productType.getSkuOptions()).thenReturn(Collections.singleton(sku));

		when(brand.getCode()).thenReturn(BRAND_CODE);
		when(storeProduct.getBrand()).thenReturn(brand);

		return storeProduct;
	}

	private Category mockCategory() {
		final Category category = mock(Category.class);
		final AvailabilityRules availabilityRules = mockAvailabilityRules();
		when(category.getAvailabilityRules()).thenReturn(availabilityRules);
		when(category.getPath()).thenReturn(Collections.singletonList("path"));
		when(category.getIdentity()).thenReturn(new NameIdentity("type", CATEGORY_CODE, null));
		return category;
	}

	private Set<com.elasticpath.domain.catalog.Category> mockCategories() {
		final CategoryImpl category = mock(CategoryImpl.class);
		when(category.getCode()).thenReturn(CATEGORY_CODE);
		return Collections.singleton(category);
	}

	private AvailabilityRules mockAvailabilityRules() {
		final AvailabilityRules rules = mock(AvailabilityRules.class);
		when(rules.getEnableDateTime()).thenReturn(CURRENT_DATE);
		when(rules.getDisableDateTime()).thenReturn(CURRENT_DATE);
		return rules;
	}

	private ProductCategory mockProductCategory() {
		final ProductCategory category = mock(ProductCategory.class);
		when(category.isDefaultCategory()).thenReturn(true);
		when(category.getFeaturedProductOrder()).thenReturn(FEATURED_VALUE);
		return category;
	}

	private ProductSku mockProductSku() {
		final ProductSku productSku = mock(ProductSku.class);

		when(productSku.getSkuCode()).thenReturn(PRODUCT_SKU_CODE);

		return productSku;
	}

	private Attribute mockAttribute(final String key) {
		final NameIdentity identity = mock(NameIdentity.class);
		final Attribute attribute = mock(Attribute.class);
		when(identity.getCode()).thenReturn(key);
		when(attribute.getIdentity()).thenReturn(identity);
		when(attribute.getTranslations()).thenReturn(Collections.singletonList(new AttributeTranslation("en",
				key + Locale.ENGLISH.toString(), "", false)));

		return attribute;
	}

	private Option mockOption() {
		final NameIdentity identity = mock(NameIdentity.class);
		final OptionTranslation translation = mock(OptionTranslation.class);

		final Option option = mock(Option.class);
		when(translation.getLanguage()).thenReturn("en");
		when(translation.getDisplayName()).thenReturn("optionDisplayName");
		when(translation.getOptionValues()).thenReturn(Collections.singletonList(new TranslatedName("optionValueName", "optionDisplayValue")));

		when(identity.getCode()).thenReturn(OPTION_KEY);
		when(option.getIdentity()).thenReturn(identity);
		when(option.getTranslations()).thenReturn(Collections.singletonList(translation));
		return option;
	}

	private Brand mockBrand() {
		final NameIdentity identity = mock(NameIdentity.class);
		final Brand brand = mock(Brand.class);
		when(identity.getCode()).thenReturn(BRAND_CODE);
		when(brand.getIdentity()).thenReturn(identity);
		when(brand.getTranslations()).thenReturn(Collections.singletonList(new Translation("en", "brandDisplayName")));
		return brand;
	}

	private TaxCode mockTaxCode() {
		final TaxCode taxCode = mock(TaxCode.class);

		when(taxCode.getCode()).thenReturn(TAX_CODE);

		return taxCode;
	}

	private Store mockStore() {
		final Store store = mock(Store.class);

		when(store.getCode()).thenReturn(STORE_CODE);
		when(store.getSupportedLocales()).thenReturn(Collections.singletonList(Locale.ENGLISH));
		when(store.getDefaultLocale()).thenReturn(Locale.ENGLISH);

		return store;
	}

	private Optional<StoreProductSku> mockStoreProductSku() {
		final StoreProductSku productSku = mock(StoreProductSku.class);
		final SkuOptionValue optionValue = mock(SkuOptionValue.class);
		final AttributeValueGroup group = mock(AttributeValueGroup.class);
		final com.elasticpath.domain.attribute.Attribute attribute = new com.elasticpath.domain.attribute.impl.AttributeImpl();
		attribute.setKey(ATTRIBUTE_SKU_KEY);
		final AttributeValue attributeValue = mock(AttributeValue.class);

		when(attributeValue.getAttribute()).thenReturn(attribute);
		when(attributeValue.getStringValue()).thenReturn(DETAILS_ITEM_VALUE);
		when(attributeValue.getValue()).thenReturn(DETAILS_ITEM_VALUE);

		when(productSku.getSkuCode()).thenReturn(PRODUCT_SKU_CODE);
		when(productSku.isDigital()).thenReturn(true);
		when(productSku.getOptionValueMap()).thenReturn(Collections.singletonMap(OPTION_KEY, optionValue));
		when(productSku.getFullAttributeValues(Locale.ENGLISH)).thenReturn(Collections.singletonList(attributeValue));
		when(productSku.getAttributeValueMap()).thenReturn(Collections.singletonMap("a", attributeValue));
		when(productSku.getAttributeValueGroup()).thenReturn(group);

		when(optionValue.getOptionValueKey()).thenReturn("optionValueName");
		return Optional.ofNullable(productSku);
	}

	private Catalog mockCatalog() {
		final Catalog catalog = mock(Catalog.class);
		when(catalog.getCode()).thenReturn(CATALOG);
		when(catalog.getDefaultLocale()).thenReturn(Locale.ENGLISH);

		return catalog;
	}

}
