/**
 * Copyright (c) Elastic Path Software Inc., 2014
 */
package com.elasticpath.importexport.common.adapters.promotion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.LocaleUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.common.dto.DisplayValue;
import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.catalog.Catalog;
import com.elasticpath.domain.catalog.impl.CatalogImpl;
import com.elasticpath.domain.misc.LocalizedProperties;
import com.elasticpath.domain.misc.LocalizedPropertyValue;
import com.elasticpath.domain.rules.Rule;
import com.elasticpath.domain.rules.RuleAction;
import com.elasticpath.domain.rules.RuleElement;
import com.elasticpath.domain.rules.RuleElementType;
import com.elasticpath.domain.rules.RuleScenarios;
import com.elasticpath.domain.rules.RuleSet;
import com.elasticpath.domain.rules.RuleSetLoadTuner;
import com.elasticpath.domain.rules.impl.CartAnySkuAmountDiscountActionImpl;
import com.elasticpath.domain.rules.impl.CartAnySkuPercentDiscountActionImpl;
import com.elasticpath.domain.rules.impl.PromotionRuleImpl;
import com.elasticpath.domain.rules.impl.RuleSetImpl;
import com.elasticpath.domain.store.Store;
import com.elasticpath.domain.store.impl.StoreImpl;
import com.elasticpath.importexport.common.adapters.DomainAdapter;
import com.elasticpath.importexport.common.caching.CachingService;
import com.elasticpath.importexport.common.dto.promotion.cart.ActionDTO;
import com.elasticpath.importexport.common.dto.promotion.cart.AvailabilityDTO;
import com.elasticpath.importexport.common.dto.promotion.cart.PromotionDTO;
import com.elasticpath.importexport.common.exception.runtime.PopulationRollbackException;
import com.elasticpath.importexport.common.exception.runtime.PopulationRuntimeException;
import com.elasticpath.service.rules.RuleSetService;

/**
 * Tests for PromotionAdapter.
 */
@SuppressWarnings({ "PMD.TooManyMethods" })
@RunWith(MockitoJUnitRunner.class)
public class PromotionAdapterTest {

	private static final Date START_DATE = new Date();

	private static final Date END_DATE = new Date(START_DATE.getTime() + 2);

	private static final String STORE_CODE = "StoreCode";

	private static final String CATALOG_CODE = "CatalogCode";

	private static final String PROMOTION_DISPLAY_NAME_EN = "promotionDisplayName_en";

	private static final String PROMOTION_DISPLAY_NAME_FR = "promotionDisplayName_fr";

	@Mock
	private Rule rule;

	private final PromotionAdapter promotionAdapter = new PromotionAdapter();

	@Mock
	private BeanFactory mockBeanFactory;

	@Mock
	private CachingService cachingService;

	@Mock
	private DomainAdapter<RuleElement, ActionDTO> actionAdapter;

	private int numberOfCalls;

	@Mock
	private LocalizedProperties localizedProperties;

	private final Map<String, LocalizedPropertyValue> localizedPropertiesMap = new HashMap<>();

	@Mock
	private LocalizedPropertyValue value1;

	@Mock
	private LocalizedPropertyValue value2;

	@Mock
	private RuleSetLoadTuner mockRuleSetLoadTuner;

	private final DisplayValue displayValue1 = new DisplayValue("fr", "frValue");

	private final DisplayValue displayValue2 = new DisplayValue("en", "enValue");

	@Before
	public void setUp() throws Exception {
		promotionAdapter.setBeanFactory(mockBeanFactory);
		promotionAdapter.setCachingService(cachingService);
		promotionAdapter.setActionAdapter(actionAdapter);

		localizedPropertiesMap.put(Rule.LOCALIZED_PROPERTY_DISPLAY_NAME + "_" + displayValue1.getLanguage(), value1);
		localizedPropertiesMap.put(Rule.LOCALIZED_PROPERTY_DISPLAY_NAME + "_" + displayValue2.getLanguage(), value2);
	}

	/**
	 * Tests high level call of <code>PromotionDTO</code> population method.
	 * It should set several fields and call other methods populating parts of promotion DTO.
	 */
	@Test
	public void testPopulateDTO() {
		final PromotionDTO target = new PromotionDTO();
		final PromotionAdapter promotionAdapter = new PromotionAdapter() {
			@Override
			void populatePromotionDTOType(final PromotionDTO promotionDTO, final Catalog catalog, final Store store) {
				++numberOfCalls;
			}

			@Override
			AvailabilityDTO createAvailabilityDTO(final Rule rule) {
				++numberOfCalls;
				return null;
			}

			@Override
			List<ActionDTO> createElementDTOList(final Rule source) {
				++numberOfCalls;
				return null;
			}
		};

		final String ruleName = "Christmas discount";
		final String ruleCode = "PROMOTION_CODE";
		final String ruleDescription = "Great discount";

		when(rule.getCatalog()).thenReturn(null);
		when(rule.getStore()).thenReturn(null);
		when(rule.getName()).thenReturn(ruleName);
		when(rule.getCode()).thenReturn(ruleCode);
		when(rule.getDescription()).thenReturn(ruleDescription);
		when(rule.getLocalizedProperties()).thenReturn(localizedProperties);
		when(localizedProperties.getLocalizedPropertiesMap()).thenReturn(localizedPropertiesMap);
		when(localizedProperties.getPropertyNameFromKey(PROMOTION_DISPLAY_NAME_EN)).thenReturn("promotionDisplayName");
		when(localizedProperties.getPropertyNameFromKey(PROMOTION_DISPLAY_NAME_FR)).thenReturn("promotionDisplayName");
		when(localizedProperties.getLocaleFromKey(PROMOTION_DISPLAY_NAME_EN)).thenReturn(LocaleUtils.toLocale("en"));
		when(localizedProperties.getLocaleFromKey(PROMOTION_DISPLAY_NAME_FR)).thenReturn(LocaleUtils.toLocale("fr"));
		when(value1.getValue()).thenReturn(displayValue1.getValue());
		when(value2.getValue()).thenReturn(displayValue2.getValue());

		numberOfCalls = 0;
		final int expectedNumberOfCalls = 3;
		promotionAdapter.populateDTO(rule, target);

		assertThat(target.getName()).isEqualTo(ruleName);
		assertThat(target.getCode()).isEqualTo(ruleCode);
		assertThat(target.getDescription()).isEqualTo(ruleDescription);
		assertThat(numberOfCalls).isEqualTo(expectedNumberOfCalls);

		List<DisplayValue> displayNames = target.getDisplayNames();
		assertThat(displayNames.size()).as("target should have two display names").isEqualTo(2);

		boolean value1Found = false;
		boolean value2Found = false;
		for (DisplayValue value : displayNames) {
			if ("fr".equals(value.getLanguage())
				&& value.getValue().equals(displayValue1.getValue())) {
				value1Found = true;
			} else if ("en".equals(value.getLanguage())
					   && value.getValue().equals(displayValue2.getValue())) {
				value2Found = true;
			}
		}

		assertThat(value1Found && value2Found)
			.as("both display name should be found")
			.isTrue();

		verify(rule).getCatalog();
		verify(rule).getStore();
		verify(rule).getName();
		verify(rule).getCode();
		verify(rule).getDescription();
		verify(localizedProperties).getLocalizedPropertiesMap();
		verify(localizedProperties).getPropertyNameFromKey(PROMOTION_DISPLAY_NAME_EN);
		verify(localizedProperties).getPropertyNameFromKey(PROMOTION_DISPLAY_NAME_FR);
		verify(localizedProperties).getLocaleFromKey(PROMOTION_DISPLAY_NAME_EN);
		verify(localizedProperties).getLocaleFromKey(PROMOTION_DISPLAY_NAME_FR);
		verify(value1).getValue();
		verify(value2).getValue();
	}

	/**
	 * In neither store nor catalog is provided then promotion type is undefined.
	 * It causes an exception
	 */
	@Test(expected = PopulationRuntimeException.class)
	public void testPopulatePromotionDTOType01() {
		promotionAdapter.populatePromotionDTOType(new PromotionDTO(), null, null);
	}

	/**
	 * If store is provided and catalog is not provided then promotion type is SHOPPING_CART_TYPE.
	 */
	@Test
	public void testPopulatePromotionDTOType02() {
		final PromotionDTO promotionDto = new PromotionDTO();
		final Store store = mock(Store.class);

		when(store.getCode()).thenReturn(STORE_CODE);

		promotionAdapter.populatePromotionDTOType(promotionDto, null, store);

		assertThat(promotionDto.getStoreCode()).isEqualTo(STORE_CODE);
		assertThat(promotionDto.getCatalogCode()).isNull();
		assertThat(promotionDto.getType()).isEqualTo(PromotionAdapter.SHOPPING_CART_TYPE);
		verify(store).getCode();
	}

	/**
	 * If catalog is provided and store is not provided then promotion type is CATALOG_TYPE.
	 */
	@Test
	public void testPopulatePromotionDTOType03() {
		final PromotionDTO promotionDto = new PromotionDTO();
		final Catalog catalog = mock(Catalog.class);

		when(catalog.getCode()).thenReturn(CATALOG_CODE);

		promotionAdapter.populatePromotionDTOType(promotionDto, catalog, null);

		assertThat(promotionDto.getCatalogCode()).isEqualTo(CATALOG_CODE);
		assertThat(promotionDto.getStoreCode()).isNull();
		assertThat(promotionDto.getType()).isEqualTo(PromotionAdapter.CATALOG_TYPE);
		verify(catalog).getCode();
	}

	/**
	 * If both store and catalog are provided then promotion type is SHOPPING_CART_TYPE both codes should be set.
	 */
	@Test
	public void testPopulatePromotionDTOType04() {
		final PromotionDTO promotionDto = new PromotionDTO();
		final Catalog catalog = mock(Catalog.class);
		final Store store = mock(Store.class);

		when(catalog.getCode()).thenReturn(CATALOG_CODE);
		when(store.getCode()).thenReturn(STORE_CODE);

		promotionAdapter.populatePromotionDTOType(promotionDto, catalog, store);

		assertThat(promotionDto.getCatalogCode()).isEqualTo(CATALOG_CODE);
		assertThat(promotionDto.getStoreCode()).isEqualTo(STORE_CODE);
		assertThat(promotionDto.getType()).isEqualTo(PromotionAdapter.SHOPPING_CART_TYPE);
		verify(catalog).getCode();
		verify(store).getCode();
	}

	/**
	 * Tests createAvailabilityDTO.
	 */
	@Test
	public void testCreateAvailabilityDTO() {
		when(rule.isEnabled()).thenReturn(true);
		when(rule.getStartDate()).thenReturn(START_DATE);
		when(rule.getEndDate()).thenReturn(END_DATE);

		AvailabilityDTO availabilityDTO = promotionAdapter.createAvailabilityDTO(rule);

		assertThat(availabilityDTO.getEnabled()).isTrue();
		assertThat(availabilityDTO.getEnableDate()).isEqualTo(START_DATE);
		assertThat(availabilityDTO.getDisableDate()).isEqualTo(END_DATE);
		verify(rule).isEnabled();
		verify(rule).getStartDate();
		verify(rule).getEndDate();
	}

	/**
	 * Tests that method createElementDTOList calls populateDTOAction method with the list of actions from the source Rule.
	 */
	@Test
	public void testCreateElementDTOList() {

		final PromotionAdapter promotionAdapter = new PromotionAdapter() {
			@Override
			void populateDTOAction(final List<ActionDTO> actions, final Set<RuleAction> ruleActions) {
				++numberOfCalls;
			}
		};


		numberOfCalls = 0;
		promotionAdapter.createElementDTOList(rule);
		assertThat(numberOfCalls).isEqualTo(1);
		verify(rule).getActions();
	}

	/**
	 * Tests one step of ActionDTO population.
	 */
	@Test
	public void testPopulateDTOAction() {
		final RuleAction firstRuleAction = mock(RuleAction.class);
		final ActionDTO firstActionDto = new ActionDTO();
		final Set<RuleAction> ruleActions = new HashSet<>();
		ruleActions.add(firstRuleAction);
		final List<ActionDTO> actions = new ArrayList<>();

		when(actionAdapter.createDtoObject()).thenReturn(firstActionDto);

		promotionAdapter.populateDTOAction(actions, ruleActions);
		assertThat(actions)
			.as("Actions list should contain the firstActionDto")
			.containsOnly(firstActionDto);
		verify(actionAdapter).createDtoObject();
		verify(actionAdapter).populateDTO(firstRuleAction, firstActionDto);
	}

	/**
	 * Tests createDtoObject.
	 */
	@Test
	public void testCreateDtoObject() {
		assertThat(promotionAdapter.createDtoObject().getClass()).isEqualTo(PromotionDTO.class);
	}

	/**
	 * Tests createDomainObject.
	 */
	@Test
	public void testCreateDomainObject() {
		when(mockBeanFactory.getPrototypeBean(ContextIdNames.PROMOTION_RULE, Rule.class)).thenReturn(new PromotionRuleImpl());

		assertThat(promotionAdapter.createDomainObject()).isNotNull();
		verify(mockBeanFactory).getPrototypeBean(ContextIdNames.PROMOTION_RULE, Rule.class);
	}

	/**
	 * Store code must not be null.
	 */
	@Test(expected = PopulationRollbackException.class)
	public void testFindStoreByNullCode() {
		promotionAdapter.findStoreByCode(null);
	}

	/**
	 * Tests that <code>PromotionAdapter</code> uses <code>CachingService</code> to search for store.
	 */
	@Test
	public void testFindStoreByCode() {
		final String storeCode = "SNAPITUP";
		final Store store = new StoreImpl();

		when(cachingService.findStoreByCode(storeCode)).thenReturn(store);

		assertThat(promotionAdapter.findStoreByCode(storeCode)).isEqualTo(store);
		verify(cachingService).findStoreByCode(storeCode);
	}

	/**
	 * Catalog code must not be null.
	 */
	@Test(expected = PopulationRollbackException.class)
	public void testFindCatalogBNullyCode() {
		promotionAdapter.findCatalogByCode(null);
	}

	/**
	 * Tests that <code>PromotionAdapter</code> uses <code>CachingService</code> to search for catalog.
	 */
	@Test
	public void testFindCatalogByCode() {
		final String catalogCode = "CopperwoodAndCo";
		final Catalog catalog = new CatalogImpl();

		when(cachingService.findCatalogByCode(catalogCode)).thenReturn(catalog);

		assertThat(promotionAdapter.findCatalogByCode(catalogCode)).isEqualTo(catalog);
		verify(cachingService).findCatalogByCode(catalogCode);
	}

	/**
	 * Checks that <code>PromotionAdapter</code> throws an exception if <code>RuleAction</code> cannot be created.
	 */
	@Test(expected = PopulationRollbackException.class)
	public void testCreateRuleActionException() {
		final String actionType = RuleElementType.CART_ANY_SKU_AMOUNT_DISCOUNT_ACTION.getPropertyKey();

		when(mockBeanFactory.getPrototypeBean(actionType, RuleAction.class)).thenReturn(null);

		promotionAdapter.createRuleAction(actionType);
		verify(mockBeanFactory).getPrototypeBean(actionType, RuleAction.class);
	}

	/**
	 * Checks that <code>PromotionAdapter</code> returns <code>RuleAction</code> instantiated by bean factory.
	 */
	@Test
	public void testCreateRuleAction() {
		final String actionType = RuleElementType.CART_ANY_SKU_AMOUNT_DISCOUNT_ACTION.getPropertyKey();
		final RuleAction ruleAction = new CartAnySkuAmountDiscountActionImpl();

		when(mockBeanFactory.getPrototypeBean(actionType, RuleAction.class)).thenReturn(ruleAction);

		assertThat(promotionAdapter.createRuleAction(actionType)).isEqualTo(ruleAction);
		verify(mockBeanFactory).getPrototypeBean(actionType, RuleAction.class);
	}

	/**
	 * Tests population of <code>AvailabilityDTO</code> object.
	 *
	 * @throws ParseException if date cannot be parsed successfully
	 */
	@Test
	public void testPopulateDomainAvailability() throws ParseException {
		final Date enableDate = new Date();
		final Date disableDate = new Date();
		AvailabilityDTO availabilityDto = new AvailabilityDTO();
		availabilityDto.setEnabled(true);
		availabilityDto.setEnableDate(enableDate);
		availabilityDto.setDisableDate(disableDate);

		PromotionDTO promotionDto = new PromotionDTO();
		promotionDto.setAvailability(availabilityDto);

		promotionAdapter.populateDomainAvailability(promotionDto, rule);

		verify(rule).setEnabled(true);
		verify(rule).setStartDate(enableDate);
		verify(rule).setEndDate(disableDate);
	}

	/**
	 * Tests population of <code>Rule</code> with <code>RuleElement</code> objects.
	 */
	@Test
	public void testPopulatedDomainActions() {
		final RuleAction ruleAction1 = new CartAnySkuAmountDiscountActionImpl();
		final RuleAction ruleAction2 = new CartAnySkuPercentDiscountActionImpl();

		final ActionDTO actionDto1 = new ActionDTO();
		actionDto1.setType(RuleElementType.CART_ANY_SKU_AMOUNT_DISCOUNT_ACTION.getPropertyKey());
		final ActionDTO actionDto2 = new ActionDTO();
		actionDto2.setType(RuleElementType.CART_ANY_SKU_PERCENT_DISCOUNT_ACTION.getPropertyKey());

		final List<ActionDTO> actionDtoList = ImmutableList.of(actionDto1, actionDto2);

		when(mockBeanFactory.getPrototypeBean(RuleElementType.CART_ANY_SKU_AMOUNT_DISCOUNT_ACTION.getPropertyKey(), RuleAction.class))
			.thenReturn(ruleAction1);
		when(mockBeanFactory.getPrototypeBean(RuleElementType.CART_ANY_SKU_PERCENT_DISCOUNT_ACTION.getPropertyKey(), RuleAction.class))
			.thenReturn(ruleAction2);

		promotionAdapter.populatedDomainActions(rule, actionDtoList);

		verify(mockBeanFactory).getPrototypeBean(RuleElementType.CART_ANY_SKU_AMOUNT_DISCOUNT_ACTION.getPropertyKey(), RuleAction.class);
		verify(actionAdapter).populateDomain(actionDto1, ruleAction1);
		verify(rule).addRuleElement(ruleAction1);
		verify(mockBeanFactory).getPrototypeBean(RuleElementType.CART_ANY_SKU_PERCENT_DISCOUNT_ACTION.getPropertyKey(), RuleAction.class);
		verify(actionAdapter).populateDomain(actionDto2, ruleAction2);
		verify(rule).addRuleElement(ruleAction2);
	}

	/**
	 * Tests that if DTO object is of type CATALOG then only catalog should be set.
	 */
	@Test
	public void testPopulatePromotionDomainType1() {
		final Catalog catalog = new CatalogImpl();
		final String catalogCode = "CopperwoodAndCo";
		final PromotionDTO promotionDto = new PromotionDTO();
		promotionDto.setCatalogCode(catalogCode);
		promotionDto.setType(PromotionAdapter.CATALOG_TYPE);

		when(cachingService.findCatalogByCode(catalogCode)).thenReturn(catalog);

		promotionAdapter.populatePromotionDomainType(promotionDto, rule);

		verify(cachingService).findCatalogByCode(catalogCode);
		verify(rule).setCatalog(catalog);
	}

	/**
	 * Tests that if DTO object is of type SHOPPING_CART then only store should be set.
	 */
	@Test
	public void testPopulatePromotionDomainType2() {
		final Store store = new StoreImpl();
		final String storeCode = "SNAPITUP";
		final PromotionDTO promotionDto = new PromotionDTO();
		promotionDto.setStoreCode(storeCode);
		promotionDto.setType(PromotionAdapter.SHOPPING_CART_TYPE);

		when(cachingService.findStoreByCode(storeCode)).thenReturn(store);

		promotionAdapter.populatePromotionDomainType(promotionDto, rule);
		verify(cachingService).findStoreByCode(storeCode);
		verify(rule).setStore(store);
	}

	/**
	 * Tests that if neither Catalog nor Shopping Cart type is set then <code>PopulationRuntimeException</code> is thrown.
	 */
	@Test(expected = PopulationRollbackException.class)
	public void testPopulatePromotionDomainTypeException() {
		final PromotionDTO promotionDto = new PromotionDTO();
		promotionDto.setType("WAREHOUSE_TYPE");		// something meaningless

		promotionAdapter.populatePromotionDomainType(promotionDto, rule);
	}

	/**
	 * Tests that <code>Rule</code> is populated with <code>RuleSet</code> for Shopping Cart.
	 */
	@Test
	public void testPopulateRuleSet() {
		when(mockBeanFactory.getPrototypeBean(ContextIdNames.RULE_SET_LOAD_TUNER, RuleSetLoadTuner.class))
				.thenReturn(mockRuleSetLoadTuner);

		final PromotionDTO promotionDTO = new PromotionDTO();
		promotionDTO.setType(PromotionAdapter.SHOPPING_CART_TYPE);

		final RuleSetService ruleSetService = mock(RuleSetService.class);
		promotionAdapter.setRuleSetService(ruleSetService);
		final RuleSet ruleSet = new RuleSetImpl();

		when(ruleSetService.findByScenarioId(RuleScenarios.CART_SCENARIO, mockRuleSetLoadTuner)).thenReturn(ruleSet);

		promotionAdapter.populateRuleSet(promotionDTO, rule);

		promotionDTO.setType(PromotionAdapter.CATALOG_TYPE);

		when(ruleSetService.findByScenarioId(RuleScenarios.CATALOG_BROWSE_SCENARIO, mockRuleSetLoadTuner)).thenReturn(ruleSet);

		promotionAdapter.populateRuleSet(promotionDTO, rule);

		verify(ruleSetService).findByScenarioId(RuleScenarios.CART_SCENARIO, mockRuleSetLoadTuner);
		verify(ruleSetService).findByScenarioId(RuleScenarios.CATALOG_BROWSE_SCENARIO, mockRuleSetLoadTuner);
		verify(rule, times(2)).setRuleSet(ruleSet);
	}

	/**
	 * Verifies that required fields are set to <code>Rule</code> as a result of population.
	 */
	@Test
	public void testPopulateDomain() {
		final PromotionAdapter promotionAdapter = new PromotionAdapter() {
			/**
			 * This method is already covered by another test.
			 */
			@Override
			void populatePromotionDomainType(final PromotionDTO source, final Rule target) {
				// do nothing.
			}

			/**
			 * This method is already covered by another test.
			 */
			@Override
			void populateDomainAvailability(final PromotionDTO source, final Rule target) {
				// do nothing.
			}

			/**
			 * This method is already covered by another test.
			 */
			@Override
			public void populatedDomainActions(final Rule rule, final List<ActionDTO> actions) {
				// do nothing.
			}

			/**
			 * This method is already covered by another test.
			 */
			@Override
			void populateRuleSet(final PromotionDTO promotionDTO, final Rule target) {
				// do nothing.
			}
		};

		final PromotionDTO promotionDto = new PromotionDTO();
		final String promotionName = "Christmas discount";
		promotionDto.setName(promotionName);
		final String promotionCode = "PROMO_1234";
		promotionDto.setCode(promotionCode);
		final String description = "Joyful Christmas discount for everyone";
		promotionDto.setDescription(description);

		List<DisplayValue> displayNames = new ArrayList<>();
		displayNames.add(displayValue1);
		displayNames.add(displayValue2);
		promotionDto.setDisplayNames(displayNames);

		when(rule.getLocalizedProperties()).thenReturn(localizedProperties);

		promotionAdapter.populateDomain(promotionDto, rule);

		verify(rule).setName(promotionName);
		verify(rule).setCode(promotionCode);
		verify(rule).setDescription(description);
		verify(rule).getLocalizedProperties();
		verify(localizedProperties).setValue(Rule.LOCALIZED_PROPERTY_DISPLAY_NAME, LocaleUtils.toLocale("fr"), displayValue1.getValue());
		verify(localizedProperties).setValue(Rule.LOCALIZED_PROPERTY_DISPLAY_NAME, LocaleUtils.toLocale("en"), displayValue2.getValue());
	}
}
