package com.elasticpath.definitions.cm;

import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import cucumber.api.java.en.Given;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.elasticpath.definitions.utils.DataHelper;
import com.elasticpath.selenium.domainobjects.Attribute;
import com.elasticpath.selenium.domainobjects.Brand;
import com.elasticpath.selenium.domainobjects.CartItemModiferGroupField;
import com.elasticpath.selenium.domainobjects.CartItemModifierGroup;
import com.elasticpath.selenium.domainobjects.Category;
import com.elasticpath.selenium.domainobjects.CategoryType;
import com.elasticpath.selenium.domainobjects.Product;
import com.elasticpath.selenium.domainobjects.ProductType;
import com.elasticpath.selenium.domainobjects.SkuOption;
import com.elasticpath.selenium.domainobjects.containers.AttributeContainer;
import com.elasticpath.selenium.domainobjects.containers.CategoryContainer;
import com.elasticpath.selenium.domainobjects.containers.ProductContainer;
import com.elasticpath.selenium.domainobjects.containers.SkuOptionContainer;
import com.elasticpath.selenium.util.Utility;

/**
 * Given steps which determine the state before the test.
 * This steps do not populate any data but are used for better human readability of the tests only.
 */
//	CHECKSTYLE:OFF: checkstyle:too many parameters
public class GivenDefinition {

	private final SkuOptionContainer optionContainer;
	private SkuOption skuOption;
	private final Brand brand;
	private final CartItemModifierGroup group;
	private final Attribute attribute;
	private final CategoryContainer categoryContainer;
	private final CategoryType categoryType;
	private final AttributeContainer attributeContainer;
	private final ProductType productType;
	private final Product product;
	private final Category category;
	private final ProductContainer productContainer;
	private static final Logger LOGGER = Logger.getLogger(GivenDefinition.class);

	/**
	 * Constructor.
	 *
	 * @param optionContainer    object to pass sku options state.
	 * @param skuOption          object to pass sku option state.
	 * @param brand              object to pass brand state.
	 * @param group              object to pass cart item modifier group state.
	 * @param attribute          object to pass attribute state.
	 * @param categoryContainer  object to pass categories state.
	 * @param categoryType       object to pass category type state.
	 * @param attributeContainer object to pass attributes state.
	 * @param productType        object to pass product type state.
	 * @param category           object to pass category state.
	 * @param productContainer   object to pass products state.
	 */
	@SuppressWarnings({"PMD.ExcessiveParameterList"})
	public GivenDefinition(
			final SkuOptionContainer optionContainer,
			final SkuOption skuOption,
			final Brand brand,
			final CartItemModifierGroup group,
			final Attribute attribute,
			final CategoryContainer categoryContainer,
			final CategoryType categoryType,
			final AttributeContainer attributeContainer,
			final ProductType productType,
			final Product product,
			final Category category,
			final ProductContainer productContainer) {
		this.optionContainer = optionContainer;
		this.skuOption = skuOption;
		this.brand = brand;
		this.group = group;
		this.attribute = attribute;
		this.categoryContainer = categoryContainer;
		this.categoryType = categoryType;
		this.attributeContainer = attributeContainer;
		this.productType = productType;
		this.product = product;
		this.category = category;
		this.productContainer = productContainer;
	}

	/**
	 * Indicates that there is catalog with a given code in Db.
	 *
	 * @param catalog existing catalog
	 */
	@Given("^I have catalog (.+)$")
	public void haveCatalog(final String catalog) {
		//This step is used only to give a better understanding of pre-existing state of the system
	}

	/**
	 * Indicates that there is virtual catalog with a given code in Db.
	 *
	 * @param virtualCatalog existing catalog
	 */
	@Given("^I have virtual catalog (.+)$")
	public void haveVirtualCatalog(final String virtualCatalog) {
		//This step is used only to give a better understanding of pre-existing state of the system
	}

	/**
	 * Indicates that there are two stores with the given codes in Db and they are connected with a given catalog.
	 *
	 * @param storeOne existing store
	 * @param storeTwo existing store
	 * @param catalog  existing catalog
	 */
	@Given("^I have stores (.+), (.+) connected to (.+) (?:virtual|)catalog$")
	public void haveTwoStores(final String storeOne, final String storeTwo, final String catalog) {
		//This step is used only to give a better understanding of pre-existing state of the system
	}

	/**
	 * Indicates that there is store with the given code in Db and it is connected with a given catalog.
	 *
	 * @param store   existing store
	 * @param catalog existing catalog
	 */
	@Given("^I have store (.+) connected to (.+) (?:virtual|)catalog$")
	public void haveStore(final String store, final String catalog) {
		//This step is used only to give a better understanding of pre-existing state of the system
	}

	/**
	 * Indicates that there is sku option in catalog with a given code in Db.
	 *
	 * @param code              existing sku option code
	 * @param firstOptionValue  the first existing sku option value code
	 * @param secondOptionValue the second existing sku option value code
	 * @param languages         list of sku option languages
	 */
	@Given("^I have sku option with code (.+) and two sku option values with codes (.+) and (.+) and the following languages$")
	public void haveOption(final String code, final String firstOptionValue, final String secondOptionValue, final List<String> languages) {
		this.skuOption.setCode(code);
		for (String language : languages) {
			this.skuOption.addSkuOptionValue(language, firstOptionValue, "");
			this.skuOption.addSkuOptionValue(language, secondOptionValue, "");
		}
		this.optionContainer.addSkuOption(this.skuOption);
	}

	/**
	 * Indicates that there is brand in catalog with a given code in Db.
	 *
	 * @param code  existing brand code
	 * @param names existing brand names
	 */
	@Given("^I have brand with code (.+)$")
	public void haveBrand(final String code, final Map<String, String> names) {
		this.brand.setCode(code);
		if (!names.isEmpty()) {
			for (Map.Entry<String, String> name : names.entrySet()) {
				this.brand.setName(name.getKey(), name.getValue());
			}
		}
	}

	/**
	 * Indicates that there is product type in catalog with a given name in Db.
	 *
	 * @param name       existing product type name
	 * @param cartGroups cart item modifier groups of existing product type
	 */
	@Given("^I have product type (.+) with cart item modifier groups$")
	public void haveProductType(final String name, final List<String> cartGroups) {
		this.productType.setProductTypeName(name);
		this.productType.setCartItemModifierGroup(cartGroups);
	}

	/**
	 * Indicates that there is cart item modifier group in catalog with a given code in Db.
	 *
	 * @param code       existing cart item modifier group code
	 * @param parameters cart item modifier group parameters
	 */
	@Given("^I have cart item modifier group with code (.+) and the following parameters$")
	public void haveGroup(final String code, final Map<String, String> parameters) {
		haveSimpleGroup(code);
		CartItemModiferGroupField field1 = new CartItemModiferGroupField();
		field1.setFieldCode(parameters.get("field1Code"));
		this.group.addField(field1);
		CartItemModiferGroupField field2 = new CartItemModiferGroupField();
		field2.setFieldCode(parameters.get("field2Code"));
		this.group.addField(field2);
		CartItemModiferGroupField field3 = new CartItemModiferGroupField();
		field3.setFieldCode(parameters.get("field3Code"));
		field3.setOption(parameters.get("field3Option1Code"), parameters.get("field3Option1Lang1"), "");
		field3.setOption(parameters.get("field3Option1Code"), parameters.get("field3Option1Lang2"), "");
		field3.setOption(parameters.get("field3Option2Code"), parameters.get("field3Option2Lang1"), "");
		field3.setOption(parameters.get("field3Option2Code"), parameters.get("field3Option2Lang2"), "");
		this.group.addField(field3);
	}

	/**
	 * Indicates that there is cart item modifier group in catalog with a given code in Db.
	 *
	 * @param code existing cart item modifier group code
	 */
	@Given("^I have cart item modifier group with group code (.+) without fields$")
	public void haveSimpleGroup(final String code) {
		this.group.setGroupCode(code);
	}

	/**
	 * Indicates that there is attribute in catalog with a given key in Db.
	 *
	 * @param key existing attribute key (code)
	 */
	@Given("^I have attribute with key (.+)$")
	public void haveAttribute(final String key) {
		this.attribute.setKey(key);
		this.attributeContainer.addAtribute(this.attribute);
	}

	/**
	 * Indicates that there is attribute in catalog with a given key in Db.
	 *
	 * @param key   existing attribute key (code)
	 * @param names attribute localized names as language-name pairs.
	 */
	@Given("^I have attribute with attribute key (.+) and the following names$")
	public void haveAttributeWithNames(final String key, final Map<String, String> names) {
		Attribute att = new Attribute();
		att.setKey(key);
		att.setAttributeKey(key);
		for (Map.Entry<String, String> name : names.entrySet()) {
			att.setName(name.getKey(), name.getValue());
		}
		this.attributeContainer.addAtribute(att);
	}

	/**
	 * Indicates that there is top level category in catalog with a given code in Db.
	 *
	 * @param code  existing category code
	 * @param names category localized language-name pairs
	 */
	@Given("^I have top level category (.+)$")
	public void haveTopLevelCategory(final String code, final Map<String, String> names) {
		Category category = new Category();
		category.setCategoryCode(code);
		for (Map.Entry<String, String> name : names.entrySet()) {
			category.setName(name.getKey(), name.getValue());
		}
		category.setCategoryName(names.get("English"));
		this.categoryContainer.addCategory(category);
		this.category.setCategoryName(names.get("English"));
		this.category.setCategoryCode(code);
	}

	/**
	 * Indicates that there is subcategory in catalog with a given code in Db.
	 *
	 * @param code       existing subcategory code
	 * @param parameters subcategory parameters
	 */
	@Given("^I have subcategory (.+) with the following parameters$")
	public void haveSubcategory(final String code, final Map<String, String> parameters) {
		Category category = new Category();
		category.setCategoryCode(code);
		category.setName(parameters.get("defaultLanguage"), parameters.get("defaultName"));
		category.setCategoryName(parameters.get("defaultName"));
		category.setParentCategory(parameters.get("parentName"));
		this.categoryContainer.addCategory(category);
	}

	/**
	 * Indicates that there is category type in catalog with a given name in Db.
	 *
	 * @param type       existing category type name
	 * @param attributes category type attributes
	 */
	@Given("^I have category type (.+) with the following attributes$")
	public void haveCategoryType(final String type, final List<String> attributes) {
		this.categoryType.setCategoryTypeName(type);
		if (!attributes.isEmpty()) {
			this.categoryType.setAttribute(attributes);
		}
	}

	/**
	 * Indicates that there is sku option in catalog with a given code in Db.
	 *
	 * @param code  existing sku option code
	 * @param names existing sku option localized names
	 */
	@Given("^I have sku option (.+) with the following names$")
	public void haveOption(final String code, final Map<String, String> names) {
		SkuOption option = new SkuOption();
		option.setCode(code);
		for (Map.Entry<String, String> name : names.entrySet()) {
			option.setName(name.getKey(), name.getValue());
		}
		this.skuOption = option;
		this.optionContainer.addSkuOption(option);
	}

	/**
	 * Indicates that there are sku option value in existing sku option in Db.
	 *
	 * @param valueCode  existing sku option value code
	 * @param optionCode existing sku option code
	 * @param names      existing sku option value localized names
	 */
	@Given("^I have option value (.+) in sku option (.+) with the following names$")
	public void haveOptionValue(final String valueCode, final String optionCode, final Map<String, String> names) {
		SkuOption option = this.optionContainer.getSkuOptionByPartialCode(optionCode);
		for (Map.Entry<String, String> name : names.entrySet()) {
			option.addSkuOptionValue(name.getKey(), valueCode, name.getValue());
		}
	}

	/**
	 * Indicates that there is product in catalog with a given code in Db.
	 *
	 * @param code       existing product code
	 * @param parameters product parameters
	 * @throws ParseException while trying to parse date.
	 */
	@Given("^I have product with code (.+) and the following parameters$")
	public void haveProduct(final String code, final Map<String, String> parameters) throws ParseException {
		this.product.setProductCode(code);
		this.product.setProductType(parameters.get("productType"));
		this.product.setCatalog(parameters.get("catalog"));
		this.product.setCategory(parameters.get("category"));
		this.product.setProductName(parameters.get("productName"));
		this.product.setStoreVisible(parameters.get("storeVisible"));
		String enableDate = Optional
				.ofNullable(parameters.get("enableDate"))
				.filter(StringUtils::isNotEmpty)
				.orElse("");
		enableDate = getProjectionDate(enableDate);
		this.product.setEnableDateTimeDays(enableDate);
		String disableDate = Optional
				.ofNullable(parameters.get("disableDate"))
				.filter(StringUtils::isNotEmpty)
				.orElse("");
		disableDate = getProjectionDate(disableDate);
		this.product.setDisableDateTimeDays(disableDate);
		Product product = new Product(this.product);
		this.productContainer.addProducts(product);
	}

	private String getProjectionDate(final String date) throws ParseException {
		String fomattedDate = "";
		if (DataHelper.CM_UI_DATE_PATTERN.matcher(date).matches()) {
			fomattedDate = DataHelper.getProjectionDate(date);
		} else if (!"".equals(date)) {
			try {
				fomattedDate = DataHelper.getProjectionDate(Utility.getDateTimeWithPlus(Integer.valueOf(date)));
			} catch (ParseException | NumberFormatException e) {
				LOGGER.error("Could not parse date", e);
			}
		}
		return fomattedDate;
	}
}
