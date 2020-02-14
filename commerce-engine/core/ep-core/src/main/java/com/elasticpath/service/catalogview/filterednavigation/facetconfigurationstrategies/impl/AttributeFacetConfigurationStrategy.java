/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.service.catalogview.filterednavigation.facetconfigurationstrategies.impl;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;

import org.apache.commons.lang.LocaleUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.elasticpath.common.dto.search.RangeFacet;
import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.attribute.Attribute;
import com.elasticpath.domain.catalogview.AttributeFilter;
import com.elasticpath.domain.catalogview.AttributeRangeFilter;
import com.elasticpath.domain.catalogview.AttributeValueFilter;
import com.elasticpath.domain.catalogview.FilterDisplayInfo;
import com.elasticpath.domain.catalogview.RangeFilter;
import com.elasticpath.domain.catalogview.impl.FilterDisplayInfoImpl;
import com.elasticpath.domain.search.Facet;
import com.elasticpath.domain.search.FacetGroup;
import com.elasticpath.domain.search.FacetType;
import com.elasticpath.service.attribute.AttributeService;
import com.elasticpath.service.attribute.impl.AttributeValueInfo;
import com.elasticpath.service.catalogview.filterednavigation.FilteredNavigationConfiguration;
import com.elasticpath.service.catalogview.filterednavigation.facetconfigurationstrategies.FacetConfigurationStrategy;


/**
 * Attribute facet configuration strategy.
 */
public class AttributeFacetConfigurationStrategy implements FacetConfigurationStrategy {
	private static final Logger LOG = Logger.getLogger(AttributeFacetConfigurationStrategy.class);
	private static final String LOWER_BOUND_GREATER_THAN_UPPER_BOUND_ERROR =
			"Range facet lower bound %f is higher than upper bound %f for Facet with uidpk %d%n";

	private static final String DASH = "-";
	private static final String UNDERSCORE = "_";

	private BeanFactory beanFactory;
	private AttributeService attributeService;

	@Override
	public boolean shouldProcess(final Facet facet) {
		return facet.getFacetGroup() != FacetGroup.SKU_OPTION.getOrdinal()
				|| facet.getFacetGroup() != FacetGroup.FIELD.getOrdinal();
	}

	@Override
	public void process(final FilteredNavigationConfiguration config, final Facet facet) {
		String attributeKey = facet.getBusinessObjectId();
		Attribute rootAttribute;
		if (facet.getFacetType() == FacetType.FACET.getOrdinal()) {
			AttributeValueFilter rootFilter = getBeanFactory().getPrototypeBean(ContextIdNames.ATTRIBUTE_FILTER, AttributeValueFilter.class);
			rootFilter.setId(attributeKey);
			rootFilter.setAttributeKey(attributeKey);
			rootAttribute = rootFilter.getAttribute();
			rootFilter.setLocalized(rootAttribute.isLocaleDependant());
			config.getAllAttributeSimpleValues().put(rootFilter.getId(), rootFilter);
			setAttributeFilter(config, rootFilter, rootAttribute, facet);
		} else {
			AttributeRangeFilter rootFilter = getBeanFactory().getPrototypeBean(ContextIdNames.ATTRIBUTE_RANGE_FILTER, AttributeRangeFilter.class);
			rootFilter.setId(attributeKey);
			rootFilter.setAttributeKey(attributeKey);
			rootAttribute = rootFilter.getAttribute();
			rootFilter.setLocalized(rootAttribute.isLocaleDependant());
			config.getAllAttributeRanges().put(rootFilter.getId(), rootFilter);
			setAttributeRangeFilter(config, facet, rootFilter);
		}
		config.getAllAttributesMap().put(attributeKey, rootAttribute);
		config.getFacetMap().put(facet.getFacetGuid(), facet);
		config.getAttributeGuidMap().put(facet.getBusinessObjectId(), facet.getFacetGuid());
	}

	private void setAttributeFilter(final FilteredNavigationConfiguration config, final AttributeValueFilter rootFilter,
									final Attribute rootAttribute, final Facet facet) {
		Set<String> values = new HashSet<>();
		List<AttributeValueInfo> attributeValues;
		if (facet.getFacetGroup() == FacetGroup.SKU_ATTRIBUTE.getOrdinal()) {
			attributeValues = attributeService.findProductSkuValueAttributeByAttributeUid(rootAttribute);
		} else {
			attributeValues = attributeService.findProductAttributeValueByAttributeUid(rootAttribute);
		}

		for (AttributeValueInfo value : attributeValues) {
			String valueString = value.getValue();
			if (values.contains(valueString)) {
				continue;
			}
			values.add(valueString);
			AttributeValueFilter attributeFilter = getBeanFactory().getPrototypeBean(ContextIdNames.ATTRIBUTE_FILTER, AttributeValueFilter.class);
			Map<String, Object> filterProperties = new HashMap<>();
			attributeFilter.setLocalized(rootFilter.isLocalized());
			filterProperties.put(AttributeFilter.ATTRIBUTE_PROPERTY, rootAttribute);

			filterProperties.put(AttributeFilter.ATTRIBUTE_VALUES_ALIAS_PROPERTY, valueString);

			filterProperties.put(AttributeValueFilter.ATTRIBUTE_VALUE_PROPERTY, valueString);

			attributeFilter.initialize(filterProperties);

			attributeFilter.setDisplayName(valueString);

			addIdentifier(config, rootAttribute, value, attributeFilter);
		}

	}

	private void addIdentifier(final FilteredNavigationConfiguration config, final Attribute rootAttribute, final AttributeValueInfo value,
							   final AttributeValueFilter attributeFilter) {
		String identicalMapKey;
		if (attributeFilter.isLocalized()) {
			String language = value.getLocalizedAttributeKey().replace(rootAttribute.getKey() + UNDERSCORE, StringUtils.EMPTY);
			try {
				attributeFilter.setLocale(LocaleUtils.toLocale(language));
			} catch (IllegalArgumentException exception) {
				LOG.error("Illegal argument", exception);
				return;
			}
			identicalMapKey = String.valueOf(attributeFilter.getLocale()).concat(String.valueOf(attributeFilter.getAttributeValue()));
		} else {
			identicalMapKey = String.valueOf(attributeFilter.getAttributeValue());
		}

		final SortedMap<String, AttributeValueFilter> attributeValuesMap = config.getAttributeSimpleValuesMap(attributeFilter.getAttributeKey());
		attributeValuesMap.put(identicalMapKey, attributeFilter);
		config.getAllAttributeSimpleValues().put(attributeFilter.getId(), attributeFilter);
	}

	private void setAttributeRangeFilter(final FilteredNavigationConfiguration config, final Facet facet,
										 final AttributeRangeFilter rootFilter) {
		SortedSet<RangeFacet> sortedRangeFacets = facet.getSortedRangeFacet();
		for (RangeFacet rangeFacet : sortedRangeFacets) {
			AttributeRangeFilter attributeRangeFilter = getBeanFactory().getPrototypeBean(ContextIdNames.ATTRIBUTE_RANGE_FILTER, 
					AttributeRangeFilter.class);
			Map<String, Object> filterProperties = new HashMap<>();
			final BigDecimal lowerBound = rangeFacet.getStart();
			boolean noLowerBound = lowerBound == null;
			final BigDecimal upperBound = rangeFacet.getEnd();
			boolean noUpperBound = upperBound == null;
			if (lowerBoundGreaterThanUpperBound(lowerBound, noLowerBound, upperBound, noUpperBound)) {
				LOG.warn(String.format(LOWER_BOUND_GREATER_THAN_UPPER_BOUND_ERROR, lowerBound, upperBound, facet.getUidPk()));
				continue;
			}
			attributeRangeFilter.setLocalized(attributeRangeFilter.isLocalized());
			final String lowerValue = noLowerBound ? null : lowerBound.toString();
			final String upperValue = noUpperBound ? null : upperBound.toString();

			filterProperties.put(AttributeFilter.ATTRIBUTE_PROPERTY, rootFilter.getAttribute());
			filterProperties.put(RangeFilter.LOWER_VALUE_PROPERTY, lowerValue);
			filterProperties.put(RangeFilter.UPPER_VALUE_PROPERTY, upperValue);

			filterProperties.put(AttributeFilter.ATTRIBUTE_VALUES_ALIAS_PROPERTY, lowerValue + DASH + upperValue);
			attributeRangeFilter.initialize(filterProperties);

			Map<Locale, FilterDisplayInfo> localizedDisplayMap = getLocaleFilterDisplayInfoMap(rangeFacet, lowerValue, upperValue);

			attributeRangeFilter.setLocalizedDisplayMap(localizedDisplayMap);
			SortedMap<AttributeRangeFilter, AttributeRangeFilter> bottomLevelAttributeRanges =
					config.getBottomLevelAttributeRanges(attributeRangeFilter.getAttributeKey());
			bottomLevelAttributeRanges.put(attributeRangeFilter, attributeRangeFilter);
			config.getAllAttributeRanges().put(attributeRangeFilter.getId(), attributeRangeFilter);
			rootFilter.addChild(attributeRangeFilter);
		}
	}

	private Map<Locale, FilterDisplayInfo> getLocaleFilterDisplayInfoMap(final RangeFacet rangeFacet, final String lowerValue,
																		 final String upperValue) {
		Map<Locale, FilterDisplayInfo> localizedDisplayMap = new HashMap<>();
		rangeFacet.getDisplayNameMap().keySet().forEach(localeString -> {
			FilterDisplayInfo filterDisplayInfo = new FilterDisplayInfoImpl();
			filterDisplayInfo.setDisplayName(rangeFacet.getDisplayNameMap().getOrDefault(localeString, lowerValue + DASH + upperValue));
			localizedDisplayMap.put(LocaleUtils.toLocale(localeString), filterDisplayInfo);
		});
		return localizedDisplayMap;
	}


	private boolean lowerBoundGreaterThanUpperBound(final BigDecimal lowerBound, final boolean noLowerBound, final BigDecimal upperBound,
													final boolean noUpperBound) {
		return !noLowerBound && !noUpperBound && lowerBound.compareTo(upperBound) > 0;
	}

	public AttributeService getAttributeService() {
		return attributeService;
	}

	public void setAttributeService(final AttributeService attributeService) {
		this.attributeService = attributeService;
	}

	public BeanFactory getBeanFactory() {
		return beanFactory;
	}

	public void setBeanFactory(final BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}
}
