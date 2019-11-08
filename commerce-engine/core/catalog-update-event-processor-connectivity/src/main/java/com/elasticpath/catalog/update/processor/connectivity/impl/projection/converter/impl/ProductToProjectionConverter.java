/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.update.processor.connectivity.impl.projection.converter.impl;

import static com.elasticpath.catalog.entity.constants.OfferPropertiesNames.PROPERTY_ITEM_TYPE;
import static com.elasticpath.catalog.entity.constants.OfferPropertiesNames.PROPERTY_TAX_CODE;
import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import com.elasticpath.catalog.CatalogReaderCapability;
import com.elasticpath.catalog.entity.AvailabilityRules;
import com.elasticpath.catalog.entity.ProjectionProperties;
import com.elasticpath.catalog.entity.Property;
import com.elasticpath.catalog.entity.attribute.Attribute;
import com.elasticpath.catalog.entity.attribute.AttributeReaderCapability;
import com.elasticpath.catalog.entity.brand.Brand;
import com.elasticpath.catalog.entity.brand.BrandReaderCapability;
import com.elasticpath.catalog.entity.category.Category;
import com.elasticpath.catalog.entity.category.CategoryReaderCapability;
import com.elasticpath.catalog.entity.offer.Association;
import com.elasticpath.catalog.entity.offer.AssociationValue;
import com.elasticpath.catalog.entity.offer.Component;
import com.elasticpath.catalog.entity.offer.Components;
import com.elasticpath.catalog.entity.offer.Item;
import com.elasticpath.catalog.entity.offer.ItemType;
import com.elasticpath.catalog.entity.offer.Offer;
import com.elasticpath.catalog.entity.offer.OfferAvailabilityRules;
import com.elasticpath.catalog.entity.offer.OfferCategories;
import com.elasticpath.catalog.entity.offer.OfferProperties;
import com.elasticpath.catalog.entity.offer.OfferRules;
import com.elasticpath.catalog.entity.offer.SelectionRules;
import com.elasticpath.catalog.entity.offer.SelectionType;
import com.elasticpath.catalog.entity.offer.ShippingProperties;
import com.elasticpath.catalog.entity.option.Option;
import com.elasticpath.catalog.entity.option.OptionReaderCapability;
import com.elasticpath.catalog.entity.translation.ItemTranslation;
import com.elasticpath.catalog.entity.translation.Translation;
import com.elasticpath.catalog.extractor.CatalogTranslationExtractor;
import com.elasticpath.catalog.extractor.ProjectionLocaleAdapter;
import com.elasticpath.catalog.spi.CatalogProjectionPluginProvider;
import com.elasticpath.catalog.update.processor.connectivity.impl.exception.NoCapabilityMatchedException;
import com.elasticpath.catalog.update.processor.connectivity.impl.projection.converter.Converter;
import com.elasticpath.catalog.update.processor.connectivity.impl.projection.extractor.adapter.LocaleDependantFieldsAdapter;
import com.elasticpath.domain.attribute.AttributeValue;
import com.elasticpath.domain.catalog.BundleConstituent;
import com.elasticpath.domain.catalog.Catalog;
import com.elasticpath.domain.catalog.ConstituentItem;
import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.catalog.ProductAssociation;
import com.elasticpath.domain.catalog.ProductCharacteristics;
import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.domain.catalogview.StoreProduct;
import com.elasticpath.domain.catalogview.StoreProductSku;
import com.elasticpath.domain.catalogview.impl.StoreAvailabilityRule;
import com.elasticpath.domain.modifier.ModifierGroup;
import com.elasticpath.domain.skuconfiguration.SkuOption;
import com.elasticpath.domain.store.Store;
import com.elasticpath.service.catalog.BundleIdentifier;
import com.elasticpath.service.catalog.ProductAssociationService;
import com.elasticpath.service.catalog.ProductCharacteristicsService;
import com.elasticpath.service.catalogview.StoreProductService;
import com.elasticpath.service.misc.TimeService;
import com.elasticpath.service.tax.TaxCodeRetriever;
import com.elasticpath.settings.SettingsReader;

/**
 * Represents an implementation of {@link Converter} for {@link Product} to {@link Offer} converting.
 */
@SuppressWarnings({"PMD.ExcessiveImports", "PMD.ExcessiveParameterList"})
public class ProductToProjectionConverter implements Converter<Product, Offer> {

	private static final int NONE_QUANTITY = 0;
	private static final int ITEM_QUANTITY = 1;
	private static final String GMT = "GMT";

	private final TimeService timeService;
	private final ProductCharacteristicsService productCharacteristicsService;
	private final TaxCodeRetriever taxCodeRetriever;
	private final StoreProductService storeProductService;
	private final ProductAssociationService productAssociationService;
	private final BundleIdentifier bundleIdentifier;
	private final SettingsReader settingsReader;
	private final CatalogTranslationExtractor catalogTranslationExtractor;
	private final CatalogProjectionPluginProvider provider;

	/**
	 * Constructor.
	 *
	 * @param storeProductService           {@link StoreProductService}
	 * @param timeService                   {@link TimeService}
	 * @param productCharacteristicsService {@link ProductCharacteristicsService}
	 * @param taxCodeRetriever              {@link TaxCodeRetriever}
	 * @param productAssociationService     {@link ProductAssociationService}
	 * @param bundleIdentifier              {@link BundleIdentifier}
	 * @param settingsReader                {@link SettingsReader}
	 * @param translationExtractor          {@link CatalogTranslationExtractor}
	 * @param provider                      {@link CatalogProjectionPluginProvider}
	 */
	@SuppressWarnings({"PMD.ExcessiveParameterList", "checkstyle:parameternumber"})
	public ProductToProjectionConverter(final StoreProductService storeProductService, final TimeService timeService,
										final ProductCharacteristicsService productCharacteristicsService,
										final TaxCodeRetriever taxCodeRetriever, final ProductAssociationService productAssociationService,
										final BundleIdentifier bundleIdentifier, final SettingsReader settingsReader,
										final CatalogTranslationExtractor translationExtractor, final CatalogProjectionPluginProvider provider) {
		this.storeProductService = storeProductService;
		this.timeService = timeService;
		this.productCharacteristicsService = productCharacteristicsService;
		this.taxCodeRetriever = taxCodeRetriever;
		this.productAssociationService = productAssociationService;
		this.bundleIdentifier = bundleIdentifier;
		this.settingsReader = settingsReader;
		this.catalogTranslationExtractor = translationExtractor;
		this.provider = provider;
	}

	@Override
	public Offer convert(final Product product, final Store store, final Catalog catalog) {
		final List<Category> categoriesList = extractNotDeletedCategories(product, store);
		final StoreProduct storeProduct = storeProductService.getProductForStore(product, store);

		if (categoriesList.isEmpty() || !storeProduct.canSyndicate()) {
			return createTombstoneOffer(product, store);
		}

		final ZonedDateTime currentTime = convertToZonedDateTime(timeService.getCurrentTime());
		final List<Property> offerProperties = extractOfferProperties(product);
		final List<StoreProductSku> storeProductSku = Optional.ofNullable(storeProduct.getProductSkus())
				.orElse(Collections.emptyMap())
				.values()
				.stream()
				.map(sku -> storeProductService.getProductSkuForStore(sku, store))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.collect(toList());

		final OfferTranslationExtractor offerTranslationExtractor = getTranslationExtractor(catalog, store, storeProduct, storeProductSku);

		final List<Item> items = storeProductSku.stream()
				.filter(item -> storeProduct.canSyndicate())
				.filter(StoreProductSku::canSyndicate)
				.map(sku -> createItem(sku, offerTranslationExtractor))
				.collect(toList());

		final List<Association> associations = extractAssociations(product.getCode(), catalog.getCode());
		final ProjectionProperties projectionProperties = new ProjectionProperties(storeProduct.getCode(), store.getCode(), currentTime,
				!storeProduct.canSyndicate());

		final Set<OfferCategories> offerCategories = new OfferCategoryExtractor(product, categoriesList).getOfferCategories();
		return new Offer(new OfferProperties(projectionProperties, offerProperties), items, new Object(), associations,
				extractComponents(storeProduct), new OfferRules(extractOfferAvailabilityRules(storeProduct), extractSelectionRules(storeProduct)),
				extractFormFields(storeProduct), offerTranslationExtractor.getOfferTranslations(), offerCategories);
	}

	private Offer createTombstoneOffer(final Product product, final Store store) {
		final ZonedDateTime currentTime = convertToZonedDateTime(timeService.getCurrentTime());
		return new Offer(product.getCode(), store.getCode(), currentTime, true);
	}

	private List<Category> extractNotDeletedCategories(final Product product, final Store store) {
		final List<Category> categoryList = extractCategories(getProductCategoriesCodes(product), store);
		return categoryList.stream().filter(category -> !category.isDeleted()).collect(toList());
	}

	private List<String> getProductCategoriesCodes(final Product product) {
		return product.getCategories()
				.stream()
				.map(com.elasticpath.domain.catalog.Category::getCode)
				.collect(toList());
	}

	private List<Category> extractCategories(final List<String> categoriesCodeList, final Store store) {
		return getReader(CategoryReaderCapability.class).findAllWithCodes(store.getCode(), categoriesCodeList);
	}

	private OfferTranslationExtractor getTranslationExtractor(final Catalog catalog,
															  final Store store,
															  final StoreProduct storeProduct,
															  final List<StoreProductSku> storeProductSku) {

		final ProjectionLocaleAdapter adapter = new LocaleDependantFieldsAdapter(catalog.getDefaultLocale(),
				storeProduct,
				store.getSupportedLocales());

		final List<Translation> translations = catalogTranslationExtractor.getProjectionTranslations(store.getDefaultLocale(),
				store.getSupportedLocales(),
				adapter);

		return new OfferTranslationExtractor(translations,
				new TranslationExtractorData(extractBrand(storeProduct, store),
						extractOptions(storeProduct, store),
						extractDetails(storeProduct, store)),
				storeProductSku,
				store,
				storeProduct,
				catalog);
	}

	private List<String> extractFormFields(final StoreProduct storeProduct) {
		return storeProduct.getProductType()
				.getModifierGroups()
				.stream()
				.map(ModifierGroup::getCode)
				.collect(toList());
	}

	private Components extractComponents(final StoreProduct storeProduct) {
		final ProductCharacteristics characteristics = productCharacteristicsService.getProductCharacteristics(storeProduct);
		if (!characteristics.isBundle()) {
			return new Components(Collections.emptyList());
		}

		return bundleIdentifier.asProductBundle(storeProduct)
				.getConstituents()
				.stream()
				.map(this::createComponent)
				.collect(collectingAndThen(toList(), Components::new));
	}

	private Component createComponent(final BundleConstituent bundle) {
		final ConstituentItem item = bundle.getConstituent();

		return item.isProductSku()
				? new Component(item.getProduct().getCode(), item.getCode(), bundle.getQuantity())
				: new Component(item.getCode(), bundle.getQuantity());
	}

	private SelectionRules extractSelectionRules(final StoreProduct storeProduct) {
		final ProductCharacteristics characteristics = productCharacteristicsService.getProductCharacteristics(storeProduct);

		if (characteristics.isDynamicBundle() && Objects.nonNull(bundleIdentifier.asProductBundle(storeProduct).getSelectionRule())) {
			return new SelectionRules(SelectionType.COMPONENT, bundleIdentifier.asProductBundle(storeProduct).getSelectionRule().getParameter());
		}

		if (!characteristics.isBundle() && storeProduct.getProductType().isMultiSku()) {
			return new SelectionRules(SelectionType.ITEM, ITEM_QUANTITY);
		}

		return new SelectionRules(SelectionType.NONE, NONE_QUANTITY);
	}

	private Item createItem(final StoreProductSku productSku, final OfferTranslationExtractor translationExtractor) {
		final String skuCode = productSku.getSkuCode();
		final String itemType = extractItemType(productSku).toString();
		final List<ItemTranslation> itemTranslations = translationExtractor.getItemTranslationsMap().get(productSku);

		if (productCharacteristicsService.getProductCharacteristics(productSku).isBundle()) {
			return new Item(skuCode, new Object(), Collections.singletonList(
					new Property(PROPERTY_ITEM_TYPE, itemType)),
					extractItemAvailabilityRules(productSku),
					extractShippingProperties(productSku), itemTranslations);
		}

		return new Item(skuCode, new Object(), Arrays.asList(
				new Property(PROPERTY_ITEM_TYPE, itemType),
				new Property(PROPERTY_TAX_CODE, taxCodeRetriever.getEffectiveTaxCode(productSku).getCode())),
				extractItemAvailabilityRules(productSku),
				extractShippingProperties(productSku), itemTranslations);
	}

	private ShippingProperties extractShippingProperties(final StoreProductSku productSku) {
		final ProductCharacteristics characteristics = productCharacteristicsService.getProductCharacteristics(productSku);

		if (!productSku.isShippable() || characteristics.isBundle()) {
			return null;
		}
		return new ShippingProperties(productSku.getWeight(), productSku.getWidth(), productSku.getLength(), productSku.getHeight(),
				settingsReader.getSettingValue("COMMERCE/SYSTEM/UNITS/weight").getValue(),
				settingsReader.getSettingValue("COMMERCE/SYSTEM/UNITS/length").getValue());
	}

	private ItemType extractItemType(final ProductSku productSku) {
		final ItemType type;
		if (productCharacteristicsService.getProductCharacteristics(productSku).isBundle()) {
			type = ItemType.BUNDLE;
		} else if (productSku.isDigital()) {
			type = ItemType.DIGITAL;
		} else {
			type = ItemType.PHYSICAL;
		}

		return type;
	}

	private List<Property> extractOfferProperties(final Product product) {
		final String offerType = product.getProductType().getName();
		final boolean notSoldSeparately = product.isNotSoldSeparately();
		final int minOrderQty = product.getMinOrderQty();
		final ProductCharacteristics productCharacteristics = productCharacteristicsService.getProductCharacteristics(product);

		if (productCharacteristics.isBundle()) {
			return new OfferPropertyExtractor(offerType, notSoldSeparately, minOrderQty, productCharacteristics).getPropertyList();
		}
		return new OfferPropertyExtractor(offerType, notSoldSeparately, minOrderQty, null).getPropertyList();
	}

	private AvailabilityRules extractItemAvailabilityRules(final ProductSku productSku) {
		ZonedDateTime enableDateTime = null;
		if (Objects.nonNull(productSku.getStartDate())) {
			enableDateTime = ZonedDateTime.ofInstant(productSku.getStartDate().toInstant(), ZoneId.of(GMT));
		}

		ZonedDateTime disableDateTime = null;
		if (Objects.nonNull(productSku.getEndDate())) {
			disableDateTime = ZonedDateTime.ofInstant(productSku.getEndDate().toInstant(), ZoneId.of(GMT));
		}

		return new AvailabilityRules(enableDateTime, disableDateTime);
	}

	private OfferAvailabilityRules extractOfferAvailabilityRules(final StoreProduct storeProduct) {
		ZonedDateTime enableDateTime = null;
		if (Objects.nonNull(storeProduct.getStartDate())) {
			enableDateTime = ZonedDateTime.ofInstant(storeProduct.getStartDate().toInstant(), ZoneId.of(GMT));
		}

		ZonedDateTime disableDateTime = null;
		if (Objects.nonNull(storeProduct.getEndDate())) {
			disableDateTime = ZonedDateTime.ofInstant(storeProduct.getEndDate().toInstant(), ZoneId.of(GMT));
		}

		ZonedDateTime releaseDateTime = null;
		if (Objects.nonNull(storeProduct.getExpectedReleaseDate())) {
			releaseDateTime = ZonedDateTime.ofInstant(storeProduct.getExpectedReleaseDate().toInstant(), ZoneId.of(GMT));
		}

		final Set<String> discoverRules = storeProduct.getDiscoverRules().stream().map(StoreAvailabilityRule::getName).collect(toSet());
		final Set<String> viewRules = storeProduct.getViewRules().stream().map(StoreAvailabilityRule::getName).collect(toSet());
		final Set<String> addToCartRules = storeProduct.getAddToCartRules().stream().map(StoreAvailabilityRule::getName).collect(toSet());

		return new OfferAvailabilityRules(enableDateTime, disableDateTime, releaseDateTime, discoverRules, viewRules,
				addToCartRules);
	}

	private List<Association> extractAssociations(final String productCode, final String catalogCode) {
		return productAssociationService.getAssociations(productCode, catalogCode, true).stream()
				.filter(ProductAssociation::canSyndicate)
				.collect(groupingBy(productAssociation -> productAssociation.getAssociationType().getName()))
				.entrySet().stream()
				.map(entry -> new Association(entry.getKey().toLowerCase(), convertToAssociationValues(entry.getValue())))
				.collect(toList());
	}

	private List<AssociationValue> convertToAssociationValues(final List<ProductAssociation> productAssociations) {
		return productAssociations.stream()
				.sorted(comparingInt(ProductAssociation::getOrdering))
				.map(this::getAssociationValue).collect(toList());
	}

	private AssociationValue getAssociationValue(final ProductAssociation productAssociation) {
		return new AssociationValue(productAssociation.getTargetProduct().getCode(), convertToZonedDateTime(productAssociation.getStartDate()),
				convertToZonedDateTime(productAssociation.getEndDate()));
	}

	private ZonedDateTime convertToZonedDateTime(final Date date) {
		return Optional.ofNullable(date)
				.map(dateValue -> ZonedDateTime.ofInstant(dateValue.toInstant(), ZoneId.of("GMT")))
				.orElse(null);
	}

	private Brand extractBrand(final StoreProduct storeProduct, final Store store) {
		if (Objects.isNull(storeProduct.getBrand())) {
			return null;
		}

		return getReader(BrandReaderCapability.class).get(store.getCode(), storeProduct.getBrand().getCode())
				.orElse(null);
	}

	private List<Option> extractOptions(final StoreProduct storeProduct, final Store store) {
		return getReader(OptionReaderCapability.class).findAllWithCodes(store.getCode(), storeProduct.getProductType()
				.getSkuOptions()
				.stream()
				.map(SkuOption::getOptionKey)
				.collect(toList()));
	}

	private List<Attribute> extractDetails(final StoreProduct storeProduct, final Store store) {
		final List<String> codes = Stream.concat(Stream.of(storeProduct.getAttributeValueMap()),
				Optional.ofNullable(storeProduct.getStoreProductSkus()).orElse(Collections.emptySet()).stream().map(ProductSku::getAttributeValueMap))
				.map(Map::values).flatMap(attributeValues -> attributeValues.stream().map(AttributeValue::getAttribute))
				.map(com.elasticpath.domain.attribute.Attribute::getKey)
				.collect(toList());
		return getReader(AttributeReaderCapability.class).findAllWithCodes(store.getCode(), codes);
	}

	private <T extends CatalogReaderCapability> T getReader(final Class<T> capabilityClass) {
		return provider.getCatalogProjectionPlugin()
				.getReaderCapability(capabilityClass)
				.orElseThrow(NoCapabilityMatchedException::new);
	}
}
