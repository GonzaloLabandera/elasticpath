package com.elasticpath.definitions.jms;

import static org.assertj.core.api.Assertions.fail;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import cucumber.api.java.en.Then;
import org.apache.commons.lang.StringUtils;
import org.json.simple.JSONObject;

import com.elasticpath.jms.cucumber.asserts.RawJsonTestFacade;
import com.elasticpath.jms.cucumber.definitions.RawJsonDefinitions;
import com.elasticpath.jms.utilities.KeyValue;
import com.elasticpath.selenium.domainobjects.Attribute;
import com.elasticpath.selenium.domainobjects.Brand;
import com.elasticpath.selenium.domainobjects.CartItemModifierGroup;
import com.elasticpath.selenium.domainobjects.Product;
import com.elasticpath.selenium.domainobjects.Category;
import com.elasticpath.selenium.domainobjects.containers.CategoryContainer;
import com.elasticpath.selenium.domainobjects.SkuOption;
import com.elasticpath.selenium.domainobjects.Store;

/**
 * Syndication JMS steps.
 */
public class JMSDefinition {

	private static final String OPTION = "option";
	private static final String CART_ITEM_MODIFIER_GROUP = "fieldMetadata";
	private static final String BRAND = "brand";
	private static final String ATTRIBUTE = "attribute";
	private static final String OFFER = "offer";
	private static final String CATEGORY = "category";
	private static final String EVENT_TYPE = "eventType";
	private static final String DATA = "data";
	private final RawJsonDefinitions rawJsonDefinitions;
	private final CartItemModifierGroup cartItemModifierGroup;
	private final SkuOption skuOption;
	private final Store store;
	private final Brand brand;
	private final Attribute attribute;
	private final Product product;
	private final CategoryContainer categoryContainer;
	private final Category category;

	/**
	 * Constructor.
	 *
	 * @param rawJsonDefinitions    RawJsonDefinitions
	 * @param store                 store state object
	 * @param attribute             attribute state object
	 * @param brand                 brand state object
	 * @param cartItemModifierGroup cart item modifier group state object
	 * @param skuOption             sku option state object
	 * @param product               product state object
	 * @param categoryContainer     category сontainer state object
	 * @param category              category state object
	 */
	@SuppressWarnings({"PMD.ExcessiveParameterList", "checkstyle:parameternumber"})
	public JMSDefinition(final RawJsonDefinitions rawJsonDefinitions, final SkuOption skuOption, final Brand brand,
						 final Attribute attribute, final Store store, final CartItemModifierGroup cartItemModifierGroup, final Product product,
						 final CategoryContainer categoryContainer, final Category category) {
		this.rawJsonDefinitions = rawJsonDefinitions;
		this.cartItemModifierGroup = cartItemModifierGroup;
		this.skuOption = skuOption;
		this.attribute = attribute;
		this.brand = brand;
		this.store = store;
		this.product = product;
		this.categoryContainer = categoryContainer;
		this.category = category;
	}

	/**
	 * Verifies that catalog event JMS message jason has expected parameters.
	 *
	 * @param store      store code which should be specified in a Json body
	 * @param parameters key value pairs which should be specified in a Json body
	 **/
	@Then("^Catalog event JMS message json for store (.+) contains the following values$")
	public void checkDomainMessageJson(final String store, final List<KeyValue> parameters) {
		final int classMapIndex = 0;
		final int nameMapIndex = 1;
		final int guidMapIndex = 2;
		final int typeMapIndex = 3;
		final int storeMapIndex = 4;
		final int codeMapIndex = 5;
		String code = Optional.ofNullable(parameters.get(codeMapIndex).getValue())
				.filter(StringUtils::isNotEmpty)
				.orElse(determineCodeByType(parameters.get(typeMapIndex).getValue()));
		String storeCode = Optional.ofNullable(parameters.get(storeMapIndex).getValue())
				.filter(StringUtils::isNotEmpty)
				.orElseGet(this.store::getCode);
		Optional<JSONObject> eventMessageJson = rawJsonDefinitions.getJsonObjectList().stream()
				.filter(jsonObject -> jsonObject.toString().contains(store))
				.findFirst();
		if (eventMessageJson.isPresent()) {
			RawJsonTestFacade.verifyJsonValues(
					Collections.singletonList(eventMessageJson.get()),
					EVENT_TYPE,
					parameters.get(classMapIndex).getKey(),
					parameters.get(classMapIndex).getValue()
			);
			RawJsonTestFacade.verifyJsonValues(
					Collections.singletonList(eventMessageJson.get()),
					EVENT_TYPE,
					parameters.get(nameMapIndex).getKey(),
					parameters.get(nameMapIndex).getValue()
			);
			RawJsonTestFacade.verifyJsonValues(
					Collections.singletonList(eventMessageJson.get()),
					"",
					parameters.get(guidMapIndex).getKey(),
					parameters.get(guidMapIndex).getValue()
			);
			RawJsonTestFacade.verifyJsonValues(
					Collections.singletonList(eventMessageJson.get()),
					DATA,
					parameters.get(typeMapIndex).getKey(),
					parameters.get(typeMapIndex).getValue()
			);
			RawJsonTestFacade.verifyJsonValues(
					Collections.singletonList(eventMessageJson.get()), DATA, parameters.get(storeMapIndex).getKey(), storeCode);
			RawJsonTestFacade.verifyJsonValues(
					Collections.singletonList(eventMessageJson.get()), DATA, parameters.get(codeMapIndex).getKey(), code);
		} else {
			fail("There is no message for provided store");
		}
	}

	/**
	 * Verifies that catalog event JMS message jason has expected parameters.
	 *
	 * @param categoryName categoryName which should be specified in a Json body
	 * @param parameters   key value pairs which should be specified in a Json body
	 **/
	@Then("^Catalog event JMS message json for category (.+) contains the following values$")
	public void checkDomainMessageJsonForCategory(final String categoryName, final List<KeyValue> parameters) {
		final int classMapIndex = 0;
		final int nameMapIndex = 1;
		final int guidMapIndex = 2;
		final int typeMapIndex = 3;
		final int storeMapIndex = 4;
		final int codeMapIndex = 5;
		String code = Optional.ofNullable(parameters.get(codeMapIndex).getValue())
				.filter(StringUtils::isNotEmpty)
				.orElse(categoryContainer.getCategoryMap().get(categoryContainer.getFullCategoryNameByPartialName(categoryName)).getCategoryCode());
		String storeCode = Optional.ofNullable(parameters.get(storeMapIndex).getValue())
				.filter(StringUtils::isNotEmpty)
				.orElseGet(this.store::getCode);
		Optional<JSONObject> eventMessageJson = rawJsonDefinitions.getJsonObjectList().stream()
				.filter(jsonObject -> jsonObject.toString().contains(storeCode)
						&& jsonObject.toString().contains(categoryContainer.getCategoryMap()
						.get(categoryContainer.getFullCategoryNameByPartialName(categoryName))
						.getCategoryCode())).findFirst();
		if (eventMessageJson.isPresent()) {
			RawJsonTestFacade.verifyJsonValues(
					Collections.singletonList(eventMessageJson.get()),
					EVENT_TYPE,
					parameters.get(classMapIndex).getKey(),
					parameters.get(classMapIndex).getValue()
			);
			RawJsonTestFacade.verifyJsonValues(
					Collections.singletonList(eventMessageJson.get()),
					EVENT_TYPE,
					parameters.get(nameMapIndex).getKey(),
					parameters.get(nameMapIndex).getValue()
			);
			RawJsonTestFacade.verifyJsonValues(
					Collections.singletonList(eventMessageJson.get()),
					"",
					parameters.get(guidMapIndex).getKey(),
					parameters.get(guidMapIndex).getValue()
			);
			RawJsonTestFacade.verifyJsonValues(
					Collections.singletonList(eventMessageJson.get()),
					DATA,
					parameters.get(typeMapIndex).getKey(),
					parameters.get(typeMapIndex).getValue()
			);
			RawJsonTestFacade.verifyJsonValues(
					Collections.singletonList(eventMessageJson.get()), DATA, parameters.get(storeMapIndex).getKey(), storeCode);
			RawJsonTestFacade.verifyJsonValues(
					Collections.singletonList(eventMessageJson.get()), DATA, parameters.get(codeMapIndex).getKey(), code);
		} else {
			fail("There is no message for provided store");
		}
	}

	/**
	 * Determine code by data type.
	 *
	 * @param type data type.
	 */
	private String determineCodeByType(final String type) {
		String code = null;
		switch (type) {
			case ATTRIBUTE:
				code = attribute.getKey();
				break;
			case BRAND:
				code = brand.getCode();
				break;
			case OPTION:
				code = skuOption.getCode();
				break;
			case CART_ITEM_MODIFIER_GROUP:
				code = cartItemModifierGroup.getGroupCode();
				break;
			case OFFER:
				code = product.getProductCode();
				break;
			case CATEGORY:
				code = category.getCategoryCode();
				break;
			default:
				fail("There is no an existing type");
				break;
		}
		return code;
	}
}
